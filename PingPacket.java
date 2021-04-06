
public class PingPacket extends Packet{
	private boolean received;
	private long timeInMillis;
	
	public PingPacket(int source, int destination, long timeInMillis) {
		super(source, destination, 1, null);
		// TODO Auto-generated constructor stub
		payload = null;
		this.timeInMillis = timeInMillis;
	}
	
	public void recieved() {
		received = true;
	}
	public boolean isRecieved() {
		return received;
	}
	public long getStartTime() {
		return timeInMillis;
	}

}
