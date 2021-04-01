

public class DijkstraTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Node nodeA = new Node("A");
		Node nodeB = new Node("B");
		Node nodeC = new Node("C");
		Node nodeD = new Node("D"); 
		Node nodeE = new Node("E");
		Node nodeF = new Node("F");

		nodeA.addDestination(nodeB, 10.12);
		
		nodeA.addDestination(nodeC, 15);

		nodeB.addDestination(nodeD, 12);
		nodeB.addDestination(nodeF, 15);

		nodeC.addDestination(nodeE, 10);

		nodeD.addDestination(nodeE, 2);
		nodeD.addDestination(nodeF, 1);

		nodeF.addDestination(nodeE, 5);

		Graph graph = new Graph();

		graph.addNode(nodeA);
		graph.addNode(nodeB);
		graph.addNode(nodeC);
		graph.addNode(nodeD);
		graph.addNode(nodeE);
		graph.addNode(nodeF);

		//Calculate all paths from a specific node
		graph.calculateShortestPathFromSource(graph, nodeA);
		
		System.out.println(graph.toString());
	}

}
