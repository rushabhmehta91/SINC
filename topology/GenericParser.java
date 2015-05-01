package topology;

import packetObjects.DataObj;
import packetObjects.GenericPacketObj;
import packetObjects.IntrestObj;
import packetObjects.PacketObj;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GenericParser {

	Gson gson = new Gson();
	Parse2 parse = new Parse2();
	PacketQueue2 packetQueue2;

	public GenericParser(PacketQueue2 packetQueue2) {
		this.packetQueue2 = packetQueue2;
		// TODO Auto-generated constructor stub
		//Implement runnable ... because this will be run in a thread 
		// in the general queue thread pool 
	}

	public void parsePacket(PacketObj packetObj){
		String type;
		JsonObject jsonObject = new JsonObject();
		try{

			jsonObject = gson.fromJson(packetObj.getPacket(), JsonObject.class);
			JsonElement jsonTypeElement = jsonObject.get("type");
			type = jsonTypeElement.getAsString();

		}catch(Exception e){
			type = "dropPacket";
		}

		//System.out.println("Inside parsePacket::type::" + type);

		switch (type){

		case "route" :
			parseRoutePacket(jsonObject, packetObj);
			break;

		default :
			System.out.println("Invalid packet type");
			break;

		}

	}

	public void parseRoutePacket(JsonObject jsonObject, PacketObj packetObj){
		JsonElement jsonTypeElement = jsonObject.get("action");
		String action = jsonTypeElement.getAsString();
		System.out.println("Routing action::" + action);
		//GenericPacketObj genericPacketObj;
		switch(action){

		case "intrest" :
			try{

				IntrestObj intrestObj = parse.parseIntrestJson(jsonObject, packetObj.getPacket());
				GenericPacketObj<IntrestObj> gpoIntrest = new GenericPacketObj<>(action, packetObj.getRecievedFromNode(), intrestObj);
				//add it to the Update Queue
				packetQueue2.addToRoutingQueue(gpoIntrest);
			}catch(Exception e){

			}
			break;

		case "data" :
			try{

				DataObj dataObj = parse.parseDataJson(jsonObject, packetObj.getPacket());
				GenericPacketObj<DataObj> gpoData= new GenericPacketObj<DataObj>(action, packetObj.getRecievedFromNode(), dataObj);
				//add it to the Update Queue
				packetQueue2.addToRoutingQueue(gpoData);
			}catch(Exception e){

			}
			break;

		default :
			System.out.println("Invalid route packet action");
			break;

		}


	}
}

