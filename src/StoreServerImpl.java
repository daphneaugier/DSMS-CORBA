

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import common.*;
import StoreApp.*;
import org.omg.CORBA.*;


class StoreServerImpl extends StorePOA {

	// File names initialization
	private String logFileName = "-ServerLog.txt";
	private String storeFileName = "-ServerDB.txt";
	private String storeWaitListFileName = "-WaitListDB.txt";
	private String userListFileName = "user_DB/UserListDB.txt";
	private String purchaseListFileName = "user_DB/purchaseListDB.txt";
	
	private String server;

	// HashMap of a store
	loadStoreDB storeStock;
	// HashMap of users
	loadUserDB userList;
	// HashMap of Purchases
	loadPurchaseDB purchaseList;
	
	private ORB orb;

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}
	
	public void shutdown() {
		orb.shutdown(false);
	}
	public StoreServerImpl(String server) throws NumberFormatException, IOException {
		
		this.server = server;
		
		// Writing inside log file
		String server_logs = "output/";
		String server_folder = "server_DB/";
		logFileName = server_logs + server + logFileName;
		storeFileName = server_folder + server + storeFileName;
		storeWaitListFileName = server_folder + server + storeWaitListFileName;
		
		Logger myLog = new Logger(logFileName);
		myLog.logInfo(" == StoreServer - Starting " + server  + " Server");

		storeStock = new loadStoreDB(storeFileName, storeWaitListFileName);
		userList = new loadUserDB(userListFileName);
		purchaseList = new loadPurchaseDB(purchaseListFileName);
		
		// Thread creation for UDP
		System.out.println(" == StoreServer - Server " + server  + " UDP listening at port " + MyUtilities.getUDPPort(server));
		UDPServer MyUDPServer = new UDPServer(MyUtilities.getUDPPort(server), server);
		MyUDPServer.start();
	}

	/*
	 * 
	 * Interface methods
	 * 
	 * - Manager Operations
	 * 
	 */
	public void addItem(String userID, String itemID, String itemName, int quantity, double price) {
		// Writing inside log file
		Logger myLog = new Logger(logFileName);
		
		myLog.logInfo(" === addItem was called on server " + server +" by user "+userID);
		myLog.logInfo("  - itemID  : " + itemID);
		myLog.logInfo("  - itemName: " + itemName);
		myLog.logInfo("  - quantity: " + quantity);
		myLog.logInfo("  - price   : " + price);
		
		storeStock.addItem(itemID, itemName, quantity, price);
	}
	
	public void removeItem(String userID, String itemID, int quantity) {
		// Writing inside log file
		Logger myLog = new Logger(logFileName);
		myLog.logInfo("removeItem was called on server " + server + " for manager: " + userID + " and item " + itemID);

		userList.checkUserExists(userID);
		storeStock.removeItem(itemID);
	}
	
	public String listItem(String managerID){
		// Writing inside log file
		Logger myLog = new Logger(logFileName);
		myLog.logInfo("listItem was called on server " + server + " for manager: " + managerID);

		userList.checkUserExists(managerID);
	    Set<String> result = storeStock.getFullList();
	    String f = "";
	    for (Iterator<String> it = result.iterator(); it.hasNext(); ) {
			f = f + it.next() + " ";
	    }

	    return f;
	}
	
	/*
	 * 
	 * Interface methods
	 * 
	 * - Customer Operations
	 * 
	 */
	
	/* 
	 * 
	 * Return code:
	 *   0 transaction successful
	 *   1 user has not enough cash
	 *   2 no stock for item
	 */
	public String purchaseItem(String customerID, String itemID, String dateOfPurchase) {
		// Writing inside log file
		Logger myLog = new Logger(logFileName);
		myLog.logInfo(" = purchaseItem was called on server " + server + " for customer: " + customerID + " and item: " + itemID);
		
		// Get user budget
	    loadUserDB userList = null;
		try {
			userList = new loadUserDB(userListFileName);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		userList.checkUserExists(customerID);
	    Double UserBudget = userList.getUserBudgetbyName(customerID);
	    myLog.logInfo(" - User " + customerID + " has cash " + UserBudget );
	    
	    String itemServer = itemID.substring(0, 2);
	    
	    String result = null;
	    
	    if (server.equals(itemServer)) {
	    	// Item is from this server
    		System.out.println("  - Item " + itemID + " on local server");

		    itemClass myItem = storeStock.getItemByID(itemID);
		    
		    if(myItem == null){
		    	result = " - There are  NO item " + itemID + " in stock";
		    } else if (myItem.getQuantity() > 0) {
		    	// There are items in the stock
			    myLog.logInfo(" - There are " + myItem.getQuantity() + " item(s) " + itemID + " in stock" );
		    	if (myItem.getPrice() < UserBudget) {
		    		// User can pay for item
		    		myItem.decreaseQuantity();
		    		storeStock.purchaseItem(itemID, myItem);				// Change Stock
		    		userList.userPurchase(customerID, myItem.getPrice());   // Decrease User Budget
		    		purchaseList.userPurchase(customerID, itemID, dateOfPurchase);              // Add item to list
		    		result = " 0 Purchase completed successfully on server " + server + " for customer: " + customerID + " and item: " + itemID;
		    	} else {
		    		// User is out of cash
		    		result = " 1 User " + customerID + " cannot afford item " + itemID;
		    	}
		    } else {
		    	// Out of stock for this item
			    myLog.logInfo(" - There are  0 item " + itemID + " left in stock" );
		    	if(dateOfPurchase =="") {
		    		// Queuing request
		    		storeStock.queueItem(itemID, customerID);
		    		result = "Queuing completed successfully on server " + server + " for customer: " + customerID + " and item: " + itemID;
		    	
		    	} else {
		    		result = " 2 Out of stock for item " + itemID;		    		
		    	}
		    }
	    } else {
	    	// Item is at another server, building UDP request
	    	if(purchaseList.firstBuyOnServer(customerID,itemServer)) {
		    	int port = MyUtilities.getUDPPort(itemServer);
		    	
	    		System.out.println("  - Looking Item " + itemID + " on remote server "+ itemServer +" at port " + port);
		    	String hostname = "localhost";
		    	
		        try {
		            InetAddress address = InetAddress.getByName(hostname);
		            DatagramSocket socket = new DatagramSocket();
		 		            	
	            	String data = "purchaseItem:"+customerID+":"+itemID+":"+dateOfPurchase;
	            	byte[] buffer = data.getBytes();
	            	
	                DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
	                socket.send(request);
	                System.out.println("  Sent request: " + data);
	 
	                byte[] buffer1 = new byte[512];
	                DatagramPacket response = new DatagramPacket(buffer1, buffer1.length);
	                socket.receive(response);
	 
	                result = new String(buffer1, 0, response.getLength());
	                socket.close();
		 
		        } catch (SocketTimeoutException ex) {
		            System.out.println("Timeout error: " + ex.getMessage());
		            ex.printStackTrace();
		        } catch (IOException ex) {
		            System.out.println("Client error: " + ex.getMessage());
		            ex.printStackTrace();
				}	    		    		
	    	} else {
	    		result = " 3 Purchase already performed on this server";
	    	}
	    }
	    myLog.logInfo(result);
	    return result;
	}
	
	public String findItem(String customerID, String itemName){
		// Writing inside log file
		Logger myLog = new Logger(logFileName);
		myLog.logInfo(" = Find was called on server " + server + " for customer: " + customerID + " and item: " + itemName);
		
		userList.checkUserExists(customerID);	
		
		myLog.logInfo(" + Find item results on local server ");
	    String final_result = getItemByName(itemName);

    	// Seek Item at other servers, building UDP request
	    String[] servers = {"ON", "BC", "QC"};
	    for(String thisServer:servers){
	    	if (thisServer != server){
	    		
	        	int port = MyUtilities.getUDPPort(thisServer);
	        	
	    		System.out.println("  - Send Find Item request to remote store "+ thisServer +" at port " + port);
	        	String hostname = "localhost";
	        	
	            try {
	                InetAddress address = InetAddress.getByName(hostname);
	                DatagramSocket socket = new DatagramSocket();
	     		            	
	            	String data = "findItem:"+customerID+":"+itemName;
	            	byte[] buffer = data.getBytes();
	            	
	                DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
	                socket.send(request);
	                System.out.println("  Sent request: " + data);

	                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
	                socket.receive(response);

	                String quote = new String(buffer, 0, response.getLength());
	                socket.close();
	                
	                System.out.println(" findItem Response received: " + quote);
	                final_result = final_result.concat(quote);
	     
	            } catch (SocketTimeoutException ex) {
	                System.out.println("Timeout error: " + ex.getMessage());
	                ex.printStackTrace();
	            } catch (IOException ex) {
	                System.out.println("Client error: " + ex.getMessage());
	                ex.printStackTrace();
				}
					    		
	    	}
	    	
	    }
	    return final_result;
	}

	public String returnItem(String customerID, String itemID, String dateOfReturn){
		// Writing inside log file
		Logger myLog = new Logger(logFileName);
		myLog.logInfo(" = returnItem was called on server " + server + " for customer: " + customerID + " and item: " + itemID);
		
		userList.checkUserExists(customerID);
	    
		String message = "no message";
		
		return message;
	}

	public String exchangeItem(String customerID, String newItemID, String oldItemID) {

		String dateOfPurchase = new SimpleDateFormat("mm/dd/yyyy HH:mm").format(new Date());
		String result = null;
		String purchaseStatus = null;
		
		if (purchaseList.checkUserPurchase(customerID, oldItemID)) {
			purchaseStatus = purchaseItem(customerID, newItemID, dateOfPurchase);
            if(Character.getNumericValue(purchaseStatus.charAt(1)) == 0)
				try {
					purchaseItem(customerID, oldItemID, dateOfPurchase);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
            result = " = " +oldItemID + " was replaced by " + newItemID;
		}else {
			result = " User " + customerID + " did not buy " +oldItemID;
		}
		
		
        return result;
	}
	
	@Override
	public String listItemAvailability() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getItemByName(String itemName) {
		
	    return storeStock.getItemDetailsByName(itemName);
	}

	@Override
	public boolean sellItem(String customerID, String itemID, String dateOfPurchase) {
		// TODO Auto-generated method stub
		return false;
	}
	

}