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

public class loadUserDB {
	
	// List of users :customers
	private HashMap<String, Double> userList = new HashMap<String, Double>();
	private String userListFileName;

	public loadUserDB(String f) throws NumberFormatException, IOException {
		userListFileName = f;
		
	    System.out.println("  * loadUserDB - Get User List from file " + userListFileName);
		try {
		      File myObj = new File(userListFileName);
		      if (myObj.createNewFile()) {
		    	  System.out.println("  * loadUserDB - User List file "+ userListFileName +" does not exist ==> created");
		      }
		}
		catch (IOException e) {
		      System.out.println("  * loadUserDB *** An error occurred.");
		      e.printStackTrace();
		}

	    String line;
	    BufferedReader reader = new BufferedReader(new FileReader(userListFileName));
	    while ((line = reader.readLine()) != null)
	    {
	        String[] parts = line.split(":");
	        if (parts.length >= 2)
	        {
	            String key = parts[0];
	            double value = Double.parseDouble(parts[1]);
	            
	            userList.put(key, value);
	        } else {
	            System.out.println("  * loadUserDB - ignoring line: " + line);
	        }
	    }

	    reader.close();
	}

	public void saveUserDB(){
	    
	    System.out.println("  * loadUserDB - Save User list keys and values to file " + userListFileName);
	    try {
	    	Path fileToDeletePath = Paths.get(userListFileName);
	    	boolean result = Files.deleteIfExists(fileToDeletePath);

	    	BufferedWriter writer = new BufferedWriter(new FileWriter(userListFileName, true));
		    for (Entry<String, Double> entry : userList.entrySet()) {
		            String line = entry.getKey() + ":" + entry.getValue();
		            writer.write(line);
		            writer.newLine();
		    }
            writer.flush();
            writer.close();
	    }
	    catch (Exception e){
		    System.out.println("  * loadUserDB *** Error saving file " + userListFileName);	    	
	    }
	}
	
	public Double getUserBudgetbyName(String userID) {
		System.out.println("  * loadUserDB - Budget for User :" + userID + " is " + userList.get(userID));
	    return userList.get(userID);
	}
	
	public void checkUserExists(String userID) {
		
		if (!userList.containsKey(userID)) {
			userList.put(userID, 1000.00);
			saveUserDB();
			System.out.println("  * loadUserDB - New user " + userID + " created");
		}
	}

	public void userPurchase(String userID, double pricePaid) {
		double newBudget = userList.get(userID) - pricePaid;
		userList.remove(userID);
		userList.put(userID, newBudget);
		saveUserDB();
	}
}