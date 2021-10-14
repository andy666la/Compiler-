package symbol;

// Class for symbols of local variables 
public class VarSymbol extends Symbol {
	private String type;	// Type of variable
	
	// Basic constructor
	public VarSymbol(String _name, String _type) {
		super(_name);
		setType(_type);
	}
	
	// Set the type of variable
	public void setType(String _type) {
		this.type = _type;
	}
	
	// Return the type of variable
	public String getType() {
		return this.type;
	}
}
