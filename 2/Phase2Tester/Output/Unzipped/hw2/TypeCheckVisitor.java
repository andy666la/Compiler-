import java.util.*;
import symbol.*;
import syntaxtree.*;
import visitor.*;

// type-checking.
public class TypeCheckVisitor implements GJNoArguVisitor<String> {
	private SymbolTable sTable;			// General Symbol table for all classes
	private boolean anyError;			// True if there is any error in source code
	private ClassSymbol currClass;		// Used during construction, record current class
	private MethodSymbol currMethod;	// Used during construction, record current method
	
	// Default constructor
	public TypeCheckVisitor(SymbolTable _st) {
		this.sTable = _st;
		setError(false);
		setCurrentClass(null);
		setCurrentMethod(null);
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
	
	// Set the mask if any error
	private void setError(boolean isError) {
		this.anyError = isError;
	}
	
	// Check if there exists any error
	public boolean isAnyError() {
		return this.anyError;
	}
	
	// Return the type of an identifier
	private String getIDType(String _name) {
		String type = null;
		if (_name != null) {
			// If the string is a type
			if (_name.equals("int") || _name.equals("boolean") || _name.equals("int[]") 
				|| this.sTable.findClass(_name) != null) {
				type = _name;
			} else {// Then the string is a name
				if (this.currMethod == null) { // If it is a field
					VarSymbol var = this.currClass.findField(_name);
					if (var == null) {
						setError(true);
					} else {
						type = var.getType();
					}
				} else { // If it is in a method
					ParamSymbol param = this.currMethod.findParam(_name);
					VarSymbol var_mtd = this.currMethod.findVar(_name);
					VarSymbol var_fld = this.currClass.findField(_name);
					if (param == null && var_mtd == null && var_fld == null) {
						setError(true);
					} else if (param != null) {
						type = param.getType();
					} else if (var_mtd != null) {
						type = var_mtd.getType();
					} else if (var_fld != null) {
						type = var_fld.getType();
					}
				}
			}
		}
		return type;
	}
	
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
			String _ret=null;
			int _count=0;
			for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
				e.nextElement().accept(this);
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
		n.f0.accept(this);
		n.f1.accept(this);
		n.f2.accept(this);
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
		
		n.f2.accept(this);
		n.f3.accept(this);
		n.f4.accept(this);
		n.f5.accept(this);
		n.f6.accept(this);
		setCurrentMethod(this.currClass.findMethod("main"));
		
		n.f7.accept(this);
		n.f8.accept(this);
		n.f9.accept(this);
		n.f10.accept(this);
		n.f11.accept(this);
		n.f12.accept(this);
		n.f13.accept(this);
		n.f14.accept(this);
		n.f15.accept(this);
		n.f16.accept(this);
		setCurrentMethod(null);
		n.f17.accept(this);
		setCurrentClass(null);
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
		ClassSymbol _class = this.sTable.findClass(className);
		if (_class == null) {
			setError(true);
		} else {
			setCurrentClass(_class);
			n.f2.accept(this);
			n.f3.accept(this);
			n.f4.accept(this);
			n.f5.accept(this);
			setCurrentClass(null);
		}
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
		ClassSymbol _class = this.sTable.findClass(className);
		if (_class == null) {
			setError(true);
		} else {
			n.f2.accept(this);
			String baseClassName = n.f3.accept(this);
			ClassSymbol _baseClass = this.sTable.findClass(baseClassName);
			if (_baseClass == null || 
				!_baseClass.getName().equals(_class.getBaseClass().getName())) {
				setError(true);
			} else {
				setCurrentClass(_class);
				n.f4.accept(this);
				n.f5.accept(this);
				n.f6.accept(this);
				n.f7.accept(this);
				setCurrentClass(null);
			}
		}
		return _ret;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 * f2 -> ";"
	 */
	public String visit(VarDeclaration n) {
		String _ret=null;
		n.f0.accept(this);
		n.f1.accept(this);
		n.f2.accept(this);
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
		n.f0.accept(this);
		n.f1.accept(this);
		String mtdName = n.f2.accept(this);
		setCurrentMethod(this.currClass.findMethod(mtdName));
		
		n.f3.accept(this);
		n.f4.accept(this);
		n.f5.accept(this);
		n.f6.accept(this);
		n.f7.accept(this);
		n.f8.accept(this);
		n.f9.accept(this);
		String rType = getIDType(n.f10.accept(this));
		if (!rType.equals(this.currMethod.getReturnType())) {
			setError(true);
		}
		
		n.f11.accept(this);
		n.f12.accept(this);
		setCurrentMethod(null);
		return _ret;
	}

	/**
	 * f0 -> FormalParameter()
	 * f1 -> ( FormalParameterRest() )*
	 */
	public String visit(FormalParameterList n) {
		String _ret=null;
		n.f0.accept(this);
		n.f1.accept(this);
		return _ret;
	}

	/**
	 * f0 -> Type()
	 * f1 -> Identifier()
	 */
	public String visit(FormalParameter n) {
		String _ret=null;
		n.f0.accept(this);
		n.f1.accept(this);
		return _ret;
	}

	/**
	 * f0 -> ","
	 * f1 -> FormalParameter()
	 */
	public String visit(FormalParameterRest n) {
		String _ret=null;
		n.f0.accept(this);
		n.f1.accept(this);
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
		String _ret = "int[]";
		n.f0.accept(this);
		n.f1.accept(this);
		n.f2.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "boolean"
	 */
	public String visit(BooleanType n) {
		String _ret = "boolean";
		n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "int"
	 */
	public String visit(IntegerType n) {
		String _ret = "int";
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
		String name = n.f0.accept(this);	// Check whether this identifier exists
		String leftType = getIDType(name);
		if (leftType != null) {
			n.f1.accept(this);
			String rightType = getIDType(n.f2.accept(this));
			if (rightType != null && !leftType.equals(rightType)) {
				setError(true);
			}
			n.f3.accept(this);
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
		String arrayName = n.f0.accept(this);
		String arrayType = getIDType(arrayName);
		if (!arrayType.equals("int[]")) {
			setError(true);
		} else {
			n.f1.accept(this);
			String idxType = getIDType(n.f2.accept(this));
			if (!idxType.equals("int")) {
				setError(true);
			} else {
				n.f3.accept(this);
				n.f4.accept(this);
				String valueType = getIDType(n.f5.accept(this));
				if (!valueType.equals("int")) {
					setError(true);
				} else {
					n.f6.accept(this);
				}
			}
		}
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
		String conditionType = getIDType(n.f2.accept(this));
		if (!conditionType.equals("boolean")) {
			setError(true);
		} else {
			n.f3.accept(this);
			n.f4.accept(this);
			n.f5.accept(this);
			n.f6.accept(this);
		}
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
		n.f1.accept(this);
		String conditionType = getIDType(n.f2.accept(this));
		if (!conditionType.equals("boolean")) {
			setError(true);
		} else {
			n.f3.accept(this);
			n.f4.accept(this);
		}
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
		String exprType = getIDType(n.f2.accept(this));
		if (!exprType.equals("int")) {
			setError(true);
		} else {
			n.f3.accept(this);
			n.f4.accept(this);
		}
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
		String _ret = getIDType(n.f0.accept(this));
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "&&"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(AndExpression n) {
		String _ret = "boolean";
		String leftType = getIDType(n.f0.accept(this));
		n.f1.accept(this);
		String rightType = getIDType(n.f2.accept(this));
		if (!leftType.equals("boolean") || !rightType.equals("boolean")) {
			setError(true);
		}
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "<"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(CompareExpression n) {
		String _ret = "boolean";
		String leftType = getIDType(n.f0.accept(this));
		n.f1.accept(this);
		String rightType = getIDType(n.f2.accept(this));
		if (!leftType.equals("int") || !rightType.equals("int")) {
			setError(true);
		}
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "+"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(PlusExpression n) {
		String _ret="int";
		String leftType = getIDType(n.f0.accept(this));
		n.f1.accept(this);
		String rightType = getIDType(n.f2.accept(this));
		if (!leftType.equals("int") || !rightType.equals("int")) {
			setError(true);
		}
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "-"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(MinusExpression n) {
		String _ret="int";
		String leftType = getIDType(n.f0.accept(this));
		n.f1.accept(this);
		String rightType = getIDType(n.f2.accept(this));
		if (!leftType.equals("int") || !rightType.equals("int")) {
			setError(true);
		}
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "*"
	 * f2 -> PrimaryExpression()
	 */
	public String visit(TimesExpression n) {
		String _ret="int";
		String leftType = getIDType(n.f0.accept(this));
		n.f1.accept(this);
		String rightType = getIDType(n.f2.accept(this));
		if (!leftType.equals("int") || !rightType.equals("int")) {
			setError(true);
		}
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "["
	 * f2 -> PrimaryExpression()
	 * f3 -> "]"
	 */
	public String visit(ArrayLookup n) {
		String _ret="int";
		String arrayType = getIDType(n.f0.accept(this));
		if (!arrayType.equals("int[]")) {
			setError(true);
		} else {
			n.f1.accept(this);
			String idxType = getIDType(n.f2.accept(this));
			n.f3.accept(this);
			if (!idxType.equals("int")) {
				setError(true);
			}
		}
		return _ret;
	}

	/**
	 * f0 -> PrimaryExpression()
	 * f1 -> "."
	 * f2 -> "length"
	 */
	public String visit(ArrayLength n) {
		String _ret="int";
		String arrayType = getIDType(n.f0.accept(this));
		n.f1.accept(this);
		n.f2.accept(this);
		if (!arrayType.equals("int[]")) {
			setError(true);
		}
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
		String className = getIDType(n.f0.accept(this));
		n.f1.accept(this);
		if (className == null) {
			setError(true);
		} else {
			String mtdName = n.f2.accept(this);
			MethodSymbol mtd = this.sTable.findClass(className).findMethod(mtdName);
			if (mtd == null) {
				setError(true);
			} else {
				_ret = mtd.getReturnType();
				n.f3.accept(this);
				n.f4.accept(this);
				n.f5.accept(this);
				// Get two lists contain the sequence of types for comparing
				ArrayList<String> argTypeList = exprListVisit(n.f4);
				ArrayList<String> paramTypeList = mtd.getTypeSequence();
				if (argTypeList.size() != paramTypeList.size()) {	// If the size does not match
					setError(true);
				} else {	// Then check the types one by one
					for (int i = 0; i < paramTypeList.size(); ++i) {
						if (!isEquivType(argTypeList.get(i), paramTypeList.get(i))) {
							setError(true);
						}
					}
				}
			}
		}
		return _ret;
	}
	
	// Check two types are the same or not (or one type inherits from another one)
	private boolean isEquivType(String argType, String paraType) {
		if (paraType.equals("int") || paraType.equals("boolean") || paraType.equals("int[]")) {
			return argType.equals(paraType);	// Fundamental types, just compare them
		} else {	// Classes, check inheritance
			if (argType.equals(paraType)) {	// If they are just equal
				return true;
			} else {	// Try to find base class of the argument
				ClassSymbol baseType = this.sTable.findClass(argType).getBaseClass();
				if (baseType == null) {	// If no base class
					return false;
				} else if (baseType.getName() == paraType) {	// If base class matches
					return true;
				} else {	// recursively check base class's base class
					return isEquivType(baseType.getName(), paraType);
				}
			}
		}
	}
	
	// Return an arraylist containing an array of types of arguments
	private ArrayList<String> exprListVisit(NodeOptional n) {
		ArrayList<String> typelist = new ArrayList<String>();
		if (n.present()) {	// Simulating the visiting method of nodelist
			if (n.node instanceof ExpressionList) {
				ExpressionList list = (ExpressionList) n.node;
				typelist.add(getIDType(list.f0.accept(this)));
				NodeListOptional restList = (NodeListOptional) list.f1;
				if (restList.present()) {
					for ( Enumeration<Node> e = restList.elements(); e.hasMoreElements(); ) {
						typelist.add(getIDType(e.nextElement().accept(this)));
					}
				}
			}
		}
		return typelist;
	}
	
	/**
	 * f0 -> Expression()
	 * f1 -> ( ExpressionRest() )*
	 */
	public String visit(ExpressionList n) {
		String _ret=null;
		n.f0.accept(this);
		_ret=n.f1.accept(this);
		return _ret;
	}

	/**
	 * f0 -> ","
	 * f1 -> Expression()
	 */
	public String visit(ExpressionRest n) {
		String _ret=null;
		n.f0.accept(this);
		_ret = getIDType(n.f1.accept(this));
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
		String _ret = getIDType(n.f0.accept(this));
		return _ret;
	}

	/**
	 * f0 -> <INTEGER_LITERAL>
	 */
	public String visit(IntegerLiteral n) {
		String _ret = "int";
		n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "true"
	 */
	public String visit(TrueLiteral n) {
		String _ret = "boolean";
		n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> "false"
	 */
	public String visit(FalseLiteral n) {
		String _ret = "boolean";
		n.f0.accept(this);
		return _ret;
	}

	/**
	 * f0 -> <IDENTIFIER>
	 */
	public String visit(Identifier n) {
		String _ret = null;
		return n.f0.accept(this);
	}

	/**
	 * f0 -> "this"
	 */
	public String visit(ThisExpression n) {
		String _ret = currClass.getName();
		n.f0.accept(this);
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
		String _ret = "int[]";
		n.f0.accept(this);
		n.f1.accept(this);
		n.f2.accept(this);
		String type = getIDType(n.f3.accept(this));
		n.f4.accept(this);
		if (!type.equals("int")) {
			setError(true);
		}		
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
		_ret = n.f1.accept(this);
		n.f2.accept(this);
		n.f3.accept(this);
		if (this.sTable.findClass(_ret) == null) {
			setError(true);
		}
		return _ret;
	}

	/**
	 * f0 -> "!"
	 * f1 -> Expression()
	 */
	public String visit(NotExpression n) {
		String _ret = "boolean";
		n.f0.accept(this);
		String type = getIDType(n.f1.accept(this));
		if (!type.equals("boolean")) {
			setError(true);
		}
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
		_ret = getIDType(n.f1.accept(this));
		n.f2.accept(this);
		return _ret;
	}
}
