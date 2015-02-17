package cs455.overlay.routing;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class RoutingEntry {
	String ipAddress;
	int port;
	int ID;
	Socket socket;
	DataOutputStream dout;
	DataInputStream din;
	
	public RoutingEntry(String ipAddress, int port, int ID, Socket socket, DataOutputStream dout, DataInputStream din) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.ID = ID;
		this.socket = socket;
		this.dout = dout;
		this.din = din;
	}
	public RoutingEntry(String ipAddress, int port, int ID) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.ID = ID;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	public int getPort() {
		return port;
	}
	public int getID() {
		return ID;
	}
	public Socket getSocket() {
		return socket;
	}
	public DataOutputStream getDout() {
		return dout;
	}
	public DataInputStream getDin() {
		return din;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public void setDout(DataOutputStream dout) {
		this.dout = dout;
	}
	public void setDin(DataInputStream din) {
		this.din = din;
	}
}
