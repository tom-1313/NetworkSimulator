import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

//Taken from https://www.baeldung.com/java-dijkstra
public class Graph {
	
	
	private HashSet<Node> nodes = new HashSet<>();
	private Node source;
    public void addNode(Node nodeA) {
       
    	Iterator it = nodes.iterator();
    	while (it.hasNext()) {	        
			Node node = (Node)it.next();
			if(node.getName().equals(nodeA.getName())){
	    		  nodes.remove(node);
	    		  nodes.add(nodeA);
	    		  return;
	    	}
    	}
    	/*
    	for(Node node : nodes) {
    	   
    	   if(node.getName().equals(nodeA.getName())){
    		  nodes.remove(node);
    		  nodes.add(nodeA);
    		  return;
    	   }
       }
    	*/
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
    
    //We are guarenteed that the source of graph1 is going to be connected to the source of graph2 with a certified distance
    //We must return a combination of these two graphs put together, with the correct distances for each.
    public Graph combineGraphs(Graph graph1, Graph graph2, double distance) {
    	Node source1 = graph1.getSourceNode();
    	Node source2 = graph2.getSourceNode();
    	
    	source1.addDestination(source2, distance);
    	Graph combinationalGraph = new Graph();
    	
    	
    	Iterator it = graph1.getPathList().iterator();
    	while (it.hasNext()) {	        
			Node node = (Node)it.next();
			combinationalGraph.addNode(node);
    	}
    	
    	Iterator it2 = graph2.getPathList().iterator();
    	while (it.hasNext()) {	        
			Node node = (Node)it.next();
			combinationalGraph.addNode(node);
    	}
    	
    	
    	combinationalGraph.addNode(source1);
    	combinationalGraph.addNode(source2);
    	
    	return combinationalGraph;
    	
    	
    }
    
    private static Node getLowestDistanceNode(Set < Node > unsettledNodes) {
        Node lowestDistanceNode = null;
        double lowestDistance = Double.MAX_VALUE;
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
    	String graphToString = "Distance and path from " + source.getName() + ":\n";
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
    //public Node getNodeDest(String from, String to) {
    	//if(nodes)
   // }
    
    public Node getSourceNode() {
    	return source;
    }
    
    public Set<Node> getPathList(){
    	return nodes;
    }
    

}


