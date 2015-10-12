package topology;

import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import packetObjects.GenericPacketObj;
import packetObjects.PacketObj;

/**
 * This class holds all of the queues that the routing layers uses</br>
 * All of the queues are thread safe and will block if nothing is in the queue</br>
 * 
 * generalQueue: the queue that the overlay places raw packets into</br>
 * 
 * updateQueue: the queue that all update packets are stored in </br>
 * 
 * routingQueue: the queue that all interest and data packets are stored in </br>
 * @author spufflez
 *
 */
public class PacketQueue2 {
	ArrayBlockingQueue<PacketObj> generalQueue;
	private static Logger logger = LogManager.getLogger(PacketQueue2.class);

	@SuppressWarnings("rawtypes")
	ArrayBlockingQueue<GenericPacketObj> routingQueue;

	/**
	 * Constructor
	 */
	public PacketQueue2(){

		generalQueue  = new ArrayBlockingQueue<PacketObj>(100, true);
		routingQueue = new ArrayBlockingQueue<GenericPacketObj>(100, true);

	}


	/**
	 * Add a raw packet to the general queue
	 * @param packet
	 */
	public void addToGeneralQueue(PacketObj packet){
		try {
			generalQueue.put(packet);
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			System.out.println(e);
//			e.printStackTrace();
		}
	}

	/**
	 * Remove a raw packet from the general queue
	 * @return raw packet 
	 */
	public PacketObj removeFromGeneralQueue(){
		try {
			return generalQueue.take();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			System.out.println(e);
//			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks if the general queue is empty 
	 * @return ture if empty and false if not empty
	 */
	public boolean isGeneralQueueEmpty(){
		return generalQueue.isEmpty();
	}


	/**
	 * Add a general packet object to the routing queue
	 * @param genericPacketObj
	 */
	@SuppressWarnings("rawtypes")
	public void addToRoutingQueue(GenericPacketObj genericPacketObj){
		try {
			routingQueue.put(genericPacketObj);
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			System.out.println(e);
//			e.printStackTrace();
		}
	}

	/**
	 * Remove a general packet object from the routing queue
	 * @return general packet objects
	 */
	@SuppressWarnings("rawtypes")
	public GenericPacketObj removeFromRoutingQueue(){
		try {
			return routingQueue.take();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			System.out.println(e);
//			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks if the routing queue is empty 
	 * @return true if empty and false if not empty
	 */
	public boolean isRoutingQueueEmpty(){
		return routingQueue.isEmpty();
	}
}
