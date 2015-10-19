package overlay;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;

public abstract class Link extends Thread {
	ObjectInputStream ois = null;
	String connectedTo;
	boolean running;
	int type; // 1 - client, 2 - server, 3 - cache server

	public Link( ObjectInputStream ois, int type) throws IOException {
		this.ois = ois;
		running = true;
		this.type = type;
	}
	
	public abstract void handleUpdate(Message m) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException;
	

}
