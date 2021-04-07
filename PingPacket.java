/*Authors: Matthew Hendrickson and Andrew DePass
*/
public class PingPacket extends Packet{
	private boolean received;
	private long timeInMillis;
	
	public PingPacket(int source, int destination, long timeInMillis) {
		super(source, destination, 1, null);
		// TODO Auto-generated constructor stub
		payload = null;
		this.timeInMillis = timeInMillis;
	}
	
	//This declares that a ping packet has been recieved
	public void recieved() {
		received = true;
	}

	//This returns whether or not a ping packet has been recieved
	public boolean isRecieved() {
		return received;
	}

	//This gets the start time of the pingpacket for later calculation of the ping time
	public long getStartTime() {
		return timeInMillis;
	}

}
