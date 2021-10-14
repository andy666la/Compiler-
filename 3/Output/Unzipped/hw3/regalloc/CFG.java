package regalloc; 

public class CFG {
	private NodeList nodes;
	
	// Constructor
	public CFG() {
		this.nodes = new NodeList();
	}
	
	// Return the node's list
	public NodeList getNodeList() {
		return this.nodes;
	}
	
	// Add an edge
	public boolean addEdge(int fromKey, int toKey) {
		Node fromNode = this.nodes.get(fromKey);
		Node toNode = this.nodes.get(toKey);
		boolean isSucc_1 = fromNode.addSucc(toNode);
		boolean isSucc_2 = toNode.addPred(fromNode);
		return (isSucc_1 && isSucc_2);
	}
	
	// Add a node
	public Node addNode(int key) {
		Node node = new Node(key);
		this.nodes.addNode(node);
		return node;
	}
	
	// Find a node
	public boolean find(int key) {
		return this.nodes.find(key);
	}
	
	// Get a node
	public Node get(int key) {
		return this.nodes.get(key);
	}
	
	// Convert it to a string
	// @Override
	public String toString() {
		String s = "";
		for (Node n: this.nodes) {
			s += n.toString() + "\n";
		}
		return s;
	}
}
