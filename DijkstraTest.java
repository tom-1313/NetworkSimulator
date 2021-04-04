

public class DijkstraTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Node nodeA = new Node("A");
		Node nodeB = new Node("B");
		Node nodeC = new Node("C");
		Node neighborD = new Node("D1");
		Node nodeD = new Node("D"); 
		Node nodeE = new Node("E");
		Node nodeF = new Node("F");

		nodeA.addDestination(nodeB, 10);
		
		nodeA.addDestination(nodeC, 15);

		//neighborD.addDestination(nodeC, 12);
		//nodeB.addDestination(nodeF, 15);

		//nodeC.addDestination(nodeE, 10);

		

		Graph graph = new Graph();

		graph.addNode(nodeA);
		graph.addNode(nodeB);
		graph.addNode(nodeC);
		//graph.addNode(neighborD);
	

		//Calculate all paths from a specific node
		graph.calculateShortestPathFromSource(graph, nodeA);
		
		System.out.println(graph.toString());
		
		
		
		Graph graph2 = new Graph();
		
		nodeD.addDestination(nodeE, 10);
		nodeD.addDestination(nodeF, 9);
		
		graph2.addNode(nodeD);
		graph2.addNode(nodeE);
		graph2.addNode(nodeF);
		
		graph2.calculateShortestPathFromSource(graph2, nodeD);
		
		System.out.println(graph2.toString());
		
		Graph combinedGraphs = graph.combineGraphs(graph, graph2, 10);
		
		combinedGraphs.calculateShortestPathFromSource(combinedGraphs, nodeD);
		
		System.out.println(combinedGraphs.toString());
	}

}
