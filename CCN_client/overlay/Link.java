package overlay;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import packetObjects.PacketObj;
import topology.SendPacket;

/**
 * Receive message objects from neighbors and process them.
 * 
 * @author Gaurav Komera
 *
 */
public class Link extends Thread {
	ObjectInputStream ois = null;
	String connectedTo;
	boolean running;
	private static Logger logger = LogManager.getLogger(Link.class);

	public Link(String peerAddress, ObjectInputStream ois) throws IOException {
		connectedTo = Client.getIP(peerAddress);
		this.ois = ois;
		running = true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void run() {
		Message m = null;
		int attempt = 0;
		logger.info("Started listening on link to " + connectedTo);
		System.out.println("Started listening on link to " + connectedTo);
		while (running) {
			try {
				m = (Message) ois.readObject();
				logger.info(System.currentTimeMillis()
						+ "Message received from: " + connectedTo);
				System.out.println(System.currentTimeMillis()
						+ "Message received from: " + connectedTo);
				logger.info("Message type: " + m.type);
				System.out.println("Message type: " + m.type);
				logger.info("Request no: " + m.requestNo);
				System.out.println("Request no: " + m.requestNo);
				attempt = 0;
				// handle updates if not previously seen
				handleUpdate(m);
			} catch (ClassNotFoundException e) {
				logger.error(e.getMessage());
				System.out.println(e);
//				e.printStackTrace();
				running = false;
			} catch (IOException e) {
				logger.error(e.getMessage());
				System.out.println(e);
				//				e.printStackTrace();
                running = false;
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				System.out.println(e);
//				e.printStackTrace();
                running = false;
			}
		}
		logger.error("Link to " + connectedTo + " dropped...");
		System.out.println("Link to " + connectedTo + " dropped...");
	}

	public void handleUpdate(Message m) throws IOException,
			ClassNotFoundException, InterruptedException {
		if (m.type == 7) {
			Message<String> m2 = m;
			PacketObj pObj = new PacketObj(m2.packet, "", true);
			Client.pq2.addToGeneralQueue(pObj);
		}
	}
}