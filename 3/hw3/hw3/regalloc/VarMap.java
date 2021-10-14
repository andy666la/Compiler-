package regalloc;
 
import java.util.ArrayList;
import java.util.Comparator;

public class VarMap {
	private String name;				// Name of variable
	private int startPoint;				// Start point of variable
	private int endPoint;				// End point of variable
	private ArrayList<Integer> def;		// Set containing all nodes having definition of this
	private ArrayList<Integer> use;		// Set containing all nodes using this
	private boolean isCalleeSaved;		// Mark for whether this is a callee-saved variable
	// Two comparators for compare two VarMap objects
	// By start point or end point
	public static final Comparator<VarMap> BY_START = new ByStart();
	public static final Comparator<VarMap> BY_END = new ByEnd();
	
	// Constructor
	public VarMap(String _name) {
		this.name = _name;
		this.startPoint = 0;
		this.endPoint = 0;
		this.isCalleeSaved = false;
		this.def = new ArrayList<Integer>();
		this.use = new ArrayList<Integer>();
	}
	
	// Set the mark of callee-saved, only need to set it true
	public void setCalleeSaved() {
		this.isCalleeSaved = true;
	}
	
	// Add a node to this.def set
	public boolean addDef(int d) {
		return this.def.add(d);
	}

	// Add a node to this.use set
	public boolean addUse(int u) {
		return this.use.add(u);
	}

	// Automatically set the start point
	// It is equal to the first definition point
	public void setStartPoint() {
		this.startPoint = this.def.get(0);
	}
	
	// Set the end point of variable
	public void setEndPoint(int ep) {
		this.endPoint = ep;
	}
	
	// Return the mark of callee-saved
	public boolean isCalleeSaved() {
		return this.isCalleeSaved;
	}
	
	// Return the name of variable
	public String getName() {
		return this.name;
	}
		
	// Return the size of this.def
	public int defSize() {
		return this.def.size();
	}
		
	// Return the size of this.use
	public int useSize() {
		return this.use.size();
	}
			
	// Return the start point of the variable
	public int getStartPoint() {
		return this.startPoint;
	}

	// Return the end point of the variable
	public int getEndPoint() {
		return this.endPoint;
	}
	
	// Comparator class
	// Compare their start points
	private static class ByStart implements Comparator<VarMap> {
		@Override
		public int compare(VarMap vm_1, VarMap vm_2) {
			return Integer.compare(vm_1.startPoint, vm_2.startPoint);
		}
	}
	
	// Comparator class
	// Compare their end points
	private static class ByEnd implements Comparator<VarMap> {
		@Override
		public int compare(VarMap vm_1, VarMap vm_2) {
			return Integer.compare(vm_1.endPoint, vm_2.endPoint);
		}
	}
	
	@Override
	public String toString() {
		String str = this.name + "\n";
		str += "CalleeSaved? " + Boolean.toString(this.isCalleeSaved) + "\n";
		str += "Start: " + Integer.toString(this.startPoint) + "\n";
		str += "End: " + Integer.toString(this.endPoint) + "\n";
		str += "Define: " + this.def + "\n";
		str += "Use: " + this.use + "\n";
		return str;
	}
}

