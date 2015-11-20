package overlay;

import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Polling extends Thread {

	static HashMap<String, Long> pollLatency;
	static boolean polling;
	private static Logger logger = LogManager.getLogger(Polling.class);

	{
		polling = true;
		pollLatency = new HashMap<String, Long>();
	}

	public Polling() {

	}

	@Override
	public void run() {
		while (polling) {
			try {
				Thread.sleep(5000);
				if (Peer.neighbors.size() > 0) {
					// poll neighbors by sending own neighbors
					JoinPacket jp = new JoinPacket();
					Message<JoinPacket> m = new Message<JoinPacket>(100, jp);
					for (String neighbor : Peer.neighbors.keySet()) {
						pollLatency.put(Peer.getIP(neighbor),
								System.currentTimeMillis());
						Peer.sendMessageX(neighbor, m);
					}					
				}
			} catch (InterruptedException e) {
				logger.info("Exception in polling");
				logger.error("Exception in polling "+ e);
				//e.printStackTrace();
			}
		}
	}

}
