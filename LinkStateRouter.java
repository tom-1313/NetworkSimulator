
/***************
 * LinkStateRouter
 * Author: Christian Duncan
 * Modified by: 
 * Represents a router that uses a Distance Vector Routing algorithm.
 ***************/
import java.util.ArrayList;
import java.util.HashMap;

//Each router has a graph that it generates via pinging its neighbors. 

public class LinkStateRouter extends Router {
	// A generator for the given LinkStateRouter class
	public static class Generator extends Router.Generator {
		public Router createRouter(int id, NetworkInterface nic) {
			return new LinkStateRouter(id, nic);
		}
	}

	Debug debug;
	public HashMap<Integer, Double> table;
	public int delay;
	public LinkStateRouter(int nsap, NetworkInterface nic) {
		super(nsap, nic);
		debug = Debug.getInstance(); // For debugging!
		table = new HashMap<Integer, Double>();
	}

	public void run() {


		long nextPingTime = System.currentTimeMillis() + delay;
		while (true) {

			//if()
			
			floodPingPackets(new PingPacket(nsap, System.currentTimeMillis()));


			boolean process = false;

			// See if there is anything to process
			NetworkInterface.TransmitPair toSend = nic.getTransmit();

			//NetworkInterface.TransmitPair toSend = null;

			if (toSend != null) {
				// There is something to send out
				process = true;
				debug.println(3, "(LinkStateRouter.run): I am being asked to transmit: " + toSend.data + " to the destination: " + toSend.destination);
				route(-1, new Packet(nsap, toSend.destination, 5, toSend.data));
			}

			NetworkInterface.ReceivePair toRoute = nic.getReceived();

			if (toRoute != null) {
				// There is something to route through - or it might have arrived at destination
				process = true;
				
				if(toRoute.data instanceof PingPacket) {
					//We process our ping data
					PingPacket p = (PingPacket) toRoute.data;
					debug.println(1, "WE received A PING PACKET YAAAAAY");
					//If our packet has been recieved, and the destination is the original sender
					if(p.isRecieved()) {
						debug.println(4, "(LinkStateRouter.run): PingPacket has arrived!  Reporting to the NIC - for accounting purposes!" + 
						" IP Address: " + nic.getNSAP() + "link from which it was sent is: " + toRoute.originator);

						//nic.sendOnLink(nic.getOutgoingLinks().indexOf(toRoute.originator), p);//This finds the link that 
						table.put(toRoute.originator);

					}else{
						p.recieved();
						//PingPacket returnPacket = new PingPacket(nsap, p.source, 20);
						
						//Need to identify the link to send it back out on.
						debug.println(1, "Returning to sender:" + p.source);

						//nic.sendOnLink(p., p)

						//Need to identify the link index and which of those links will have the ip address, in link 65


						nic.sendOnLink(nic.getOutgoingLinks().indexOf(toRoute.originator), p);
						
						//route()


						//Each ping packet when sent will store the current time
						//If we sent it
						//Then we check the time and store the value in a hashmap
						//If we didn't send it, then we 

					}

				}else if (toRoute.data instanceof Packet) {
					Packet p = (Packet) toRoute.data;
					if (p.dest == nsap) {
						// It made it!  Inform the "network" for statistics tracking purposes
						debug.println(4, "(LinkStateRouter.run): Packet has arrived!  Reporting to the NIC - for accounting purposes!");
						debug.println(6, "(LinkStateRouter.run): Payload: " + p.payload);
						nic.trackArrivals(p.payload);
					} else if (p.hopCount > 0) {
						// Still more routing to do
						p.hopCount--;
						route(toRoute.originator, p);
					} else {
						debug.println(5, "Packet has too many hops.  Dropping packet from " + p.source + " to " + p.dest + " by router " + nsap);
					}
				} 


				

			}


			if (!process) {
				// Didn't do anything, so sleep a bit
				try { Thread.sleep(1); } catch (InterruptedException e) { }
			}
		}
	}

	//For some small amount of time, wait and then send a pingpacket to all neighbors

	private void floodPingPackets(PingPacket p) {
		ArrayList<Integer> outLinks = nic.getOutgoingLinks();
		int size = outLinks.size();
		for (int i = 0; i < size; i++) {
			nic.sendOnLink(i, p);
		}
	}

	private void route(int linkOriginator, Packet p) {
		ArrayList<Integer> outLinks = nic.getOutgoingLinks();
		int size = outLinks.size();
		for (int i = 0; i < size; i++) {
			if (outLinks.get(i) != linkOriginator) {
				// Not the originator of this packet - so send it along!
				nic.sendOnLink(i, p);
			}
		}
	}

}
