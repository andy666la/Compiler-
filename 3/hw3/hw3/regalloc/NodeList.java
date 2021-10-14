package regalloc;
 
import java.util.HashSet;
import java.util.Iterator;

public class NodeList implements Iterable<Node> {
	
	private HashSet<Node> nodeList;
	
	public NodeList() {
		this.nodeList = new HashSet<Node>();
	}
	
	// Method for Iteration
	// @Override
	public Iterator<Node> iterator() {
		return this.nodeList.iterator();
	}
	
	// Return the number of nodes contained in the list
	public int size() {
		return this.nodeList.size();
	}
	
	// Add node to the list
	public boolean addNode(Node n) {
		return this.nodeList.add(n);
	}
	
	// Find method
	public boolean find(int key) {
		for (Node n : this.nodeList) {
			if (n.isKey(key)) {	// If we find a matching one
				return true;
			}
		}
		return false;
	}
	
	// Get method
	public Node get(int key) {
		for (Node n : this.nodeList) {
			if (n.isKey(key)) {	// If we find a matching one
				return n;
			}
		}
		return null;
	}
	
	// Compare two NodeLists
	public boolean isEqual(NodeList list) {
		if (list.size() != this.size()) {
			return false;
		} else {
			for (Node node : list) {
				if (!this.find(node.getKey())) {
					return false;
				}
			}
			return true;
		}
	}
	
	// Convert the list to a string
	// @Override
	public String toString() {
		String str = "{";
		for (Node n : this.nodeList) {
			str += Integer.toString(n.getKey()) + ", ";
		}
		return str + "}\n";
	}
}
