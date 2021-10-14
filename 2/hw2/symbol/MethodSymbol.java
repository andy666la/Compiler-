package symbol;

import java.util.*;

// Class for methods 
public class MethodSymbol extends Symbol {
	private String returnType;						// Type of returned varible
	private HashMap<String, ParamSymbol> params;	// List of parameters, name is the key
	private HashMap<String, VarSymbol> vars;		// List of local variables, name is the key
	private ArrayList<ParamSymbol> orderedParams;	// Record the sequence of parameters
	private ArrayList<VarSymbol> orderedVars;		// Record the sequence of local variables
	
	// Default constructor
	public MethodSymbol(String _name, String _rType) {
		super(_name);
		setReturnType(_rType);
		this.params = new HashMap<String, ParamSymbol>();
		this.vars = new HashMap<String, VarSymbol>();
		this.orderedParams = new ArrayList<ParamSymbol>();
		this.orderedVars = new ArrayList<VarSymbol>();
	}

	// Set the return type
	public void setReturnType(String _rType) {
		this.returnType = _rType;
	}
	
	// Return the type of return
	public String getReturnType() {
		return this.returnType;
	}
	
	// Return the number of parameters
	public int paramSize() {
		return this.params.size();
	}

	// Add parameter to this method
	public boolean addParam(String _pName, String _pType) {
		boolean is_added = false;	// task for the condition of this parameter
		// If this symbol is not in the list
		if (!this.params.containsKey(_pName)) {
			ParamSymbol param = new ParamSymbol(_pName, _pType);
			this.params.put(_pName, param);
			this.orderedParams.add(param);
			is_added = true;
		}
		return is_added;
	}
	
	// Look up the parameter list for matching parameter
	public ParamSymbol findParam(String _name) {
		return this.params.get(_name);
	}

	// Return the list of parameters
	public ArrayList<ParamSymbol> getParamList() {
		return this.orderedParams;
	}
	
	// Return the declaration sequence
	public ArrayList<String> getTypeSequence() {
		ArrayList<String> typelist = new ArrayList<String>();
		for (int i = 0; i < paramSize(); ++i) {
			typelist.add(this.orderedParams.get(i).getType());
		}
		return typelist;
	}

	// Add local variable to this method
	public boolean addVar(String _vName, String _vType) {
		boolean is_added = false;	// task for the condition of this variable
		// If this symbol is not in the list
		if (!this.vars.containsKey(_vName)) {
			VarSymbol var = new VarSymbol(_vName, _vType);
			this.vars.put(_vName, var);
			this.orderedVars.add(var);
			is_added = true;
		}
		return is_added;
	}
	
	// Look up the variable list for matching variable
	public VarSymbol findVar(String _name) {
		return this.vars.get(_name);
	}
	
	// Return the number of variables
	public int varSize() {
		return this.vars.size();
	}

	// Return the list of local variables
	public ArrayList<VarSymbol> getVarList() {
		return this.orderedVars;
	}
}
