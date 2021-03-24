/***************
 * DistanceVectorRouter
 * Author: Christian Duncan
 * Modified by: Adam Curley, Tom Gadacy, Matt Hendrickson, Andrew DePass
 * Represents a router that uses a Distance Vector Routing algorithm.
 ***************/
import java.util.ArrayList;

public class DistanceVectorRouter extends Router {
    /*
    TODO: 1. Create a Table for the neighboring Routers based on the fastest times from the ping. 2. Create a thread that updates the table every so often
     */

    // A generator for the given DistanceVectorRouter class
    public static class Generator extends Router.Generator {
        public Router createRouter(int id, NetworkInterface nic) {
            return new DistanceVectorRouter(id, nic);
        }
    }

    Debug debug;
    
    public DistanceVectorRouter(int nsap, NetworkInterface nic) {
        super(nsap, nic);
        debug = Debug.getInstance();  // For debugging!
    }

    public void run() {
        while (true) {
            // See if there is anything to process
            boolean process = false;
            NetworkInterface.TransmitPair toSend = nic.getTransmit();
            if (toSend != null) {
                // There is something to send out
                // Check the table to see what route is the fastest to the destination, send it to that router
                process = true;
                debug.println(3, "(DistanceVectorRouter.run): I am being asked to transmit: " + toSend.data + " to the destination: " + toSend.destination);
            }

            NetworkInterface.ReceivePair toRoute = nic.getReceived();
            if (toRoute != null) {
                // There is something to route through - or it might have arrived at destination
                // if the destination = this router, do nothing
                // else send it to the fastest router based on the table
                process = true;
                debug.println(3, "(DistanceVectorRouter.run): I am being asked to transmit: " + toSend.data + " to the destination: " + toSend.destination);
            }

            if (!process) {
                // Didn't do anything, so sleep a bit
                try { Thread.sleep(1); } catch (InterruptedException e) { }
            }
        }
    }

   //not implemented yet
    /** Route the given packet out.
     In our case, we go to all nodes except the originator
     **/
    private void route(int linkOriginator, FloodRouter.Packet p) {
//        ArrayList<Integer> outLinks = nic.getOutgoingLinks();
//        int size = outLinks.size();
//        for (int i = 0; i < size; i++) {
//            if (outLinks.get(i) != linkOriginator) {
//                // Not the originator of this packet - so send it along!
//                nic.sendOnLink(i, p);
//            }
//        }
    }
}
