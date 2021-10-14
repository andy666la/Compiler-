package symbol;

// Base class for all symbols 
public class Symbol {
	private String name;	// name of the symbol
	
	// Basic constructor
	public Symbol(String _name) {
		setName(_name);
	}
	
	// Set the symbol's name
	public void setName(String _name) {
		this.name = _name;
	}
	
	// Convert a symbol to a string
	public String getName() {
		return this.name;
	}
}
