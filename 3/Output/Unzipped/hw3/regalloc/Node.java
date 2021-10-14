package regalloc;

import java.util.HashSet;

public class Node implements Comparable<Node> {
	private int key;
	private NodeList succ;
	private NodeList pred;
	public HashSet<String> in;		
	public HashSet<String> out;		
	public HashSet<String> def;		
	public HashSet<String> use;		
	
	// Construction Method
	public Node(int key) {
		this.key = key;
		this.succ = new NodeList();
		this.pred = new NodeList();
		this.in = new HashSet<String>();
		this.out = new HashSet<String>();
		this.def = new HashSet<String>();
		this.use = new HashSet<String>();
	}
	
	// Method for comparing
	public int compareTo(Node _node) {
		return Integer.compare(this.key, _node.key);
	}
	
	// Compare this node's key with another integer
	public boolean isKey(int _key) {
		return (this.key == _key)? true : false;
	}
	
	// Return the key of this node
	public int getKey() {
		return this.key;
	}
	
	// Add a successor node to the list
	public boolean addSucc(Node n) {
		return this.succ.addNode(n);
	}
	
	// Return the successor nodes
	public NodeList getSucc() {
		return this.succ;
	}
	
	// Return the number of successor nodes
	// which is also the out degree of current node
	public int outDegree() {
		return this.succ.size();
	}
	
	// Add a predecessor node to the list
	public boolean addPred(Node n) {
		return this.pred.addNode(n);
	}
	
	// Return the predecessor nodes
	public NodeList getPred() {
		return this.pred;
	}
	
	// Return the number of successor nodes
	// which is also the in degree of current node
	public int inDegree() {
		return this.pred.size();
	}
	
	// Convert the node to a string
	public String toString() {
		String str = "Key: " + Integer.toString(this.getKey()) + "\n";
		str += "Predecessor Nodes: " + this.pred.toString();
		str += "Successor Nodes: " + this.succ.toString();
		str += "In: " + this.in.toString();
		str += "\nOut: " + this.out.toString();
		str += "\nDefine: " + this.def.toString();
		str += "\nUse: " + this.use.toString() + "\n";
		return str;
	}
}
