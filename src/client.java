

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import common.*;
import StoreApp.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;

/**
 * This is the implementation of the customerClient. It invokes the server system
 * to test the correct operation of the DSMS by invoking multiple servers.
 * 
 * @author Daphné Augier - 40036123
 */

public class client {
	
	static Store StoreImpl;
	
	private String userListFileName = "user_DB/UserListDB.txt";
	loadUserDB userList;
	
	static Scanner in = new Scanner(System.in);


	public static void main(String[] args) throws Exception{
        
        boolean goagain = false;
		
		Logger myLog;
		
		try{
	        // create and initialize the ORB
	        ORB orb = ORB.init(args, null);

	        // get the root naming context
	        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
	        // Use NamingContextExt instead of NamingContext. This is 
	        // part of the Interoperable naming Service.  
	        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	 
	        // We are ready, let's go for user interactions
	        
			do{
				MyUtilities.displayMessageBox("Welcome to DSMS Client");
				// Display Server Selection menu and return user input as `BC, ON or QC
				String store = MyUtilities.getServerID();
		        // resolve the Object Reference in Naming
		        StoreImpl = (Store) StoreHelper.narrow(ncRef.resolve_str(store));

		        System.out.println("Obtained a handle on server object: " + StoreImpl);
		        
				System.out.println("\n------------\n");
				System.out.println("1 - Manager");
				System.out.println("2 - Customer");
				
				int numRole = MyUtilities.getUserMenu("Select your role ", 2);
				
				String role = "";
				switch(numRole)
				{
				case 1:
					role = "M";
					break;
				case 2:
					role = "U";
					break;
				}
								
				// This creates the ID prefix to identify the server ie: QCU
				String server = store + role;
				
				System.out.print("\nYour 4 digits ID => ");
		        String id = (in.nextLine()).trim();      
		
				id = "00000" + id;
				id = id.substring(id.length()-4,id.length());
						
				String userID = server + id;
				
				// Log file for user
				myLog = new Logger("output/" + userID + ".txt");
				myLog.logInfo("Starting Client log for " + userID);
				myLog.logInfo("Your requests will go to server: " + store);
		
				int operationNum = 0;
		
				String itemID     = store + "1012";
				String itemName = "Another name";
				String dateOfPurchase = "10102020";
				String dateOfReturn = "10102020";

				int quantity = 0;
				double price = 1.0;
		
				// Operations depend on the role : Manager or Customer
				switch(numRole)
				{
				case 1:			// Manager				
					operationNum = MyUtilities.askManagerChoice();
					switch(operationNum)
					{
					case 1:
						itemID  = MyUtilities.askString("item ID");
						itemName= MyUtilities.askString("item Name");
						String q= MyUtilities.askString("quantity");
			            quantity = Integer.parseInt(q);
			            
			            boolean correct = false;
			            do {
				            try {
								System.out.println("Please enter the price:");
								String p=in.nextLine();
					            price = Double.parseDouble(p);
					            correct = true;
				            }
				            catch(NumberFormatException e) {
				            	System.out.println(" *** Price should be a numeric value");			            	
				            }			            	
			            }while(!correct);

			            StoreImpl.addItem(userID, itemID, itemName, quantity, price);
						myLog.logInfo(" == add Item completed");						
						break;
					case 2:
						System.out.println("Please enter the item ID:");
						itemID=in.nextLine();

						StoreImpl.removeItem(userID, itemID, quantity);
						myLog.logInfo(" == Remove item completed");
						break;
					case 3:
						String result = StoreImpl.listItem(userID);
						myLog.logInfo("List item results: " +result );
						break;
					}			
					break;
				case 2:			// Customer			
					operationNum = MyUtilities.askCustomerChoice();
		
					switch(operationNum)
					{
					case 1:
						System.out.println("Please enter the item ID:");
						itemID=in.nextLine();

						dateOfPurchase = new SimpleDateFormat("mmddyyyy").format(new Date());
						System.out.println("Date of purchase id :"+dateOfPurchase);

						String result = StoreImpl.purchaseItem(userID, itemID, dateOfPurchase);
						System.out.println(result);

						switch(Character.getNumericValue(result.charAt(1))) {
							case 0:
								myLog.logInfo("Puchase Item: " + itemID + " completed successfully");
							break;
							case 1:
								myLog.logInfo("You don't have enought cash to purchase Item: "+itemID );
							break;
							case 2:
								myLog.logInfo("No stock for item " + itemID);
								
								System.out.println("Do you want to be added to a waiting queue");

								System.out.println("1 - yes");
								System.out.println("2 - no");
								
								operationNum = MyUtilities.getUserMenu("Operation #", 2);
								
								switch(operationNum) {
									case 1:
										myLog.logInfo("Adding user on waiting list for Item: "+itemID );
										result = StoreImpl.purchaseItem(userID, itemID, "");									
									break;
									case 2:
										myLog.logInfo("User "+userID+" did not request waiting list for Item: "+itemID );
									break;
								}

							break;						
						}
						break;
					case 2:
						System.out.println("Please enter the item Name:");
						itemName=in.nextLine();
						result = StoreImpl.findItem(userID, itemName);
						myLog.logInfo("Find item results:" + result);
						break;
					case 3:
						System.out.println("Please enter the date of return (ddmmyyyy) :");
						dateOfReturn=in.nextLine();
						StoreImpl.returnItem(userID, itemID, dateOfReturn);
						myLog.logInfo("Return Item completed ");
						break;
					case 4:
						System.out.println("Please enter the old item ID:");
						String oldItemID=in.nextLine();
						System.out.println("Please enter the new item ID:");
						String newItemID=in.nextLine();

						StoreImpl.exchangeItem(userID, newItemID, oldItemID);
						myLog.logInfo("Exchange Item completed ");
						break;
						
					}			
					break;
				}
		    
				System.out.println("Do you want to continue?\n");
				System.out.println("1 - yes");
				System.out.println("2 - no");
				
				operationNum = MyUtilities.getUserMenu("Your choice #", 2);
		
				switch(operationNum)
				{
				case 1:
					goagain = true;
					break;
				case 2:
					goagain = false;
					break;
				}    	
			} while(goagain);
			myLog.logInfo("+------------------------------+");
			myLog.logInfo("| Client - End of transactions |");
			myLog.logInfo("+------------------------------+");

	        StoreImpl.shutdown();

	        } catch (Exception e) {
	          System.out.println("ERROR : " + e) ;
	          e.printStackTrace(System.out);
	          }
	    }

	
	
	public client(String store, String userID) throws NumberFormatException, IOException {
		System.out.println(" --- Client instance started for server "+store);
		
		loadUserDB userList = new loadUserDB(userListFileName);
		userList.checkUserExists(userID);
		
	}

}
