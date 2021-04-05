

public class DijkstraTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Node nodeA = new Node("11");
		//Node nodeB = new Node("B");
		//Node nodeBImposter = new Node("12");
		Node nodeC = new Node("14");
		Node nodeD = new Node("15"); 
		Node nodeE = new Node("16");
		Node nodeF = new Node("17");
		//Node nodeG = new Node("G");
		
		Graph graph = new Graph();

		graph.addNode(nodeA);
		//graph.addNode(nodeB);
		graph.addNode(nodeC);
		graph.addNode(nodeD);
		graph.addNode(nodeE);
		graph.addNode(nodeF);
		
		//nodeB.addDestination(nodeE, 12);
		nodeE.addDestination(nodeD, 12);
		//nodeA.addDestination(nodeB, 10);
		
		nodeA.addDestination(nodeC, 15);
		Node imposterB = graph.addNode(new Node("12"));
		imposterB.addDestination(nodeF, 10);
		nodeA.addDestination(imposterB, 120);
		

		//neighborD.addDestination(nodeC, 12);
		//nodeB.addDestination(nodeF, 15);

		//nodeC.addDestination(nodeE, 10);

		

		
	

		//Calculate all paths from a specific node
		graph.calculateShortestPathFromSource(graph, nodeA);
		
		System.out.println(graph.toString());
		
		
		
		
		

	}

}
