package topology;

import java.util.concurrent.ArrayBlockingQueue;
import packetObjects.GenericPacketObj;


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
public class CacheServerPacketQueue2 extends PacketQueue2{
	
	@SuppressWarnings("rawtypes")
	ArrayBlockingQueue<GenericPacketObj> updateQueue;

	/**
	 * COnstructor
	 */
	@SuppressWarnings("rawtypes")
	public CacheServerPacketQueue2(){
		super();
		updateQueue = new ArrayBlockingQueue<GenericPacketObj>(100, true);

	}

	/**
	 * Add a general packet object to the update queue
	 * @param genericPacketObj
	 */
	@SuppressWarnings("rawtypes")
	public void addToUpdateQueue(GenericPacketObj genericPacketObj){
		try {
			updateQueue.put(genericPacketObj);
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			System.out.println(e);
//			e.printStackTrace();
		}
	}

	/**
	 * Remove a general packet object from the update queue
	 * @return general packet objects
	 */
	@SuppressWarnings("rawtypes")
	public GenericPacketObj removeFromUpdateQueue(){
		try {
			//System.out.println("trying to remove from update queue");
			//System.out.println(updateQueue.size());
			GenericPacketObj gpo = updateQueue.take();
			//System.out.println("packet taken from update queue");

			return gpo;
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			System.out.println(e);
//			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks if the update queue is empty
	 * @return true if empty and false if not empty
	 */
	public boolean isUpdateQueueEmpty(){
		return updateQueue.isEmpty();
	}

}
