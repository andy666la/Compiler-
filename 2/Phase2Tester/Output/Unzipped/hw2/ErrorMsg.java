 
public class ErrorMsg {
	private boolean anyErrors;	// False if there is any error
	
	// Default constructor
	public ErrorMsg() {
		setAnyError(false);
	}
	
	// Set the condition of errors
	public void setAnyError(boolean _anyError) {
		this.anyErrors = _anyError;
	}
	
	// Return if existing any error
	public boolean isAnyError() {
		return this.anyErrors;
	}
	
	// Output Error
	public void complain(String msg) {
		setAnyError(true);
		System.out.println(msg);
	}
}
