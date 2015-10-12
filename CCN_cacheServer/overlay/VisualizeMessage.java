package overlay;

import java.io.Serializable;

public class VisualizeMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3068854244453067126L;
	String sourceID;
	int type; // 1=neighbours,2=request,3=respone
	Object message;
	int machineType;

	public String getSourceID() {
		return sourceID;
	}

	public void setSourceID(String sourceID) {
		this.sourceID = sourceID;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public int getMachineType() {
		return machineType;
	}

	public void setMachineType(int machineType) {
		this.machineType = machineType;
	}

	public VisualizeMessage(String sourceID, int type, int machineType, Object message) {
		// TODO Auto-generated constructor stub
		this.sourceID = sourceID;
		this.type = type;
		this.machineType = machineType;
		this.message = message;

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "id:" + sourceID + ";type:" + type + ";message:" + message + ";";
	}

}
