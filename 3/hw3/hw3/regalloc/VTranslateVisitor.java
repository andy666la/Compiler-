package regalloc; 

import java.io.IOException;
import java.util.HashMap;

import cs132.util.IndentPrinter;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;

public class VTranslateVisitor extends Visitor<IOException> {
	private HashMap<String, String> regMap;
	private IndentPrinter printer;
	
	// Construction Method
	public VTranslateVisitor(HashMap<String, String> _regMap, IndentPrinter _printer) {
		this.regMap = _regMap;
		this.printer = _printer;
	}
	
	// General visit method
	public void visit(VInstr i) throws IOException {
		if (i instanceof VAssign) {
			visit((VAssign) i);
		} else if (i instanceof VCall) {
			visit((VCall) i);
		} else if (i instanceof VBuiltIn) {
			visit((VBuiltIn) i);
		} else if (i instanceof VMemWrite) {
			visit((VMemWrite) i);
		} else if (i instanceof VMemRead) {
			visit((VMemRead) i);
		} else if (i instanceof VBranch) {
			visit((VBranch) i);
		} else if (i instanceof VGoto) {
			visit((VGoto) i);
		} else if (i instanceof VReturn) {
			visit((VReturn) i);
		}
	}
	
	
	@Override
	public void visit(VAssign a) throws IOException {
		if (this.regMap.get(a.dest.toString()) != null) {
			String reg = this.regMap.get(a.dest.toString());
			String reg2 = "";
			String s = "$" + reg + " = ";
			if (a.source instanceof VLitInt) {
				s += a.source.toString();
			} else if (a.source instanceof VLabelRef<?>) {
				s += a.source.toString();
			} else {
				reg2 = this.regMap.get(a.source.toString());
				s += "$" + this.regMap.get(a.source.toString());
			}
			if (reg2 == "v0") {
				printer.println("$v0 = local[8]");
			} else if (reg2 == "v1") {
				printer.println("$v1 = local[9]");
			}
			printer.println(s);
			if (reg == "v0") {
				printer.println("local[8] = $v0");
			} else if (reg == "v1") {
				printer.println("local[9] = $v1");
			}
		}
	}

	@Override
	public void visit(VCall c) throws IOException {		
		for (int i = 0; i < c.args.length; ++i) {
			String s = "";
			if (i < 4) {
				s += "$a" + Integer.toString(i) + " = ";
			} else {
				s += "out[" + Integer.toString(i - 4) + "] = ";
			}
			if (c.args[i] instanceof VLitInt || c.args[i] instanceof VLabelRef<?>) {
				s += c.args[i].toString();
			} else {
				String reg2 = this.regMap.get(c.args[i].toString());
				s += "$" + reg2;
				if (reg2 == "v0") {
					printer.println("$v0 = local[8]");
				} else if (reg2 == "v1") {
					printer.println("$v1 = local[9]");
				}
			}
			printer.println(s);
		}
		if (c.addr instanceof VAddr.Label) {
			printer.println("call " + c.addr.toString());
		} else if (c.addr instanceof VAddr.Var<?>) {
			String reg = this.regMap.get(c.addr.toString());
			if (reg == "v0") {
				printer.println("$v0 = local[8]");
			} else if (reg == "v1") {
				printer.println("$v1 = local[9]");
			}
			printer.println("call $" + reg);
		}
		if (this.regMap.get(c.dest.ident) != null) {
			printer.println("$" + this.regMap.get(c.dest.ident) + " = $v0");
		}
	}

	@Override
	public void visit(VBuiltIn b) throws IOException {
		String s = "", reg = "";
		if (b.dest != null) {
			reg = this.regMap.get(b.dest.toString());
			s += "$" + reg + " = ";
		}
		s += b.op.name + "(";
		for (int i = 0; i < b.args.length; ++i) {
			if (b.args[i] instanceof VLitInt || b.args[i] instanceof VLitStr) {
				s += b.args[i].toString();
			} else {
				String reg2 = this.regMap.get(b.args[i].toString());
				s += "$" + reg2;
				if (reg2 == "v0") {
					printer.println("$v0 = local[8]");
				} else if (reg2 == "v1") {
					printer.println("$v1 = local[9]");
				}
			}
			if (i < b.args.length - 1) {
				s += " ";
			} else {
				s += ")";
			}
		}
		printer.println(s);
		if (reg == "v0") {
			printer.println("local[8] = $v0");
		} else if (reg == "v1") {
			printer.println("local[9] = $v1");
		}
	}

	@Override
	public void visit(VMemWrite w) throws IOException {	
		String s = "[$", reg = "", reg2 = "";
		reg = this.regMap.get(((VMemRef.Global)w.dest).base.toString());
		s += reg;
		int offset = ((VMemRef.Global)w.dest).byteOffset;
		if (offset != 0) {
			s += "+" + Integer.toString(offset);
		}
		s += "] = ";
		if (w.source instanceof VLitInt) {
			s += w.source.toString();
		} else if (w.source instanceof VLabelRef<?>) {
			s += w.source.toString();
		} else {
			reg2 = this.regMap.get(w.source.toString());
			s += "$" + reg2;
		}
		if (reg2 == "v0") {
			printer.println("$v0 = local[8]");
		} else if (reg2 == "v1") {
			printer.println("$v1 = local[9]");
		}
		printer.println(s);
		if (reg == "v0") {
			printer.println("local[8] = $v0");
		} else if (reg == "v1") {
			printer.println("local[9] = $v1");
		}
	}

	@Override
	public void visit(VMemRead m) throws IOException {
		String reg = this.regMap.get(m.dest.toString());
		String s = "$" + reg + " = [$";
		String reg2 = this.regMap.get(((VMemRef.Global)m.source).base.toString());
		s += reg2;
		int offset = ((VMemRef.Global)m.source).byteOffset;
		if (offset != 0) {
			s += "+" + Integer.toString(offset);
		}
		s += "]";
		if (reg2 == "v0") {
			printer.println("$v0 = local[8]");
		} else if (reg2 == "v1") {
			printer.println("$v1 = local[9]");
		}
		printer.println(s);
		if (reg == "v0") {
			printer.println("local[8] = $v0");
		} else if (reg == "v1") {
			printer.println("local[9] = $v1");
		}
	}

	@Override
	public void visit(VBranch b) throws IOException {
		String s = "";
		if (b.positive) {
			s += "if ";
		} else {
			s += "if0 ";
		}
		if (b.value != null) {
			if (b.value instanceof VLitInt) {
				s += b.value.toString();
			} else {
				String reg2 = this.regMap.get(b.value.toString());
				s += "$" + reg2;
				if (reg2 == "v0") {
					printer.println("$v0 = local[8]");
				} else if (reg2 == "v1") {
					printer.println("$v1 = local[9]");
				}
			}
		}
		s += " goto :" + b.target.ident;
		printer.println(s);
	}

	@Override
	public void visit(VGoto g) throws IOException {
		printer.println("goto " + g.target.toString());
	}

	@Override
	public void visit(VReturn r) throws IOException {
		if (r.value != null) {
			String s = "$v0 = ";
			if (r.value instanceof VLitInt) {
				s += r.value.toString();
			} else {
				s += "$" + this.regMap.get(r.value.toString());
			}
			printer.println(s);
		}
	}

}
