package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NodeReportsOverlaySetupStatus {
	byte[] marshalledBytes;
	int successStatus;
	String info;
	
	public NodeReportsOverlaySetupStatus(byte[] data) throws IOException {
		marshalledBytes = data;
		
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type = din.readInt();
		successStatus = din.readInt();
		int infoLength = din.readInt();
		byte[] infoBytes = new byte[infoLength];
		din.readFully(infoBytes);
		info = new String(infoBytes);
		
		baInputStream.close();
		din.close();
	}
	
	public NodeReportsOverlaySetupStatus(int successStatus, String info) throws IOException {
		this.successStatus = successStatus;
		this.info = info;
		
		byte[] infoData = info.getBytes();
		int infoLength = infoData.length;
		
		marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(7);
		dout.writeInt(successStatus);
		dout.writeInt(infoLength);
		dout.write(infoData);
		dout.flush();
		
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
	}

	public byte[] getMarshalledBytes() {
		return marshalledBytes;
	}

	public int getSuccessStatus() {
		return successStatus;
	}

	public String getInfo() {
		return info;
	}
	
	
}
