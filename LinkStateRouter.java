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
    public HashMap<int, Double> table;
    
    public LinkStateRouter(int nsap, NetworkInterface nic) {
        super(nsap, nic);
        debug = Debug.getInstance();  // For debugging!
    }

    public void run() {

        table = new Hashmap<>();
        
        while (true) {
            // See if there is anything to process
            boolean process = false;
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

            if (!process) {
                // Didn't do anything, so sleep a bit
                try { Thread.sleep(1); } catch (InterruptedException e) { }
            }
        }
    }
}
