package regalloc; 

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class LinearScanner {
	// General use constants
	private final String[] tRegNames = {"t0", "t1", "t2", "t3", 
					"t4", "t5", "t6", "t7", "t8"};
	private final String[] sRegNames = {"s0", "s1", "s2", "s3", 
					"s4", "s5", "s6", "s7", "v0", "v1"};
	private final int tRegNum = tRegNames.length;
	private final int sRegNum = sRegNames.length;
	// Useful fields
	private VarMap[] calleeSaved;					// Callee-saved variables
	private VarMap[] callerSaved;					// Caller-saved variables
	private HashMap<String, String> registerMap;	// The map of variables to registers
	private ArrayDeque<String> regPool;				// The pool of registers
	
	// Construction Method
	public LinearScanner(HashMap<String, VarMap> liveMap) {
		setSavedVars(liveMap);
		this.registerMap = new HashMap<String, String>();
		this.regPool = new ArrayDeque<String>();
		registerAlloc(this.calleeSaved, 's');
		registerAlloc(this.callerSaved, 't');
	}
	
	// Find all the caller-saved variables
	private void setSavedVars(HashMap<String, VarMap> liveMap) {
		HashSet<VarMap> calleeSaved = new HashSet<VarMap>();
		HashSet<VarMap> callerSaved = new HashSet<VarMap>();
		// Separate these variables into two sets
		for (VarMap vm : liveMap.values()) {
			if (vm.isCalleeSaved()) {
				calleeSaved.add(vm);
			} else {
				callerSaved.add(vm);
			}
		}
		// Store them in class fields
		this.calleeSaved = new VarMap[calleeSaved.size()];
		this.callerSaved = new VarMap[callerSaved.size()];
		calleeSaved.toArray(this.calleeSaved);
		callerSaved.toArray(this.callerSaved);
		// Sort each field by start points
		sortIntervals(this.calleeSaved);
		sortIntervals(this.callerSaved);
	}
	
	// Sort the intervals by start points
	private void sortIntervals(VarMap[] maps) {
		// exchange sort, enough for our compiler
		for (int i = 0; i < maps.length; ++i) {	
			for (int j = i; j > 0; --j) {
				if (VarMap.BY_START.compare(maps[j], maps[j - 1]) < 0) {
					VarMap tmp = maps[j];
					maps[j] = maps[j - 1]; maps[j - 1] = tmp;
				} else {
					break;
				}
			}
		}
	}
	
	// Initialize the register conditions
	private void InitRegPool(char type) {
		if (type == 't') {
			this.regPool.clear();
			// Add all $t* registers
			for (int i = 0; i < tRegNum; ++i) {
				this.regPool.addLast(tRegNames[i]);
			}
		} else if (type == 's') {
			this.regPool.clear();
			// Add all $s* registers
			for (int i = 0; i < sRegNum; ++i) {
				this.regPool.addLast(sRegNames[i]);
			}
		}
	}
	
	// Return current available register
	private String getRegister() {
		return this.regPool.removeFirst();
	}
	
	// Insert element to active, sorted by end points
	private void insertVar(ArrayList<VarMap> list, VarMap v) {
		int index = -1;		// index = -1 means either no element in list
							// or the end point of v is maximum
		if (list.size() > 0) {
			for (int i = 0; i < list.size(); ++i) {
				if (VarMap.BY_END.compare(list.get(i), v) > 0) {
					index = i; break;
				}
			}
		}
		// Add the element to the list by index
		if (index != -1) {
			list.add(index, v);
		} else {
			list.add(v);
		}
	}

	// Register allocation
	private void registerAlloc(VarMap[] vars, char type) {
		ArrayList<VarMap> active = new ArrayList<VarMap>();
		ArrayList<VarMap> spilled = new ArrayList<VarMap>();
		InitRegPool(type);
		// Set maximum size
		int maxsize = (type == 't')? this.tRegNum : this.sRegNum;
		for (VarMap vm : vars) {
			expireOld(active, vm);
			if (active.size() == maxsize) {
				spillInterval(active, spilled, vm);
			} else {
				String reg = getRegister();
				if (this.registerMap.get(vm.getName()) == null) {
					this.registerMap.put(vm.getName(), reg);
				}
				insertVar(active, vm);
			}
		}
	}

	// Expire old intervals
	private void expireOld(ArrayList<VarMap> active, VarMap v) {
		ArrayList<VarMap> toRemove = new ArrayList<VarMap>();
		for (VarMap a : active) {
			if (a.getEndPoint() < v.getStartPoint()) {
				String reg = this.registerMap.get(a.getName());
				this.regPool.addFirst(reg);
				toRemove.add(a);
			}
		}
		active.removeAll(toRemove);
	}
	
	// Spill these nodes if needed
	private void spillInterval(ArrayList<VarMap> active, ArrayList<VarMap> spilled, 
							VarMap v) {
		VarMap last = active.get(active.size() - 1);
		if (last.getEndPoint() > v.getEndPoint()) {
			String reg = this.registerMap.get(last.getName());
			this.registerMap.put(v.getName(), reg);
			spilled.add(last);
			active.remove(last);
			insertVar(active, v);
		} else {
			spilled.add(v);
		}
	}
	
	// Return the register map
	public HashMap<String, String> getRegMap() {
		return this.registerMap;
	}
	
	// Return number of local stack
	public int getLocalNum() {
		HashSet<String> registers = new HashSet<String>();
		for (VarMap vm : this.calleeSaved) {
			registers.add(this.registerMap.get(vm.getName()));
		}	
		return registers.size();
	}
}

