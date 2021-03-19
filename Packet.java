
public class Packet {
	//Destination Address and Sender Address
	public int destNsap;
	public int senderNsap;
	
	//the ID of the packet
	public int ID;
	
	public Packet(int destNsap, int senderNsap, int ID) {
		this.destNsap = destNsap;
		this.senderNsap = senderNsap;
	}
}
