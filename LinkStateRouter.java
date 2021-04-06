
/***************
 * LinkStateRouter
 * Author: Christian Duncan
 * Modified by: 
 * Represents a router that uses a Distance Vector Routing algorithm.
 ***************/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Each router has a graph that it generates via pinging its neighbors. 

public class LinkStateRouter extends Router {
	// A generator for the given LinkStateRouter class
	public static class Generator extends Router.Generator {
		public Router createRouter(int id, NetworkInterface nic) {
			return new LinkStateRouter(id, nic);
		}
	}

	Debug debug;
	public HashMap<Integer, Double> linkTable;
	public Map<Integer, HashMap<Integer, Double>> networkTable;
	public static final int delay = 1000;
	public Graph graph;
	public Node router;
	public final int tableHopCount = 15;
	int flip;

	public LinkStateRouter(int nsap, NetworkInterface nic) {
		super(nsap, nic);
		linkTable = new HashMap<Integer, Double>();
		networkTable = new HashMap<Integer, HashMap<Integer, Double>>();
		graph = new Graph();
		router = new Node(String.valueOf(nsap));
		debug = Debug.getInstance(); // For debugging!
		flip = 0;
	}

	public void run() {

		long nextPingTime = System.currentTimeMillis();
		while (true) {
			
			boolean process = false;

			// See if there is anything to process
			NetworkInterface.TransmitPair toSend = nic.getTransmit();

			// NetworkInterface.TransmitPair toSend = null;
			if (toSend != null) {
				// There is something to send out
				process = true;
				debug.println(3, "(LinkStateRouter.run): I am being asked to transmit: " + toSend.data
						+ " to the destination: " + toSend.destination);
				route(nsap, new Packet(nsap, toSend.destination, tableHopCount, toSend.data));
			}
			
			// Send a ping every delay, and update our graph
			if (nextPingTime <= System.currentTimeMillis() && flip == 0) {
				
				//Send pings to all of our neighbors
				floodPingPackets();
				
				nextPingTime = System.currentTimeMillis() + delay;
				flip++;
			}else if(nextPingTime <= System.currentTimeMillis() && flip == 1) {
				
				//Send graph packets to everyone 
				floodGraphPackets();
				
				nextPingTime = System.currentTimeMillis() + delay;
				flip++;
			}else if(nextPingTime <= System.currentTimeMillis() && flip == 2) {
				
				//Create a new graph
				graph = new Graph();
				
				//Our current router
				router = new Node(nsap);
				graph.addNode(router);
				
				
				
				debug.println(3, router.getName() + ": ");
				
				//All of our direct destinations
				linkTable.forEach((routerInt, distance) -> {
					Node tableNode = graph.addNode(new Node(routerInt));
					router.addDestination(tableNode, distance);
					//tableNode.addDestination(router, distance);
					debug.println(3, "	" + routerInt + " - " + distance);
					//System.out.println(distance);
					
				});
				
				
				networkTable.forEach((routerHeadInt, map) -> {
					//check and see if we already have a node with this name, if we do, then just use the one we already have instead of creating a new one
					Node headerNode = graph.addNode(new Node(routerHeadInt));
					debug.println(1, "		" + routerHeadInt + ": ");
					for (Map.Entry<Integer,Double> entry : map.entrySet()) {
						int routerInt = entry.getKey();
						double distance = entry.getValue();
						
						Node tableNode = graph.addNode(new Node(routerInt));
						headerNode.addDestination(tableNode, distance);
						//tableNode.addDestination(headerNode, distance);
						debug.println(3, "			" + routerInt + " - " + distance);
						
					}

				});
				
				debug.println(1, "");
				debug.println(1, "");
				debug.println(1, "");
				//Actually calculate our graph
				graph.calculateShortestPathFromSource(graph, router);
				debug.println(1, graph.toString());
				//debug.println(2, router.output());
				nextPingTime = System.currentTimeMillis() + delay;
				flip = 0;
			}


			NetworkInterface.ReceivePair toRoute = nic.getReceived();

			if (toRoute != null) {
				// There is something to route through - or it might have arrived at destination
				process = true;

				if (toRoute.data instanceof GraphPacket) {
					// debug.println(1, "Graph packet!!!!!!!!");
					GraphPacket p = (GraphPacket) toRoute.data;
					//double timeTakenToTraverse = System.currentTimeMillis() - p.getStartTime();
					
					if(p.hopCount > 0) {
						p.hopCount--;
						floodRoute(nsap, p);
					}
					
					networkTable.put(p.source, p.linkTable);
					//router.addDestination(p.graph.getSourceNode(), timeTakenToTraverse);
					//concatonateGraphNodes(p);
					

				}else if (toRoute.data instanceof PingPacket) {
					// We process our ping data
					PingPacket p = (PingPacket) toRoute.data;

					if (p.isRecieved() && p.dest == nsap) {
						
						debug.println(4, nsap + " successfully sent and recieved a ping to " + p.source);

						double timeTaken = (double) ((System.currentTimeMillis() - p.getStartTime()) / 2);

						debug.println(4,
								"(LinkStateRouter.run): PingPacket has arrived!  Reporting to the NIC - for accounting purposes!"
										+ " IP Address: " + nic.getNSAP() + "link from which it was sent is: "
										+ toRoute.originator + " time taken: " + timeTaken);

						linkTable.put(p.source, timeTaken);

						// If the packet reaches its destination, but we
					} else if (p.dest == nsap) {
						p.recieved();
						
						//debug.println(4, nsap + " has been pinged by " + p.source);
						// PingPacket returnPacket = new PingPacket(nsap, p.source, 20);
						int temp = p.dest;
						p.dest = p.source;
						p.source = temp;
						p.hopCount = tableHopCount;
						// Need to identify the link to send it back out on.
						debug.println(4, nsap + " Returning a ping to sender: " + p.dest);
						//floodRoute(nsap, p);
						route(nsap, p);

					}else {
						p.hopCount--;
						route(nsap, p);
					}

				} else if (toRoute.data instanceof Packet) {
					Packet p = (Packet) toRoute.data;
					if (p.dest == nsap) {
						// It made it! Inform the "network" for statistics tracking purposes
						debug.println(4,
								"(LinkStateRouter.run): Packet has arrived!  Reporting to the NIC - for accounting purposes!");
						debug.println(6, "(LinkStateRouter.run): Payload: " + p.payload);
						nic.trackArrivals(p.payload);
					} else {
						route(nsap, p);
					}
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
	}

	// For some small amount of time, wait and then send a pingpacket to all
	// neighbors

	private void floodPingPackets() {
		for (int randNSAP : nic.getOutgoingLinks()) {
			PingPacket p = new PingPacket(nsap, randNSAP, System.currentTimeMillis());
			debug.println(4, nsap + " is sending a ping to " + randNSAP);
			nic.sendOnLink(nic.getOutgoingLinks().indexOf(randNSAP), p);
		}
		
	}
	
	private void floodPingPackets(PingPacket p) {
		for (int randNSAP : nic.getOutgoingLinks()) {
			//debug.println(4, nsap + " is returning a ping to " + randNSAP);
			nic.sendOnLink(nic.getOutgoingLinks().indexOf(randNSAP), p);
		}
	}

	private void floodGraphPackets() {
		/*
		ArrayList<Integer> outLinks = nic.getOutgoingLinks();
		int size = outLinks.size();
		for (int i = 0; i < size; i++) {
			nic.sendOnLink(i, p);
		}
		*/
		for (int randNSAP : nic.getOutgoingLinks()) {
			GraphPacket p = new GraphPacket(nsap, randNSAP, tableHopCount, linkTable);
			nic.sendOnLink(nic.getOutgoingLinks().indexOf(p.dest), p);
		}
	}

	private void route(int linkOriginator, Packet p) {
		//Send a packet to its destination as defined in the packet class
		int destinationNSAP = graph.getCalculatedDestination(p.dest);
		
		boolean graphCalculatedCorrectly = (destinationNSAP != -1);
		
		boolean destIsNeighbor = nic.getOutgoingLinks().contains(destinationNSAP);
			
			// Route through the graph!
			if (graphCalculatedCorrectly && destIsNeighbor) {
				nic.sendOnLink(nic.getOutgoingLinks().indexOf(destinationNSAP), p);
				//debug.println(1, "routed correctly!");
			}
			// If we can't route through the graph, we see if we can send to a neighbor!
			else if(nic.getOutgoingLinks().contains(p.dest)){
				nic.sendOnLink(nic.getOutgoingLinks().indexOf(p.dest), p);
				// Oh no! We can't send it anywhere and are forced to drop the packet!
			}else {
				
				if(p instanceof PingPacket) {
					debug.println(1, nsap + " Sent a return ping via flooding to " + p.dest);
					floodPingPackets((PingPacket)p);
				}
				//debug.println(1, "Error: Unable to route from " + nsap + " to " + p.dest + " " + destinationNSAP);
			}

		
	}
	
	
	private void floodRoute(int linkOriginator, Packet p) {
        ArrayList<Integer> outLinks = nic.getOutgoingLinks();
        int size = outLinks.size();
        for (int i = 0; i < size; i++) {
            if (outLinks.get(i) != nsap && nic.getOutgoingLinks().indexOf(p.source) != linkOriginator) {
                // Not the originator of this packet - so send it along!
                nic.sendOnLink(i, p);
            }
        }
    }
    

}
