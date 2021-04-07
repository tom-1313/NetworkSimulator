
//Below is a test of the sourced dijkstra calculations that will be implemented in the network for the Graph Packet class.
public class DijkstraTest {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Node nodeA = new Node("11");
		
		Node nodeC = new Node("14");
		Node nodeD = new Node("15"); 
		Node nodeE = new Node("16");
		Node nodeF = new Node("17");
		
		
		Graph graph = new Graph();

		graph.addNode(nodeA);
		
		graph.addNode(nodeC);
		graph.addNode(nodeD);
		graph.addNode(nodeE);
		graph.addNode(nodeF);
		
		
		nodeE.addDestination(nodeD, 12);
		
		
		nodeA.addDestination(nodeC, 15);
		Node imposterB = graph.addNode(new Node("12"));
		imposterB.addDestination(nodeF, 10);
		nodeA.addDestination(imposterB, 120);
		

		//Calculate all paths from a specific node
		graph.calculateShortestPathFromSource(graph, nodeA);
		
		System.out.println(graph.toString());
		
		
		
		
		

	}

}
