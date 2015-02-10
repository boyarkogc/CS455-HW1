package cs455.overlay.transport;

// TCPServer.java
// A server program implementing TCP socket
import java.net.*;
import java.io.*;

public class TCPServer {
	public static void main (String args[]) {
		try {
			int serverPort = 6880;
			ServerSocket listenSocket = new ServerSocket(serverPort);
	  
			System.out.println("Server is now listening... ... ...");
			while(true) {
				Socket clientSocket = listenSocket.accept();
				TCPConnection c = new TCPConnection(clientSocket);
			}
		}catch(IOException e) {
			System.out.println("Listen :"+e.getMessage());}
	}
}
