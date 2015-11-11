package overlay;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import caching.ContentStore;
import topology.MainEntryPoint;
import topology.CacheServerPassToRoutingLayer;
import overlay.VisualizeMessage;

/**
 * Peer class to represent a node in the overlay network. Implements the
 * PeerInterface interface.
 * 
 * @author Gaurav Komera
 *
 */
public class Peer { // implements PeerInterface
	// IP of this node
	public static String IP;
	// map of neighboring cacheServers
	public static HashMap<String, SocketContainer> neighbors;
	// socket to communicate with neighboring nodes
	static Socket peerSocket;
	// socket to communicate with vizualizeServer
	static Socket vizualizeServerSocket;
	static String vizualizeServer = null;
	// ID of this node
	public static String ID;
	// serverSocket to listen on
	static ServerSocket serverSocket;
	// map of neighboring clients and servers
	static HashMap<String, SocketContainer> clientServers;
	// map of vacancies with current and neighboring nodes
	static HashMap<String, Integer> vacancies;
	// set of all nodes in the connected network
	static HashSet<String> allNodes;
	static Scanner scanner;
	static int logN;
	public static MainEntryPoint mep = null;
	static NodeDetails nDetails = null;

	static HashMap<String, String> idIPMap;

	static LinkedList<Long> requests;

	static CacheServerPassToRoutingLayer routing;
	private static Logger logger = LogManager.getLogger(Peer.class);

	// static block for initializing static content
	// like serverSocket used for listening
	{
		while (true) {

			try {
				serverSocket = new ServerSocket(43125);
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.info("Server socket started at port 43125...");
		logger.info("Server socket started at port 43125...");

		neighbors = new HashMap<String, SocketContainer>();
		vacancies = new HashMap<String, Integer>();
		allNodes = new HashSet<String>();
		scanner = null;
		logN = 0;
		requests = new LinkedList<Long>();
		idIPMap = new HashMap<String, String>();
		clientServers = new HashMap<String, SocketContainer>();
	}

	/**
	 * Constructor generates ID and initializes peerSocket to null
	 * 
	 * peerSocket - used to accept connections from neighbors
	 */
	public Peer() {
		try {
			IP = getIP(InetAddress.getLocalHost().getHostAddress());
			ID = generateID("") + ""; // unique ID based on IP address

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		peerSocket = null;
	}

	// Main thread
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

		Peer p = new Peer();
		if (args.length == 0) {
			logger.info("Please pass command line arguments " + "suggesting the mode in which the node is to "
					+ "be started.");
			logger.info("Suggestion:\n\n\t type 'java Peer man' on " + "the command line");
			return;
		} else if (args.length == 1) {
			if (args[0].equals("man")) {
				logger.info("To start a new network");
				logger.info("\n\tjava Peer start new\n");
				logger.info("To join an existing network");
				logger.info("\n\tjava Peer join <node address>");
				logger.info("\t <node address> is the IP address of a "
						+ "node that is already part of the existing " + "network.\n");
				return;
			} else if (args[0].toLowerCase().equals("start")) {
				p.start();
				// Start listening on server socket for new connections
				p.listen();
				// start routing layer threads
				mep = startRouting();
				Thread mepThread = new Thread(mep);
				mepThread.start();
			}
		} else if (args.length == 2) {
			if (args[0].toLowerCase().equals("join")) {
				long startTime = System.nanoTime();
				String server = args[1].toLowerCase();

				// Start listening on server socket
				p.listen();
				mep = startRouting();
				Thread mepThread = new Thread(mep);
				mepThread.start();

				// join node
				Message<JoinPacket> m = Peer.join(server);
				List<String> potentialNeighbors = new ArrayList<String>(m.packet.neighbors);

				// connect to node that was dropped by peer
				while (!linksSatisfied() && m.type == -2) {
					m = Peer.join(m.packet.dropped);
					potentialNeighbors.clear();
					potentialNeighbors.addAll(m.packet.neighbors);
				}

				// connecting to more neighbors to satisfy log n condition for
				// this node
				int i = 0;
				while (!linksSatisfied() && i < potentialNeighbors.size()) {

					// do not send request to already connected neighbor
					while (i < potentialNeighbors.size() && Peer.neighbors.containsKey(potentialNeighbors.get(i))) {
						i++;
					}
					if (i == potentialNeighbors.size()) {
						break;
					}
					m = Peer.join(potentialNeighbors.get(i));

					// connect to node that was dropped by peer
					while (!linksSatisfied() && m.type == -2) {
						m = Peer.join(m.packet.dropped);
						potentialNeighbors.clear();
						potentialNeighbors.addAll(m.packet.neighbors);
					}

					// potentialNeighbors.clear();
					// potentialNeighbors.addAll(m.packet.neighbors);
					i = 0;
				}
				logger.info("Node joined in: " + (System.nanoTime() - startTime));
				logger.info("Node joined in: " + (System.nanoTime() - startTime));
			}
		}

		// start polling neighbors
		Polling poll = new Polling();
		poll.start();

		/*************************************
		 * Start ROUTING
		 *************************************/

		routing = new CacheServerPassToRoutingLayer(mep.packetQueue2);
		scanner = new Scanner(System.in);

		boolean alive = true;
		String action = "";

		// connection to visualize server
		Thread a = new Thread(new Runnable() {
			boolean alive = true;

			@Override
			public void run() {
				if (nDetails == null) {
					nDetails = new NodeDetails(ID, idIPMap.get(ID), "", 2, "");
				}
				// TODO Auto-generated method stub
				while (alive) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					String currentNeigh = mep.printDirectlyConnectedRouters() + mep.printDirectlyConnectedClietns();
					String oldNeigh = nDetails.getNeighbours();
					String currentPrefix = ContentStore.store.keySet().toString();
					String oldPrefix = nDetails.getContentStore();

					if (!currentNeigh.equals(oldNeigh) || !currentPrefix.equals(oldPrefix)) {
						if (!currentNeigh.equals(oldNeigh)) {
							nDetails.setNeighbours(currentNeigh);
						}
						if (!currentPrefix.equals(oldPrefix)) {
							nDetails.setContentStore(currentPrefix);
						}
						sendtoVisualizeServer();
					}

				}
			}

		});
		a.start();

		while (alive) {

			// add functions here to get node repo
			// Fib data
			// pit data
			// directly connected clients data
			// directly connected routers data
			// update msg data
			action = scanner.next();

			switch (action) {
			case "node":
				logger.info("-printing nodes-");
				mep.printNodeRepo();
				break;

			case "nas":
				logger.info("neighbors size: " + neighbors.size());
				logger.info("allNodes size: " + allNodes.size());
				break;

			case "idipmap":
				logger.info("ID: " + ID);
				logger.info("idipmap: " + idIPMap.get(ID));
				break;

			case "nd":
				// scanner
				action = scanner.next();
				mep.printNodeDetails(action);
				break;

			case "fib":
				mep.printFIB();
				break;

			case "pit":
				mep.printPIT();
				break;

			case "drr":
				logger.info("Directly Connected Router:" + mep.printDirectlyConnectedRouters());
				break;

			case "drc":
				logger.info("Directly connected client:" + mep.printDirectlyConnectedClietns());
				break;

			case "msgIDs":
				mep.printMsgIDsSeen();
				break;

			case "si":
				logger.info("Enter content name:");
				action = scanner.next();
				mep.intrestPacket(action);
				break;

			case "sd":
				logger.info("Enter content name:");
				String contentName = scanner.next();
				logger.info("Enter origin Router name:");
				String originRouter = scanner.next();
				logger.info("Enter from node:");
				String fromNode = scanner.next();
				mep.dataPacket(contentName, originRouter, fromNode);
				break;

			case "sp":
				logger.info("enter a prefix");
				action = scanner.next();
				logger.info("add or remove boolean");
				boolean addRemovePrefix = scanner.hasNextBoolean();
				mep.prefix(action, addRemovePrefix);
				break;

			case "spl":
				logger.info("add or remove boolean");
				boolean addRemovePrefixList = scanner.hasNextBoolean();
				mep.prefixList(addRemovePrefixList);
				break;

			case "kill":
				mep.killThreads();
				alive = false;
				logger.info("killing program");
				break;

			case "ping":
				logger.info("Enter node id to ping:");
				String pingNode = scanner.next();
				mep.ping(pingNode);
				break;

			case "tp":
				logger.info("Enter node id to ping:");
				String timePingNodeID = scanner.next();
				logger.info("Enter the amount of pings to send:");
				int timePingCount = scanner.nextInt();
				mep.timedPing(timePingNodeID, timePingCount);
				break;

			case "ap":
				logger.info("Enter node id to ping:");
				String autoPingNodeID = scanner.next();
				logger.info("Enter the amount of pings to send:");
				int autoPingCount = scanner.nextInt();
				mep.autoPing(autoPingNodeID, autoPingCount);
				break;

			case "conv":
				logger.info("Printing convergence times: ");
				mep.convergenceTime();
				break;

			case "overlay":
				logger.info("neighbors: " + neighbors);
				logger.info("clients+servers: " + clientServers);
				break;
			default:
				logger.info("default hit");
				break;
			}

		}
		logger.info("program terminating");
		logger.info("program terminating");
	}

	// 1,000,000,000 nano time == 1 second
	public static MainEntryPoint startRouting() {
		MainEntryPoint mep = new MainEntryPoint(ID + "", 10000, 7000000000L, 20000, 60000000000L, 20000);
		routing = new CacheServerPassToRoutingLayer(mep.packetQueue2);

		return mep;

	}

	/**
	 * This method updates meta data after adding new peer connection.
	 */
	public static void addPeer(JoinPacket packet, Socket peerSocket, ObjectOutputStream oos, ObjectInputStream ois,
			CacheServerLink link) throws IOException {
		// logger.info("addPeer called");
		String peer = getIP(peerSocket.getRemoteSocketAddress().toString());
		neighbors.put(peer, new SocketContainer(peerSocket, ois, oos, link));
		allNodes.add(peer);
		allNodes.addAll(packet.allNodes);
		logger.info("New allNodes: " + allNodes);
		logger.info("New allNodes: " + allNodes);

	}

	public static String getIP(String port) {
		int i = 0;
		int slash = 0;
		int end = port.length();
		for (; i < port.length(); i++) {
			if (port.charAt(i) == '/') {
				slash = i + 1;
			} else if (port.charAt(i) == ':') {
				end = i;
				break;
			}
		}
		return port.substring(slash, end);
	}

	// send remaining neighbors information about new peer
	public static void updateNeighbors(List<String> except, JoinPacket packet, int type) throws IOException {
		// send neighbors with new peer info
		int i = 0;
		logger.info("Total neighbors: " + neighbors.size());
		logger.info("Total neighbors: " + neighbors.size());
		packet.doNotConnect = except;
		Message<JoinPacket> m = new Message<JoinPacket>(type, packet);
		for (Entry<String, SocketContainer> e : neighbors.entrySet()) {
			if (!except.contains(e.getKey())) {
				++i;
				logger.info("Sending new neighbor update: " + e.getKey());
				logger.info("Sending new neighbor update: " + e.getKey());
				e.getValue().oos.writeObject(m);
			}
		}
		logger.info("Total neighbors notified: " + i);
		logger.info("Total neighbors notified: " + i);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Message<JoinPacket> join(String peer)
			throws IOException, ClassNotFoundException, InterruptedException {
		long joinStartTime = System.currentTimeMillis();
		allNodes.add(getIP(IP));
		peerSocket = new Socket(peer, 43125);

		JoinPacket packet = new JoinPacket();
		Message<JoinPacket> joinMessage = new Message<JoinPacket>(1, packet);

		ObjectOutputStream oos = new ObjectOutputStream(peerSocket.getOutputStream());
		ObjectInputStream ois = new ObjectInputStream(peerSocket.getInputStream());

		oos.writeObject(joinMessage);
		oos.flush();
		logger.info("Join message sent");
		logger.info("Join message sent");
		logger.info("Waiting for acknowledgement");
		logger.info("Waiting for acknowledgement");
		Message<JoinPacket> mAck = (Message) ois.readObject();
		long joinStartFinish = System.currentTimeMillis();
		logger.info("Acknowledgement type: " + mAck.type);
		logger.info("Acknowledgement type: " + mAck.type);

		// start listening to connected peer for any future communication
		CacheServerLink link = new CacheServerLink(peerSocket.getRemoteSocketAddress() + "", ois, 3);
		link.start();
		//
		addPeer(mAck.packet, peerSocket, oos, ois, link);
		// logger.info("all links up.. now contacting neighbors");
		// updateNeighbors(connectedTo, m.packet, 50);

		// INFORM ROUTING LAYER ABOUT NEW NEIGHBOR
		logger.info(System.currentTimeMillis() + " sent to routing:: "
				+ generateID(peerSocket.getRemoteSocketAddress().toString()) + " cost::"
				+ (joinStartFinish - joinStartTime));
		logger.info(System.currentTimeMillis() + " sent to routing:: "
				+ generateID(peerSocket.getRemoteSocketAddress().toString()) + " cost::"
				+ (joinStartFinish - joinStartTime));
		routing.addLink(generateID(peerSocket.getRemoteSocketAddress().toString()) + "",
				(int) (joinStartFinish - joinStartTime));

		return mAck;
	}

	private static void sendtoVisualizeServer() {
		if (vizualizeServer == null) {
			// String defaultVS="127.0.0.1";
			String defaultVS = "172.31.38.100";
			logger.info("Vizualiztion server not set...Enter y or yes to set it to default i.e. " + defaultVS);
			// Scanner sc=new Scanner(System.in);
			// String reply=sc.nextLine();
			String reply = "y";
			if (reply.compareToIgnoreCase("y") == 0 || reply.compareToIgnoreCase("yes") == 0) {
				vizualizeServer = defaultVS;
			} else {
				vizualizeServer = reply;
			}
			// sc.close();
		}
		try {
			vizualizeServerSocket = new Socket(vizualizeServer, 56732);
			VisualizeMessage message = new VisualizeMessage(ID, 1, 2, nDetails);

			ObjectOutputStream oos = new ObjectOutputStream(vizualizeServerSocket.getOutputStream());

			oos.writeObject(message);
			oos.flush();
			oos.close();
			logger.info("message sent");
			logger.info("message sent");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Update meta-data of node using received join packet.
	 *
	 * @param m
	 */
	public static void updateMetaData(Message<JoinPacket> m) {
		logger.info("** Updating meta data - start**");
		logger.info("** Updating meta data - start**");
		JoinPacket packet = m.packet;
		logger.info("Packet allNodes: " + packet.allNodes);
		logger.info("Packet allNodes: " + packet.allNodes);
		logger.info("Packet neighbors: " + packet.neighbors);
		logger.info("Packet neighbors: " + packet.neighbors);
		// allNodes.addAll(packet.allNodes);
		// vacancies.putAll(packet.vacancies);
		logger.info("After update");
		logger.info("After update");
		logger.info("allNodes: " + allNodes);
		logger.info("allNodes: " + allNodes);
		logger.info("neighbors: " + neighbors);
		logger.info("neighbors: " + neighbors);
		logger.info("** Updating meta data - finish**");
		logger.info("** Updating meta data - finish**");
	}

	/**
	 * Method to be called by upper layers to send a message to a particular
	 * <br/>
	 * neighbor.<br/>
	 *
	 * Message type should be set to 7.
	 *
	 * @param ID
	 * @param m
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static boolean sendMessage(String ID, Message m) {
		// logger.info("ID: " + ID);
		// logger.info("idipmap: " + idIPMap.get(ID));
		// logger.info("neighbors: " + neighbors.get(idIPMap.get(ID)));
		// logger.info("client: " + clientServers.get(idIPMap.get(ID)));
		SocketContainer sc = neighbors.get(idIPMap.get(ID));
		if (sc == null) {
			sc = clientServers.get(idIPMap.get(ID));
		}
		if (sc != null) {
			synchronized (sc) {
				try {
					// logger.info(":::ID::: " + ID);
					sc.oos.writeObject(m);
				} catch (IOException e) {
					return false;
				}
				return true;
			}
		} else {
			logger.error("Message not sent.. neighbor with ID: " + ID + "not found.");
			// logger.info("Message not sent.. neighbor with ID: " + ID +
			// "not found.");
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	public static boolean sendMessageX(String IP, Message m) {
		try {
			// logger.info("SendMessageX::" + IP + " looking in "
			// + neighbors.keySet());
			if (neighbors.get(IP) != null) {
				synchronized (neighbors.get(IP)) {
					SocketContainer sc = neighbors.get(IP);
					sc.oos.writeObject(m);
				}
			} else {
				synchronized (clientServers.get(IP)) {
					SocketContainer sc = clientServers.get(IP);
					sc.oos.writeObject(m);
				}
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Method to be called by upper layers to send a message to a list of<br/>
	 * neighbors.<br/>
	 *
	 * Message type should be set to 7.
	 *
	 * @param IDs
	 * @param m
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static boolean sendMessage(List<String> IDs, Message m) throws IOException {
		for (String id : IDs) {
			if (!sendMessage(id, m)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method to be called by upper layers to send a message to all<br/>
	 * neighbors except ID.<br/>
	 * <p/>
	 * Message type should be set to 7.
	 *
	 * @param ID
	 * @param m
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public static boolean sendMessageToAllBut(String ID, Message m) throws IOException {
		List<String> IPs = new ArrayList<String>(neighbors.keySet());
		for (String ip : IPs) {
			if (!("" + idIPMap.get(ID)).equals(ip)) {
				if (!sendMessage(generateID(ip) + "", m)) {
					return false;
				}
			}
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	public static boolean sendMessageToAllButX(String IP, Message m) throws IOException {
		List<String> IPs = new ArrayList<String>(neighbors.keySet());
		for (String ip : IPs) {
			if (!("" + IP).equals(ip)) {
				if (!sendMessageX(ip, m)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Update the number of required neighbors
	 */
	public static void updateLogN() {
		logN = (int) Math.ceil(Math.log10(allNodes.size()) / Math.log10(2));
	}

	/**
	 * Node ID generator
	 *
	 * @return
	 * @throws UnknownHostException
	 */
	public static long generateID(String IP) throws UnknownHostException {
		String hostAddress = InetAddress.getLocalHost().getHostAddress();
		if (!IP.equals("")) {
			hostAddress = IP;
		}
		hostAddress = getIP(hostAddress);
		// logger.info("Generating ID... (" + hostAddress + ")");
		long prime1 = 105137;
		long prime2 = 179422891;
		long ID = 0;
		for (int i = 0; i < hostAddress.length(); i++) {
			char c = hostAddress.charAt(i);
			if (c != '.') {
				ID += (prime1 * (hostAddress.charAt(i) * i)) % prime2;
			}
		}
		// logger.info("ID: " + ID);
		idIPMap.put(ID + "", hostAddress);

		return ID;
	}

	/**
	 * Checks if new join request can be processed by current node.<br/>
	 * Does this by checking if number of links after the node joins in are<br/>
	 * within limits of log<i>n</i>.
	 *
	 * @param peerSocket
	 * @return
	 */
	public static boolean nodeDropRequired() {
		int newNetworkSize = Peer.allNodes.size();
		int presentNeighbors = Peer.neighbors.size();
		logger.info("newNetworkSize: " + newNetworkSize);
		logger.info("newNetworkSize: " + newNetworkSize);
		logger.info("presentNeighbors: " + presentNeighbors);
		logger.info("presentNeighbors: " + presentNeighbors);
		int requiredNeighbors = (int) Math.ceil(Math.log(newNetworkSize) / Math.log(2));
		logger.info("Neighbors present: " + presentNeighbors + "\nNeighbors required: " + requiredNeighbors);
		logger.info("Neighbors present: " + presentNeighbors + "\nNeighbors required: " + requiredNeighbors);
		if (presentNeighbors > requiredNeighbors) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method checks if the number of links on the node are correct
	 * according to the total number of nodes in the network
	 */
	public static boolean linksSatisfied() {
		int linksPresent = neighbors.size();
		int linksRequired = (int) Math.ceil(Math.log(allNodes.size()) / Math.log(2));
		logger.info("linksPresent: " + linksPresent);
		logger.info("linksPresent: " + linksPresent);
		logger.info("linksRequired: " + linksRequired);
		logger.info("linksRequired: " + linksRequired);
		return linksPresent == linksRequired;
	}

	/**
	 * Method that starts a new thread to begin listening for new nodes that
	 * wish to join in.
	 */
	public void listen() {
		Listen listen = new Listen(this);
		listen.start();
	}

	public void start() throws IOException {
		logger.info("Starting node...");
		logger.info("Starting node...");
		logger.info("IP: " + IP);
		logger.info("IP: " + IP);
		logger.info("Waiting for client peer...");
		logger.info("Waiting for client peer...");
		allNodes.add(getIP(IP));
	}
}
