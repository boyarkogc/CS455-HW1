package cs455.overlay.node;

import java.net.*;
import java.util.HashSet;
import java.io.*; 

import cs455.overlay.wireformats.*;

public class Registry {
	private static HashSet<Integer> IDs = new HashSet<Integer>();
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

	private static final int port = 4444;
	
	private static final int OverlayNodeSendsRegistration = 2;
	//private static final int RegistryReportsRegistrationStatus = 3;
	private static final int OverlayNodeSendsDeregistration = 4;
	//private static final int RegistryReportsDeregistrationStatus = 5;
	
	private static Registry registry = new Registry();
	
	private Registry() {
		
	}
	
	public static Registry getInstance() {
		return registry;
	}
	
	/*protected static void demoMethod() {
		///////
	}*/
	public static void main(String[] args) throws Exception {
		Registry reg = Registry.getInstance();
		System.out.println("The registry is running.");
        ServerSocket registry = new ServerSocket(port);
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
		private String nodeIP;
		private int nodePort;
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
						//read the type of message
						type = din2.readInt();
						//do different things depending on the type of message received
						switch (type) {
							case OverlayNodeSendsRegistration:
								OverlayNodeSendsRegistration reg = new OverlayNodeSendsRegistration(data);
								nodeIP = reg.getipAddress();
								nodePort = reg.getPort();
								System.out.println(nodeIP + " " + nodePort);
								break;
							case OverlayNodeSendsDeregistration:
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