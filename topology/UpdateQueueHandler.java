package topology;

import packetObjects.PacketObj;

public class UpdateQueueHandler implements Runnable {
	PacketQueue packetQueue;
	NodeRepository nodeRepo;
	FIB fib;
	DirectlyConnectedNodes directlyConnectedNodes;
	UpdateMsgsSeen updateMsgsSeen;

	Parse parse;
	volatile boolean running;
	PacketObj packetObj;

	public UpdateQueueHandler(PacketQueue packetQueue, 
			NodeRepository nodeRepo, 
			FIB fib, 
			DirectlyConnectedNodes directlyConnectedNodes,
			UpdateMsgsSeen updateMsgsSeen) {

		this.packetQueue = packetQueue;
		this.nodeRepo = nodeRepo;
		this.fib = fib;
		this.directlyConnectedNodes = directlyConnectedNodes;
		this.updateMsgsSeen = updateMsgsSeen;

		parse = new Parse();
		running = true;
	}

	public void killUpdateHandler(){
		running = false;
	}

	@Override
	public void run() {

		//loop endlessly
		while(running){

			//remove a packet from the queue
			//because this is a blocking queue, this will block until 
			//something is placed in the queue
			packetObj = packetQueue.removeFromUpdateQueue();
			if(packetObj != null){
				//give to the thread pool for processing 
				//executer service == java's thread pool

				Thread thread = new Thread(new UpdateSwitch(packetObj, 
						nodeRepo, 
						fib, 
						directlyConnectedNodes, 
						updateMsgsSeen));
				thread.start();
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}//if the packet was null, drop it 


		}//end while loop

	}
}