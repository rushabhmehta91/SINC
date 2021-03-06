package collectData;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import collectData.Listen;
import overlay.NodeDetails;

public class StartServer {
	// IP of this node
	public static String IP;
	// socket to communicate with neighboring nodes
	static Socket peerSocket;
	// serverSocket to listen on
	static ServerSocket serverSocket;
	// hashmap of nodes
	static HashMap<String, NodeDetails> nodes = null;
	
	static boolean changeflag = true;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	// ID of this node
	private static Logger logger = LogManager.getLogger(StartServer.class);

	// static block for initializing static content
	// like serverSocket used for listening
	{
		nodes=new HashMap<>();
		while (true) {
			try {
				serverSocket = new ServerSocket(56732);
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.info("Server socket started at port 56732...");
		System.out.println("Server socket started at port 56732...");

	}

	/**
	 * Constructor generates ID and initializes peerSocket to null
	 * 
	 * peerSocket - used to accept connections from neighbors
	 */
	public StartServer() {
		try {
			IP = getIP(InetAddress.getLocalHost().getHostAddress());
			nodes = new HashMap<>();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
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

	public void listen() {
		Listen listen = new Listen(this);
		listen.start();
	}

	public void addnode(NodeDetails n1) {
		System.out.println(n1.getId());
		System.out.println(n1.getIp());
		changeflag = true;
		System.out.println("change flag true");
		if (!nodes.isEmpty() && nodes.containsKey(n1.getId())) {
			nodes.get(n1.getId()).setNeighbours(n1.getNeighbours());
		} else {
			nodes.put(n1.getId(), n1);
		}
		
	}

	// Main thread
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		StartServer ss = new StartServer();
		System.out.println("VS started");
		ss.listen();

		Thread a = new Thread(new Runnable() {
			boolean alive = true;

			@Override
			public void run() {
				while (alive) {
					
					if (changeflag) {
						createJSONFile();
						changeflag = false;
					}
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		});
		a.start();
	}

	@SuppressWarnings("unchecked")
	protected static void createJSONFile() {
		
		JSONArray nodeList=new JSONArray();
		for (String ID : nodes.keySet()){
			JSONObject node=new JSONObject();
			node.put("id",nodes.get(ID).getId());
			node.put("label", nodes.get(ID).getIp()+"-"+nodes.get(ID).getId());
			node.put("x", (int)(Math.random()*10));
			node.put("y", (int)(Math.random()*10));
			node.put("size", (int)(Math.random()*10));
			node.put("color", "#666");
			nodeList.add(node);
		}
		HashSet<String> edgeIDList=new HashSet<>();
		JSONArray edgeList=new JSONArray();
		for (String ID : nodes.keySet()){
			String neighbourList[]=nodes.get(ID).getNeighbours().split(",");
			for(int index=0;index<neighbourList.length;index++){
				if(!ID.equals(neighbourList[index]) && !edgeIDList.contains(neighbourList[index]+"-"+ID)){
					JSONObject edge=new JSONObject();
					edgeIDList.add(ID+"-"+neighbourList[index]);
					edge.put("id", ID+"-"+neighbourList[index]);
					edge.put("source", ID);
					edge.put("target", neighbourList[index]);
					edge.put("size", (int)(Math.random()*10));
					edge.put("color", "#ccc");
					edgeList.add(edge);
				}
			}
		}
		
		JSONObject jsonString =new JSONObject();
		jsonString.put("nodes", nodeList);
		jsonString.put("edges", edgeList);
		System.out.println("\nJSON Object: " + jsonString);
		FileWriter file = null;
		try {
			file = new FileWriter("/home/ubuntu/data.json");
		
        try {
            file.write(jsonString.toJSONString());
            System.out.println("Successfully Copied JSON Object to File...");
            //System.out.println("\nJSON Object: " + jsonString);
 
        } catch (IOException e) {
            e.printStackTrace();
 
        } finally {
				file.flush();
				file.close();
            
        }
        } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	

}
