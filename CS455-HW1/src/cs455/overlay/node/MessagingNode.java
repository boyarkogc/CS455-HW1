package cs455.overlay.node;

import java.net.*;
import java.io.*;

import cs455.overlay.wireformats.Event;

public class MessagingNode implements Node {
	InetAddress nodeip;
	int nodePort;
	int ID;
	
	public InetAddress getNodeip() {
		return nodeip;
	}

	public int getNodePort() {
		return nodePort;
	}

	public int getID() {
		return ID;
	}

	public void onEvent(Event e) {
		// TODO Auto-generated method stub
		
	}
	
	
}
