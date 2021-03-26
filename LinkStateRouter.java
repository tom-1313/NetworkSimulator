/***************
 * LinkStateRouter
 * Author: Christian Duncan
 * Modified by: 
 * Represents a router that uses a Distance Vector Routing algorithm.
 ***************/
import java.util.ArrayList;
import java.util.HashMap;

import FloodRouter.Packet;

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
        debug = Debug.getInstance();  // For debugging!
        table = new HashMap<Integer, Double>();
    }

    public void run() {

    
        while (true) {
        	
        	boolean process = false;

            // See if there is anything to process
            NetworkInterface.TransmitPair toSend = nic.getTransmit();
            if (toSend != null) {
                // There is something to send out
                process = true;
                debug.println(3, "(LinkStateRouter.run): I am being asked to transmit: " + toSend.data + " to the destination: " + toSend.destination);
            }

            NetworkInterface.ReceivePair toRoute = nic.getReceived();
            if (toRoute != null) {
                // There is something to route through - or it might have arrived at destination
                process = true;
                debug.println(3, "(LinkStateRouter.run): I am being asked to transmit: " + toSend.data + " to the destination: " + toSend.destination);
            }
        	
            if (toRoute != null) {
                // There is something to route through - or it might have arrived at destination
                process = true;
                if (toRoute.data instanceof Packet) {
                    Packet p = (Packet) toRoute.data;
                    if (p.dest == nsap) {
                        // It made it!  Inform the "network" for statistics tracking purposes
                        debug.println(4, "(FloodRouter.run): Packet has arrived!  Reporting to the NIC - for accounting purposes!");
                        debug.println(6, "(FloodRouter.run): Payload: " + p.payload);
                        nic.trackArrivals(p.payload);
                    } else if (p.hopCount > 0) {
                        // Still more routing to do
                        p.hopCount--;
                        route(toRoute.originator, p);
                    } else {
                        debug.println(5, "Packet has too many hops.  Dropping packet from " + p.source + " to " + p.dest + " by router " + nsap);
                    }
                } else {
                    debug.println(0, "Error.  The packet being tranmitted is not a recognized Flood Packet.  Not processing");
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
