package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistryRequestsTaskInitiate {
	byte[] marshalledBytes;
	int packets;
	public RegistryRequestsTaskInitiate(byte[] data) throws IOException {
		marshalledBytes = data;
		
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type = din.readInt();
		packets = din.readInt();
		
		baInputStream.close();
		din.close();
	}
	public RegistryRequestsTaskInitiate(int packets) throws IOException {
		this.packets = packets;
		
		marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout2 = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout2.writeInt(8);
		dout2.writeInt(packets);
		dout2.flush();
		
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout2.close();
	}
	public byte[] getMarshalledBytes() {
		return marshalledBytes;
	}
	public int getPackets() {
		return packets;
	}
}
