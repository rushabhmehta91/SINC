package UnitTests;

import java.util.ArrayList;

import packetObjects.LinkObj;
import packetObjects.ModifyNodeObj;
import packetObjects.NeighborRequestObj;
import packetObjects.PrefixListObj;
import packetObjects.PrefixObj;
import topology.DirectlyConnectedNodes;
import topology.FIB;
import topology.NeighborAndCostStrings;
import topology.NodeRepository;
import topology.PIT;
import topology.ProcessUpdates;
import topology.UpdateMsgsSeen;

public class UpdateSimulation {

	NodeRepository nodeRepo;
	FIB fib;
	ProcessUpdates process;
	DirectlyConnectedNodes directlyConnectedNodes;
	UpdateMsgsSeen upDatesSeen;

	public UpdateSimulation(){
		directlyConnectedNodes = new DirectlyConnectedNodes();
		upDatesSeen = new UpdateMsgsSeen();

		nodeRepo = new NodeRepository("A");
		fib = new FIB(nodeRepo, new PIT(), directlyConnectedNodes);
		process = new ProcessUpdates(nodeRepo, upDatesSeen, fib, directlyConnectedNodes);

	}


	public void startSimulation(){

		//initialize my self with a node name and cost of zero
		nodeRepo.HMaddNode("A");
		nodeRepo.HMgetNode("A").setBestCost(0);
		nodeRepo.HMgetNode("A").setOriginNextHop("A");
		fib.addPrefixToFIB("A", "A");
		//could as a directly connected client 
		directlyConnectedNodes.addDirectlyConnectedClient("A");
		directlyConnectedNodes.getDirectlyConnectedClient("A").addPrefix("A");
		System.out.println("Does node A exist in the graph: " + nodeRepo.HMdoesNodeExist("A"));
		System.out.println("Is node A a directly connected client: " + directlyConnectedNodes.doesDirectlyConnectedClientExist("A"));
		ArrayList<String> prefixs = directlyConnectedNodes.getDirectlyConnectedClient("A").getPrefixArrayList();
		System.out.println("Size of client A's prefix list: " + prefixs.size());
		System.out.println("Client A's prefix: " + prefixs.get(0));
		System.out.println("Is prefix A in the FIB table: " + fib.doesHashMapContainPrefix(1, "A"));
		System.out.println("");
	}

	public void addLinks(){

		//add a directly connected link
		//this will also request that nodes neighbors
		//this will also send out an update to the nodes saying these are my neighbors
		LinkObj linkObj1 = new LinkObj("B", 20);
		process.addLink(linkObj1);
		System.out.println("Does node B exist in the graph: " + nodeRepo.HMdoesNodeExist("B"));
		System.out.println("Node B's bestcost: " + nodeRepo.HMgetNode("B").getBestCost());
		System.out.println("Does the FIB have a entry for B: " +fib.doesHashMapContainPrefix(1, "B"));
		System.out.println("");

		//add link 
		LinkObj linkObj2 = new LinkObj("G", 90);
		process.addLink(linkObj2);
		System.out.println("Does node G exist in the graph: " + nodeRepo.HMdoesNodeExist("G"));
		System.out.println("Node G's bestcost: " + nodeRepo.HMgetNode("G").getBestCost());
		System.out.println("Does the FIB have a entry for G: " +fib.doesHashMapContainPrefix(1, "G"));
		System.out.println("");

		//add link
		LinkObj linkObj3 = new LinkObj("D", 80);
		process.addLink(linkObj3);
		System.out.println("Does node D exist in the graph: " + nodeRepo.HMdoesNodeExist("D"));
		System.out.println("Node D's bestcost: " + nodeRepo.HMgetNode("D").getBestCost());
		System.out.println("Does the FIB have a entry for D: " +fib.doesHashMapContainPrefix(1, "D"));
		System.out.println("");

		String[] directlyConnectRouters = directlyConnectedNodes.getDirectlyConnectedRoutersList();
		for(String router : directlyConnectRouters){
			System.out.println("directly connect router: " + router);
		}
		System.out.println("");
	}

	public void requestForNeighbors(){

		//Receive request for neighbors and prefixes 
		//send neighbors data packet and prefix data packet
		NeighborRequestObj neighborRequestObj = new NeighborRequestObj("B");
		process.processIntrestRequestForNeighbors(neighborRequestObj);
	}

	public void neighborsAndPrefixResponse(){

		//Receive neighbor response updates 
		ArrayList<NeighborAndCostStrings> neighbors1 = new ArrayList<NeighborAndCostStrings>();
		neighbors1.add(new NeighborAndCostStrings("F", 20));
		ModifyNodeObj modifyNodeObj = new ModifyNodeObj("B", neighbors1, "B1234");
		process.processNeighborsResponse(modifyNodeObj);
		//is node f in the graph 
		System.out.println("Does node F exist: " + nodeRepo.HMdoesNodeExist("F"));
		// do I have a fib for node f
		System.out.println("Does fib containf prefix for F: " + fib.doesHashMapContainPrefix(1, "F"));
		//was node f added to B's neighbors
		System.out.println("Does node B have F as a neighbor: " + nodeRepo.HMgetNode("B").doesNeighborExist("F"));
		System.out.println("B's neighbor F with cost: " + nodeRepo.HMgetNode("B").getNeighbor(0));
		System.out.println("");

		//Receive prefix response updates 
		ArrayList<String> prefixes1 = new ArrayList<String>();
		prefixes1.add("B");
		prefixes1.add("prefix1B");
		prefixes1.add("prefix2B");
		PrefixListObj prefixListObj1 = new PrefixListObj(prefixes1, "B", true, "B2345");
		process.processPrefixListResponse(prefixListObj1);
		//do the fib entries exist 
		System.out.println("Is B in the fib: " + fib.doesHashMapContainPrefix(1, "B"));
		System.out.println("B's best cost advertiser: " + fib.getBestCostAdvertiser(1, "B"));
		System.out.println("Is prefix1B in the fib: " + fib.doesHashMapContainPrefix(1, "prefix1B"));
		System.out.println("prefix1B's best cost advertiser:" + fib.getBestCostAdvertiser(1, "prefix1B"));
		System.out.println("Is prefix2B in the fib: " + fib.doesHashMapContainPrefix(1, "prefix2B"));
		System.out.println("prefix2B's best cost advertiser:" + fib.getBestCostAdvertiser(1, "prefix2B"));
		System.out.println("");
	}

	public void modifyNode(){

		//receive modify node update
		ArrayList<NeighborAndCostStrings> neighbors2 = new ArrayList<NeighborAndCostStrings>();
		neighbors2.add(new NeighborAndCostStrings("F", 22));
		ModifyNodeObj modifyNodeObj2 = new ModifyNodeObj("B", neighbors2, "B3456");
		process.modifyNode(modifyNodeObj2, "B");
		System.out.println("Does node F exist in the graph: " + nodeRepo.HMdoesNodeExist("F"));
		System.out.println("Node F's bestcost: " + nodeRepo.HMgetNode("F").getBestCost());
		ArrayList<NeighborAndCostStrings> neighbors = nodeRepo.HMgetNode("B").getNeighbors();
		for(NeighborAndCostStrings neighbor : neighbors){
			System.out.println("B's neighbor and cost: " + neighbor);
		}
		System.out.println("");
	}

	public void modifyLink(){
		//modify Link 
		LinkObj linkObj4 = new LinkObj("B", 24);
		process.modifyLink(linkObj4);
		System.out.println("Does node B exist in the graph: " + nodeRepo.HMdoesNodeExist("B"));
		System.out.println("Node B's bestcost: " + nodeRepo.HMgetNode("B").getBestCost());
		System.out.println("Node F's bestcost: " + nodeRepo.HMgetNode("F").getBestCost());
		System.out.println("");
	}	

	public void removeLink(){

		//receive a remove link
		LinkObj linkObj5 = new LinkObj("B", 90);
		process.removeLink(linkObj5);
		System.out.println("Does node B exist in the graph: " + nodeRepo.HMdoesNodeExist("B"));
		System.out.println("Node B's best cost: " + nodeRepo.HMgetNode("B").getBestCost());
		System.out.println("Does node F exist in the graph: " + nodeRepo.HMdoesNodeExist("F"));
		System.out.println("Node F's best cost: " + nodeRepo.HMgetNode("F").getBestCost());
		System.out.println("Does directly connected router B exist: " + directlyConnectedNodes.doesDirectlyConnectedRouterExist("B"));
		System.out.println("Does fib entry B exist: " + fib.doesHashMapContainPrefix(1, "B"));
		System.out.println("Does fib entry prefixB1 exist: " + fib.doesHashMapContainPrefix(1, "prefix1B"));
		System.out.println("Does fib entry prefixB2 exist: " + fib.doesHashMapContainPrefix(1, "prefix2B"));


		//receive a add link
		LinkObj linkObj6 = new LinkObj("B", 50);
		process.addLink(linkObj6);
		System.out.println("Does node B exist in the graph: " + nodeRepo.HMdoesNodeExist("B"));
		System.out.println("Node B's best cost: " + nodeRepo.HMgetNode("B").getBestCost());
		System.out.println("Does node F exist in the graph: " + nodeRepo.HMdoesNodeExist("F"));
		System.out.println("Node F's best cost: " + nodeRepo.HMgetNode("F").getBestCost());
	}


	public void addClient(){
		System.out.println("-Add client-");

		//receive a add client 
		LinkObj linkObj7 = new LinkObj("client1", 0);
		process.addClientLink(linkObj7);
		System.out.println("Does directly connected client1 exist: " + directlyConnectedNodes.doesDirectlyConnectedClientExist("client1"));
		ArrayList<String> prefixes = directlyConnectedNodes.getDirectlyConnectedClient("client1").getPrefixArrayList();
		for(String prefix : prefixes){
			System.out.println("client1's prefix: " + prefix);
		}
		System.out.println("Does fib have client1 prefix: " + fib.doesHashMapContainPrefix(1, "client1"));
		//receive a add client 
		LinkObj linkObj8 = new LinkObj("client2", 0);
		process.addClientLink(linkObj8);
		System.out.println("Does directly connected client2 exist: " + directlyConnectedNodes.doesDirectlyConnectedClientExist("client2"));
		ArrayList<String> prefixes2 = directlyConnectedNodes.getDirectlyConnectedClient("client2").getPrefixArrayList();
		for(String prefix : prefixes2){
			System.out.println("directley connected client1's prefix: " + prefix);
		}
		System.out.println("Does fib have client1 prefix: " + fib.doesHashMapContainPrefix(1, "client2"));
	}

	public void addPrefixesToClient(){
		System.out.println("\n-Add prefixes to client-");
		//receive a prefix from client
		PrefixObj prefixObj3 = new PrefixObj("client1Prefix1", "clients1234", "client1", true);
		process.addCLientPrefix(prefixObj3, "client1");
		System.out.println("Does directly connected client1 exist: " + directlyConnectedNodes.doesDirectlyConnectedClientExist("client1"));
		ArrayList<String> prefixes2 = directlyConnectedNodes.getDirectlyConnectedClient("client1").getPrefixArrayList();
		for(String prefix : prefixes2){
			System.out.println("directley connected client1's prefix: " + prefix);
		}
		System.out.println("Does fib have client1Prefix1 prefix: " + fib.doesHashMapContainPrefix(1, "client1Prefix1"));


		//receive a prefix list from client
		ArrayList<String> prefixes4 = new ArrayList<String>();
		prefixes4.add("client1Prefix2");
		prefixes4.add("client1Prefix3");
		PrefixListObj prefixListObj2 = new PrefixListObj(prefixes4, "client1", true, "client12345");
		process.addClientPrefixList(prefixListObj2, "client1");
		System.out.println("Does directly connected client1 exist: " + directlyConnectedNodes.doesDirectlyConnectedClientExist("client1"));
		ArrayList<String> prefixes3 = directlyConnectedNodes.getDirectlyConnectedClient("client1").getPrefixArrayList();
		for(String prefix : prefixes3){
			System.out.println("directley connected client1's prefix: " + prefix);
		}
		System.out.println("Does fib have client1Prefix2 prefix: " + fib.doesHashMapContainPrefix(1, "client1Prefix2"));
		System.out.println("Does fib have client1Prefix3 prefix: " + fib.doesHashMapContainPrefix(1, "client1Prefix3"));

	}

	public void removeClient(){

		System.out.println("\n-Remove client-");
		//receive a remove client
		LinkObj linkObj9 = new LinkObj("client1", 0);
		process.removeClientLink(linkObj9);
		System.out.println("Does directly connected client1 exist: " + directlyConnectedNodes.doesDirectlyConnectedClientExist("client1"));
		System.out.println("Does fib have client1prefix: " + fib.doesHashMapContainPrefix(1, "client1"));
		System.out.println("Does fib have client1Prefix1 prefix: " + fib.doesHashMapContainPrefix(1, "client1Prefix1"));
		System.out.println("Does fib have client1Prefix2 prefix: " + fib.doesHashMapContainPrefix(1, "client1Prefix2"));
		System.out.println("Does fib have client1Prefix3 prefix: " + fib.doesHashMapContainPrefix(1, "client1Prefix3"));
	}

	public void prefixUpdate(){

		System.out.println("\n-prefixUpdate-");

		//receive a prefix update
		PrefixObj prefixObj4 = new PrefixObj("prefix3B", "B5678", "B", true);
		process.addPrefix(prefixObj4, "B");
		System.out.println("does prefix3B exist in fib: " + fib.doesHashMapContainPrefix(1, "prefix3B"));

		//receive a prefix update
		PrefixObj prefixObj5 = new PrefixObj("prefix3B", "B5678", "B", false);
		process.removePrefix(prefixObj5, "B");
		System.out.println("does prefix3B exist in fib: " + fib.doesHashMapContainPrefix(1, "prefix3B"));

		//receive a prefix List update
		ArrayList<String> prefixes5 = new ArrayList<String>();
		prefixes5.add("prefix5B/video");
		prefixes5.add("prefix6B/games");
		PrefixListObj prefixListObj3 = new PrefixListObj(prefixes5, "B", true, "B7891");
		process.addPrefixList(prefixListObj3, "B");
		System.out.println("does prefix5B/video exist in fib: " + fib.doesHashMapContainPrefix(2, "prefix5B/video"));
		System.out.println("does prefix6B/games exist in fib: " + fib.doesHashMapContainPrefix(2, "prefix6B/games"));

	}

	/*
	 * removing a node does not require an update be sent
	 */





}
