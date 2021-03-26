
public class PingPacket extends Packet{
	private boolean received;
	
	public PingPacket(int source, int dest, int hopCount) {
		super(source, dest, hopCount, null);
		// TODO Auto-generated constructor stub
		payload = null;
		hopCount = 0;
	}

	public Pingpacket(int source, int dest, int ID)
	{
		ID = this.ID;
		source = this.source;
		dest = this.dest;

	}
	
	public void recieved() {
		received = true;
	}
	public boolean isRecieved() {
		return received;
	}

}
