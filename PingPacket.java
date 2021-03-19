
public class PingPacket extends Packet{
	private boolean received;
	
	public PingPacket(int destNsap, int senderNsap, int ID) {
		super(destNsap, senderNsap, ID);
		// TODO Auto-generated constructor stub
	}
	
	public void recieved() {
		received = true;
	}

}
