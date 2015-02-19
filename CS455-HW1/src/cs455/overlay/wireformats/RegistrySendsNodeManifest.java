package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistrySendsNodeManifest {
	byte[] marshalledBytes;
	int ID1, ID2, ID3;
	String ipAddress1, ipAddress2, ipAddress3;
	int port1, port2, port3;
	int routingTableSize;
	int totalNodes;
	int[] nodeIDs;
	
	public RegistrySendsNodeManifest(byte[] data) throws IOException {
		marshalledBytes = data;
		
		ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
		DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
		
		int type = din.readInt();
		routingTableSize = din.readInt();
		
		ID1 = din.readInt();
		int ipLength1 = din.readInt();
		byte[] ipAddressBytes1 = new byte[ipLength1];
		din.readFully(ipAddressBytes1);
		ipAddress1 = new String(ipAddressBytes1);
		port1 = din.readInt();
		
		ID2 = din.readInt();
		int ipLength2 = din.readInt();
		byte[] ipAddressBytes2 = new byte[ipLength2];
		din.readFully(ipAddressBytes2);
		ipAddress2 = new String(ipAddressBytes2);
		port2 = din.readInt();
		
		ID3 = din.readInt();
		int ipLength3 = din.readInt();
		byte[] ipAddressBytes3 = new byte[ipLength3];
		din.readFully(ipAddressBytes3);
		ipAddress3 = new String(ipAddressBytes3);
		port3 = din.readInt();
		
		totalNodes = din.readInt();
		nodeIDs = new int[totalNodes];
		for (int i = 0; i < totalNodes; i++) {
			nodeIDs[i] = din.readInt();
		}
		
		baInputStream.close();
		din.close();
	}
	
	public RegistrySendsNodeManifest(int ID1, int ID2, int ID3, String ipAddress1, String ipAddress2, String ipAddress3,
			int port1, int port2, int port3, int routingTableSize, int totalNodes, int[] nodeIDs) throws IOException {
		this.ID1 = ID1;
		this.ID2 = ID2;
		this.ID3 = ID3;
		this.ipAddress1 = ipAddress1;
		this.ipAddress2 = ipAddress2;
		this.ipAddress3 = ipAddress3;
		this.port1 = port1;
		this.port2 = port2;
		this.port3 = port3;
		this.routingTableSize = routingTableSize;
		this.totalNodes = totalNodes;
		this.nodeIDs = nodeIDs;
		marshalledBytes = null;
		
		ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
		
		dout.writeInt(6);
		dout.writeInt(routingTableSize);
		
		dout.writeInt(ID1);
		byte[] ipAddress1Bytes = ipAddress1.getBytes();
		dout.writeInt(ipAddress1Bytes.length);
		dout.write(ipAddress1Bytes);
		dout.writeInt(port1);
		
		dout.writeInt(ID2);
		byte[] ipAddress2Bytes = ipAddress2.getBytes();
		dout.writeInt(ipAddress2Bytes.length);
		dout.write(ipAddress2Bytes);
		dout.writeInt(port2);
		
		dout.writeInt(ID3);
		byte[] ipAddress3Bytes = ipAddress3.getBytes();
		dout.writeInt(ipAddress3Bytes.length);
		dout.write(ipAddress3Bytes);
		dout.writeInt(port3);
		
		dout.writeInt(totalNodes);
		for (int i = 0; i < nodeIDs.length; i++) {
			dout.writeInt(nodeIDs[i]);
		}
		
		dout.flush();
		
		marshalledBytes = baOutputStream.toByteArray();
		baOutputStream.close();
		dout.close();
	}

	public int getID1() {
		return ID1;
	}

	public int getID2() {
		return ID2;
	}

	public int getID3() {
		return ID3;
	}

	public String getIpAddress1() {
		return ipAddress1;
	}

	public String getIpAddress2() {
		return ipAddress2;
	}

	public String getIpAddress3() {
		return ipAddress3;
	}

	public int getPort1() {
		return port1;
	}

	public int getPort2() {
		return port2;
	}

	public int getPort3() {
		return port3;
	}

	public int getRoutingTableSize() {
		return routingTableSize;
	}

	public int getTotalNodes() {
		return totalNodes;
	}

	public int[] getNodeIDs() {
		return nodeIDs;
	}

	public byte[] getBytes() {
		return marshalledBytes;
	}
}
