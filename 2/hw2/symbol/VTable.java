package symbol;

import java.util.*;

// Class for V-Table 
public class VTable {
	private ArrayList<CMPair> vTable;	// The list to be returned

	// Constructors
	public VTable() {
		this.vTable = new ArrayList<CMPair>();
	}
	
	public VTable(VTable vt) {
		this.vTable = new ArrayList<CMPair>();
		for (int i = 0; i < vt.vTable.size(); ++i) {
			CMPair p = vt.vTable.get(i);
			this.vTable.add(new CMPair(p.getClassName(), p.getMethodName()));
		}
	}
	
	// Find pair from the table
	public int find(String mName) {
		for (int i = 0; i < vTable.size(); ++i) {
			if (mName.equals(vTable.get(i).getMethodName())) {
				return i;
			}
		}
		return (vTable.size() + 1);
	}
	
	// Add pair to table
	public void add(String cName, String mName) {
		int idx = find(mName);
		if (idx < vTable.size()) {
			this.vTable.get(idx).setClassName(cName);
		} else {
			this.vTable.add(new CMPair(cName, mName));
		}
	}
	
	// Return the size of V-Table
	public int size() {
		return this.vTable.size();
	}
	
	// Return the pair with index i
	public CMPair get(int i) {
		return this.vTable.get(i);
	}
	
	public void print() {
		for (CMPair p : vTable) {
			System.out.println(p.getClassName() + " " + p.getMethodName());
		}
	}
}
