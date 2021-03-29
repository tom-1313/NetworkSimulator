
public class PingPacket extends Packet{
	private boolean received;
	private long timeInMillis;
	
	public PingPacket(int source, long timeInMillis) {
		super(source, -1, 99999, null);
		// TODO Auto-generated constructor stub
		payload = null;
		hopCount = 99999;
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
