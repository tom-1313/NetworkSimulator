import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

//Taken from https://www.baeldung.com/java-dijkstra
public class Graph {
	
	
	private Set<Node> nodes = new HashSet<>();
	private Node source;
    public void addNode(Node nodeA) {
        nodes.add(nodeA);
    }
    
    public Graph calculateShortestPathFromSource(Graph graph, Node source) {
        this.source = source;
    	source.setDistance(0);
        Set<Node> settledNodes = new HashSet<>();
        Set<Node> unsettledNodes = new HashSet<>();

        unsettledNodes.add(source);

        while (unsettledNodes.size() != 0) {
            Node currentNode = getLowestDistanceNode(unsettledNodes);
            unsettledNodes.remove(currentNode);
            for (Entry < Node, Double> adjacencyPair: 
              currentNode.getAdjacentNodes().entrySet()) {
                Node adjacentNode = adjacencyPair.getKey();
                Double edgeWeight = (double)adjacencyPair.getValue();
                if (!settledNodes.contains(adjacentNode)) {
                    CalculateMinimumDistance(adjacentNode, edgeWeight, currentNode);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
        }
        return graph;
    }
    
    private static Node getLowestDistanceNode(Set < Node > unsettledNodes) {
        Node lowestDistanceNode = null;
        double lowestDistance = Float.MAX_VALUE;
        for (Node node: unsettledNodes) {
            double nodeDistance = node.getDistance();
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }
    
    private static void CalculateMinimumDistance(Node evaluationNode,
    		  Double edgeWeigh, Node sourceNode) {
    		    Double sourceDistance = sourceNode.getDistance();
    		    if (sourceDistance + edgeWeigh < evaluationNode.getDistance()) {
    		        evaluationNode.setDistance(sourceDistance + edgeWeigh);
    		        LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
    		        shortestPath.add(sourceNode);
    		        evaluationNode.setShortestPath(shortestPath);
    		    }
    		}
    
    @Override
    public String toString() {
    	String graphToString = "Distance and path from " + source.getName() + ": ";
    	for(Node currNode : nodes) {
    		graphToString += currNode.getName() + ": " + currNode.getDistance() + ", path = { ";
    		
    		List<Node> reversedPathList = currNode.getShortestPath();
    		Collections.reverse(reversedPathList);
    		
    		for(Node currPathNode : reversedPathList) {
    			graphToString += currPathNode.getName() + " ";
    		}
    		graphToString += "} \n";
    		//break;
    	}
    	return graphToString;
    }
    

}


