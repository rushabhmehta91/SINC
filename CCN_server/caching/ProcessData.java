package caching;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import overlay.ServerLinks;
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
			GenericPacketObj gpo = ServerLFS.pq2.removeFromRoutingQueue();
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
		String content = null;
		byte cacheFlag = dataObj.getCacheFlag();
		if (dataObj != null && cacheFlag == 2) {
			content = dataObj.getData();
			ServerLFS.incomingContent(content);
			logger.info("Content with name " + content + "is placed in cached");
			System.out.println("Content with name " + content + "is placed in cached");
		}

	}

	public void processIntrestObj(IntrestObj intrestObj, String receivedFromNode) {
		String contentName = null;
		boolean copyFlag = false;
		if (intrestObj != null) {
			contentName = intrestObj.getContentName();
			Content requestedContent = ServerLFS.serveRequest(contentName);
			if (requestedContent != null) {
				try {
					ServerLFS.updateScoreOnIterface(requestedContent, receivedFromNode);
					if (ServerLFS.shouldCopy(requestedContent, receivedFromNode)) {
						copyFlag = true;
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
					System.out.println(e);
//					e.printStackTrace();
				}
				ServerLFS.sendDataObj(requestedContent, intrestObj.getOriginRouterName(), receivedFromNode, copyFlag);

			}

		}
		logger.info("Content name: " + contentName);
		System.out.println("Content name: " + contentName);
	}
}