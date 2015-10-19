package topology;

import overlay.Client;
import overlay.Message;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;



/**
 * This class is used to convert an object to json and add the </br>
 * headers. The packet is saved as a json string and saved to </br>
 * original packet in the object. 
 * @author spufflez
 *
 */
public class ClientSendPacket extends SendPacket{

	private static Logger logger = LogManager.getLogger(ClientSendPacket.class);


	/**
	 * Send a packet to the cache server
	 * @param packet
	 */
	public void forwardPacket(String packet) {

		// this will forward a packet to only the router specified
		Message<String> packetMessage = new Message<String>(7, packet);
		Client.sendMessage(packetMessage);
		logger.info("-------------------------------------------");
		logger.info("    -Forward packet next hop provided-");
		logger.info("-------------------------------------------");
		System.out.println("-------------------------------------------");
		System.out.println("    -Forward packet next hop provided-");
//		System.out.println("packet: " + packet);
		System.out.println("-------------------------------------------");
		System.out.println("");
	}
}