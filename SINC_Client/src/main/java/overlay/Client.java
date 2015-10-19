package overlay;

import packetObjects.IntrestObj;
import topology.GeneralQueueHandler;
import topology.PacketQueue2;
import topology.ClientSendPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Client {
	static ObjectInputStream ois;
	static ObjectOutputStream oos;
	static ClientLink link;
	static ClientSendPacket sendPacketObj;
	static String ID;
	static String name;
	static String cacheServerAddress="";
	static HashMap<String, String> idIPMap = new HashMap<String, String>();
	static GeneralQueueHandler gqh;
	static PacketQueue2 pq2;
	static ProcessData pd;
	static NodeDetails nDetails=null;
	// socket to communicate with vizualizeServer
	static Socket vizualizeServerSocket;
	static String vizualizeServer=null;
	// used to get rtt, can be removed
	static ConcurrentHashMap<String, Long> rtt;
	private static Logger logger = LogManager.getLogger(Client.class);

	public static void main(String[] args) throws IOException {
		Scanner s = new Scanner(System.in);
		sendPacketObj = new ClientSendPacket();
		boolean clientStarted = true;
		boolean connected = false;
		// used for rtt, can be removed
		rtt = new ConcurrentHashMap<String, Long>();

		pq2 = new PacketQueue2();
		gqh = new GeneralQueueHandler(pq2, true);
		Thread gqhThread = new Thread(gqh);
		gqhThread.start();
		pd = new ProcessData(rtt);
		pd.start();
		
		// connection to visualize server
		Thread a = new Thread(new Runnable() {
			boolean alive = true;

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (alive) {
					String currentNeigh=cacheServerAddress;
					String oldNeigh=nDetails.getNeighbours();
					if(!currentNeigh.equals(oldNeigh)){
						nDetails.setNeighbours(currentNeigh);
						sendtoVisualizeServer();	
					}
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			
		});
		
		if(nDetails==null){
			nDetails=new NodeDetails(ID,idIPMap.get(ID),cacheServerAddress,3);
		}
		a.start();
		while (clientStarted) {
			while (!connected) {
				try {

					System.out.print("Enter cache server to connect to: ");
					cacheServerAddress = s.nextLine();
					Socket cacheServer = new Socket(cacheServerAddress, 43125);
					System.out.println("crating socket");
					ois = new ObjectInputStream(cacheServer.getInputStream());
					oos = new ObjectOutputStream(cacheServer.getOutputStream());
					Message<JoinPacket> joinMessage = new Message<JoinPacket>(11); // handle
					// in
					// Peer
					System.out.println("hiiii");
					oos.writeObject(joinMessage);
					link = new ClientLink(cacheServerAddress, ois,1);
					link.start();
					ID = generateID(getIP(cacheServerAddress)) + "";
					connected = true;
					// oos.writeObject("joining client");
				} catch (UnknownHostException e) {
					logger.error("Connection error.. Please try again.." + e);
					System.out.println("Connection error.. Please try again..");
				}
			}
			
			
			System.out.println("Enter content to be fetched(EXIT to exit): ");
			String msg = s.nextLine();
			IntrestObj intrst = new IntrestObj(msg, "", 1);
			sendPacketObj.createIntrestPacket(intrst);
			sendPacketObj.forwardPacket(intrst.getOriginalPacket());
			rtt.put(msg, System.currentTimeMillis());
		}
	}
	private static void sendtoVisualizeServer() {
		if (vizualizeServer == null) {
			String defaultVS = "172.31.38.100";
			System.out.println(
					"Vizualiztion server not set...Enter y or yes to set it to default i.e. " + defaultVS);
			//Scanner sc = new Scanner(System.in);
			//String reply = sc.nextLine();
			String reply = "y";
			if (reply.compareToIgnoreCase("y") == 0 || reply.compareToIgnoreCase("yes") == 0) {
				vizualizeServer = defaultVS;
			} else {
				vizualizeServer = reply;
			}
//			sc.close();
		}
		try {
			vizualizeServerSocket = new Socket(vizualizeServer, 56732);
			VisualizeMessage message = new VisualizeMessage(ID,1,3,nDetails); 

			ObjectOutputStream oos = new ObjectOutputStream(vizualizeServerSocket.getOutputStream());

			oos.writeObject(message);
			oos.flush();
			logger.info("message sent");
			// System.out.println("Join message sent");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

	}

	public static long generateID(String IP) throws UnknownHostException {
		String hostAddress = InetAddress.getLocalHost().getHostAddress();
		if (!IP.equals("")) {
			hostAddress = IP;
		}
		hostAddress = getIP(hostAddress);
		// System.out.println("Generating ID... (" + hostAddress + ")");
		long prime1 = 105137;
		long prime2 = 179422891;
		long ID = 0;
		for (int i = 0; i < hostAddress.length(); i++) {
			char c = hostAddress.charAt(i);
			if (c != '.') {
				ID += (prime1 * (hostAddress.charAt(i) * i)) % prime2;
			}
		}
		// System.out.println("ID: " + ID);
		 idIPMap.put(ID + "", hostAddress);

		return ID;
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

	@SuppressWarnings("rawtypes")
	public static boolean sendMessage(Message m) {
		try {
			oos.writeObject(m);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
