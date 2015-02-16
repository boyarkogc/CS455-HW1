package cs455.overlay.transport;

import cs455.overlay.*;

import java.net.*;
import java.io.*;

public class TCPServer {
	
	private ServerSocket socket;
	private int portNumber;

	public TCPServer(int portNumber) throws IOException {
		this.portNumber = portNumber;
	}
	
	public void run() {
		int dataLength;
		boolean listening = true;
        
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            while (listening) {
	            new TCPServerThread(serverSocket.accept()).start();
	        }
	    }catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
	}
}