package cs455.overlay.transport;

// TCPClient.java
// A client program implementing TCP socket
import java.net.*;
import java.io.*;

public class TCPClient {
	
	public static void main (String args[]) { // arguments supply message and hostname of destination
		Socket s = null;
		try {
			int serverPort = 6880;
			String ip = "localhost";//ip to connect to
			String data = "Hello, How are you?";//message to send
			  
			s = new Socket(ip, serverPort);
			DataInputStream input = new DataInputStream( s.getInputStream());
			DataOutputStream output = new DataOutputStream( s.getOutputStream());
		  
			//Step 1 send length
			System.out.println("Length"+ data.length());
			output.writeInt(data.length());
			//Step 2 send length
			System.out.println("Writing.......");
			output.writeBytes(data);
			
			//Step 1 read length
			int nb = input.readInt();
			byte[] digit = new byte[nb];
			//Step 2 read byte
			for (int i = 0; i < nb; i++) {
				digit[i] = input.readByte();
			}
			String st = new String(digit);
			System.out.println("Received: "+ st);
		}catch (UnknownHostException e) {
			System.out.println("Sock:"+e.getMessage());
		}catch (EOFException e) {
			System.out.println("EOF:"+e.getMessage());
		}catch (IOException e) {
			System.out.println("IO:"+e.getMessage());
		}finally {
			if(s!=null) {
				try {
					s.close();
				}catch (IOException e) {/*close failed*/}
			}
		}
	}
}
