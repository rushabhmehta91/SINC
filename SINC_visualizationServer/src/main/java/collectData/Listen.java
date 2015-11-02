package collectData;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;

import overlay.NodeDetails;
import overlay.VisualizeMessage;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * Listen for connection requests from cacheServer, client and server on Server Socket <br/>
 * 
 * 
 * @author Rushabh Mehta
 *
 */
public class Listen extends Thread {
	StartServer p;
	ObjectInputStream ois;
	boolean running;
	private static Logger logger = LogManager.getLogger(Listen.class);

	/**
	 * Constructor for initializing peer.
	 * 
	 * @param p
	 * @param serverSocket
	 */
	public Listen(StartServer p) {
		this.p = p;
		running = true;
	}

	@Override
	public void run() {
		while (running) {
			try {
				// wait for new peer
				StartServer.peerSocket = StartServer.serverSocket.accept();
				ois = new ObjectInputStream(StartServer.peerSocket.getInputStream());
				VisualizeMessage message =  (VisualizeMessage) ois.readObject();
				if(message.getType()==1){
					NodeDetails node=(NodeDetails)message.getMessage();
					System.out.println("Received: " + message.getSourceID());
					p.addnode(node);
				}
				
				ois.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}  catch (ClassNotFoundException e) {
				logger.error(e.getMessage());
			} finally {
				StartServer.peerSocket = null;
				ois = null;
			}
		}
	}


}
