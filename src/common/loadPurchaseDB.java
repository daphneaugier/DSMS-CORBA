package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;

import common.itemClass;

public class loadPurchaseDB {
	
	// List of users :customers
	private HashMap<String, String> purchaseList = new HashMap<String, String>();
	private String purchaseListFileName;

	public loadPurchaseDB(String f) throws NumberFormatException, IOException {
		purchaseListFileName = f;
		
	    System.out.println(" * Get purchase List from file " + purchaseListFileName);
		try {
		      File myObj = new File(purchaseListFileName);
		      if (myObj.createNewFile()) {
		    	  System.out.println(" * purchase List file "+ purchaseListFileName +" does not exist ==> created");
		      }
		}
		catch (IOException e) {
		      System.out.println(" *** An error occurred.");
		      e.printStackTrace();
		}

	    String line;
	    BufferedReader reader = new BufferedReader(new FileReader(purchaseListFileName));
	    while ((line = reader.readLine()) != null)
	    {
	        String[] parts = line.split(":");
	        if (parts.length >= 2)
	        {
	            String key = parts[0];
	            String value = parts[1];
	            
	            purchaseList.put(key, value);
	        } else {
	            System.out.println("ignoring line: " + line);
	        }
	    }

	    reader.close();
	}

	public void saveUserDB(){
	    
	    System.out.println(" * Save purchasde list keys and values to file " + purchaseListFileName);
	    try {
	    	Path fileToDeletePath = Paths.get(purchaseListFileName);
	    	boolean result = Files.deleteIfExists(fileToDeletePath);

	    	BufferedWriter writer = new BufferedWriter(new FileWriter(purchaseListFileName, true));
		    for (Entry<String, String> entry : purchaseList.entrySet()) {
		            String line = entry.getKey() + ":" + entry.getValue();
		            writer.write(line);
		            writer.newLine();
		    }
            writer.flush();
            writer.close();
	    }
	    catch (Exception e){
		    System.out.println(" *** Error saving purchase list file " + purchaseListFileName);	    	
	    }
	}
	
	public String getUserPuchase(String userID) {
		System.out.println("Purchase for User :" + userID + " is " + purchaseList.get(userID));
	    return purchaseList.get(userID);
	}
	
	public void checkUserExists(String userID) {
		
		if (!purchaseList.containsKey(userID)) {
			purchaseList.put(userID, "");
			saveUserDB();
			System.out.println("New user " + userID + " created");
		}
	}

	public void userPurchase(String userID, String itemID, String dateOfPurchase) {
		checkUserExists(userID);
		String newList = purchaseList.get(userID) + ";" + itemID + "=" + dateOfPurchase;
		purchaseList.remove(userID);
		purchaseList.put(userID, newList);
		saveUserDB();
	}

	public boolean firstBuyOnServer(String customerID, String itemServer) {
		return true;		
	}

	public boolean checkUserPurchase(String customerID, String oldItemID) {
		if(purchaseList.get(customerID).contains(oldItemID)){
			return true;
		}else {
			return false;			
		}
	}
}