import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Node {
	    private String name;
	    private String returnStr;
	    private List<Node> shortestPath = new LinkedList<>();
	    
	    private double distance = Float.MAX_VALUE;
	    
	    Map<Node, Double> adjacentNodes = new HashMap<>();

	    public void addDestination(Node destination, double distance) {
	        if(!adjacentNodes.containsKey(destination)) {
	        	adjacentNodes.put(destination, distance);
	        }else {
	        	adjacentNodes.replace(destination, distance);
	        }
	    	
	    }
	 
	    public Node(String name) {
	        this.name = name;
	    }
	    
	    public Node(int nsap) {
			this.name = String.valueOf(nsap);
	    	// TODO Auto-generated constructor stub
		}

		public void setDistance(double newDistance) {
	    	distance = newDistance;
	    }
	    
	    public double getDistance() {
	    	return distance;
	    }
	    public List<Node> getShortestPath(){
	    	return shortestPath;
	    }
	    public void setShortestPath(List<Node> newShortestPath) {
	    	shortestPath = newShortestPath;
	    }
	    public Map<Node, Double> getAdjacentNodes(){
	    	return adjacentNodes;
	    }
	    public String getName() {
	    	return name;
	    }

	    public String output() {
	    	returnStr = "";
	    	adjacentNodes.forEach((node, distance) ->{
	    		
	    		returnStr += node.getName() + " ";
	    	});
	    	
	    	return returnStr;
	    }
	    
}
