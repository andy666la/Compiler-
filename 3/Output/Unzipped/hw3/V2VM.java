import cs132.util.*; 
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VBuiltIn.Op;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

import regalloc.*;
import regalloc.Node;

// Class for translating Vapor to Vapor-M
public class V2VM {
	public static void main(String[] args) throws IOException {
		VaporProgram prog = parseVapor(System.in, System.err);
		IndentPrinter printer = new IndentPrinter(new PrintWriter(System.out));
		printDataSegment(prog, printer);
		printFunction(prog, printer);
		printer.close();	
	}
	
	// Parse the Vapor source file to get AST
	private static VaporProgram parseVapor(InputStream in, PrintStream err) throws IOException {
		Op[] ops = {
			Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
			Op.PrintIntS, Op.HeapAllocZ, Op.Error,
		};
		boolean allowLocals = true;
		String[] registers = null;
		boolean allowStack = false;
		VaporProgram tree;
		try {
			tree = VaporParser.run(new InputStreamReader(in), 1, 1, java.util.Arrays.asList(ops),
									allowLocals, registers, allowStack);
		} catch (ProblemException ex) {
			err.println(ex.getMessage());
			return null;
		}		
		return tree;
	}
	
	// Print data segments
	private static void printDataSegment(VaporProgram prog, IndentPrinter printer) throws IOException {
		for (VDataSegment dataSegment : prog.dataSegments) {
			printer.println("const " + dataSegment.ident);
			printer.indent();
			for (VOperand.Static value : dataSegment.values) {
				printer.println(value);
			}
			printer.dedent();
			printer.println();
		}
	}
	
	// Print functions
	private static void printFunction(VaporProgram prog, IndentPrinter printer) throws IOException {
		for (VFunction function : prog.functions) {
			// Get in/out size
			int in = (function.params.length > 4)? function.params.length - 4 : 0;
			int out = 0;
			for (int i = 0; i < function.body.length; ++i) {
				if (function.body[i] instanceof VCall) {
					int new_out = ((VCall)function.body[i]).args.length;
					new_out = (new_out > 4)? new_out - 4 : 0;
					out = Math.max(out, new_out);
				}
			}
			// System.out.println(getLiveInterval(function));
			// Register allocation by linear scan algorithm
			LinearScanner ls = new LinearScanner(getLiveInterval(function));
			HashMap<String, String> regMap = ls.getRegMap();
			int local = ls.getLocalNum();
			
			// Create visitor to generate code
			VTranslateVisitor visitor = new VTranslateVisitor(regMap, printer);
			printer.println("func " + function.ident + 
							" [in " + Integer.toString(in) + 
							", out " + Integer.toString(out) + 
							", local " + Integer.toString(local) + "]");
			printer.indent();
			// if local is not equal to 0, we need to save them
			for (int i = 0; i < Math.min(local, 8); ++i) {
				String i_s = Integer.toString(i);
				printer.println("local[" + i_s + "] = $s" + i_s);
			}
			
			// pass the arguments by its sequences
			for (int i = 0; i < function.params.length; ++i) {
				String reg = regMap.get(function.params[i].ident);
				if (reg != null) {
					if (i < 4) {
						printer.println("$" + reg + " = $a" + Integer.toString(i));
					} else {
						printer.println("$" + reg + " = in[" + Integer.toString(i - 4) + "]");
					}
				}
			}
			
			// Deal with all the body statements and labels
			int index_l = 0, index_b = 0;
			int beginline = function.sourcePos.line + 1;
			int finalline = function.body[function.body.length - 1].sourcePos.line + 1;
			for (int line = beginline; line < finalline; ++line) {
				// print labels
				if (index_l < function.labels.length) {
					if (function.labels[index_l].sourcePos.line == line) {
						printer.dedent();
						printer.println(function.labels[index_l].ident + ":");
						printer.indent();
						++index_l;
					}
				}
				// print statements
				if (index_b < function.body.length) {
					if (function.body[index_b].sourcePos.line == line) {
						visitor.visit(function.body[index_b]);
						++index_b;
					}
				}
			}
			
			// Restore the registers
			for (int i = 0; i < Math.min(local, 8); ++i) {
				String i_s = Integer.toString(i);
				printer.println("$s" + i_s + " = local[" + i_s + "]");
			}
			printer.println("ret");
			printer.dedent();
			printer.println();
		}
	}
	
	// Construct CFG for a function
	private static CFG buildCFG(VFunction func) {
		CFG flowGraph = new CFG();	// Create an empty CFG
		// Add a first node to CFG
		Node prevNode = flowGraph.addNode(func.body[0].sourcePos.line);
		setVar(prevNode, func.body[0]);
		// Add the following nodes to CFG
		for (int i = 1; i < func.body.length; ++i) {
			int key = func.body[i].sourcePos.line;
			Node node = flowGraph.addNode(key);
			if (!(func.body[i - 1] instanceof VGoto)) {
				flowGraph.addEdge(prevNode.getKey(), key);
			}
			setVar(node, func.body[i]);
			prevNode = node;
		}
		// Set the successor nodes of each branch/goto statements
		for (int i = 0; i < func.body.length; ++i) {
			Node node = flowGraph.get(func.body[i].sourcePos.line);
			if (func.body[i] instanceof VBranch) {
				VBranch branch = (VBranch) func.body[i];
				String label = branch.target.ident;
				int t = getLine(func, label);
				flowGraph.addEdge(node.getKey(), t);
			} else if (func.body[i] instanceof VGoto) {
				VGoto _goto = (VGoto) func.body[i];
				if (_goto.target instanceof VAddr.Label<?>) {
					VAddr.Label<VCodeLabel> al = (VAddr.Label<VCodeLabel>) _goto.target;
					String label = al.label.ident;
					int t = getLine(func, label);
					flowGraph.addEdge(node.getKey(), t);
				}
			}
		}
		// Compute the in/out sets of each node until convergence
		boolean[] isSame = new boolean[func.body.length];
		do {
			for (int i = func.body.length - 1; i >= 0; --i) {
				Node node = flowGraph.get(func.body[i].sourcePos.line);
				HashSet<String> new_in = new HashSet<String>();
				HashSet<String> new_out = new HashSet<String>();
				// in = use + (out - def)
				new_in.addAll(node.out); new_in.removeAll(node.def); new_in.addAll(node.use);
				// out = sum{succ} in
				for (Node n : node.getSucc()) {
					new_out.addAll(n.in);
				}
				isSame[i] = (new_in.equals(node.in)) && (new_out.equals(node.out));
				node.in.clear(); node.in.addAll(new_in);
				node.out.clear(); node.out.addAll(new_out);
			}
		} while (!isConverge(isSame));
		return flowGraph;
	}
	
	// Set the variables defined and used for the node
	private static void setVar(Node n, VInstr instr) {
		if (instr instanceof VAssign) {
			VAssign assign = (VAssign) instr;
			n.def.add(assign.dest.toString());
			if (!(assign.source instanceof VLitStr) && !(assign.source instanceof VOperand.Static)) {
				n.use.add(assign.source.toString());
			}
		} else if (instr instanceof VBranch) {
			VBranch branch = (VBranch) instr;
			n.use.add(branch.value.toString());
		} else if (instr instanceof VBuiltIn) {
			VBuiltIn builtIn = (VBuiltIn) instr;
			if (builtIn.dest != null) {
				n.def.add(builtIn.dest.toString());
			}
			for (VOperand arg : builtIn.args) {
				if (!(arg instanceof VLitStr) && !(arg instanceof VOperand.Static)) {
					n.use.add(arg.toString());
				} 
			}
		} else if (instr instanceof VCall) {
			VCall call = (VCall) instr;
			n.def.add(call.dest.toString());
			if (call.addr instanceof VAddr.Var<?>) {
				n.use.add(call.addr.toString());
			}
			for (VOperand arg : call.args) {
				if (!(arg instanceof VLitStr) && !(arg instanceof VOperand.Static)) {
					n.use.add(arg.toString());
				} 
			}
		} else if (instr instanceof VMemRead) {
			VMemRead memRead = (VMemRead) instr;
			n.def.add(memRead.dest.toString());
			if (memRead.source instanceof VMemRef.Global) {
				VMemRef.Global ref = (VMemRef.Global) memRead.source;
				n.use.add(ref.base.toString());
			}
		} else if (instr instanceof VMemWrite) {
			VMemWrite memWrite = (VMemWrite) instr;
			if (!(memWrite.source instanceof VLitStr) && !(memWrite.source instanceof VOperand.Static)) {
				n.use.add(memWrite.source.toString());
			}
			if (memWrite.dest instanceof VMemRef.Global) {
				VMemRef.Global ref = (VMemRef.Global) memWrite.dest;
				n.use.add(ref.base.toString());
			}
		} else if (instr instanceof VReturn) {
			VReturn _return = (VReturn) instr;
			if (_return.value != null) {
				if (!(_return.value instanceof VLitStr) && !(_return.value instanceof VOperand.Static)) {
					n.use.add(_return.value.toString());
				}
			}
		} 
	}

	// Find whether this line is an instruction
	private static int getInstrLine(VFunction func, int line) {
		if (func.body[0].sourcePos.line > line) {
			return func.body[0].sourcePos.line;
		}
		for (int i = 0; i < func.body.length; ++i) {
			if (func.body[i].sourcePos.line < line &&
				func.body[i + 1].sourcePos.line > line) {
				return func.body[i + 1].sourcePos.line;
			}
		}
		return 0;
	}
	
	// Get the line number of label
	private static int getLine(VFunction func, String label) {
		for (VCodeLabel l : func.labels) {
			if (l.ident.equals(label)) {
				int line = getInstrLine(func, l.sourcePos.line);
				if (line != 0) {
					return line;
				}
			}
		}
		return 0;
	}
	
	// check whether construction converges
	private static boolean isConverge(boolean[] status) {
		for (boolean s : status) {
			if (s == false) {
				return false;
			}
		}
		return true;
	}

	// Create collections of live intervals
	private static HashMap<String, VarMap> getLiveInterval(VFunction func) {
		CFG cfg = buildCFG(func);	// get the CFG of the function
		// System.out.println(cfg);
		// Create a map list of variables
		HashMap<String, VarMap> liveIntervals = new HashMap<String, VarMap>();
		for (String var : func.vars) {	
			liveIntervals.put(var, new VarMap(var));
		}
		// Add definition line for parameters
		for (VVarRef.Local param : func.params) {
			liveIntervals.get(param.ident).addDef(func.sourcePos.line);
		}
		// Traverse the whole instruction list to set the def and use for variables
		for (int i = 0; i < func.body.length; ++i) {
			Node node = cfg.get(func.body[i].sourcePos.line);
			for (String d : node.def) {	// Set def
				liveIntervals.get(d).addDef(node.getKey());
			}
			for (String u : node.use) {	// Set use
				liveIntervals.get(u).addUse(node.getKey());
			}
			// Deal with function call
			if (func.body[i] instanceof VCall) {
				HashSet<String> saved = new HashSet<String>();
				// callee-saved = out - def
				saved.addAll(node.out); saved.removeAll(node.def);
				for (String var : saved) {
					liveIntervals.get(var).setCalleeSaved();
				}
			}
			if (i < func.body.length - 1) {
				HashSet<String> end = new HashSet<String>();
				// endpoint of variable is current if it does not live in the node
				end.addAll(node.in); 
				end.removeAll(cfg.get(func.body[i + 1].sourcePos.line).in);
				for (String var : end) {
					liveIntervals.get(var).setEndPoint(node.getKey());
				}
			} else {
				for (String var : node.in) {
					liveIntervals.get(var).setEndPoint(node.getKey());
				}
			}
		}
		// Remove unused parameters and local variables
		for (String var : func.vars) {
			if (liveIntervals.get(var).useSize() == 0) {
				liveIntervals.remove(var);
			}
		}
		// Set the start point and the end point of variables
		for (String key : liveIntervals.keySet()) {
			VarMap vm = liveIntervals.get(key);
			vm.setStartPoint();
		}
		return liveIntervals;
	}
}

