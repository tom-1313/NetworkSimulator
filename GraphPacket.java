
public class GraphPacket extends PingPacket{
	public Graph graph;
	
	public GraphPacket(int source, int destination, long timeInMillis, Graph graph) {
		super(source, destination, timeInMillis);
		// TODO Auto-generated constructor stub
		this.graph = graph;
	}

}
