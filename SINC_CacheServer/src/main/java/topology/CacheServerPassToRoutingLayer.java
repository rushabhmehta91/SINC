package topology;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import packetObjects.LinkObj;
import packetObjects.PacketObj;

/**
 * This class is used by the overlay, it contains functions that the overlay </br>
 * calls to construct and pass packets to the routing layer.
 * 
 * @author spufflez
 *
 */
public class CacheServerPassToRoutingLayer extends PassToRoutingLayer{

	CacheServerPacketQueue2 packetQueue2;
	CacheServerSendPacket sendPacket;
	private static Logger logger = LogManager.getLogger(CacheServerPassToRoutingLayer.class);

	/**
	 * Constructor
	 * @param packetQueue2
	 */
	public CacheServerPassToRoutingLayer(CacheServerPacketQueue2 packetQueue2){
		super(packetQueue2);
		this.packetQueue2 = packetQueue2;
		this.sendPacket = new CacheServerSendPacket();
	}

	/**
	 * Creates a add link packet and places it in the general queue
	 * @param nodeName
	 * @param nodeCost
	 */
	public void addLink(String nodeName, int nodeCost){
		logger.info("New link to: " + nodeName);
		//make the obj
		LinkObj addlinkObj = new LinkObj(nodeName, nodeCost);
		//create json
		sendPacket.createAddLinkPacket(addlinkObj);

		PacketObj packetObj = new PacketObj(addlinkObj.getOriginalPacket(), nodeName, true);
		//add to the queue
		packetQueue2.addToGeneralQueue(packetObj);
	}

	/**
	 * Creates a remove link packet and places it in the general queue
	 * @param nodeName
	 * @param nodeCost
	 */
	public void removeLink(String nodeName, int nodeCost){
		LinkObj removelinkObj = new LinkObj(nodeName, nodeCost);
		//create json
		sendPacket.createRemoveLinkPacket(removelinkObj);

		PacketObj packetObj = new PacketObj(removelinkObj.getOriginalPacket(), nodeName, true);
		//add to the queue
		packetQueue2.addToGeneralQueue(packetObj);
	}

	/**
	 * Creates a modify link packet and places it in the general queue
	 * @param nodeName
	 * @param nodeCost
	 */
	public void modifyLink(String nodeName, int nodeCost){
		LinkObj modifylinkObj = new LinkObj(nodeName, nodeCost);
		//create json
		sendPacket.createModifyLinkPacket(modifylinkObj);
		PacketObj packetObj = new PacketObj(modifylinkObj.getOriginalPacket(), nodeName, true);
		//add to the queue
		packetQueue2.addToGeneralQueue(packetObj);
	}

	/**
	 * Creates a add client  packet and places it in the general queue</br>
	 * the cost is not used so it can be any value
	 * @param nodeName
	 * @param nodeCost
	 */
	public void addClient(String nodeName, int nodeCost){
		LinkObj addClientLinkObj = new LinkObj(nodeName, nodeCost);
		//create json
		sendPacket.createAddClient(addClientLinkObj);

		PacketObj packetObj = new PacketObj(addClientLinkObj.getOriginalPacket(), nodeName, true);
		//add to the queue
		packetQueue2.addToGeneralQueue(packetObj);
	}

	/**
	 * Creates a remove client packet and places it in the general queue
	 * @param nodeName
	 * @param nodeCost
	 */
	public void removeClient(String nodeName, int nodeCost){
		LinkObj removeClientLinkObj = new LinkObj(nodeName, nodeCost);
		//create json
		sendPacket.createRemoveClient(removeClientLinkObj);

		PacketObj packetObj = new PacketObj(removeClientLinkObj.getOriginalPacket(), nodeName, true);
		//add to the queue
		packetQueue2.addToGeneralQueue(packetObj);
	}


}
