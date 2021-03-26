
/***************
 * LinkStateRouter
 * Author: Christian Duncan
 * Modified by: 
 * Represents a router that uses a Distance Vector Routing algorithm.
 ***************/
import java.util.ArrayList;
import java.util.HashMap;

public class LinkStateRouter extends Router {
	// A generator for the given LinkStateRouter class
	public static class Generator extends Router.Generator {
		public Router createRouter(int id, NetworkInterface nic) {
			return new LinkStateRouter(id, nic);
		}
	}

	Debug debug;
	public HashMap<Integer, Double> table;

	public LinkStateRouter(int nsap, NetworkInterface nic) {
		super(nsap, nic);
		debug = Debug.getInstance(); // For debugging!
		table = new HashMap<Integer, Double>();
	}

	public void run() {

    
        while (true) {
        	
        	boolean process = false;
        	nic.sendOnLink(100, new PingPacket(nsap, 100, 20));
        	
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
                if (toRoute.data instanceof Packet) {
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
                    
                }else if(toRoute.data instanceof PingPacket) {
                	//We process our ping data
                	PingPacket p = (PingPacket) toRoute.data;
                	
                	//If our packet has been recieved, and the destination is the original sender
                	if(p.dest == nsap && p.isRecieved()) {
                		debug.println(4, "(LinkStateRouter.run): PingPacket has arrived!  Reporting to the NIC - for accounting purposes!");
                		
                	}else if(p.dest == nsap && !p.isRecieved()){
                		p.isRecieved();
                		PingPacket returnPacket = new PingPacket(nsap, p.source, 20);
                		route(toRoute.originator, returnPacket);
                			
                	
                		//route()
                	
                	
                	//Each ping packet when sent will store the current time
                	//If we sent it
                		//Then we check the time and store the value in a hashmap
                		//If we didn't send it, then we 
        
            	}else {
                    debug.println(0, "Error.  The packet being tranmitted is not a recognized Packet.  Not processing");
                }
            }
        	
            }


            if (!process) {
                // Didn't do anything, so sleep a bit
                try { Thread.sleep(1); } catch (InterruptedException e) { }
            }
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
