/***************
 * DistanceVectorRouter
 * Author: Christian Duncan
 * Modified by: Adam Curley, Tom Gadacy, Matt Hendrickson, Andrew DePass
 * Represents a router that uses a Distance Vector Routing algorithm.
 ***************/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DistanceVectorRouter extends Router {
    /*
    TODO:
     */

    // A generator for the given DistanceVectorRouter class
    public static class Generator extends Router.Generator {
        public Router createRouter(int id, NetworkInterface nic) {
            return new DistanceVectorRouter(id, nic);
        }
    }

    public static class Packet {
        // This is how we will store our Packet Header information
        int source;
        int dest;
        int hopCount;  // Maximum hops to get there
        long starTime = System.currentTimeMillis();
        Object payload;
        boolean arrived = false; //if true, packet arrived at destination send back.


        public Packet(int source, int dest, int hopCount, Object payload) {
            this.source = source;
            this.dest = dest;
            this.hopCount = hopCount;
            this.payload = payload;

        }
    }

    // this is the special packet that contains the distance table
    public static class DistPacket {
        Map<Integer, DLPair> distTable;

        //Integer = Router, DLPair = distance to the Router and the link to send it on
        public DistPacket(Map<Integer, DLPair> distTable) {
            this.distTable = distTable;
        }
    }

    // Distance and Link in a pair
    private static class DLPair {
        double distance;
        int link;

        public DLPair(double distance, int link) {
            this.distance = distance;
            this.link = link;
        }
    }

    Debug debug;
    private Map<Integer, DLPair> routeMap = new HashMap<>(); // Integer and Pair (Distance and Link) used for routing
    private ArrayList<Map<Integer, DLPair>> neighborMap = new ArrayList<>();
    private ArrayList<Integer> outLinks = nic.getOutgoingLinks();
    private long nextRecalcTime;
    private static final long TIME_BETWEEN_RECALC = 1000; // (milliseconds)

    
    public DistanceVectorRouter(int nsap, NetworkInterface nic) {
        super(nsap, nic);
        debug = Debug.getInstance();  // For debugging!
        nextRecalcTime = System.currentTimeMillis() + TIME_BETWEEN_RECALC;
    }

    public void run() {
        while (true) {
            if (System.currentTimeMillis() >= nextRecalcTime) {
                pingNeigbhors();
                recalculate();
            }
            // See if there is anything to process
            boolean process = false;
            NetworkInterface.TransmitPair toSend = nic.getTransmit();
            if (toSend != null) {
                // There is something to send out
                // Check the table to see what route is the fastest to the destination, send it to that router
                process = true;
//                debug.println(0, "(DistanceVectorRouter.run): I am being asked to transmit: " + toSend.data + " to the destination: " + toSend.destination);
                route(toSend.destination, new Packet(nsap, toSend.destination,0, toSend.data));

            }

            NetworkInterface.ReceivePair toRoute = nic.getReceived();
            if (toRoute != null) {
                // There is something to route through - or it might have arrived at destination
                // if the destination = this router, do nothing
                // else send it to the fastest router based on the table
                process = true;
                //debug.println(3, "(DistanceVectorRouter.run): I am being asked to transmit: " + toRoute.data + " to the destination: " + toRoute.destination);
                processPacket(toRoute.originator, toRoute.data);
            }

            if (!process) {
                // Didn't do anything, so sleep a bit
                try { Thread.sleep(50); } catch (InterruptedException e) { }
            }
        }
    }

    private void processPacket(int originator, Object data) {
        if (data instanceof DistPacket) {
            DistPacket p = (DistPacket) data;
            neighborMap.add(p.distTable);
        }
        if (data instanceof Packet) {
            Packet packet = (Packet) data;
            if (packet.dest == nic.getNSAP()) {
                packet.arrived = true;
                outLinks = nic.getOutgoingLinks();
                nic.sendOnLink(outLinks.indexOf(originator), packet);
            } else if (packet.arrived = true) {
                //Update the time on the neighbor map
                routeMap.put(packet.dest, new DLPair(System.currentTimeMillis() - packet.starTime, outLinks.indexOf(originator)) );
                neighborMap.add(routeMap);
            }
        }
    }

    private void recalculate() {
        nextRecalcTime = System.currentTimeMillis() + TIME_BETWEEN_RECALC;
        Map<Integer, DLPair> tempMap = new HashMap<>();
        tempMap.put(nic.getNSAP(), new DLPair(0, -1));

        // Map<Integer, DLPair>
        // build the table from the neighbors
        // for debugging print out the list of tables
        for (int i = 0; i < neighborMap.size(); i++) {
//            neighborMap.get(i).forEach((id, dl) -> debug.println(0, "node: " + id + " distance = " + dl.distance + " link: " + dl.link));
            neighborMap.get(i).forEach((id, dl) -> tempMap.put(id, dl));
        }

        // transmit table to the neighbors
        DistPacket distPacket = new DistPacket(tempMap);
        ArrayList<Integer> outLinks = nic.getOutgoingLinks();
        int size = outLinks.size();
        for (int i = 0; i < size; i++) {
            nic.sendOnLink(i, distPacket);
        }

        if (nic.getNSAP() == 10) {
            tempMap.forEach((id, dl) -> debug.println(0, "node: " + id + " distance = " + dl.distance + " link: " + dl.link));
        }

        routeMap = tempMap; //makes it the new map (might need to be synchronized)
    }

    private void pingNeigbhors() {
        ArrayList<Integer> outLinks = nic.getOutgoingLinks();
        int size = outLinks.size();
        for (int i = 0; i < size; i++) {
            nic.sendOnLink(i, new Packet(nic.getNSAP(), outLinks.get(i), 0, null));
        }
    }

    /** Route the given packet out.
     Sends the given packet out based on the table
    **/
    private void route(int link, Packet p) {
//        System.out.println("LINK: " + link);
        if (routeMap.get(link) != null) {
            nic.sendOnLink(routeMap.get(link).link,p);
        }

    }
}
