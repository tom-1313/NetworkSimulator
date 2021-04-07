/*
Source: https://www.baeldung.com/java-dijkstra
Modified: Matthew Hendrickson and Andrew DePass
*/


// This is a node class externally sourced to test the acquired dijkstra's algorithm
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Node {
	    private String name; //This is used to identify each node
	    private String returnStr; //This is used to output information on adjacent nodes
	    private List<Node> shortestPath = new LinkedList<>(); //This holds the nodes for the shortest paths
	    
	    private double distance = Float.MAX_VALUE;//this stores the distance to a given node, which 
												  //will be eventually passed to the 'adjacentNodes' HashMap
	    
	    Map<Node, Double> adjacentNodes = new HashMap<>();//This is a Hashmap containing adjacent nodes and their distances

	    
		//This adds a new destination to the to the adjacentNodes hashmap
		public void addDestination(Node newNode, double distance) {
			//in the case that the HashMap has something the relevant key is found and removed
			//This is then replaced by the new node and distance.
	    	if(!adjacentNodes.isEmpty()) {
	    		for (Map.Entry<Node,Double> entry : adjacentNodes.entrySet()) {
	    			if(newNode.getName().equals(entry.getKey().getName())) {
	    				adjacentNodes.remove(entry.getKey());
	    				break;
	    				
	    			}
	    		}
	    	}
	    	adjacentNodes.put(newNode, distance);
	    	
	    	
	    
	    	
	    }
		
		//This outputs the Node name
	    public Node(String name) {
	        this.name = name;
	    }
	    
		//This outputs the Node name as well, which is just the identifying nsap
	    public Node(int nsap) {
			this.name = String.valueOf(nsap);
	    	// TODO Auto-generated constructor stub
		}

		//This sets a new distance
		public void setDistance(double newDistance) {
	    	distance = newDistance;
	    }
	    
		//This is a getter for the distance to a node
	    public double getDistance() {
	    	return distance;
	    }

		//This returns the list of nodes in the shortest paths linked list 
	    public List<Node> getShortestPath(){
	    	return shortestPath;
	    }

		//This allows for replacement of a Node in the shortestPath linked list
	    public void setShortestPath(List<Node> newShortestPath) {
	    	shortestPath = newShortestPath;
	    }

		//This retrieves adjacent nodes from the hashmap
	    public Map<Node, Double> getAdjacentNodes(){
	    	return adjacentNodes;
	    }

		//retrieves the name of a node
	    public String getName() {
	    	return name;
	    }

		//This method outputs the adjacent nodes for a given node
	    public String output() {
	    	returnStr = " ";
	    	adjacentNodes.forEach((node, distance) ->{
	    		
	    		returnStr += "Nodes: " + node.getName() + " ";
	    	});
	    	
	    	return returnStr;
	    }

		//This verifies if a node is adjacent to a given node
		public String getAdjacentNodeFromName(String name) {
			// TODO Auto-generated method stub
			returnStr = "";
			adjacentNodes.forEach((node, distance) ->{
	    		if(node.getName().equals(name)) {
	    			returnStr = name;
	    		}
	    		
	    	});
	    	
			return returnStr;
		}
	    
}
