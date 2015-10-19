
package topology;

import overlay.Message;
import overlay.Server;
import packetObjects.LinkObj;
import packetObjects.PrefixListObj;
import packetObjects.PrefixObj;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.google.gson.JsonObject;

/**
 * This class is used to convert an object to json and add the </br>
 * headers. The packet is saved as a json string and saved to </br>
 * original packet in the object. 
 * @author spufflez
 *
 */
public class ServerSendPacket extends SendPacket {

	private static Logger logger = LogManager.getLogger(ServerSendPacket.class);
	

	/**
	 * Creates an add client packet</br>
	 * called by the overlay, to tell the cache server a client is connecting
	 * @param linkObj
	 */
	public void createAddClient(LinkObj linkObj) {
		JsonObject packet = new JsonObject();

		packet.addProperty("type", "update");
		packet.addProperty("action", "addClient");
		packet.addProperty("nodeName", linkObj.getNeighboringNode());
		packet.addProperty("cost", linkObj.getCost());

		linkObj.setOriginalPacket(packet.toString());
	}

	/**
	 * Creates an remove client packet</br>
	 * called by the overlay, to tell the cache server a client has died
	 * @param linkObj
	 */
	public void createRemoveClient(LinkObj linkObj) {
		JsonObject packet = new JsonObject();

		packet.addProperty("type", "update");
		packet.addProperty("action", "removeClient");
		packet.addProperty("nodeName", linkObj.getNeighboringNode());
		packet.addProperty("cost", linkObj.getCost());

		linkObj.setOriginalPacket(packet.toString());
	}

	

	/**
	 * Creates a prefix packet to be sent between cache servers</br>
	 * This packet will contain content names each server knows about
	 * @param prefixObj
	 */
	public void createPrefixPacket(PrefixObj prefixObj) {
		JsonObject packet = new JsonObject();

		packet.addProperty("type", "update");
		packet.addProperty("action", "prefix");
		packet.addProperty("addRemove", prefixObj.getAddRemoveFlag());
		packet.addProperty("prefix", prefixObj.getPrefixName());
		packet.addProperty("advertiser", prefixObj.getAdvertiser());
		packet.addProperty("msgID", prefixObj.getMsgID());

		prefixObj.setOriginalPacket(packet.toString());
	}

	/**
	 * Creates a prefixList packet to be sent between cache servers</br>
	 * This packet will contain a list content names each server knows about
	 * @param prefixObj
	 */
	public void createPrefixListPacket(PrefixListObj prefixListObj) {
		JsonObject packet = new JsonObject();

		packet.addProperty("type", "update");
		packet.addProperty("action", "prefixList");
		packet.addProperty("addRemove", prefixListObj.getAddRemoveFlag());
		String prefixArray = gson.toJson(prefixListObj.getPrefixList());
		packet.addProperty("prefixList", prefixArray);
		packet.addProperty("advertiser", prefixListObj.getAdvertiser());
		packet.addProperty("msgID", prefixListObj.getMsgID());

		prefixListObj.setOriginalPacket(packet.toString());

	}


	/**
	 * Send a packet to the cache server
	 * @param packet
	 */
	public void forwardPacket(Object packet, String nextHop) {

		// this will forward a packet to only the router specified
		Message<Object> packetMessage = new Message<Object>(7, packet);
		logger.info("-------------------------------------------");
		logger.info("    -Forward packet next hop provided-");
		logger.info("nextHop: " + nextHop);
		logger.info("-------------------------------------------");
		Server.sendMessage(nextHop, packetMessage);
		System.out.println("-------------------------------------------");
		System.out.println("    -Forward packet next hop provided-");
		//System.out.println("packet: " + packet);
		System.out.println("nextHop: " + nextHop);
		System.out.println("-------------------------------------------");
		System.out.println("");
	}

}

