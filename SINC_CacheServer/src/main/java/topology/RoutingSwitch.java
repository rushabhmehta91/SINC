package topology;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import packetObjects.DataObj;
import packetObjects.GenericPacketObj;
import packetObjects.IntrestObj;
import packetObjects.ModifyNodeObj;
import packetObjects.NeighborRequestObj;
import packetObjects.PrefixListObj;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;


/**
 * The class decides how to process a routing packet (interest and data packet) </br>
 * Interest packets are first checked to see if the interest packet is</br>
 * for the cache server, if it is the packet is processed</br>
 * If the packet is not for the cache server, it is forwarded.</br>
 * </br>
 * Data packets are checked if they are for the server, if it is addressed</br>
 * to the server, it is processed. If not it is forwarded based on the flag</br>
 * setting 0,1,2.
 * @author spufflez
 *
 */
public class RoutingSwitch implements Runnable{

	//String packet;
	@SuppressWarnings("rawtypes")
	GenericPacketObj genericPacketObj;
	FIB fib;
	ProcessRoutingPackets process;
	PIT pit;
	//Parse parse;
	NodeRepository nodeRepo;
	DirectlyConnectedNodes directlyConnectedNodes;
	CacheServerPacketQueue2 packetQueue2;
	String recievedFromNode;
	private static Logger logger = LogManager.getLogger(RoutingSwitch.class);

	/**
	 * Constructor
	 * @param genericPacketObj
	 * @param fib
	 * @param pit
	 * @param directlyConnectedNodes
	 * @param nodeRepo
	 * @param packetQueue2
	 */
	@SuppressWarnings("rawtypes")
	public RoutingSwitch(GenericPacketObj genericPacketObj,
			FIB fib,
			PIT pit,
			DirectlyConnectedNodes directlyConnectedNodes,
			NodeRepository nodeRepo,
			CacheServerPacketQueue2 packetQueue2){

		//this.packet = packet;
		this.genericPacketObj = genericPacketObj;
		this.fib = fib;
		this.pit = pit;
		this.directlyConnectedNodes = directlyConnectedNodes;
		this.nodeRepo = nodeRepo;
		this.packetQueue2 = packetQueue2;
		this.recievedFromNode = genericPacketObj.getRecievedFromNode();

		//parse = new Parse();
		process = new ProcessRoutingPackets(nodeRepo, fib, pit, directlyConnectedNodes, recievedFromNode);
	}


	@Override
	public void run() {
		String action = genericPacketObj.getAction();


		switch(action){
		case "intrest" :
			IntrestObj intrestObj = (IntrestObj) genericPacketObj.getObj();
			String[] contentNameSplit = intrestObj.getContentName().split("/");

//			System.out.println("machine name"+nodeRepo.getThisMachinesName());
			//if(intrestObj.getContentName().equals(nodeRepo.getThisMachinesName()) == false){
			if(contentNameSplit[0].equals(nodeRepo.getThisMachinesName()) == false){
				try {
					process.processIntrest(intrestObj, this.recievedFromNode);
				} catch (IOException e) {
					logger.error(e.getMessage());
					System.out.println(e);
//					e.printStackTrace();
				}
			}else{

				if(contentNameSplit.length == 2){
					if(contentNameSplit[1].equals("ping") == true){
						process.preocessPingRequest(intrestObj);
					}else{

						CacheServerSendPacket sendPacket = new CacheServerSendPacket();
						//the packet is for this node
						//parse the neighbor request 
						//NeighborRequestObj neighborRequestObj = new NeighborRequestObj(genericPacketObj.getRecievedFromNode());
						NeighborRequestObj neighborRequestObj = new NeighborRequestObj(intrestObj.getContentName(), intrestObj.getOriginRouterName(), genericPacketObj.getRecievedFromNode());


						//create the packet
						sendPacket.createNeighborRequestPacket(neighborRequestObj);

						//add to the update queue
						GenericPacketObj<NeighborRequestObj> genericPacketObjUpdate = 
								new GenericPacketObj<NeighborRequestObj>("neighborRequest", 
										genericPacketObj.getRecievedFromNode(), 
										neighborRequestObj);

						packetQueue2.addToUpdateQueue(genericPacketObjUpdate);
					}
				}
			}
			break;

		case "data" :


			DataObj dataObj = (DataObj) genericPacketObj.getObj();
			String[] dataContentNameSplit = dataObj.getContentName().split("/");
			boolean packetIsForMe = false;
			if(dataContentNameSplit.length == 2){
				if(dataObj.getOriginRouterName().equals(nodeRepo.getThisMachinesName())){

					if(dataContentNameSplit[1].equals("np") || dataContentNameSplit[1].equals("ping")){
						packetIsForMe = true;
					}
				}
			}

			//if(dataObj.getContentName().equals(nodeRepo.getThisMachinesName()) == false){
			if(packetIsForMe == false){
				switch(dataObj.getFlag()){
				case 0 :
					process.processData0(dataObj);
					break;

				case 1 :
					process.processData1(dataObj);
					break;

				case 2 :
					process.processData2(dataObj);
					break;

				default : 
					logger.error("data flag set to an incorrect value");
					System.out.println("data flag set to an incorrect value");
					break;
				}
			}else{

				if(dataContentNameSplit.length == 2){
					if(dataContentNameSplit[1].equals("ping") == true){
						//process ping response
						process.processPingReply(dataObj);
					}else{

						//the packet is for this node
						Gson gson = new Gson();
						JsonObject jsonObject = gson.fromJson(dataObj.getData(), JsonObject.class);
						JsonElement jsonActionElement = jsonObject.get("action");
						String nestedAction = jsonActionElement.getAsString();
						CacheServerSendPacket sendPacket = new CacheServerSendPacket();

						if(nestedAction.equals("prefixResponse") == true){
							//call prefix function
							JsonElement jsonIDElement = jsonObject.get("msgID");
							String msgID = jsonIDElement.getAsString();


							JsonElement jsonAdvertiserElement = jsonObject.get("advertiser");
							String advertiser = jsonAdvertiserElement.getAsString();

							JsonElement JE = jsonObject.get("prefixList");
							Type TYPE = new TypeToken<ArrayList<String>>(){}.getType();
							ArrayList<String> prefixList = new Gson().fromJson(JE.getAsString(), TYPE);

							PrefixListObj prefixListObj = new PrefixListObj(prefixList, advertiser, true, msgID);


							//create the update packet
							sendPacket.createPrefixResponsePacket(prefixListObj);

							//add to update queue
							//packetObj = new PacketObj(prefixListObj.getOriginalPacket(), nodeRepo.getThisMachinesName(), false);
							GenericPacketObj<PrefixListObj> genericPacketObjPrefix = 
									new GenericPacketObj<PrefixListObj>("prefixResponse", 
											genericPacketObj.getRecievedFromNode(), 
											prefixListObj);

							packetQueue2.addToUpdateQueue(genericPacketObjPrefix);
						}else{
							//call neighbors function

							JsonElement jsonNameElement = jsonObject.get("nodeName");
							String nodeName = jsonNameElement.getAsString();

							JsonElement jsonIDElement = jsonObject.get("msgID");
							String msgID = jsonIDElement.getAsString();

							JsonElement jsonNeighborsElement = jsonObject.get("neighbors");
							String neighborsString = jsonNeighborsElement.getAsString();
							Type neighborsType = new TypeToken<ArrayList<NeighborAndCostStrings>>(){}.getType();
							ArrayList<NeighborAndCostStrings> neighborsList = gson.fromJson(neighborsString, neighborsType);


							ModifyNodeObj modifyNodeObj = new ModifyNodeObj(nodeName, neighborsList, msgID);
							//create the update packet
							sendPacket.createNeighborResponsePacket(modifyNodeObj);

							//add to update queue
							//packetObj = new PacketObj(modifyNodeObj.getOriginalPacket(), nodeRepo.getThisMachinesName(), false);
							GenericPacketObj<ModifyNodeObj> genericPacketObjNeighbors = 
									new GenericPacketObj<ModifyNodeObj>("neighborResponse", 
											genericPacketObj.getRecievedFromNode(), modifyNodeObj);
							packetQueue2.addToUpdateQueue(genericPacketObjNeighbors);
						}// ends if else neighbors/prefixes



					}//ends if else for ping/neighbors-prefixes
					//break;
				}//ends if length grater then 2
				break;
			}

		default : 
			logger.error("Invalid route action");
			System.out.println("Invalid route action");
			break;
		}
	}
}
