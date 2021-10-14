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

// Class for translating Vapor-M to MIPS 
public class VM2M {
	public static void main(String[] args) throws IOException {
		VaporProgram prog = parseVapor(System.in, System.err);
		IndentPrinter printer = new IndentPrinter(new PrintWriter(System.out));
		printDataSegment(prog, printer);
		printFunction(prog, printer);
		printer.close();	
	}

	private static VaporProgram parseVapor(InputStream in, PrintStream err) throws IOException {
		Op[] ops = { Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
				Op.PrintIntS, Op.HeapAllocZ, Op.Error, 
		};
		
		boolean allowLocals = false;
		String[] registers = {
				"v0", "v1",
				"a0", "a1", "a2", "a3",
				"t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
				"s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
				"t8",
		};
		boolean allowStack = true;
		
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

	private static void printDataSegment(VaporProgram prog, IndentPrinter printer) throws IOException {
		printer.println(".data");
		printer.println();
		for (VDataSegment dataSegment : prog.dataSegments) {
			printer.println(dataSegment.ident + ":");
			printer.indent();
			for (VOperand.Static value : dataSegment.values) {			
				printer.println(value.toString().substring(1));
			}
			printer.dedent();
			printer.println();
		}
	}
	
	private static void printFunction(VaporProgram prog, IndentPrinter printer) throws IOException {
		// Initialize the function segment
		initFuncSeg(printer);
		MIPSTranslater visitor = new MIPSTranslater(printer);
		// Print all functions
		for (VFunction function : prog.functions) {
			printer.println(function.ident + ":");
			printer.indent();
			String offset = Integer.toString(4 * (function.stack.out + function.stack.local + 2));
			// Register store for $sp and $fp 
			printer.println("sw $fp -8($sp)");
			printer.println("move $fp $sp");
			printer.println("subu $sp $sp " + offset);
			printer.println("sw $ra -4($fp)");
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
			printer.println("lw $ra -4($fp)");
			printer.println("lw $fp -8($fp)");
			printer.println("addu $sp $sp " + offset);
			printer.println("jr $ra");
			printer.dedent();
			printer.println();
		}
		// Final instructions
		if (visitor.anyPrint) {
			printer.println("_print:"); printer.indent();
			printer.println("li $v0 1   # syscall: print integer");
			printer.println("syscall");
			printer.println("la $a0 _newline");
			printer.println("li $v0 4   # syscall: print string");
			printer.println("syscall");
			printer.println("jr $ra");
			printer.dedent(); printer.println();
		}
		if (visitor.anyError) {
			printer.println("_error:"); printer.indent();
			printer.println("li $v0 4   # syscall: print string");
			printer.println("syscall");
			printer.println("li $v0 10  # syscall: exit");
			printer.println("syscall");
			printer.dedent(); printer.println();
		}
		if (visitor.anyHeapAlloc) {
			printer.println("_heapAlloc:"); printer.indent();
			printer.println("li $v0 9   # syscall: sbrk");
			printer.println("syscall");
			printer.println("jr $ra");
			printer.dedent(); printer.println();
		}
		endFuncSeg(printer);	
	}
	
	// Initialize function segments
	private static void initFuncSeg(IndentPrinter printer) throws IOException {
		printer.println(".text");
		printer.println(); printer.indent();
		printer.println("jal Main"); printer.println("li $v0 10"); printer.println("syscall"); 
		printer.println(); printer.dedent(); 
	}
	
	// Final part of data segment
	private static void endFuncSeg(IndentPrinter printer) throws IOException {
		printer.println(".data");
		printer.println(".align 0");
		printer.println("_newline: .asciiz \"\\n\"");
		printer.println("_str0: .asciiz \"null pointer\\n\"");	
	}
}
