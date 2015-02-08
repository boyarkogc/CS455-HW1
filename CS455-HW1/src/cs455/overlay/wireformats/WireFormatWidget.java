package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class WireFormatWidget {
	private int type;
	private long timestamp;
	private String identifier;
	private int tracker;
	
	public WireFormatWidget(byte[] marshalledBytes) throws IOException {
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledBytes);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		
		type = din.readInt();
		timestamp = din.readLong();
		
		int identifierLength = din.readInt();
		byte [] identifierBytes = new byte [identifierLength];
		din.readFully(identifierBytes);
		
		identifier = new String(identifierBytes);
		tracker = din.readInt();
		
		baInputStream.close();
		din.close();
	}
	
	public byte[] getBytes() throws IOException {
		byte[] marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(type);
		dout.writeLong(timestamp);
		
		byte[] identifierBytes = identifier.getBytes();
		int elementLength = identifierBytes.length;
		
		dout.writeInt(elementLength);
		dout.write(identifierBytes);
		dout.writeInt(tracker);
		dout.flush();
		
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
		
		return marshalledBytes;
	}
	
	public int getType() {
		return type;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public int getTracker() {
		return tracker;
	}
	
	public void setType(int t) {
		type = t;
	}
	
	public void setTimeStamp(long t) {
		timestamp = t;
	}
	
	public void setIdentifier(String i) {
		identifier = i;
	}
	
	public void setTracker (int t) {
		tracker = t;
	}
	
}
