import symbol.*;

// print indent and var.
public class Printer {
	private int counter;
	private int depth;
	private int labelCter;
	
	// Default constructor
	public Printer() {
		this.counter = 0;
		this.depth = 0;
		this.labelCter = 1;
	}
	
	// Increase the counter
	public void incCounter() {
		++this.counter;
	}
	
	// Decrease the counter
	public void decCounter() {
		--this.counter;
	}
	
	// Reset the counter
	public void resetCounter() {
		this.counter = 0;
	}
	
	// Increase the depth
	public void incDepth() {
		++this.depth;
	}
	
	// Decrease the counter
	public void decDepth() {
		--this.depth;
	}
	
	// Reset the depth
	public void resetDepth() {
		this.depth = 0;
	}
	
	// Increase the depth
	public void incLCounter() {
		++this.labelCter;
	}
	
	// Decrease the counter
	public void decLCounter() {
		--this.labelCter;
	}
	
	// Reset the depth
	public void resetLCounter() {
		this.labelCter = 1;
	}
	
	// Get the label counter
	public String getLCounter() {
		String label = Integer.toString(this.labelCter);
		incLCounter();
		return label;
	}
	
	// Add indent to the statement
	public void printStmt(String stmt) {
		String statement = "";
		for (int i = 0; i < depth; ++i) {
			statement += "\t";
		}
		statement += stmt;
		System.out.println(statement);
	}
	
	// Return variable identifier
	public String newVariable() {
		String var = "t." + Integer.toString(this.counter);
		incCounter();
		return var;
	}
	
	// Print v-table
	public void printVTable(ClassSymbol _class) {
		// Recursion, print base class first
		VTable t = _class.getVTable();
		for (int i = 0; i < t.size(); ++i) {
			CMPair p = t.get(i);
			printStmt(":" + p.getClassName() + "." + p.getMethodName());
		}
	}
}
