package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OverlayNodeReportsTrafficSummary {
	int ID;
	int packetsSent;
	int packetsRelayed;
	int packetsReceived;
	long sumSent;
	long sumReceived;
	byte[] marshalledBytes;
	
	public OverlayNodeReportsTrafficSummary(byte[] data) throws IOException {
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type = din.readInt();
		ID = din.readInt();
		packetsSent = din.readInt();
		packetsRelayed = din.readInt();
		sumSent = din.readLong();
		packetsReceived = din.readInt();
		sumReceived = din.readLong();
		
		baInputStream.close();
		din.close();
	}
	public OverlayNodeReportsTrafficSummary(int ID, int packetsSent, int packetsReceived, int packetsRelayed, long sumSent, long sumReceived) throws IOException {
		this.ID = ID;
		this.packetsSent = packetsSent;
		this.packetsRelayed = packetsRelayed;
		this.packetsReceived = packetsReceived;
		this.sumSent = sumSent;
		this.sumReceived = sumReceived;
		
		marshalledBytes = null;
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(12);
		dout.writeInt(ID);
		dout.writeInt(packetsSent);
		dout.writeInt(packetsRelayed);
		dout.writeLong(sumSent);
		dout.writeInt(packetsReceived);
		dout.writeLong(sumReceived);
		dout.flush();
		
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
	}
	public int getID() {
		return ID;
	}
	public int getPacketsSent() {
		return packetsSent;
	}
	public int getPacketsRelayed() {
		return packetsRelayed;
	}
	public int getPacketsReceived() {
		return packetsReceived;
	}
	public long getSumSent() {
		return sumSent;
	}
	public long getSumReceived() {
		return sumReceived;
	}
	public byte[] getMarshalledBytes() {
		return marshalledBytes;
	}
}
