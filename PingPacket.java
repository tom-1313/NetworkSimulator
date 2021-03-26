
public class PingPacket extends Packet{
	private boolean received;
	
	public PingPacket(int source, int dest, int hopCount, Object payload) {
		super(source, dest, hopCount, payload);
		// TODO Auto-generated constructor stub
	}
	
	public void recieved() {
		received = true;
	}

}
