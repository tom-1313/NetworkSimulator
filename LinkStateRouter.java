
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
	public static final int delay = 1000;
	public Graph graph;
	public Node router;

	public LinkStateRouter(int nsap, NetworkInterface nic) {
		super(nsap, nic);
		debug = Debug.getInstance(); // For debugging!
		table = new HashMap<Integer, Double>();
		graph = new Graph();
		router = new Node(String.valueOf(nsap));
	}

	public void run() {

		long nextPingTime = System.currentTimeMillis() + delay;
		while (true) {

			// Send a ping every delay, and update our graph
			if (nextPingTime <= System.currentTimeMillis()) {
				for (int randNSAP : nic.getOutgoingLinks()) {
					floodPingPackets(new PingPacket(nsap, randNSAP, System.currentTimeMillis()));

					// create our initial graphs, then flood graph packets of the nodes that contain
					// our graphs
					graph.calculateShortestPathFromSource(graph, router);
					floodGraphPackets(new GraphPacket(nsap, randNSAP, System.currentTimeMillis(), graph));
				}
				nextPingTime = System.currentTimeMillis() + delay;

				graph.calculateShortestPathFromSource(graph, router);
				debug.println(1, graph.toString());
			}

			boolean process = false;

			// See if there is anything to process
			NetworkInterface.TransmitPair toSend = nic.getTransmit();

			// NetworkInterface.TransmitPair toSend = null;

			if (toSend != null) {
				// There is something to send out
				process = true;
				debug.println(3, "(LinkStateRouter.run): I am being asked to transmit: " + toSend.data
						+ " to the destination: " + toSend.destination);
				route(-1, new Packet(nsap, toSend.destination, 5, toSend.data));
			}

			NetworkInterface.ReceivePair toRoute = nic.getReceived();

			if (toRoute != null) {
				// There is something to route through - or it might have arrived at destination
				process = true;

				if (toRoute.data instanceof GraphPacket) {
					// debug.println(1, "Graph packet!!!!!!!!");
					GraphPacket p = (GraphPacket) toRoute.data;
					double timeTakenToTraverse = System.currentTimeMillis() - p.getStartTime();
					router.addDestination(p.graph.getSourceNode(), timeTakenToTraverse);
					graph.addNode(p.graph.getSourceNode());
					for (Node newNode : p.graph.getPathList()) {
						if(!newNode.getName().equals(String.valueOf(nsap)) && newNode.getDistance() != 0.0);{
							graph.addNode(newNode);
						}
						
					}

				}else if (toRoute.data instanceof PingPacket) {
					// We process our ping data
					PingPacket p = (PingPacket) toRoute.data;
					// debug.println(1, "WE received A PING PACKET YAAAAAY");
					// If our packet has been recieved, and the destination is the original sender
					// debug.println(1, p.dest + " " + nsap);
					if (p.isRecieved() && p.dest == nsap) {

						double timeTaken = (double) (System.currentTimeMillis() - p.getStartTime());
						debug.println(4,
								"(LinkStateRouter.run): PingPacket has arrived!  Reporting to the NIC - for accounting purposes!"
										+ " IP Address: " + nic.getNSAP() + "link from which it was sent is: "
										+ toRoute.originator + " time taken: " + timeTaken);

						// nic.sendOnLink(nic.getOutgoingLinks().indexOf(toRoute.originator), p);//This
						// finds the link that
						table.put(toRoute.originator, timeTaken);

						Node newNode = new Node(String.valueOf(p.source));
						router.addDestination(newNode, timeTaken);
						graph.addNode(newNode);

						// If the packet reaches its destination, but we
					} else if (p.dest == nsap) {
						p.recieved();
						// PingPacket returnPacket = new PingPacket(nsap, p.source, 20);
						int temp = p.dest;
						p.dest = p.source;
						p.source = temp;
						// Need to identify the link to send it back out on.
						debug.println(4, "Returning to sender:" + nic.getOutgoingLinks().indexOf(toRoute.originator));

						nic.sendOnLink(nic.getOutgoingLinks().indexOf(toRoute.originator), p);

						// Need to identify the link index and which of those links will have the ip
						// address, in link 65

						// nic.sendOnLink(nic.getOutgoingLinks().indexOf(toRoute.originator), p);

						// route()

						// Each ping packet when sent will store the current time
						// If we sent it
						// Then we check the time and store the value in a hashmap
						// If we didn't send it, then we

					}

				} else if (toRoute.data instanceof Packet) {
					Packet p = (Packet) toRoute.data;
					if (p.dest == nsap) {
						// It made it! Inform the "network" for statistics tracking purposes
						debug.println(4,
								"(LinkStateRouter.run): Packet has arrived!  Reporting to the NIC - for accounting purposes!");
						debug.println(6, "(LinkStateRouter.run): Payload: " + p.payload);
						nic.trackArrivals(p.payload);
					} else if (p.hopCount > 0) {
						// Still more routing to do
						p.hopCount--;
						route(toRoute.originator, p);
					} else {
						debug.println(5, "Packet has too many hops.  Dropping packet from " + p.source + " to " + p.dest
								+ " by router " + nsap);
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

	private void floodPingPackets(PingPacket p) {
		ArrayList<Integer> outLinks = nic.getOutgoingLinks();
		int size = outLinks.size();
		for (int i = 0; i < size; i++) {
			nic.sendOnLink(i, p);
		}
	}

	private void floodGraphPackets(GraphPacket p) {
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
