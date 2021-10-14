package symbol;

// Class for symbols of parameters 
public class ParamSymbol extends Symbol {
	private String type;	// Type of parameter
	
	// Basic constructor
	public ParamSymbol(String _name, String _type) {
		super(_name);
		setType(_type);
	}
	
	// Set the type of parameter
	public void setType(String _type) {
		this.type = _type;
	}
	
	// Return the type of parameter
	public String getType() {
		return this.type;
	}
}
