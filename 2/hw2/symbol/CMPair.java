package symbol;

// Class for Class-Method Pair 
public class CMPair {
	private String className;
	private String methodName;
	
	// Default constructor
	public CMPair(String cName, String mName) {
		setClassName(cName);
		setMethodName(mName);
	}
	
	// Set class name
	public void setClassName(String cName) {
		this.className = cName;
	}
	
	// Get class name
	public String getClassName() {
		return this.className;
	}
	
	// Set class name
	public void setMethodName(String mName) {
		this.methodName = mName;
	}
	
	// Get method name
	public String getMethodName() {
		return this.methodName;
	}
	
	// Comparing method
	public boolean equals(CMPair p) {
		return (this.className.equals(p.className) 
			&& this.methodName.equals(p.methodName));
	}
}
