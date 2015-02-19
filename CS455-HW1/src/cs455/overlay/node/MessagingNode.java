package cs455.overlay.node;

import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
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
	
	private static int ID;
	private static RoutingEntry registry;
	
	//private Queue<OverlayNodeSendsData> relayQueue = new LinkedList<OverlayNodeSendsData>();
	
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
	
	public static void connectToNode(String ipAddress, int port, int ID) throws UnknownHostException, IOException {
		Socket socket = new Socket(ipAddress, port);
		DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
		DataInputStream din = new DataInputStream(socket.getInputStream());
		RoutingEntry node = new RoutingEntry(ipAddress, port, ID, socket, dout, din);
		int index = 0;
		
		for (int i = 0; i < routingTable.size(); i++) {
			if (routingTable.get(i).getID() < ID) {
				index = i + 1;
			}
		}
		if (index >= routingTable.size()) {
			routingTable.add(node);
		}else {
			routingTable.add(index, node);
		}
		OverlayNodeSendsData overlaynode = new OverlayNodeSendsData("Hi, this is node " + getID());
		sendData(overlaynode.getBytes(), node);
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
						System.out.println("Hello");
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
						routingTable.add(entry);
						
						socket = new Socket(nodeManifest.getIpAddress3(), nodeManifest.getPort3());
						dout = new DataOutputStream(socket.getOutputStream());
						din = new DataInputStream(socket.getInputStream());
						entry = new RoutingEntry(nodeManifest.getIpAddress3(), nodeManifest.getPort3(), nodeManifest.getID3(), 
								socket, dout, din);
						routingTable.add(entry);
						
						System.out.println("Routing Table:");
						System.out.println("ipAddress: " + routingTable.get(0).getIpAddress() + " port: " + routingTable.get(0).getPort() + 
								" ID: " + routingTable.get(0).getID());
						System.out.println("ipAddress: " + routingTable.get(1).getIpAddress() + " port: " + routingTable.get(1).getPort() + 
								" ID: " + routingTable.get(1).getID());
						System.out.println("ipAddress: " + routingTable.get(2).getIpAddress() + " port: " + routingTable.get(2).getPort() + 
								" ID: " + routingTable.get(2).getID());
						break;
					case RegistryRequestsTaskInitiate:
						break;
					case RegistryRequestsTrafficSummary:
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
								System.out.println(overlayNode.getInfo());
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
				case "connect":
					String ipAddress = splitMessage[1];
					int port = Integer.parseInt(splitMessage[2]);
					connectToNode(ipAddress, port, 129);
					break;
				default:
					System.out.println("Command could not be understood.");
					break;
			}
		}
	}
}
