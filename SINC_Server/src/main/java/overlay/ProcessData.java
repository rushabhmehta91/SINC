package overlay;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import caching.Content;
import caching.ContentPacket;
import overlay.Link;
import packetObjects.DataObj;
import packetObjects.GenericPacketObj;
import packetObjects.IntrestObj;

public class ProcessData extends Thread {
	private static Logger logger = LogManager.getLogger(ProcessData.class);
	public ProcessData() {
//		System.out.println("server process data constructer:");
	}

	@Override
	public void run() {
		while (true) {
			GenericPacketObj gpo = Server.pq2.removeFromRoutingQueue();
			logger.info("server process data action: " + gpo.getAction());
			System.out.println("server process data action: " + gpo.getAction());
			String receivedFromNode = gpo.getRecievedFromNode();
			logger.info("recieved from node: " + gpo.getRecievedFromNode());
			System.out.println("recieved from node: " + gpo.getRecievedFromNode());
			if (gpo.getAction().equals("intrest")) {
				IntrestObj intrestObj = (IntrestObj) gpo.getObj();
				processIntrestObj(intrestObj, receivedFromNode);

			} else {
				if (gpo.getAction().equals("data")) {
					DataObj dataObj = (DataObj) gpo.getObj();
					processDataObj(dataObj);

				}
			}
			continue;
		}
	}

	public void processDataObj(DataObj dataObj) {
		Object content = null;
		byte cacheFlag = dataObj.getCacheFlag();
		if (dataObj != null && cacheFlag == 2) {
			content = dataObj.getData();
			Server.incomingContent(content);
			logger.info("Content with name " + content + "is placed in cached");
			System.out.println("Content with name " + content + "is placed in cached");
		}

	}

	public void processIntrestObj(IntrestObj intrestObj, String receivedFromNode) {
		String contentName = null;
		boolean copyFlag = false;
		if (intrestObj != null) {
			contentName = intrestObj.getContentName();
			ContentPacket requestedContent = Server.serveRequest(contentName);
			if (requestedContent != null) {
				try {
					Server.updateScoreOnIterface(requestedContent.getContent(), receivedFromNode);
					if (Server.shouldCopy(requestedContent.getContent(), receivedFromNode)) {
						copyFlag = true;
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
					System.out.println(e);
//					e.printStackTrace();
				}
				Server.sendDataObj(requestedContent, intrestObj.getOriginRouterName(), receivedFromNode, copyFlag);

			}

		}
		logger.info("Content name: " + contentName);
		System.out.println("Content name: " + contentName);
	}
}