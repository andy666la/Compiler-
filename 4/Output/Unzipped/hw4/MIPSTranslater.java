import cs132.util.IndentPrinter;
import cs132.vapor.ast.VAddr;
import cs132.vapor.ast.VAssign;
import cs132.vapor.ast.VBranch;
import cs132.vapor.ast.VBuiltIn;
import cs132.vapor.ast.VCall;
import cs132.vapor.ast.VCodeLabel;
import cs132.vapor.ast.VGoto;
import cs132.vapor.ast.VInstr;
import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VLabelRef;
import cs132.vapor.ast.VLitInt;
import cs132.vapor.ast.VMemRead;
import cs132.vapor.ast.VMemRef;
import cs132.vapor.ast.VMemWrite;
import cs132.vapor.ast.VReturn;
import cs132.vapor.ast.VVarRef;
 
import java.io.IOException;

public class MIPSTranslater extends Visitor<IOException> {
	private IndentPrinter printer;
	public boolean anyPrint;
	public boolean anyError;
	public boolean anyHeapAlloc;
	
	public MIPSTranslater(IndentPrinter printer) {
		this.printer = printer;
		this.anyPrint = false;
		this.anyError = false;
		this.anyHeapAlloc = false;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void visit(VAssign stmt) throws IOException {
		String dest = stmt.dest.toString();
		if (stmt.source instanceof VLitInt) {
			printer.println("li " + dest + " " + stmt.source.toString());
		} else if (stmt.source instanceof VLabelRef<?>) {
			printer.println("la " + dest + " " + 
						((VLabelRef<VCodeLabel>)stmt.source).ident);
		} else if (stmt.source instanceof VVarRef.Register) {
			printer.println("move " + dest + " " + stmt.source.toString());
		}
	}

	@Override
	public void visit(VCall stmt) throws IOException {
		String target;
		if (stmt.addr instanceof VAddr.Var<?>) {
			target = stmt.addr.toString();
			printer.println("jalr " + target);
		} else {
			target = stmt.addr.toString().substring(1);
			printer.println("jal " + target);
		}
	}

	@Override
	public void visit(VBuiltIn stmt) throws IOException {
		String opName = stmt.op.name;
		if (opName == "Add") {
			String dest = stmt.dest.toString();
			if (stmt.args[0].getClass() != stmt.args[1].getClass()) {
				if (stmt.args[0] instanceof VLitInt) {
					printer.println("li $t9 " + stmt.args[0].toString());
					String reg = stmt.args[1].toString();
					printer.println("add " + dest + " $t9 " + reg);
				} else {
					String reg = stmt.args[0].toString();
					String num = stmt.args[1].toString();
					printer.println("addi " + dest + " " + reg + " " + num);
				}
			} else {
				if (stmt.args[0] instanceof VLitInt) {
					int num1 = ((VLitInt)stmt.args[0]).value;
					int num2 = ((VLitInt)stmt.args[1]).value;
					int num = num1 + num2;
					printer.println("li " + dest + " " + Integer.toString(num));
				} else {
					String reg1 = stmt.args[0].toString();
					String reg2 = stmt.args[1].toString();
					printer.println("add " + dest + " " + reg1 + " " + reg2);
				}
			}
		} else if (opName == "Sub") {
			String dest = stmt.dest.toString();
			if (stmt.args[0].getClass() != stmt.args[1].getClass()) {
				if (stmt.args[0] instanceof VLitInt) {
					printer.println("li $t9 " + stmt.args[0].toString());
					String num = stmt.args[1].toString();
					printer.println("sub " + dest + " $t9 " + num);
				} else {
					String reg = stmt.args[0].toString();
					String num = stmt.args[1].toString();
					printer.println("sub " + dest + " " + reg + " " + num);
				}
			} else {
				if (stmt.args[0] instanceof VLitInt) {
					int num1 = ((VLitInt)stmt.args[0]).value;
					int num2 = ((VLitInt)stmt.args[1]).value;
					int num = num1 - num2;
					printer.println("li " + dest + " " + Integer.toString(num));
				} else {
					String reg1 = stmt.args[0].toString();
					String reg2 = stmt.args[1].toString();
					printer.println("sub " + dest + " " + reg1 + " " + reg2);
				}
			}
		} else if (opName == "MulS") {
			String dest = stmt.dest.toString();
			if (stmt.args[0].getClass() != stmt.args[1].getClass()) {
				if (stmt.args[0] instanceof VLitInt) {
					printer.println("li $t9 " + stmt.args[0].toString());
					String num = stmt.args[1].toString();
					printer.println("mul " + dest + " $t9 " + num);
				} else {
					String reg = stmt.args[0].toString();
					String num = stmt.args[1].toString();
					printer.println("mul " + dest + " " + reg + " " + num);
				}
			} else {
				if (stmt.args[0] instanceof VLitInt) {
					int num1 = ((VLitInt)stmt.args[0]).value;
					int num2 = ((VLitInt)stmt.args[1]).value;
					int num = num1 * num2;
					printer.println("li " + dest + " " + Integer.toString(num));
				} else {
					String reg1 = stmt.args[0].toString();
					String reg2 = stmt.args[1].toString();
					printer.println("mul " + dest + " " + reg1 + " " + reg2);
				}
			}
		} else if (opName == "LtS") {
			String dest = stmt.dest.toString();
			if (stmt.args[0].getClass() != stmt.args[1].getClass()) {
				String reg = stmt.args[0].toString();
				String num = stmt.args[1].toString();
				printer.println("slti " + dest + " " + reg + " " + num);
			} else {
				String reg1 = stmt.args[0].toString();
				String reg2 = stmt.args[1].toString();
				printer.println("slt " + dest + " " + reg1 + " " + reg2);
			}
		} else if (opName == "Error") {
			this.anyError = true;
			this.printer.println("la $a0 _str0");
			this.printer.println("j _error");
		} else if (opName == "HeapAllocZ") {
			this.anyHeapAlloc = true;
			if (stmt.args[0] instanceof VLitInt) {
				this.printer.println("li $a0 " + stmt.args[0].toString());
			} else {
				this.printer.println("move $a0 " + stmt.args[0].toString());
			}
			this.printer.println("jal _heapAlloc");
			this.printer.println("move " + stmt.dest + " $v0");
		} else if (opName == "PrintIntS") {
			this.anyPrint = true;
			if (stmt.args[0] instanceof VLitInt) {
				this.printer.println("li $a0 " + stmt.args[0].toString());
			} else {
				this.printer.println("move $a0 " + stmt.args[0].toString());
			}
			this.printer.println("jal _print");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(VMemWrite stmt) throws IOException {
		// TODO Auto-generated method stub
		String offset, base;
		if (stmt.dest instanceof VMemRef.Global) {
			offset = Integer.toString(((VMemRef.Global)stmt.dest).byteOffset);
			base = ((VMemRef.Global)stmt.dest).base.toString();
		} else {
			offset = Integer.toString(4 * ((VMemRef.Stack)stmt.dest).index);
			base = "$sp";
		}
		if (stmt.source instanceof VLitInt) {
			printer.println("li $t9 " + stmt.source.toString());
			printer.println("sw $t9 " + offset + "(" + base + ")");
		} else if (stmt.source instanceof VLabelRef<?>) {
			printer.println("la $t9 " + ((VLabelRef<VCodeLabel>)stmt.source).ident);
			printer.println("sw $t9 " + offset + "(" + base + ")");
		} else if (stmt.source instanceof VVarRef.Register) {
			printer.println("sw " + stmt.source.toString() + " " 
						+ offset + "(" + base + ")");
		}	
	}

	@Override
	public void visit(VMemRead stmt) throws IOException {
		String dest = stmt.dest.toString();
		String offset, base;
		if (stmt.source instanceof VMemRef.Global) {
			offset = Integer.toString(((VMemRef.Global)stmt.source).byteOffset);
			base = ((VMemRef.Global)stmt.source).base.toString();
		} else {
			offset = Integer.toString(4 * ((VMemRef.Stack)stmt.source).index);
			String region = ((VMemRef.Stack)stmt.source).region.name();
			if (region == "Local") {
				base = "$sp";
			} else {
				base = "$fp";
			}
		}
		printer.println("lw " + dest + " " + offset + "(" + base + ")");
	}

	@Override
	public void visit(VBranch stmt) throws IOException {
		if (stmt.positive) {
			printer.println("bnez " + stmt.value.toString() + " " + stmt.target.ident);
		} else {
			printer.println("beqz " + stmt.value.toString() + " " + stmt.target.ident);
		}
	}

	@Override
	public void visit(VGoto stmt) throws IOException {
		printer.println("j " + stmt.target.toString().substring(1));
	}

	@Override
	public void visit(VReturn stmt) throws IOException {
		return;
	}
}

