package overlay;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import packetObjects.PacketObj;


/**
 * Created by Chiran on 4/26/15.
 */
public class ServerLink extends Link {

	public ServerLink(String peerAddress, ObjectInputStream ois, int type) throws IOException {
		super( ois, type);
		connectedTo = Server.getIP(peerAddress);
		// TODO Auto-generated constructor stub
	}

	private static Logger logger = LogManager.getLogger(ServerLink.class);

	@Override
	public void run() {
		System.out.println(running);
		Message m = null;
		while (running) {
			try {
				m = (Message) ois.readObject();
				logger.info("Message received from: " + connectedTo);
				logger.info("Message type: " + m.type);
				logger.info("Request no: " + m.requestNo);
				System.out.println("Message received from: " + connectedTo);
				System.out.println("Message type: " + m.type);
			//	System.out.println("Request no: " + m.requestNo);
				handleUpdate(m);

			} catch (Exception e) {
				logger.error(e.getMessage());
				System.out.println(e);
				//e.printStackTrace();
				running = false;
			}
			if (!running) {
				System.out.println("not running");
				// inform neighbors about dropped node
			}

		}
		logger.info("Server dropped...");
        System.out.println("Server dropped...");
    }

	public void handleUpdate(Message m) throws UnknownHostException {

		if (m.type == 7) {
			Message<String> m2 = m;
			/*
			 * packetObj needs a received from node ... 
			 * with out it, packets can not be routed
			 * change "" to a node ID
			 */
			PacketObj pObj = new PacketObj(m2.packet,
					Server.generateID(Server.getIP(connectedTo)) + "",
					true);
            Server.pq2.addToGeneralQueue(pObj);
		}

	}


}
