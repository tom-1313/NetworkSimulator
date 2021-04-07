
/***************
 * LinkStateRouter
 * Author: Christian Duncan
 * Modified by: Matthew Hendrickson and Andrew DePass
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
	public HashMap<Integer, Double> linkTable; //This stores a table of the links the packets can be sent on
	public Map<Integer, HashMap<Integer, Double>> networkTable; //this stores a table of the entire network shich is sent to every packet
	public static final int delay = 1000; //This variable holds the delay which is accounted for in our time calculations for reporting
	public Graph graph; //This graph is used to hold the map of the network and conduct shortest distance calculations
	public Node router; //This is an instance of a router
	public final int tableHopCount = 15; //This limits the travel of the Graph packet in the network
	int flip;//this variable is used in conditionals to ensure that creation and dispersal of the graph packet happens in sequence

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
				
				
				
				debug.println(2, router.getName() + ": ");
				
				//All of our direct destinations
				linkTable.forEach((routerInt, distance) -> {
					Node tableNode = graph.addNode(new Node(routerInt));
					router.addDestination(tableNode, distance);
					//tableNode.addDestination(router, distance);
					debug.println(2, "	" + routerInt + " - " + distance);
					//System.out.println(distance);
					
				});
				
				
				networkTable.forEach((routerHeadInt, map) -> {
					//check and see if we already have a node with this name, if we do, then just use the one we already have instead of creating a new one
					Node headerNode = graph.addNode(new Node(routerHeadInt));
					debug.println(2, "		" + routerHeadInt + ": ");
					for (Map.Entry<Integer,Double> entry : map.entrySet()) {
						int routerInt = entry.getKey();
						double distance = entry.getValue();
						
						Node tableNode = graph.addNode(new Node(routerInt));
						headerNode.addDestination(tableNode, distance);
						debug.println(2, "			" + routerInt + " - " + distance);
						
					}

				});
				
				debug.println(2, "");
				debug.println(2, "");
				debug.println(2, "");
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


				//Procedure when what needs to be routed is a graph packet
				if (toRoute.data instanceof GraphPacket) {
					
					GraphPacket p = (GraphPacket) toRoute.data; 
					
					//If the hopcount of the data is greater than zero the graph packet is broadcasted across the network
					if(p.hopCount > 0) {
						p.hopCount--;
						floodRoute(nsap, p);
					}
					
					networkTable.put(p.source, p.linkTable); //The instance of the graphpacket is added to the greater network table.
					
					
				//Procedure when what needs to be routed is a ping packet	
				}else if (toRoute.data instanceof PingPacket) {
					// We process our ping data
					PingPacket p = (PingPacket) toRoute.data;

					//if the ping packet is received we calculate the time taken and store it in the link table.
					if (p.isRecieved() && p.dest == nsap) {
						
						debug.println(4, nsap + " successfully sent and recieved a ping to " + p.source);

						double timeTaken = (double) ((System.currentTimeMillis() - p.getStartTime()) / 2);

						debug.println(4,
								"(LinkStateRouter.run): PingPacket has arrived!  Reporting to the NIC - for accounting purposes!"
										+ " IP Address: " + nic.getNSAP() + "link from which it was sent is: "
										+ toRoute.originator + " time taken: " + timeTaken);

						linkTable.put(p.source, timeTaken);

						// If the packet reaches its destination, it will be returned to the sender
					} else if (p.dest == nsap) {
						p.recieved();
						
						int temp = p.dest;
						p.dest = p.source;
						p.source = temp;
						p.hopCount = tableHopCount;
						
						debug.println(4, nsap + " Returning a ping to sender: " + p.dest); // Identifies the link to send it back out on.

						route(nsap, p);

					}else {
						p.hopCount--;
						route(nsap, p);
					}

				//Procedure when what needs to be routed is a packet.
				} else if (toRoute.data instanceof Packet) {
					Packet p = (Packet) toRoute.data;

					//This is the case where the packet made it to its destination
					if (p.dest == nsap) {
						//Belown informs the network for statistics tracking purposes
						debug.println(4,
								"(LinkStateRouter.run): Packet has arrived!  Reporting to the NIC - for accounting purposes!");
						debug.println(6, "(LinkStateRouter.run): Payload: " + p.payload);
						nic.trackArrivals(p.payload);

					//If the packet has not made it to its destination it is sent back to its originator 
					} else { 
						route(nsap, p);
					}
				}


				// If nothing has happened the thread is put to sleep
				if (!process) {
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

	//This is the method for flooding ping packets across the network
	private void floodPingPackets() {
		for (int randNSAP : nic.getOutgoingLinks()) {
			PingPacket p = new PingPacket(nsap, randNSAP, System.currentTimeMillis());
			debug.println(4, nsap + " is sending a ping to " + randNSAP);
			nic.sendOnLink(nic.getOutgoingLinks().indexOf(randNSAP), p);
		}
		
	}
	
	//This is the method for flooding a specific ping packet across the network
	private void floodPingPackets(PingPacket p) {
		for (int randNSAP : nic.getOutgoingLinks()) {
			nic.sendOnLink(nic.getOutgoingLinks().indexOf(randNSAP), p);
		}
	}

	//This is the method for flooding Graph packets across the network
	private void floodGraphPackets() {
		
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
				
			}
			// If we can't route through the graph, we see if we can send to a neighbor
			else if(nic.getOutgoingLinks().contains(p.dest)){
				nic.sendOnLink(nic.getOutgoingLinks().indexOf(p.dest), p);
			
			// Finally if we can't send the packet anywhere we drop the packet
			}else {
				
				if(p instanceof PingPacket) {
					debug.println(1, nsap + " Sent a return ping via flooding to " + p.dest);
					floodPingPackets((PingPacket)p);
				}
				
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
