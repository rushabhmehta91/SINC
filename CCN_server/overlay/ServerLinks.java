package overlay;

import java.io.ObjectInputStream;
import java.net.UnknownHostException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import packetObjects.PacketObj;
import caching.ServerLFS;

/**
 * Created by Chiran on 4/26/15.
 */
public class ServerLinks extends Thread {

	String nodeConnected;
	ObjectInputStream ois = null;
	boolean running;
	private static Logger logger = LogManager.getLogger(ServerLinks.class);


	public ServerLinks(String cacheNodeIp, ObjectInputStream ois) {
		nodeConnected = cacheNodeIp;
		this.ois = ois;
		running = true;

	}

	@Override
	public void run() {

		Message m = null;
		while (running) {
			try {
				m = (Message) ois.readObject();
				logger.info("Message received from: " + nodeConnected);
				logger.info("Message type: " + m.type);
				logger.info("Request no: " + m.requestNo);
				System.out.println("Message received from: " + nodeConnected);
				System.out.println("Message type: " + m.type);
				System.out.println("Request no: " + m.requestNo);
				handleUpdate(m);

			} catch (Exception e) {
				logger.error(e.getMessage());
				System.out.println(e);
				//e.printStackTrace();
				running = false;
			}
			if (!running) {

				// inform neighbors about dropped node
			}

		}
		logger.info("Server dropped...");
        System.out.println("Server dropped...");
    }

	private void handleUpdate(Message m) throws UnknownHostException {

		if (m.type == 7) {
			Message<String> m2 = m;
			/*
			 * packetObj needs a received from node ... 
			 * with out it, packets can not be routed
			 * change "" to a node ID
			 */
			PacketObj pObj = new PacketObj(m2.packet,
					ServerLFS.generateID(ServerLFS.getIP(nodeConnected)) + "",
					true);
            ServerLFS.pq2.addToGeneralQueue(pObj);
		}

	}


}
