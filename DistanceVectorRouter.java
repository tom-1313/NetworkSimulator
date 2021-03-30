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
    /**
     * TODO: Lots of routes are dropped in route method. Table may not be built before routing. Maybe have a delay before routing packets.
     *
     **/

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
    private double[] pingDist; //Ping distance of the neighbors based on the index of the link
    private ArrayList<Map<Integer, DLPair>> neighborMap = new ArrayList<>(); //routeMaps of neighboring routers
    private ArrayList<Integer> outLinks = nic.getOutgoingLinks(); //Outgoing links of this router
    private long nextRecalcTime;
    private static final long TIME_BETWEEN_RECALC = 1000; // (milliseconds)


    public DistanceVectorRouter(int nsap, NetworkInterface nic) {
        super(nsap, nic);
        debug = Debug.getInstance();  // For debugging!
        nextRecalcTime = System.currentTimeMillis() + TIME_BETWEEN_RECALC;
        pingDist = new double[outLinks.size()];
        for (int i = 0; i < pingDist.length; i++) {
            pingDist[i] = Double.POSITIVE_INFINITY;
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
                route(toSend.destination, new Packet(nsap, toSend.destination, 0, toSend.data));

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
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    //Process the packets recieved from the network
    private void processPacket(int originator, Object data) {

        //If its another router's table, add it to the neighbor table map
        if (data instanceof DistPacket) {
            DistPacket p = (DistPacket) data;
            neighborMap.add(p.distTable);
        }

        //If the data is a ping then it is send back to originator,
        //If the data already arrived to destination, update the table
        if (data instanceof Ping) {
            Ping ping = (Ping) data;
            if (ping.dest == nic.getNSAP()) {
                ping.arrived = true;
                nic.sendOnLink(outLinks.indexOf(originator), ping);
            } else if (ping.arrived) {
                //Update the time on the neighbor map
//                routeMap.put(ping.dest, new DLPair((System.currentTimeMillis() - ping.startTime)/2, outLinks.indexOf(originator)));
//                neighborPing.put(ping.dest, new DLPair((System.currentTimeMillis() - ping.startTime) / 2, outLinks.indexOf(originator)));
                pingDist[outLinks.indexOf(originator)] = (System.currentTimeMillis() - ping.startTime) / 2.0;
            }
        }

        //If it is a packet and it reached it's destination then the packet is tracked,
        //If not it is routed again
        if (data instanceof Packet) {
            Packet packet = (Packet) data;
            if (packet.dest == nsap) {
                nic.trackArrivals(packet.payload);
            } else {
                route(packet.dest, packet);
            }
        }
    }

    //Recalculates the routeMap using the neighboring routeMaps stored in neighborMap
    private void recalculate() {
        nextRecalcTime = System.currentTimeMillis() + TIME_BETWEEN_RECALC;
        Map<Integer, DLPair> tempMap = new HashMap<>();
        tempMap.put(nic.getNSAP(), new DLPair(0, -1));

        //TODO: For each value of the neighborMap and pingDist, add the values up at the index,
        // check to see if tempMap has that value.
        // If it does and its less than the current value in tempMap, add it to tempMap.
        for (int i = 0; i < neighborMap.size(); i++) {
//            neighborMap.get(i).forEach((id, dl) -> debug.println(0, "node: " + id + " distance = " + dl.distance + " link: " + dl.link));
            neighborMap.get(i).forEach((id, dl) -> tempMap.put(id, dl));
        }

        // transmit table to the neighbors
        DistPacket distPacket = new DistPacket(tempMap);
        int size = outLinks.size();
        for (int i = 0; i < size; i++) {
            nic.sendOnLink(i, distPacket);
        }

        if (nic.getNSAP() == 10) {
//            tempMap.forEach((id, dl) -> debug.println(0, "node: " + id + " distance = " + dl.distance + " link: " + dl.link));
//            System.out.println("Finished printing temp");
        }

        routeMap = tempMap; //makes it the new map (might need to be synchronized)
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
     * Sends the given packet out based on the table
     **/
    private void route(int link, Packet p) {
//        debug.println(0, "Attempting to send to " + link + " via the routMap of " + routeMap.get(link).link);
        if (routeMap.get(link) != null) {
            nic.sendOnLink(routeMap.get(link).link, p);
        } else {
            nic.sendOnLink(0,p);
//            debug.println(0, "Can't route packet. Sending to link: " + outLinks.get(0));
        }
    }
}
