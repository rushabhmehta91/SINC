package topology;

import packetObjects.DataObj;
import packetObjects.GenericPacketObj;
import packetObjects.IntrestObj;
import packetObjects.PacketObj;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


/**
 * This class parse the raw packet passed from the overlay to the routing layer.</br>
 * This class is responsible for parsing every type of packet, and dropping </br>
 * packets if they are not formatted correctly.
 * @author spufflez
 *
 */
public class GenericParser {

	Gson gson = new Gson();
	Parse2 parse;
	PacketQueue2 packetQueue2;
	boolean isJson = true;
	private static Logger logger = LogManager.getLogger(GenericParser.class);

	/**
	 * Constructor
	 * @param packetQueue2
	 */
	public GenericParser(PacketQueue2 packetQueue2) {
		parse = new Parse2();
		this.packetQueue2 = packetQueue2;

	}

	public GenericParser() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Parses a packet and adds it to either the update or routing queue
	 * @param packetObj
	 */
	public void parsePacket(PacketObj packetObj){
		String type;
		JsonObject jsonObject = new JsonObject();
		try{
			jsonObject = gson.fromJson((String)packetObj.getPacket(), JsonObject.class);
			JsonElement jsonTypeElement = jsonObject.get("type");
			type = jsonTypeElement.getAsString();
			isJson = true;
		}catch(Exception e){
			try{
				jsonObject = null;
				isJson = false;
				DataObj dataObj = (DataObj) packetObj.getPacket();
				type = dataObj.getType();
			}catch(Exception e1){
				type = "dropPacket";
			}
		}

		switch (type){

		case "route" :
			parseRoutePacket(jsonObject, packetObj, isJson);
			break;

		default :
			break;

		}

	}


	/**
	 * Parses a routing packet
	 * @param jsonObject
	 * @param packetObj
	 */
	public void parseRoutePacket(JsonObject jsonObject, PacketObj packetObj, boolean isJson){
		JsonElement jsonTypeElement;
		String action = null;
		DataObj dataObj = null;
		
		if ( isJson ) {
		    jsonTypeElement = jsonObject.get("action");
		    action = jsonTypeElement.getAsString();
		} else {
		    dataObj = (DataObj) packetObj.getPacket();
		    action = dataObj.getAction();
		}

		logger.info("Routing action::" + action);
		switch(action){

		case "intrest" :
			try{

				IntrestObj intrestObj = parse.parseIntrestJson(jsonObject, (String)packetObj.getPacket());
				GenericPacketObj<IntrestObj> gpoIntrest = new GenericPacketObj<>(action, packetObj.getRecievedFromNode(), intrestObj);
				//add it to the Update Queue
				packetQueue2.addToRoutingQueue(gpoIntrest);
			}catch(Exception e){
				logger.error(e.getStackTrace());

			}
			break;

		case "data" :
			try{
			    if ( isJson ) {
			    	  jsonTypeElement = jsonObject.get("action");
	    				dataObj = parse.parseDataJson(jsonObject, (String) packetObj.getPacket());
	    				GenericPacketObj<DataObj> gpoData= new GenericPacketObj<DataObj>(action, packetObj.getRecievedFromNode(), dataObj);
	    				//add it to the Update Queue
	    				packetQueue2.addToRoutingQueue(gpoData);
	    				logger.info("generic parser added data OBJ to routing queue");
				    } else {
				    	logger.info("data received and forwarding now");
				        dataObj = (DataObj) packetObj.getPacket();
			            GenericPacketObj<DataObj> gpoData= new GenericPacketObj<DataObj>(action, packetObj.getRecievedFromNode(), dataObj);

		            packetQueue2.addToRoutingQueue(gpoData);
			    }
			}catch(Exception e){
				logger.error(e.getStackTrace());
			}
			break;

		default :
			logger.error("Invalid route packet action");
			break;

		}


	}
}

