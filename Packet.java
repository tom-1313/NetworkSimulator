
public class Packet {
	int source;
	int dest;
	int hopCount;  // Maximum hops to get there
	Object payload;  // The payload!

	public Packet(int source, int dest, int hopCount, Object payload) {
		this.source = source;
		this.dest = dest;
		this.hopCount = hopCount;
		this.payload = payload;
	}
}
