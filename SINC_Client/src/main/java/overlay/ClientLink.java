package overlay;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import packetObjects.DataObj;
import packetObjects.PacketObj;
import topology.ClientSendPacket;

/**
 * Receive message objects from neighbors and process them.
 * 
 * @author Gaurav Komera
 *
 */
public class ClientLink extends Link {
	

	private static Logger logger = LogManager.getLogger(ClientLink.class);

	public ClientLink(String peerAddress, ObjectInputStream ois, int type) throws IOException {
		super(ois, type);
		connectedTo = Client.getIP(peerAddress);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void run() {
		Message m = null;
		int attempt = 0;
		logger.info("Started listening on link to " + connectedTo);
		while (running) {
			try {
				m = (Message) ois.readObject();
				logger.info("Message received from: " + connectedTo);
				logger.info("Message type: " + m.type);
				logger.info("Request no: " + m.requestNo);
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
	}

	public void handleUpdate(Message m) throws IOException,
			ClassNotFoundException, InterruptedException {
		if (m.type == 7) {
			Message<Object> m2 = m;
			PacketObj pObj = new PacketObj(m2.packet, "", true);
			Client.pq2.addToGeneralQueue(pObj);			
		}
	}
}