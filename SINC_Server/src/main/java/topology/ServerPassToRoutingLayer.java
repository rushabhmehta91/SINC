package topology;

import packetObjects.LinkObj;
import packetObjects.PacketObj;

/**
 * This class is used by the overlay, it contains functions that the overlay </br>
 * calls to construct and pass packets to the routing layer.
 * 
 * @author spufflez
 *
 */
public class ServerPassToRoutingLayer extends PassToRoutingLayer {
	ServerSendPacket sendPacket;

	public ServerPassToRoutingLayer(PacketQueue2 packetQueue2) {
		super(packetQueue2);
		sendPacket=new ServerSendPacket();
	}


	/**
	 * Creates a add client  packet and places it in the general queue</br>
	 * the cost is not used so it can be any value
	 * @param nodeName
	 * @param nodeCost
	 */	public void addClient(String nodeName, int nodeCost) {
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
	 public void removeClient(String nodeName, int nodeCost) {
		 LinkObj removeClientLinkObj = new LinkObj(nodeName, nodeCost);
		 //create json
		 sendPacket.createRemoveClient(removeClientLinkObj);

		 PacketObj packetObj = new PacketObj(removeClientLinkObj.getOriginalPacket(), nodeName, true);
		 //add to the queue
		 packetQueue2.addToGeneralQueue(packetObj);
	 }


}
