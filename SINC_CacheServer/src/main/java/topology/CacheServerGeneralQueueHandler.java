package topology;

import packetObjects.PacketObj;

/**
 * This class removes packets from the queue that the overlay places packets in</br>
 * and parses them into objects. The objects are then placed in the routing </br>
 * or update queue.
 * 
 * @author spufflez
 *
 */
public class CacheServerGeneralQueueHandler implements Runnable{

	//Parse parse;
	CacheServerPacketQueue2 packetQueue2;
	volatile boolean running;
	CacheServerGenericParser genericParser;

	/**
	 * Constructor
	 * @param packetQueue2
	 * @param running
	 */
	public CacheServerGeneralQueueHandler(CacheServerPacketQueue2 packetQueue2, boolean running) {
		//parse = new Parse();
		this.packetQueue2 = packetQueue2;
		this.running = running;
		genericParser = new CacheServerGenericParser(packetQueue2);
	}

	public void killGeneralHandler(){
		running = false;
	}

	@Override
	public void run() {
		//loop endlessly
		while(running){

			//remove a packet from the queue
			//because this is a blocking queue, this will block until 
			//something is placed in the queue
			PacketObj packetObj = packetQueue2.removeFromGeneralQueue();

			if(packetObj != null){

				genericParser.parsePacket(packetObj);

			}

		}//end while loop

	}

}
