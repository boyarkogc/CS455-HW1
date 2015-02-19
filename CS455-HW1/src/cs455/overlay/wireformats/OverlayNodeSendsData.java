package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OverlayNodeSendsData {
	String info;
	byte[] marshalledBytes;
	
	public OverlayNodeSendsData(byte[] data) throws IOException {
		marshalledBytes = data;
		
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type = din.readInt();
		int infoLength = din.readInt();
		byte[] infoBytes = new byte[infoLength];
		din.readFully(infoBytes);
		info = new String(infoBytes);
		
		baInputStream.close();
		din.close();
	}
	
	public OverlayNodeSendsData(String info) throws IOException {
		this.info = info;
		marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout2 = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout2.writeInt(9);
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
	
	public String getInfo() {
		return info;
	}
}
