/*
Modifiers: Matthew Hendrickson and Andrew DePass
*/
import java.util.HashMap;


//This class serves the purpose of creating the Graph packet
//The graph packet will hold routes to neighbors for the network.
//It will also conduct the Dijkstra shortest distance calculations.
public class GraphPacket extends Packet{
	public HashMap<Integer, Double> linkTable;
	
	public GraphPacket(int source, int destination, int hopCount, HashMap<Integer, Double> linkTable) {
		super(source, destination, hopCount, null);
		// TODO Auto-generated constructor stub
		this.linkTable = linkTable;
	}

}
