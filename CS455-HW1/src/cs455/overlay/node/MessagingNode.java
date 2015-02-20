package cs455.overlay.node;

import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.io.*;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.wireformats.*;

public class MessagingNode extends Thread {
	private static final int RegistryReportsRegistrationStatus = 3;
	private static final int RegistryReportsDeregistrationStatus = 5;
	private static final int RegistrySendsNodeManifest = 6;
	private static final int RegistryRequestsTaskInitiate = 8;
	private static final int OverlayNodeSendsData = 9;
	private static final int RegistryRequestsTrafficSummary = 11;
	
	private static final int RegistryID = 128;
	
	private static ArrayList<RoutingEntry> routingTable;
	private static ArrayList<Integer> allIDs;
	
	private static int ID;
	private static RoutingEntry registry;
	
	static int sendTracker;
	static int receiveTracker;
	static int relayTracker;
	static long sendSummation;
	static long receiveSummation;
	
	private static Queue<OverlayNodeSendsData> relayQueue = new LinkedList<OverlayNodeSendsData>();
	
	public MessagingNode(Socket socket, int port) throws IOException {
		registry = new RoutingEntry(socket.getInetAddress().getHostAddress(), port, RegistryID, socket, 
				new DataOutputStream(socket.getOutputStream()), new DataInputStream(socket.getInputStream()));
		
		register();
	}
	
	public static int getID() {
		return ID;
	}
	
	public static void sendData(byte[] dataToSend, RoutingEntry dest) throws IOException {
		int dataLength = dataToSend.length;
		dest.getDout().writeInt(dataLength);
		dest.getDout().write(dataToSend, 0, dataLength);
		dest.getDout().flush();
	}
	
	public static void register() throws IOException {
		OverlayNodeSendsRegistration reg = new OverlayNodeSendsRegistration(InetAddress.getLocalHost().getHostAddress(),
				registry.getPort());
		
		sendData(reg.getBytes(), registry);
	}
	
	public static void deRegister() throws IOException {
		OverlayNodeSendsDeregistration dereg = new OverlayNodeSendsDeregistration(InetAddress.getLocalHost().getHostAddress(),
				registry.getSocket().getLocalPort(), ID);
		
		sendData(dereg.getBytes(), registry);
	}
	
	public synchronized static RoutingEntry getRoutingTableEntry(int ID) {
		int index = 0;
		for (int i = 0; i < routingTable.size(); i++) {
			if (routingTable.get(i).getID() <= ID) {
				index = i;
			}
		}
		return routingTable.get(index);
	}
	
	public void run() {
		int dataLength;
		int type;
		
		System.out.println("Node is running");
		
		while (registry.getSocket() != null) {
			try {
				dataLength = registry.getDin().readInt();
				byte[] data = new byte[dataLength];
				registry.getDin().readFully(data, 0, dataLength);
				
				ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
				DataInputStream din2 = new DataInputStream(new BufferedInputStream(baInputStream));
				//read the type of message(OverlayNodeSendsRegistration, OverlayNodeSendsDeregistration, etc.)
				type = din2.readInt();
				switch (type) {
					case RegistryReportsRegistrationStatus:
						
						System.out.println("Registry has sent registration status");
						
						RegistryReportsRegistrationStatus reg = new RegistryReportsRegistrationStatus(data);
						if (reg.getID() != -1) {
							ID = reg.getID();
						}
						System.out.println(reg.getInfo());
						break;
					case RegistryReportsDeregistrationStatus:
						System.out.println("Registry has sent deregistration status");
						
						RegistryReportsRegistrationStatus dereg = new RegistryReportsRegistrationStatus(data);
						if (dereg.getID() == -1) {
							ID = dereg.getID();
						}
						System.out.println(dereg.getInfo());	
						break;
					case RegistrySendsNodeManifest:
						for (int i = 0; i < routingTable.size(); i++) {
							routingTable.get(i).getDout().close();
							routingTable.get(i).getDin().close();
							routingTable.get(i).getSocket().close();
						}
						routingTable.clear();
						
						RegistrySendsNodeManifest nodeManifest = new RegistrySendsNodeManifest(data);
						
						Socket socket = new Socket(nodeManifest.getIpAddress1(), nodeManifest.getPort1());
						DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
						DataInputStream din = new DataInputStream(socket.getInputStream());
						RoutingEntry entry = new RoutingEntry(nodeManifest.getIpAddress1(), nodeManifest.getPort1(), nodeManifest.getID1(), 
								socket, dout, din);
						routingTable.add(entry);
						
						socket = new Socket(nodeManifest.getIpAddress2(), nodeManifest.getPort2());
						dout = new DataOutputStream(socket.getOutputStream());
						din = new DataInputStream(socket.getInputStream());
						entry = new RoutingEntry(nodeManifest.getIpAddress2(), nodeManifest.getPort2(), nodeManifest.getID2(), 
								socket, dout, din);
						if (routingTable.get(0).getID() > entry.getID()) {
							routingTable.add(0, entry);
						}else {
							routingTable.add(entry);
						}
						
						socket = new Socket(nodeManifest.getIpAddress3(), nodeManifest.getPort3());
						dout = new DataOutputStream(socket.getOutputStream());
						din = new DataInputStream(socket.getInputStream());
						entry = new RoutingEntry(nodeManifest.getIpAddress3(), nodeManifest.getPort3(), nodeManifest.getID3(), 
								socket, dout, din);
						if (routingTable.get(0).getID() > entry.getID()) {
							if (routingTable.get(1).getID() > entry.getID()) {
								routingTable.add(entry);
							}
							routingTable.add(1, entry);
						}else {
							routingTable.add(0, entry);
						}
						
						/*System.out.println("Routing Table:");
						System.out.println("ipAddress: " + routingTable.get(0).getIpAddress() + " port: " + routingTable.get(0).getPort() + 
								" ID: " + routingTable.get(0).getID());
						System.out.println("ipAddress: " + routingTable.get(1).getIpAddress() + " port: " + routingTable.get(1).getPort() + 
								" ID: " + routingTable.get(1).getID());
						System.out.println("ipAddress: " + routingTable.get(2).getIpAddress() + " port: " + routingTable.get(2).getPort() + 
								" ID: " + routingTable.get(2).getID());*/
						
						for (int i = 0; i < nodeManifest.getTotalNodes(); i++) {
							allIDs.add(nodeManifest.getNodeIDs()[i]);
						}
						
						NodeReportsOverlaySetupStatus report = new NodeReportsOverlaySetupStatus(ID, "Initiation successful");
						sendData(report.getMarshalledBytes(), registry);
						break;
					case RegistryRequestsTaskInitiate:
						RegistryRequestsTaskInitiate taskInitiate = new RegistryRequestsTaskInitiate(data);
						int packets = taskInitiate.getPackets();
						new DataRouter(packets).start();
						break;
					case RegistryRequestsTrafficSummary:
						OverlayNodeReportsTrafficSummary summary = new OverlayNodeReportsTrafficSummary(ID, sendTracker, receiveTracker,
								relayTracker, sendSummation, receiveSummation);
						sendData(summary.getMarshalledBytes(), registry);
						break;
				}
				baInputStream.close();
				din2.close();
			
			}catch(SocketException se) {
				System.out.println(se.getMessage());
				break;
			}catch(IOException ioe) {
				System.out.println(ioe.getMessage());
				break;
			}
		}
	}
	
	public static void main (String args[]) throws NumberFormatException, IOException {
		routingTable = new ArrayList<RoutingEntry>();
		allIDs = new ArrayList<Integer>();
		Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
		
		ServerSocket nodeServer = new ServerSocket(0);
		MessagingNode msg = new MessagingNode(socket, nodeServer.getLocalPort());
		NodeCommandParser parser = new NodeCommandParser();
		msg.start();
		parser.start();
		
		try {
            while (true) {
                new NodeServerThread(nodeServer.accept()).start();
            }
        }finally {
            nodeServer.close();
        }
	}
	
	private static class NodeServerThread extends Thread {
		private Socket socket;
		private DataInputStream din;
		
		public NodeServerThread(Socket socket) {
			this.socket = socket;
		}
		
		public void run() {
			try {
				din = new DataInputStream(socket.getInputStream());//stream that receives data from node
				int type;//variable to store the message type
				int dataLength;//length of the message
				
				System.out.println("Node server is running");
				
				while (socket != null) {
					try {
						//read data into a byte array
						dataLength = din.readInt();
						byte[] data = new byte[dataLength];
						din.readFully(data, 0, dataLength);
						//create new streams to read data from the byte array
						ByteArrayInputStream baInputStream = new ByteArrayInputStream(data);
						DataInputStream din2 = new DataInputStream(new BufferedInputStream(baInputStream));
						//read the type of message(OverlayNodeSendsRegistration, OverlayNodeSendsDeregistration, etc.)
						type = din2.readInt();
						//do different things depending on the type of message received
						switch (type) {
							case OverlayNodeSendsData:
								OverlayNodeSendsData overlayNode = new OverlayNodeSendsData(data);
								synchronized (relayQueue) {
									relayQueue.add(overlayNode);
								}
								break;
						}
						baInputStream.close();
						din2.close();
						
					}catch(SocketException se) {
						System.out.println(se.getMessage());
						break;
					}catch(IOException ioe) {
						System.out.println(ioe.getMessage());
						break;
					}
				}
				
			}catch(Exception e) {
				
			}finally {
				
			}
		}
	}
	
	private static class DataRouter extends Thread {
		Random rand;
		int packetsToSend;
		int count;
		
		OverlayNodeSendsData data;
		
		public DataRouter(int packets) {
			sendTracker = 0;
			receiveTracker = 0;
			relayTracker = 0;
			sendSummation = 0;
			receiveSummation = 0;
			rand = new Random();
			packetsToSend = packets;
			count = 0;
			
			data = new OverlayNodeSendsData();
		}
		
		public void run() {
			while (count < 25) {
				synchronized(relayQueue) {
					data = relayQueue.poll();
				}
				if (data == null) {
					if (sendTracker < packetsToSend) {
						try {
							sendNewData();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else {
						count++;
						try {
							this.sleep(rand.nextInt(1000));
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else {
					count = 0;
					if (data.getDestID() == ID) {
						receiveTracker++;
						receiveSummation+=data.getPayload();
						//System.out.println("Received packet from " + data.getSourceID());
					}else {
						relayTracker++;
						//System.out.println("Relayed packet from " + data.getSourceID() + " to " + data.getDestID());
						try {
							relay(appendTrace(data));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			OverlayNodeReportsTaskFinished done;
			try {
				done = new OverlayNodeReportsTaskFinished(registry.getIpAddress(), registry.getPort(), ID);
				sendData(done.getMarshalledBytes(), registry);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		private void sendNewData() throws IOException {
			int payload = rand.nextInt();
			int sign = rand.nextInt(2);
			int dest = allIDs.get(rand.nextInt(allIDs.size()));
			while (dest == ID) {
				dest = allIDs.get(rand.nextInt(allIDs.size()));
			}
			int source = ID;
			int[] trace = {ID};
			if (sign == 1) {
				payload = payload * -1;
			}
			sendTracker++;
			sendSummation += payload;
			
			OverlayNodeSendsData dataToSend = new OverlayNodeSendsData(dest, source, payload, trace);
			relay(dataToSend);
			
			//System.out.println("Sent packet to " + dest);
		}
		
		private OverlayNodeSendsData appendTrace(OverlayNodeSendsData data) throws IOException {
			int[] trace = new int[data.getTraceIDs().length + 1];
			for (int i = 0; i < trace.length - 1; i++) {
				trace[i] = data.getTraceIDs()[i];
			}
			trace[trace.length -1] = ID;
			OverlayNodeSendsData newData = new OverlayNodeSendsData(data.getDestID(), data.getSourceID(), data.getPayload(), trace);
			return newData;
		}
		
		private void relay(OverlayNodeSendsData dataToSend) throws IOException {
			if (routingTable.get(0).getID() == dataToSend.getDestID()) {
				sendData(dataToSend.getMarshalledBytes(), routingTable.get(0));
			}else if (routingTable.get(1).getID() <= dataToSend.getDestID()) {
				if (routingTable.get(2).getID() <= dataToSend.getDestID()) {
					sendData(dataToSend.getMarshalledBytes(), routingTable.get(2));
				}else {
					sendData(dataToSend.getMarshalledBytes(), routingTable.get(1));
				}
			}else {
				sendData(dataToSend.getMarshalledBytes(), routingTable.get(2));
			}
		}
	}
	private static class NodeCommandParser extends Thread {
		public NodeCommandParser() {}
		
		public void run() {
			try {
				BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
				String message = "";
				while (true) {
					message = fromConsole.readLine();
					handleInput(message);
				}
			}
			catch (Exception ex) {
				System.out.println(ex.toString() + " Unexpected error while reading from console!");
			}
		}
		
		public void handleInput(String message) throws IOException {
		    String[] splitMessage = message.split(" ");
		    String first = splitMessage[0];
		    
			switch (first) {
				case "print-counters-and-diagnostics":
					break;
				case "exit-overlay":
					deRegister();
					break;
				default:
					System.out.println("Command could not be understood.");
					break;
			}
		}
	}
}
