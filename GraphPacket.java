import java.util.HashMap;

public class GraphPacket extends Packet{
	public HashMap<Integer, Double> linkTable;
	
	public GraphPacket(int source, int destination, int hopCount, HashMap<Integer, Double> linkTable) {
		super(source, destination, hopCount, null);
		// TODO Auto-generated constructor stub
		this.linkTable = linkTable;
	}

}
