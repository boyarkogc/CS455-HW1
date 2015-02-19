package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class OverlayNodeSendsData {
	int sourceID;
	int destID;
	int payload;
	int[] traceIDs;
	
	byte[] marshalledBytes;
	
	public OverlayNodeSendsData(byte[] data) throws IOException {
		marshalledBytes = data;
		
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type = din.readInt();
		destID = din.readInt();
		sourceID = din.readInt();
		payload = din.readInt();
		
		int traceLength = din.readInt();
		traceIDs = new int[traceLength];
		for (int i = 0; i < traceLength; i++) {
			traceIDs[i] = din.readInt();
		}
		
		baInputStream.close();
		din.close();
	}
	
	public OverlayNodeSendsData(int sourceID, int destID, int payload, int[]traceIDs) throws IOException {
		marshalledBytes = null;
		
		this.sourceID = sourceID;
		this.destID = destID;
		this.payload = payload;
		this.traceIDs = traceIDs;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(9);
		dout.writeInt(destID);
		dout.writeInt(sourceID);
		dout.writeInt(payload);
		dout.writeInt(traceIDs.length);
		for (int i = 0; i < traceIDs.length; i++) {
			dout.writeInt(traceIDs[i]);
		}
		dout.flush();
		
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
	}
	
	public OverlayNodeSendsData() {
		
	}

	public int getSourceID() {
		return sourceID;
	}

	public int getDestID() {
		return destID;
	}

	public int getPayload() {
		return payload;
	}

	public int[] getTraceIDs() {
		return traceIDs;
	}

	public byte[] getMarshalledBytes() {
		return marshalledBytes;
	}
}
