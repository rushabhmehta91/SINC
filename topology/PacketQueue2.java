package topology;

import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ConcurrentLinkedQueue;

import packetObjects.GenericPacketObj;
import packetObjects.PacketObj;


public class PacketQueue2 {
	//ConcurrentLinkedQueue<PacketObj> clq = new ConcurrentLinkedQueue<PacketObj>();
	ArrayBlockingQueue<PacketObj> generalQueue;

	@SuppressWarnings("rawtypes")
	ArrayBlockingQueue<GenericPacketObj> routingQueue;


	public PacketQueue2(){

		generalQueue  = new ArrayBlockingQueue<PacketObj>(100, true);
		routingQueue = new ArrayBlockingQueue<GenericPacketObj>(100, true);

	}

	public void addToGeneralQueue(PacketObj packet){
		try {
			generalQueue.put(packet);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public PacketObj removeFromGeneralQueue(){
		try {
			return generalQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isGeneralQueueEmpty(){
		return generalQueue.isEmpty();
	}

	@SuppressWarnings("rawtypes")
	public void addToRoutingQueue(GenericPacketObj genericPacketObj){
		try {
			routingQueue.put(genericPacketObj);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("rawtypes")
	public GenericPacketObj removeFromRoutingQueue(){
		try {
			return routingQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isRoutingQueueEmpty(){
		return routingQueue.isEmpty();
	}
}
