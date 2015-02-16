package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OverlayNodeSendsRegistration {
	String ipAddress;
	int port;
	byte[] marshalledBytes;
	
	public OverlayNodeSendsRegistration(String ipAddress, int port) throws IOException {
		this.ipAddress = ipAddress;
		this.port = port;
		
		byte[] ipData = ipAddress.getBytes();
		int ipLength = ipData.length;
		
		marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout2 = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout2.writeInt(2);
		dout2.writeInt(ipLength);
		dout2.write(ipData);
		dout2.writeInt(port);
		dout2.flush();
		
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout2.close();
	}
	
	public OverlayNodeSendsRegistration(byte[] marshalledBytes) throws IOException {
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type = din.readInt();
		int ipLength = din.readInt();
		byte[] ipAddressBytes = new byte[ipLength];
		din.readFully(ipAddressBytes);
		ipAddress = new String(ipAddressBytes);
		port = din.readInt();
		
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
}
