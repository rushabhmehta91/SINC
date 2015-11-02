package overlay;

import java.io.Serializable;
import java.util.Set;

public class NodeDetails implements Serializable {
	/**
		 * 
		 */
	private static final long serialVersionUID = 8472322575115720397L;
	String ip;
	String id;
	String neighbours;
	String contentStore;
	int machineType;

	public NodeDetails(String ID, String IP, String neigh, int machineType, String contentStore) {
		this.id = ID;
		this.ip = IP;
		this.neighbours = neigh;
		this.machineType=machineType;
		this.contentStore=contentStore;

	}

	public int getMachineType() {
		return machineType;
	}

	public void setMachineType(int machineType) {
		this.machineType = machineType;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNeighbours() {
		return neighbours;
	}

	public void setNeighbours(String neighbours) {
		this.neighbours = neighbours;
	}

	public String getContentStore() {
		return contentStore;
	}

	public void setContentStore(String contentStore) {
		this.contentStore = contentStore;
	}

}
