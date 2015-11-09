package caching;

import overlay.DataBytes;
import overlay.Peer;
import packetObjects.DataObj;
import packetObjects.PrefixListObj;
import packetObjects.PrefixObj;
import topology.CacheServerSendPacket;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Created by rushabhmehta91 on 4/6/15.
 */
public class ContentStore {

	public static HashMap<String, Content> store;
	public static ArrayList<String> storeList;
	public static CacheServerSendPacket sendPacketObj;
	static Runtime r = Runtime.getRuntime();
	private static ObjectOutputStream oos = null;
	private static ObjectInputStream ois = null;
	private static Logger logger = LogManager.getLogger(ContentStore.class);

	static {
		storeList = new ArrayList<String>();
		store = new HashMap<String, Content>();
		sendPacketObj = new CacheServerSendPacket();

	}


	public static ContentPacket serveRequest(String fileName) {
		ContentPacket c;
		DataBytes dataBytes = null;
		byte[] bytesArr;
		System.out.println("Serving request:" + fileName);
		if (storeList.contains(fileName)) {
			logger.info("Request content found!!!!!");
			System.out.println("Request content found!!!!!");
			// return store.get(fileName);

			// put file object in content and return it.
			File f = new File("cache/" + fileName);
			try {
                bytesArr = IOUtils.toByteArray(new FileInputStream(f));
                dataBytes = new DataBytes(bytesArr);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
			
			c = new ContentPacket(store.get(fileName), dataBytes);
			return c;

		} else {
			logger.warn("Request content not found on server. sending 404");
			System.out.println("Request content not found on server. sending 404");
			return null;
		}

	}

	public static void sendDataObj(ContentPacket sendingContent, String originRouter, String receivedFromNode,
			boolean copyFlag) {
		logger.info("Sending requested content");
		System.out.println("Sending requested content");
		byte copyFlagValue;
		if (copyFlag) {
			copyFlagValue = (byte) 2;
		} else {
			copyFlagValue = (byte) 1;
		}
		ArrayList<String> path=new ArrayList<>();
		path.add(Peer.ID);
		// System.out.println(convertContentToString(sendingContent));
		DataObj dataObj = new DataObj(sendingContent.getContent().getContentName(), originRouter, (byte) 0, sendingContent, path, copyFlagValue, true);
//		sendPacketObj.createDataPacket(dataObj);
		sendPacketObj.forwardPacket(dataObj, receivedFromNode);
	}
	private static void addContentToStore(Content content) {
		// long size = value.length();
		// ArrayList<Integer> trail = new ArrayList<Integer>();
		// trail.add(-1);
		// Content contentToBeInserted = new Content(key, trail, size, value);
		store.put(content.getContentName(), content);
		storeList.add(content.getContentName());
		// advertiseNewlyAdded(contentToBeInserted);
	}

	

	/**
	 * Update N score of the interface and check for interface score score if it
	 * is zero than initiale copy and delete depending to N score on rest all
	 * interface
	 *
	 * @param contentStoreCopy
	 *            - content in content store
	 * @param interfaceId
	 *            - interface Id on which content is requested
	 * @return
	 * @throws Exception
	 */
	public static void updateScoreOnIterface(Content contentStoreCopy, String interfaceId) throws Exception {
		logger.info("updating score to " + (contentStoreCopy.listofScoreOnInterfaces.get(interfaceId) - 1));
		System.out.println("updating score to " + (contentStoreCopy.listofScoreOnInterfaces.get(interfaceId) - 1));
		if (!contentStoreCopy.listofScoreOnInterfaces.containsKey(interfaceId)) {
			contentStoreCopy.listofScoreOnInterfaces.put(interfaceId, contentStoreCopy.getMaxNScore());
		} else {
			contentStoreCopy.listofScoreOnInterfaces.replace(interfaceId,
					contentStoreCopy.listofScoreOnInterfaces.get(interfaceId) - 1);
		}
	}

	public static boolean shouldCopy(Content contentStoreCopy, String interfaceId) {
		boolean copyFlag = false;
		if (contentStoreCopy.listofScoreOnInterfaces.get(interfaceId) == 0) {
			copyFlag = true;
		}
		return copyFlag;
	}

	public static boolean shouldDelete(Content contentStoreCopy) {
		boolean deleteFlag = true;
		for (String index : contentStoreCopy.listofScoreOnInterfaces.keySet()) {
			if (contentStoreCopy.listofScoreOnInterfaces.get(index) < contentStoreCopy.getMaxNScore() / 2) {
				deleteFlag = false;
			}
		}
		return deleteFlag;
	}

	public static String convertContentToString(Content myObject) {
		String serializedObject = "";
		byte buffer[];
		// serialize the object
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream so = new ObjectOutputStream(bo);
			so.writeObject(myObject);
			so.flush();
			// buffer = bo.toByteArray();
			// serializedObject = new String(buffer);
			serializedObject = bo.toString("ISO-8859-1");
		} catch (Exception e) {
			logger.error(e.getMessage());
			System.out.println(e);
		}
		return serializedObject;
	}

	public static Content convertStringToContent(String serializedObject) {
		Content contentObj = null;
		try {
			// deserialize the object
			byte b[] = serializedObject.getBytes("ISO-8859-1");
			ByteArrayInputStream bi = new ByteArrayInputStream(b);
			ObjectInputStream si = new ObjectInputStream(bi);
			contentObj = (Content) si.readObject();
		} catch (Exception e) {
			logger.error(e.getMessage());
			System.out.println(e);
			// e.printStackTrace();
		}
		return contentObj;
	}

	/**
	 * When incoming packet have content which to be stored in content store
	 * than check the size of the the store if store has required size then
	 * place it in store else replace.
	 *
	 * @param packet
	 *            - incoming packet
	 * @param recievedFromNode
	 * @return
	 */
	public static boolean incomingContent(DataObj packet, String recievedFromNode) {
		logger.info("incoming content received");
		System.out.println("incoming content received");
		ContentPacket cp = (ContentPacket) packet.getData();
		if (cp.getContent().getSizeInBytes() <= r.freeMemory()) {
			return place(cp, recievedFromNode);
		} else {
			return replace(packet, recievedFromNode);
		}
	}

	/**
	 * If content store has no space then replace the least recently used
	 * content from content store with new content
	 *
	 * @param receivedContent
	 * @param recievedFromNode
	 * @return
	 */
	private static boolean replace(DataObj receivedContent, String recievedFromNode) {
		return false;
	}

	/**
	 * Place the incoming content in the store. If content is in the store than
	 * replace the content else just add the content in the store
	 *
	 * @param cp
	 *            - incoming content
	 * @param recievedFromNode
	 * @return
	 */
	public static boolean place(ContentPacket cp, String recievedFromNode) {
		byte[] dataBytes = null;
		String contentName = null;
		if (cp != null) {
			Content c = cp.getContent();
			contentName = c.getContentName();
			try {
				dataBytes = (byte[]) cp.getData();
				System.out.println("writing content file!!");
				File f = new File("cache/" + contentName);
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
				bos.write(dataBytes);
				bos.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!store.containsKey(contentName)) {
				addContentToStore(c);
				try {
					advertiseNewlyAdded(c, true);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				return true;
			} else {
				if (store.replace(contentName, c) != null) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;

		// if (!store.containsKey(receivedContent.getContentName())) {
		// store.put(receivedContent.getContentName(), receivedContent);
		// store.get(receivedContent.getContentName()).trail.add(recievedFromNode);
		// logger.info("content placed");
		// System.out.println("content placed");
		// try {
		// advertiseNewlyAdded(receivedContent, true);
		// } catch (UnknownHostException e) {
		// logger.error(e.getMessage());
		// System.out.println(e);
		//// e.printStackTrace();
		// }
		// return true;
		// } else {
		// if (store.replace(receivedContent.getContentName(), receivedContent)
		// != null) {
		// return true;
		// } else {
		// return false;
		// }
		// }

	}

	private static void advertise(ArrayList<String> contentList, String cacheServerAddress)
			throws UnknownHostException {

		PrefixListObj list = new PrefixListObj(contentList, Peer.generateID(Peer.getIP(Peer.IP)) + "", true,
				Peer.generateID(Peer.getIP(Peer.IP)) + System.nanoTime() + "");
		// sendPacketObj.createPrefixListPacket(list);
		sendPacketObj.createClientPrefixList(list);
		sendPacketObj.forwardPacket(list.getOriginalPacket(), cacheServerAddress);
	}

	private static void advertiseNewlyAdded(Content content, boolean addRemove) throws UnknownHostException {
		logger.info("advertizing newly added content");
		System.out.println("advertizing newly added content");
		// write code to advertize single prefixObj
		PrefixObj list = new PrefixObj(content.getContentName(),
				Peer.generateID(Peer.getIP(Peer.IP)) + System.nanoTime() + "",
				Peer.generateID(Peer.getIP(Peer.IP)) + "", addRemove);
		// sendPacketObj.createPrefixPacket(list);
		sendPacketObj.createClientPrefix(list);
		for (String e : Peer.mep.getDirectlyConnectedNodes().getDirectlyConnectedRouters().keySet()) {
			sendPacketObj.forwardPacket(list.getOriginalPacket(), e);
		}
		for (String e : Peer.mep.getDirectlyConnectedNodes().getDirectlyConnectedClients().keySet()) {
			sendPacketObj.forwardPacket(list.getOriginalPacket(), e);
		}
	}

	/**
	 * delete content from current content store
	 *
	 * @param content
	 *            - content requested
	 * @return
	 */
	public static boolean deleteContent(Content content) {
		if (store.remove(content.getContentName()) != null) {
			File f = new File("cache/" + content.getContentName());
			f.delete();
			try {
				advertiseNewlyAdded(content, false);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				System.out.println(e);

			}
			return true;
		} else {
			return false;
		}

	}

	public Set<String> getPrefixList() {
		return store.keySet();
	}
}
