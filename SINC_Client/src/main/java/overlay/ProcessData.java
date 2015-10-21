package overlay;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import packetObjects.DataObj;
import packetObjects.GenericPacketObj;

public class ProcessData extends Thread {
	ConcurrentHashMap<String, Long> rtt;
	private static Logger logger = LogManager.getLogger(ProcessData.class);

	public ProcessData(ConcurrentHashMap<String, Long> rtt) {
		this.rtt = rtt;
		logger.info("client process data constructer:");
		System.out.println("client process data constructer:");
	}

	@Override
	public void run() {
		while (true) {
			GenericPacketObj<DataObj> gpo = Client.pq2.removeFromRoutingQueue();
			DataObj dataObj = null;
			logger.info("client process data action: " + gpo.getAction());
			System.out.println("client process data action: " + gpo.getAction());
			switch (gpo.getAction()) {
			case "data":
				dataObj = (DataObj) gpo.getObj();
				break;
			default:
				dataObj = null;
				break;
			}
			if (dataObj == null) {
				continue;
			}
			processDataObj(dataObj);
		}
	}

	public void processDataObj(DataObj dataObj) {
	    DataBytes dataBytes = null;
		String contentName = null;
		if (dataObj != null) {
			contentName = dataObj.getContentName();
			try {
			    dataBytes = (DataBytes) dataObj.getData();
			    System.out.println("writing content file!!");
			    File f = new File(contentName);
			    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
			    bos.write(dataBytes.data);
			    bos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//used for rtt, can be removed
		if(rtt.containsKey(contentName) == true){
			logger.info("rtt mili seconds: " + (System.currentTimeMillis() - rtt.get(contentName)));
			System.out.println("rtt mili seconds: " + (System.currentTimeMillis() - rtt.get(contentName)) );
		}
		logger.info("Content name: " + contentName);
		System.out.println("Content name: " + contentName);
	}
}
