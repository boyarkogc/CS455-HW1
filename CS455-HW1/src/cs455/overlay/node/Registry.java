package cs455.overlay.node;

import java.net.*;
import java.util.ArrayList;
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
	
	private static ArrayList<RoutingEntry> overlay;
	private static ArrayList<Integer> unusedIDs;
	
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
	
	/*protected static void demoMethod() {
		///////
	}*/
	public static void main(String[] args) throws Exception {
		Registry reg = Registry.getInstance();
		overlay = new ArrayList<RoutingEntry>();
		unusedIDs = new ArrayList<Integer>();
		rand = new Random();
		for (int i = 0; i < 128; i++) {
			unusedIDs.add(i);
		}
        ServerSocket registry = new ServerSocket(4444);
		System.out.println("Registry is running");
        try {
            while (true) {
                new RegistryThread(registry.accept()).start();
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
									RoutingEntry node = new RoutingEntry(reg.getipAddress(), reg.getPort(), ID);
									overlay.add(node);
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
}