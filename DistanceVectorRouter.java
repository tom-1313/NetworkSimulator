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

    // A generator for the given DistanceVectorRouter class
    public static class Generator extends Router.Generator {
        public Router createRouter(int id, NetworkInterface nic) {
            return new DistanceVectorRouter(id, nic);
        }
    }

    //Class to ping neighbors
    public class Ping {
        long startTime;
        boolean arrived;
        int dest;

        public Ping(int dest, long startTime) {
            this.dest = dest;
            this.startTime = startTime;
        }
    }

    // This is the special packet that contains the distance table
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
    private Map<Integer, DLPair> routeTable = new HashMap<>(); // Integer and Pair (Distance and Link) used for routing
    public double[] pingDist; //Ping distance of the neighbors based on the index of the link
    private ArrayList<Integer> outLinks = nic.getOutgoingLinks(); //Outgoing links of this router
    private ArrayList<Map<Integer, DLPair>> neighborMap = new ArrayList<>(); //routeMaps of neighboring routers
    private long nextRecalcTime;
    private static final long TIME_BETWEEN_RECALC = 1000; // (milliseconds)


    public DistanceVectorRouter(int nsap, NetworkInterface nic) {
        super(nsap, nic);
        debug = Debug.getInstance();  // For debugging!
        nextRecalcTime = System.currentTimeMillis() + TIME_BETWEEN_RECALC;
        pingDist = new double[outLinks.size()];

        //Initialize the pingDist to Infinity and add the values to the neighborMap
        for (int i = 0; i < pingDist.length; i++) {
            pingDist[i] = Double.POSITIVE_INFINITY;

            Map<Integer, DLPair> initTable = new HashMap<>();
            initTable.put(outLinks.get(i), new DLPair(pingDist[i], i));
            neighborMap.add(initTable);
        }
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
                process = true;
//                debug.println(0, "(DistanceVectorRouter.run): I am being asked to transmit: " + toSend.data + " to the destination: " + toSend.destination);
                route(toSend.destination, new Packet(nsap, toSend.destination, 5, toSend.data));

            }

            NetworkInterface.ReceivePair toRoute = nic.getReceived();
            if (toRoute != null) {
                // There is something to route through - or it might have arrived at destination
                process = true;
                //debug.println(3, "(DistanceVectorRouter.run): I am being asked to transmit: " + toRoute.data + " to the destination: " + toRoute.destination);
                processPacket(toRoute.originator, toRoute.data);
            }

            if (!process) {
                // Didn't do anything, so sleep a bit
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    //Process the packets recieved from the network
    private void processPacket(int originator, Object data) {

        //Update the table in the neighborMap with the new incoming table
        if (data instanceof DistPacket) {
            DistPacket p = (DistPacket) data;
            neighborMap.set(outLinks.indexOf(originator), p.distTable);
        }

        //If the data is a ping then it is send back to originator,
        //If the data already arrived to destination, update the table
        if (data instanceof Ping) {
            Ping ping = (Ping) data;
            if (ping.dest == nic.getNSAP()) {
                ping.arrived = true;
                nic.sendOnLink(outLinks.indexOf(originator), ping);
            } else if (ping.arrived) {
                pingDist[outLinks.indexOf(originator)] = (System.currentTimeMillis() - ping.startTime) / 2.0;
            }
        }

        //If it is a packet and it reached it's destination then the packet is tracked,
        //If not it is routed again and its hop count is decreased
        if (data instanceof Packet) {
            Packet packet = (Packet) data;
            if (packet.dest == nsap) {
                nic.trackArrivals(packet.payload);
            } else if (packet.hopCount > 0) {
//                packet.hopCount--;
                route(packet.dest, packet);
            } else {
                debug.println(0, "Packet has too many hops.  Dropping packet from " + packet.source + " to " + packet.dest + " by router " + nsap);
            }
        }
    }

    //Recalculates the routeMap using the neighboring routeMaps stored in neighborMap
    private void recalculate() {
        nextRecalcTime = System.currentTimeMillis() + TIME_BETWEEN_RECALC;
        Map<Integer, DLPair> tempTable = new HashMap<>();
        tempTable.put(nic.getNSAP(), new DLPair(0, -1));

        //add the pingDist table to the temp map.
        for (int i = 0; i < pingDist.length; i++) {
            tempTable.put(outLinks.get(i), new DLPair(pingDist[i], i));
        }

        for (int i = 0; i < neighborMap.size(); i++) {
            final int CURRENT_LINK = i;
            neighborMap.get(i).forEach((id, dl) -> {
                double dist = dl.distance + pingDist[CURRENT_LINK];
                if (routeTable.get(id) == null || dist < routeTable.get(id).distance) {
                    tempTable.put(id, new DLPair(dist, CURRENT_LINK));
                }
            });
        }

        // transmit table to the neighbors
        DistPacket distPacket = new DistPacket(tempTable);
        int size = outLinks.size();
        for (int i = 0; i < size; i++) {
            nic.sendOnLink(i, distPacket);
        }

        routeTable = tempTable; //makes it the new map (might need to be synchronized)
    }

    private void pingNeigbhors() {
        ArrayList<Integer> outLinks = nic.getOutgoingLinks();
        int size = outLinks.size();
        for (int i = 0; i < size; i++) {
            nic.sendOnLink(i, new Ping(outLinks.get(i), System.currentTimeMillis()));
        }
    }

    /**
     * Route the given packet out.
     * Sends the given packet out based on the table if the destination is not on the table it routes it to the first router
     **/
    private void route(int link, Packet p) {
        if (routeTable.get(link) != null) {
            nic.sendOnLink(routeTable.get(link).link, p);
        } else {
            nic.sendOnLink(0, p);
        }
    }
}
