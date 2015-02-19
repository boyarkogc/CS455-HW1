package cs455.overlay.node;

import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.io.*; 

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.wireformats.*;

public class Registry {
	private static final int OverlayNodeSendsRegistration = 2;
	private static final int OverlayNodeSendsDeregistration = 4;
	private static final int NodeReportsOverlaySetupStatus = 7;
	private static final int OverlayNodeReportsTaskFinished = 10;
	private static final int OverlayNodeReportsTrafficSummary = 12;
	
	private static LinkedList<RoutingEntry> overlay;
	private static ArrayList<Integer> unusedIDs;
	//private static ArrayList<RegistryThread> threads;
	
	private static Random rand;
	
	private static Registry registry = new Registry();
	
	private Registry() {
		
	}
	
	private synchronized static int getUnusedID() {
		int randomNum = rand.nextInt((unusedIDs.size()));
		int ID = unusedIDs.get(randomNum);
		unusedIDs.remove(randomNum);
		return ID;
	}
	
	private synchronized static boolean inRegistry(String ipAddress, int port) {
		for (int i = 0; i < overlay.size(); i++) {
			if (overlay.get(i).getIpAddress().equals(ipAddress) && overlay.get(i).getPort() == port) {
				return true;
			}
		}
		return false;
	}
	
	private synchronized static void addToOverlay(String ipAddress, int port, int ID, Socket socket, DataOutputStream dout, DataInputStream din) {
		RoutingEntry node = new RoutingEntry(ipAddress, port, ID, socket, dout, din);
		int index = 0;
		for (int i = 0; i < overlay.size(); i++) {
			if (overlay.get(i).getID() < ID) {
				index = i + 1;
			}
		}
		if (index >= overlay.size()) {
			overlay.add(node);
		}else {
			overlay.add(index, node);
		}
	}
	
	private synchronized static void removeFromRegistry(String ipAddress, int port, int ID) {
		for (int i = 0; i < overlay.size(); i++) {
			if (overlay.get(i).getIpAddress().equals(ipAddress) && overlay.get(i).getPort() == port) {
				overlay.remove(i);
			}
		}
	}
	
	public static Registry getInstance() {
		return registry;
	}
	
	private synchronized static void setupOverlay() throws IOException {
		int ID1, ID2, ID3;
		String ipAddress1, ipAddress2, ipAddress3;
		int port1, port2, port3;
		int routingTableSize = 3;
		
		DataOutputStream dout;
		
		int totalNodes = overlay.size();
		int[] nodeIDs = new int[overlay.size()];
		
		for (int i = 0; i < overlay.size(); i++) {
			nodeIDs[i] = overlay.get(i).getID();
		}
		
		for (int j = 0; j < overlay.size(); j++) {
			int counter = j;
			dout = overlay.get(j).getDout();
			
			counter = (j + 1) % overlay.size();
			ID1 = overlay.get(counter).getID();
			ipAddress1 = overlay.get(counter).getIpAddress();
			port1 = overlay.get(counter).getPort();
			
			counter = (j + 2) % overlay.size();
			ID2 = overlay.get(counter).getID();
			ipAddress2 = overlay.get(counter).getIpAddress();
			port2 = overlay.get(counter).getPort();
			
			counter = (j + 4) % overlay.size();
			ID3 = overlay.get(counter).getID();
			ipAddress3 = overlay.get(counter).getIpAddress();
			port3 = overlay.get(counter).getPort();
			
			RegistrySendsNodeManifest routingTable = new RegistrySendsNodeManifest(ID1, ID2, ID3, 
					ipAddress1, ipAddress2, ipAddress3, port1, port2, port3, routingTableSize, totalNodes, nodeIDs);
			
			dout.writeInt(routingTable.getBytes().length);
			dout.write(routingTable.getBytes());
			dout.flush();
			System.out.println("Hi");
		}
	}
	
	/*protected static void demoMethod() {
		///////
	}*/
	public static void main(String[] args) throws Exception {
		Registry reg = Registry.getInstance();
		overlay = new LinkedList<RoutingEntry>();
		unusedIDs = new ArrayList<Integer>();
		rand = new Random();
		
		for (int i = 0; i < 128; i++) {
			unusedIDs.add(i);
		}
        ServerSocket registry = new ServerSocket(4444);
        RegistryCommandParser parser = new RegistryCommandParser();
        parser.start();
        
		System.out.println("Registry is running");
        try {
            while (true) {
            	new RegistryThread(registry.accept()).start();
            	//threads.add(thread);
            }
        }finally {
            registry.close();
        }
	}
	
	private static class RegistryThread extends Thread {
		private int ID;
		private Socket socket;
		private DataInputStream din;
		private DataOutputStream dout;
		
		/**
		* Constructs a handler thread, squirreling away the socket.
		* All the interesting work is done in the run method.
		*/
		public RegistryThread(Socket socket) {
			this.socket = socket;
		}
		
		public void sendData(byte[] dataToSend) throws IOException {
			int dataLength = dataToSend.length;
			dout.writeInt(dataLength);
			dout.write(dataToSend, 0, dataLength);
			dout.flush();
		}
		
		public void run() {
			try {
				din = new DataInputStream(socket.getInputStream());//stream that receives data from node
				dout = new DataOutputStream(socket.getOutputStream());//stream that sends data to node
				int type;//variable to store the message type
				int dataLength;//length of the message
				
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
							//read the node's IP and port, and if they aren't in the overlay, add them
							//and let them know they were added; also send them their assigned ID
							case OverlayNodeSendsRegistration:
								OverlayNodeSendsRegistration reg = new OverlayNodeSendsRegistration(data);
								if (inRegistry(reg.getipAddress(), reg.getPort())) {
									RegistryReportsRegistrationStatus status = new RegistryReportsRegistrationStatus(-1, "Registration request " +
										"unsuccessful. You are already registered within the overlay.");
									sendData(status.getBytes());
								}else {
									ID = getUnusedID();
									addToOverlay(reg.getipAddress(), reg.getPort(), ID, socket, 
											new DataOutputStream(socket.getOutputStream()), new DataInputStream(socket.getInputStream()));
									RegistryReportsRegistrationStatus status = new RegistryReportsRegistrationStatus(ID, "Registration request " +
										"successful. The number of nodes currently constituting the overlay is " + overlay.size());
									sendData(status.getBytes());
								}
								break;
							//if this node was already in the overlay, remove it and let it know it was removed
							//otherwise, send them an error message
							case OverlayNodeSendsDeregistration:
								OverlayNodeSendsDeregistration dereg = new OverlayNodeSendsDeregistration(data);
								if (inRegistry(dereg.getipAddress(), dereg.getPort())) {
									removeFromRegistry(dereg.getipAddress(), dereg.getPort(), dereg.getID());
									RegistryReportsDeregistrationStatus status2 = new RegistryReportsDeregistrationStatus(ID, "Deregistration request " +
											"successful. You have been removed from the overlay.");
									sendData(status2.getBytes());
								}else {
									ID = -1;
									RegistryReportsDeregistrationStatus status2 = new RegistryReportsDeregistrationStatus(ID, "Deregistration request " +
										"unsuccessful. You were not in the overlay.");
									sendData(status2.getBytes());
								}
								break;
							case NodeReportsOverlaySetupStatus:
								break;
							case OverlayNodeReportsTaskFinished:
								break;
							case OverlayNodeReportsTrafficSummary:
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
	
	private static class RegistryCommandParser extends Thread {
		public RegistryCommandParser() {}
		
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
				case "list-messaging-nodes":
					for (int i = 0; i < overlay.size(); i++) {
						System.out.println("ipAddress: " + overlay.get(i).getIpAddress() + 
								" port: " + overlay.get(i).getPort() +" ID: " + overlay.get(i).getID());
					}
					break;
				case "setup-overlay":
					setupOverlay();
					break;
				case "list-routing-tables":
					for (int i = 0; i < overlay.size(); i++) {
						int counter = i;
						System.out.println("Routing table for Node " + overlay.get(i).getID() + ":");
						
						counter = (i + 1) % overlay.size();
						System.out.println("ipAddress: " + overlay.get(counter).getIpAddress() + 
								" port: " + overlay.get(counter).getPort() +" ID: " + overlay.get(counter).getID());
						
						counter = (i + 2) % overlay.size();
						System.out.println("ipAddress: " + overlay.get(counter).getIpAddress() + 
								" port: " + overlay.get(counter).getPort() +" ID: " + overlay.get(counter).getID());
						
						counter = (i + 4) % overlay.size();
						System.out.println("ipAddress: " + overlay.get(counter).getIpAddress() + 
								" port: " + overlay.get(counter).getPort() +" ID: " + overlay.get(counter).getID());
						
						System.out.println();
					}
					break;
				case "start":
					break;
				default:
					System.out.println("Command could not be understood.");
					break;
			}
		}
	}
}