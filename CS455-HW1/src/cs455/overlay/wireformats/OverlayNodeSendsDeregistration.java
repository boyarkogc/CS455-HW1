package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OverlayNodeSendsDeregistration {
	String ipAddress;
	int port;
	int ID;
	byte[] marshalledBytes;
	
	public OverlayNodeSendsDeregistration(String ipAddress, int port, int ID) throws IOException {
		this.ipAddress = ipAddress;
		this.port = port;
		this.ID = ID;
		
		byte[] ipData = ipAddress.getBytes();
		int ipLength = ipData.length;
		
		marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(4);
		dout.writeInt(ipLength);
		dout.write(ipData);
		dout.writeInt(port);
		dout.writeInt(ID);
		dout.flush();
		
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
	}
	
	public OverlayNodeSendsDeregistration(byte[] data) throws IOException {
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type = din.readInt();
		int ipLength = din.readInt();
		byte[] ipAddressBytes = new byte[ipLength];
		din.readFully(ipAddressBytes);
		ipAddress = new String(ipAddressBytes);
		port = din.readInt();
		ID = din.readInt();
		
		baInputStream.close();
		din.close();
	}
	
	public byte[] getBytes() throws IOException {
		return marshalledBytes;
	}
	
	public String getipAddress() {
		return ipAddress;
	}
	
	public int getPort() {
		return port;
	}
	
	public int getID() {
		return ID;
	}
}