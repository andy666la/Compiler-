import java.util.*;
import symbol.*;
import syntaxtree.*;
import visitor.*;

//  creating .vapor file
public class TranslateVisitor implements GJNoArguVisitor<String> {
	private SymbolTable sTable;					// Symbol table from BuildSTVisitor
	private ClassSymbol currClass;				// Scope for current class
	private MethodSymbol currMethod;			// Scope for current method
	private HashMap<String, String> currScope;	// Current scope mapping table
	private Printer printer;					// Printer class
	
	// default constructor
	public TranslateVisitor(SymbolTable _sTable) {
		this.sTable = _sTable;
		setCurrentClass(null);
		setCurrentMethod(null);
		this.currScope = new HashMap<String, String>();
		this.printer = new Printer();
	}
	
	// Set current class for the scope
	private void setCurrentClass(ClassSymbol _class) {
		this.currClass = _class;
	}
	
	// Get current class
	private ClassSymbol getCurrentClass() {
		return this.currClass;
	}
	
	// Set current method for the scope
	private void setCurrentMethod(MethodSymbol _mtd) {
		this.currMethod = _mtd;
	}
	
	// Get current method
	private MethodSymbol getCurrentMethod() {
		return this.currMethod;
	}
	
	// Initialize .vapor file with v-tables
	private void initialize() {
		ArrayList<ClassSymbol> list = this.sTable.getClassList();
		for (int i = 1; i < list.size(); ++i) {	// Ignore the main class
			printer.printStmt("const vmt_" + list.get(i).getName());
			printer.incDepth();
			printer.printVTable(list.get(i));
			printer.decDepth();
			printer.printStmt("");
		}
	}
	
	// Determine string's type: identifier? value?
	private String getStrType(String str) {
		if (this.currClass.findField(str) != null) {
			int idx = this.currClass.getClassRecord().indexOf(str) * 4 + 4;
			String newVar = printer.newVariable();
			printer.printStmt(newVar + " = [this+" + Integer.toString(idx) + "]");
			return newVar;
		} else if (this.currScope.get(str) != null) {	// If it is an identifier
			return this.currScope.get(str);
		} else {	// It is a value
			return str;
		}
	}
	
	// visit method
	
	//
	// Auto class visitors--probably don't need to be overridden.
	//
	public String visit(NodeList n) {
		String _ret=null;
		int _count=0;
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			e.nextElement().accept(this);
			_count++;
		}
		return _ret;
	}

	public String visit(NodeListOptional n) {
		if ( n.present() ) {
			String _ret = "";
			int _count=0;
			for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
				_ret += e.nextElement().accept(this);
				_count++;
			}
			return _ret;
		}
		else
			return null;
	}

	public String visit(NodeOptional n) {
		if ( n.present() )
			return n.node.accept(this);
		else
			return null;
	}

	public String visit(NodeSequence n) {
		String _ret=null;
		int _count=0;
		for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
			e.nextElement().accept(this);
			_count++;
		}
		return _ret;
	}

	public String visit(NodeToken n) { 
		return n.toString(); 
	}

	//
	// User-generated visitor methods below
	//

	/**
	 * f0 -> MainClass()
	 * f1 -> ( TypeDeclaration() )*
	 * f2 -> <EOF>
	 */
	public String visit(Goal n) {
		String _ret=null;
		initialize();
		n.f0.accept(this); n.f1.accept(this); n.f2.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> "public"
	 * f4 -> "static"
	 * f5 -> "void"
	 * f6 -> "main"
	 * f7 -> "("
	 * f8 -> "String"
	 * f9 -> "["
	 * f10 -> "]"
	 * f11 -> Identifier()
	 * f12 -> ")"
	 * f13 -> "{"
	 * f14 -> ( VarDeclaration() )*
	 * f15 -> ( Statement() )*
	 * f16 -> "}"
	 * f17 -> "}"
	 */
	public String visit(MainClass n) {
		String _ret=null;
		n.f0.accept(this); 
		String mainClassName = n.f1.accept(this); 
		setCurrentClass(this.sTable.findClass(mainClassName));
		
		n.f2.accept(this); n.f3.accept(this);
		n.f4.accept(this); n.f5.accept(this); n.f6.accept(this); 
		setCurrentMethod(this.currClass.findMethod("main"));
		
		n.f7.accept(this); n.f8.accept(this); n.f9.accept(this); 
		n.f10.accept(this); n.f11.accept(this);
		n.f12.accept(this);
		printer.printStmt("func Main()");
		printer.incDepth();
		n.f13.accept(this); n.f14.accept(this); 
		n.f15.accept(this); n.f16.accept(this);
		printer.printStmt("ret");
		printer.decDepth();
		printer.resetCounter();
		setCurrentMethod(null);
		this.currScope.clear();
		
		n.f17.accept(this);
		setCurrentClass(null);
		printer.printStmt("");
		return _ret;
	}

	/**
	 * f0 -> ClassDeclaration()
	 *		 | ClassExtendsDeclaration()
	 */
	public String visit(TypeDeclaration n) {
		String _ret=null;
		n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "{"
	 * f3 -> ( VarDeclaration() )*
	 * f4 -> ( MethodDeclaration() )*
	 * f5 -> "}"
	 */
	public String visit(ClassDeclaration n) {
		String _ret=null;
		n.f0.accept(this);
		String className = n.f1.accept(this);
		setCurrentClass(this.sTable.findClass(className));
		n.f2.accept(this); n.f3.accept(this); n.f4.accept(this); n.f5.accept(this);
		setCurrentClass(null);
		return _ret;
	}

	/**
	 * f0 -> "class"
	 * f1 -> Identifier()
	 * f2 -> "extends"
	 * f3 -> Identifier()
	 * f4 -> "{"
	 * f5 -> ( VarDeclaration() )*
	 * f6 -> ( MethodDeclaration() )*
	 * f7 -> "}"
	 */
	public String visit(ClassExtendsDeclaration n) {
		String _ret=null;
		n.f0.accept(this);
		String className = n.f1.accept(this);
		setCurrentClass(this.sTable.findClass(className));
		n.f2.accept(this); n.f3.accept(this); n.f4.accept(this);
		n.f5.accept(this); n.f6.accept(this); n.f7.accept(this);
		setCurrentClass(null);
		return _ret;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 * f2 -> ";"
	 */
	public String visit(VarDeclaration n) {
		String _ret=null;
		n.f0.accept(this); n.f1.accept(this); n.f2.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "public"
	 * f1 -> Type()
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( FormalParameterList() )?
	 * f5 -> ")"
	 * f6 -> "{"
	 * f7 -> ( VarDeclaration() )*
	 * f8 -> ( Statement() )*
	 * f9 -> "return"
	 * f10 -> Expression()
	 * f11 -> ";"
	 * f12 -> "}"
	 */
	public String visit(MethodDeclaration n) {
		String _ret=null;
		n.f0.accept(this); n.f1.accept(this);
		// Get the function declaration string
		String methodName = n.f2.accept(this);
		setCurrentMethod(this.currClass.findMethod(methodName));
		String decStr = "func " + this.currClass.getName() + "." + methodName + "(this";
		n.f3.accept(this);
		String argStr = n.f4.accept(this);
		if (n.f4.present()) {	// if any argument
			decStr += " " + argStr;
		}
		decStr += ")";
		// Print function declaration
		n.f5.accept(this);
		printer.printStmt(decStr);
		printer.incDepth();
		
		n.f6.accept(this); n.f7.accept(this); n.f8.accept(this); n.f9.accept(this);
		String returnSym = getStrType(n.f10.accept(this));
		n.f11.accept(this);
		printer.printStmt("ret " + returnSym);	// Print return value
		n.f12.accept(this);
		
		setCurrentMethod(null);
		this.currScope.clear();
		printer.decDepth();
		printer.resetCounter();
		printer.printStmt("");
		return _ret;
	}

	/**
	 * f0 -> FormalParameter()
	 * f1 -> ( FormalParameterRest() )*
	 */
	public String visit(FormalParameterList n) {
		String _ret = "";
		_ret += n.f0.accept(this);
		String follow = n.f1.accept(this);
		if (n.f1.present()) {
			_ret += follow;
		}
		return _ret;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 */
	public String visit(FormalParameter n) {
		String _ret = null;
		n.f0.accept(this);
		_ret = n.f1.accept(this);
		this.currScope.put(_ret, _ret);
		return _ret;
	}

	/**
	 * f0 -> ","
	 * f1 -> FormalParameter()
	 */
	public String visit(FormalParameterRest n) {
		String _ret = " ";
		n.f0.accept(this);
		_ret += n.f1.accept(this);
		return _ret;
	}

	/**
	 * f0 -> ArrayType()
	 *		 | BooleanType()
	 *		 | IntegerType()
	 *		 | Identifier()
	 */
	public String visit(Type n) {
		String _ret=null;
		n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "int"
	 * f1 -> "["
	 * f2 -> "]"
	 */
	public String visit(ArrayType n) {
		String _ret=null;
		n.f0.accept(this);
		n.f1.accept(this);
		n.f2.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "boolean"
	 */
	public String visit(BooleanType n) {
		String _ret=null;
		n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "int"
	 */
	public String visit(IntegerType n) {
		String _ret=null;
		n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> Block()
	 *		 | AssignmentStatement()
	 *		 | ArrayAssignmentStatement()
	 *		 | IfStatement()
	 *		 | WhileStatement()
	 *		 | PrintStatement()
	 */
	public String visit(Statement n) {
		String _ret=null;
		n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "{"
	 * f1 -> ( Statement() )*
	 * f2 -> "}"
	 */
	public String visit(Block n) {
		String _ret=null;
		n.f0.accept(this);
		n.f1.accept(this);
		n.f2.accept(this);
		return _ret;
	}

	/**
	 * f0 -> Identifier()
	 * f1 -> "="
	 * f2 -> Expression()
	 * f3 -> ";"
	 */
	public String visit(AssignmentStatement n) {
		String _ret=null;
		String varName = n.f0.accept(this);		// variable's name in .java file
		String progVar;
		if (this.currScope.get(varName) == null) {
			progVar = printer.newVariable();	// variable's name in .vapor file
			this.currScope.put(varName, progVar);	// Add it to current scope
		} else {
			progVar = this.currScope.get(varName);
		}
		
		n.f1.accept(this);
		String value = getStrType(n.f2.accept(this));
		n.f3.accept(this);
		
		if (this.currClass.findField(varName) == null) {
			String varType = null;
			if (this.currMethod.findVar(varName) != null) {
				varType = this.currMethod.findVar(varName).getType();
			} else if (this.currMethod.findParam(varName) != null){
				varType = this.currMethod.findParam(varName).getType();
			}
			if (!varType.equals("int[]")) {
				printer.printStmt(progVar + " = " + value);
			} else {
				printer.printStmt("blen = MulS(" + value + " 4)");
				printer.printStmt("len = Add(blen 4)");
				printer.printStmt(progVar + " = HeapAllocZ(len)");
				printer.printStmt("[" + progVar + "] = " + value);
			}
		} else {
			String varType = this.currClass.findField(varName).getType();
			int idx = this.currClass.getClassRecord().indexOf(varName) * 4 + 4;
			if (!varType.equals("int[]")) {
				printer.printStmt("[this+" + Integer.toString(idx) + "] = " + value);
			} else {
				printer.printStmt("blen = MulS(" + value + " 4)");
				printer.printStmt("len = Add(blen 4)");
				String newVar = printer.newVariable();
				printer.printStmt(newVar + " = HeapAllocZ(len)");
				printer.printStmt("[" + newVar + "] = " + value);
				printer.printStmt("[this+" + Integer.toString(idx) + "] = " + newVar);
			}
		}
		
		return _ret;
	}

	/**
	 * f0 -> Identifier()
	 * f1 -> "["
	 * f2 -> Expression()
	 * f3 -> "]"
	 * f4 -> "="
	 * f5 -> Expression()
	 * f6 -> ";"
	 */
	public String visit(ArrayAssignmentStatement n) {
		String _ret=null;
		String arrayName = getStrType(n.f0.accept(this));
		n.f1.accept(this);
		String index = getStrType(n.f2.accept(this));
		n.f3.accept(this);
		n.f4.accept(this);
		String value = getStrType(n.f5.accept(this));
		n.f6.accept(this);
		
		String label = printer.getLCounter();
		printer.printStmt("s = [" + arrayName + "]");
		printer.printStmt("i = " + index);
		printer.printStmt("ok = LtS(i s)");
		printer.printStmt("if ok goto :Visit" + label);
		printer.printStmt("Error(\"array index out of bounds\")");
		printer.printStmt("Visit" + label + ": ok = LtS(-1 i)");
		printer.printStmt("if ok goto :NewVisit" + label);
		printer.printStmt("Error(\"array index out of bounds\")");
		printer.printStmt("NewVisit" + label + ": o = MulS(i 4)");
		printer.printStmt("d = Add(" + arrayName + " o)");
		printer.printStmt("[d+4] = " + value);
		return _ret;
	}

	/**
	 * f0 -> "if"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 * f5 -> "else"
	 * f6 -> Statement()
	 */
	public String visit(IfStatement n) {
		String _ret=null;
		n.f0.accept(this);
		n.f1.accept(this);
		String condition = getStrType(n.f2.accept(this));
		String label = printer.getLCounter();
		printer.printStmt("if0 " + condition + " goto :if" + label + "_else");
		n.f3.accept(this);
		printer.incDepth();
		n.f4.accept(this);
		printer.printStmt("goto :if" + label + "_end");
		printer.decDepth();
		
		n.f5.accept(this);
		printer.printStmt("if" + label + "_else:");
		printer.incDepth();
		n.f6.accept(this);
		printer.decDepth();
		printer.printStmt("if" + label + "_end:");
		return _ret;
	}

	/**
	 * f0 -> "while"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> Statement()
	 */
	public String visit(WhileStatement n) {
		String _ret=null;
		n.f0.accept(this);
		String label = printer.getLCounter();
		printer.printStmt("loop" + label + "_begin:");
		n.f1.accept(this);
		String condition = getStrType(n.f2.accept(this));
		printer.printStmt("if0 " + condition + " goto :loop" + label + "_end");
		n.f3.accept(this);
		printer.incDepth();
		n.f4.accept(this);
		printer.printStmt("goto :loop" + label + "_begin");
		printer.decDepth();
		printer.printStmt("loop" + label + "_end:");
		return _ret;
	}

	/**
	 * f0 -> "System.out.println"
	 * f1 -> "("
	 * f2 -> Expression()
	 * f3 -> ")"
	 * f4 -> ";"
	 */
	public String visit(PrintStatement n) {
		String _ret=null;
		n.f0.accept(this);
		n.f1.accept(this);
		String value = getStrType(n.f2.accept(this));
		n.f3.accept(this);
		n.f4.accept(this);
		printer.printStmt("PrintIntS(" + value + ")");
		return _ret;
	}

	/**
	 * f0 -> AndExpression()
	 *		 | CompareExpression()
	 *		 | PlusExpression()
	 *		 | MinusExpression()
	 *		 | TimesExpression()
	 *		 | ArrayLookup()
	 *		 | ArrayLength()
	 *		 | MessageSend()
	 *		 | PrimaryExpression()
	 */
	public String visit(Expression n) {
		String _ret = n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "&&"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(AndExpression n) {
		String _ret=null;
		String left = getStrType(n.f0.accept(this));
		n.f1.accept(this);
		String right = getStrType(n.f2.accept(this));
		_ret = printer.newVariable();
		printer.printStmt(_ret + " = MulS(" + left + " " + right + ")");
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "<"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(CompareExpression n) {
		String _ret=null;
		String left = getStrType(n.f0.accept(this));
		n.f1.accept(this);
		String right = getStrType(n.f2.accept(this));
		_ret = printer.newVariable();
		printer.printStmt(_ret + " = LtS(" + left + " " + right + ")");
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "+"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(PlusExpression n) {
		String _ret=null;
		String left = getStrType(n.f0.accept(this));
		n.f1.accept(this);
		String right = getStrType(n.f2.accept(this));
		_ret = printer.newVariable();
		printer.printStmt(_ret + " = Add(" + left + " " + right + ")");
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "-"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(MinusExpression n) {
		String _ret=null;
		String left = getStrType(n.f0.accept(this));
		n.f1.accept(this);
		String right = getStrType(n.f2.accept(this));
		_ret = printer.newVariable();
		printer.printStmt(_ret + " = Sub(" + left + " " + right + ")");
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "*"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(TimesExpression n) {
		String _ret=null;
		String left = getStrType(n.f0.accept(this));
		n.f1.accept(this);
		String right = getStrType(n.f2.accept(this));
		_ret = printer.newVariable();
		printer.printStmt(_ret + " = MulS(" + left + " " + right + ")");
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "["
	 * f2 -> PrimaryExpression()
	 * f3 -> "]"
	 */
	public String visit(ArrayLookup n) {
		String _ret=null;
		String arrayName = getStrType(n.f0.accept(this));
		n.f1.accept(this);
		String index = getStrType(n.f2.accept(this));
		n.f3.accept(this);
		_ret = printer.newVariable();
		String label = printer.getLCounter();
		printer.printStmt("s = [" + arrayName + "]");
		printer.printStmt("i = " + index);		
		printer.printStmt("ok = LtS(i s)");
		printer.printStmt("if ok goto :Visit" + label);
		printer.printStmt("Error(\"array index out of bounds\")");
		printer.printStmt("Visit" + label + ": ok = LtS(-1 i)");
		printer.printStmt("if ok goto :NewVisit" + label);
		printer.printStmt("Error(\"array index out of bounds\")");
		printer.printStmt("NewVisit" + label + ": o = MulS(i 4)");
		printer.printStmt("d = Add(" + arrayName + " o)");
		printer.printStmt(_ret + " = [d+4]");
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> "length"
	 */
	public String visit(ArrayLength n) {
		String _ret=null;
		String arrayName = getStrType(n.f0.accept(this));
		n.f1.accept(this);
		n.f2.accept(this);
		_ret = printer.newVariable();
		printer.printStmt(_ret + " = [" + arrayName + "]");
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> Identifier()
	 * f3 -> "("
	 * f4 -> ( ExpressionList() )?
	 * f5 -> ")"
	 */
	public String visit(MessageSend n) {
		String _ret=null;
		String var = getStrType(n.f0.accept(this));
		String newVar = printer.newVariable();
		printer.printStmt(newVar + " = [" + var + "]");
		n.f1.accept(this);
		String methodName = n.f2.accept(this);
		String offset = getMethodOffSet(findClassName(n.f0), methodName);
		printer.printStmt(newVar + " = [" + newVar + "+" + offset + "]");
		n.f3.accept(this);
		_ret = printer.newVariable();
		String callStr = _ret + " = call " + newVar + "(" + var;
		String argList = n.f4.accept(this);
		if (n.f4.present()) {
			callStr += " " + argList;
		}
		callStr += ")";
		printer.printStmt(callStr);
		n.f5.accept(this);
		return _ret;
	}
	
	// a method to get type of a class's instance
	private String findClassName(PrimaryExpression n) {
		String className = null;
		if (n.f0.choice instanceof AllocationExpression) {	// Class allocation
			AllocationExpression expr = (AllocationExpression)n.f0.choice;
			className = expr.f1.f0.toString();
		} else if (n.f0.choice instanceof ThisExpression) {	// This expression
			className = this.currClass.getName();
		} else if (n.f0.choice instanceof BracketExpression) {
			BracketExpression expr1 = (BracketExpression)n.f0.choice;
			if (expr1.f1.f0.choice instanceof MessageSend) {
				MessageSend expr = (MessageSend)expr1.f1.f0.choice;
				return findClassName(expr.f0);
			}
		} else {	// Parameter / Variable name
			String varName = n.accept(this);
			if (this.currMethod.findParam(varName) != null) {
				className = this.currMethod.findParam(varName).getType();
			} else if (this.currMethod.findVar(varName) != null) {
				className = this.currMethod.findVar(varName).getType();
			} else if (this.currClass.findField(varName) != null) {
				className = this.currClass.findField(varName).getType();
			}
		}
		return className;
	}
	
	// another method to get the offset of method
	private String getMethodOffSet(String className, String methodName) {
		ClassSymbol _class = this.sTable.findClass(className);
		VTable v = _class.getVTable();
		int idx = v.find(methodName) * 4;
		return Integer.toString(idx);
	}
	
	/**
	 * f0 -> Expression()
	 * f1 -> ( ExpressionRest() )*
	 */
	public String visit(ExpressionList n) {
		String _ret=null;
		_ret = getStrType(n.f0.accept(this));
		String follow = n.f1.accept(this);
		if (n.f1.present()) {
			_ret += follow;
		}
		return _ret;
	}

	/**
	 * f0 -> ","
	 * f1 -> Expression()
	 */
	public String visit(ExpressionRest n) {
		String _ret=" ";
		n.f0.accept(this);
		_ret += getStrType(n.f1.accept(this));
		return _ret;
	}

	/**
	 * f0 -> IntegerLiteral()
	 *		 | TrueLiteral()
	 *		 | FalseLiteral()
	 *		 | Identifier()
	 *		 | ThisExpression()
	 *		 | ArrayAllocationExpression()
	 *		 | AllocationExpression()
	 *		 | NotExpression()
	 *		 | BracketExpression()
	 */
	public String visit(PrimaryExpression n) {
		String _ret = n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n) {
		String _ret = n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "true"
	 */
	public String visit(TrueLiteral n) {
		String _ret;
		n.f0.accept(this);
		_ret = printer.newVariable();
		printer.printStmt(_ret + " = 1");
		return _ret;
	}

	/**
	 * f0 -> "false"
	 */
	public String visit(FalseLiteral n) {
		String _ret;
		n.f0.accept(this);
		_ret = printer.newVariable();
		printer.printStmt(_ret + " = 0");
		return _ret;
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Identifier n) {
		String _ret = n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "this"
	 */
	public String visit(ThisExpression n) {
		String _ret = n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "new"
	 * f1 -> "int"
	 * f2 -> "["
	 * f3 -> Expression()
	 * f4 -> "]"
	 */
	public String visit(ArrayAllocationExpression n) {
		String _ret=null;
		n.f0.accept(this);
		n.f1.accept(this);
		n.f2.accept(this);
		_ret = getStrType(n.f3.accept(this));
		n.f4.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "new"
	 * f1 -> Identifier()
	 * f2 -> "("
	 * f3 -> ")"
	 */
	public String visit(AllocationExpression n) {
		String _ret=null;
		n.f0.accept(this);
		String className = n.f1.accept(this);
		int size = 4 * this.sTable.findClass(className).getClassRecord().size() + 4;
		n.f2.accept(this);
		n.f3.accept(this);
		_ret = printer.newVariable();
		String label = printer.getLCounter();
		printer.printStmt(_ret + " = HeapAllocZ(" + Integer.toString(size) + ")");
		printer.printStmt("[" + _ret + "] = :vmt_" + className);
		printer.printStmt("if " + _ret + " goto :null" + label);
		printer.printStmt("Error(\"null pointer\")");
		printer.printStmt("null" + label + ":");
		return _ret;
	}

	/**
	 * f0 -> "!"
	 * f1 -> Expression()
	 */
	public String visit(NotExpression n) {
		String _ret=null;
		n.f0.accept(this);
		String expr = getStrType(n.f1.accept(this));
		_ret = printer.newVariable();
		printer.printStmt(_ret + " = Sub(1 " + expr + ")");
		return _ret;
	}

	/**
	 * f0 -> "("
	 * f1 -> Expression()
	 * f2 -> ")"
	 */
	public String visit(BracketExpression n) {
		String _ret = null;
		n.f0.accept(this);
		_ret = n.f1.accept(this);
		n.f2.accept(this);
		return _ret;
	}
}

