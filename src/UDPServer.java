

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer extends Thread {
	
    private DatagramSocket socket;
	public int port;
	public String server;
	
	public UDPServer(int p, String s) {
		port = p;
		server = s;
	}

	public void run(){
        System.out.println(" == UDPServer - Call received at port " + port);

        DatagramSocket socket;
        String result = null;
		try {
			socket = new DatagramSocket(port);
	        byte[] buffer = new byte[256];
	         
	        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
	        socket.receive(request);

            String message = new String(buffer, 0, request.getLength());
	        System.out.println(" == UDPServer port " + port + " received "+ message);

	        String[] parts = message.split(":");
	        String command = parts[0];
	        
	        String customerID;
	        String itemID;
	        String itemName;
	        String dateOfPurchase;

	        StoreServerImpl s = null;
	        try{
	        	s= new StoreServerImpl(server);
	        }
	        catch (Exception e){
	        	e.printStackTrace();
	        }
	        
	        switch(command){
	        case "purchaseItem":
		         customerID  = parts[1];
		         itemID  = parts[2];
		         dateOfPurchase  = parts[3];
		         result = "Purchase result from "+server+" : " + s.purchaseItem(customerID, itemID, dateOfPurchase);
		         System.out.println(" UDP Server purchaseItem return: [" + result + "]");
	        break;
	        case "findItem":
		         customerID  = parts[1];
		         itemName  = parts[2];
		         result = s.getItemByName(itemName);
		         System.out.println(" UDP Server FindItem return: [" + result + "]");
	        }

	        buffer = result.getBytes();
	        InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();
 
            DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.send(response);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("    UDPServer port " + port + " end of messages");

	}
}