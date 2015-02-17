package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistryReportsRegistrationStatus {
	byte[] marshalledBytes;
	int ID;
	String info;
	
	public RegistryReportsRegistrationStatus(byte[] marshalledBytes) throws IOException {
		this.marshalledBytes = marshalledBytes;
		
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type = din.readInt();
		ID = din.readInt();
		int infoLength = din.readInt();
		byte[] infoBytes = new byte[infoLength];
		din.readFully(infoBytes);
		info = new String(infoBytes);
		
		baInputStream.close();
		din.close();
	}
	
	public RegistryReportsRegistrationStatus(int ID, String info) throws IOException {
		this.ID  = ID;
		this.info = info;
		marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout2 = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout2.writeInt(3);
		dout2.writeInt(ID);
		
		byte[] infoBytes = info.getBytes();
		dout2.writeInt(infoBytes.length);
		dout2.write(infoBytes);
		dout2.flush();
		
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout2.close();
	}
	
	public byte[] getBytes() {
		return marshalledBytes;
	}
	
	public int getID() {
		return ID;
	}
	
	public String getInfo() {
		return info;
	}
}
