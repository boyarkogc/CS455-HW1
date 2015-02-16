package cs455.overlay.node;

import java.net.*;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;
import java.io.*;

import cs455.overlay.wireformats.*;

public class MessagingNode extends Thread implements Node {
	private Socket socket;
	private static DataOutputStream dout;
	
	
	//queue to hold messages being sent from other nodes in the overlay
	//private Queue<OverlayNodeSendsData> relayQueue = new LinkedList<OverlayNodeSendsData>();
	
	public MessagingNode(Socket socket) throws IOException {
		this.socket = socket;
		dout = new DataOutputStream(socket.getOutputStream());
	}
	
	public static void sendData(byte[] dataToSend) throws IOException {
		int dataLength = dataToSend.length;
		dout.writeInt(dataLength);
		dout.write(dataToSend, 0, dataLength);
		dout.flush();
	}
	 
	public static void main (String args[]) throws NumberFormatException, IOException {
		Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
		MessagingNode msg = new MessagingNode(socket);
		String ip = InetAddress.getLocalHost().getHostAddress();
		int port = socket.getLocalPort();
		
		OverlayNodeSendsRegistration reg = new OverlayNodeSendsRegistration(ip, port);
		sendData(reg.getBytes());
	}

	public void onEvent(Event e) {
		// TODO Auto-generated method stub
		
	}
}
