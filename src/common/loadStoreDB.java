package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import common.itemClass;

import java.util.Set;

public class loadStoreDB {
	
	// HashMap of a store
	private HashMap<String, itemClass> storeStock = new HashMap<String, itemClass>();
	// WaitList of a store
	private HashMap<String, List<String>> storeWaitList = new HashMap<String, List<String>>();
	
	private String storeFile;
	private String storeWaitListFile;

	public loadStoreDB(String f1, String f2) throws IOException {
		storeFile = f1;
		storeWaitListFile = f2;		
		
		try {
		      File myObj = new File(storeFile);
		      if (myObj.createNewFile()) {
		    	  System.out.println("  * loadStoreDB - Store file "+ storeFile +" does not exist ==> created");
		      }
		}
		catch (IOException e) {
		      System.out.println(" *** An error occurred.");
		      e.printStackTrace();
		}

		try {
		      File myObj = new File(storeWaitListFile);
		      if (myObj.createNewFile()) {
		    	  System.out.println("  * loadStoreDB - Wait List Store file "+ storeWaitListFile +" does not exist ==> created");
		      }
		}
		catch (IOException e) {
		      System.out.println(" *** An error occurred.");
		      e.printStackTrace();
		}

		
		// Add keys and values from file
	    System.out.println("  * loadStoreDB - Read Store List from file " + storeFile);
	    try {
		    String line;
	        int count = 0;
		    BufferedReader reader = new BufferedReader(new FileReader(storeFile));
		    while ((line = reader.readLine()) != null)
		    {
		        String[] parts = line.split(":");
		        if (parts.length >= 4)
		        {
		            String key = parts[0];
		            String name = parts[1];
		            int quantity = Integer.parseInt(parts[2]);
		            double price = Double.parseDouble(parts[3]);
		            
		            storeStock.put(key, new itemClass(name, quantity, price));
		            count++;
		        } else {
		            System.out.println("ignoring line: " + line);
		        }
		    }
		    System.out.println("  * loadStoreDB - "+count+" item(s) read from store list");

		    reader.close();	    	
	    }
	    catch (FileNotFoundException e) {
	    	e.printStackTrace();
	    }
	    
	    System.out.println("  * loadStoreDB - Read queue list from file " + storeWaitListFile);
	    
	    try {
		    String line;
		    BufferedReader reader = new BufferedReader(new FileReader(storeWaitListFile));
		    while ((line = reader.readLine()) != null)
		    {
		        String[] parts = line.split(":");
		        if (parts.length >= 42)
		        {
		            String key = parts[0];
		            String[] value = parts[1].split("?");
		            List<String> list = Arrays.asList(value);
		            storeWaitList.put(key, list);
		        } else {
		            System.out.println("ignoring line: " + line);
		        }
		    }

		    reader.close();	    	
	    }
	    catch (FileNotFoundException e) {
	    	e.printStackTrace();
	    }

	}
	
	public void saveStoreDB() {
	    
	    System.out.println("  * loadStoreDB - Save store list keys and values to file " + storeFile);
	    try {
	    	Path fileToDeletePath = Paths.get(storeFile);
	    	boolean result = Files.deleteIfExists(fileToDeletePath);
	    	
	    	int count = 0;
	    	BufferedWriter writer = new BufferedWriter(new FileWriter(storeFile, true));
		    for (Entry<String, itemClass> entry : storeStock.entrySet()) {
		            String line = entry.getKey() + ":" + entry.getValue().getName() + ":" + entry.getValue().getQuantity() + ":" + entry.getValue().getPrice();
		            writer.write(line);
		            writer.newLine();
		            count++;
		    }
		    System.out.println("  * loadStoreDB - "+ count + " item(s) written to file");
            writer.flush();
            writer.close();
	    }
	    catch (Exception e){
		    System.out.println("  * loadStoreDB *** Error saving file " + storeFile);	    	
	    }
	    
	    System.out.println("  * loadStoreDB - Save store queue list keys and values to file " + storeWaitListFile);
	    try {
	    	Path fileToDeletePath = Paths.get(storeWaitListFile);
	    	boolean result = Files.deleteIfExists(fileToDeletePath);

	    	BufferedWriter writer = new BufferedWriter(new FileWriter(storeWaitListFile, true));
		    for (Entry<String, List<String>> entry : storeWaitList.entrySet()) {
		            String line = entry.getKey() + ":";
		            List<String> values = entry.getValue();
		            for(String value: values){
		            	line += value + "?";
		         	}
		            writer.write(line);
		            writer.newLine();
		    }
            writer.flush();
            writer.close();
	    }
	    catch (Exception e){
		    System.out.println("  * loadStoreDB *** Error saving file " + storeWaitListFile);	    	
	    }
	}

	public <K, V> Set<String> getFullList() {
		System.out.println("  * loadStoreDB - Full list requested");
	    Set<String> keys = new HashSet<>();
	    for (Entry<String, itemClass> entry : storeStock.entrySet()) {
	            keys.add(entry.getKey() + " " + entry.getValue().getName() + " " + entry.getValue().getQuantity() + " " + entry.getValue().getPrice());
	    }
	    return keys;
	}
	
	public <K, V> Set<String> getItemByName(V itemName) {
		System.out.println(" + loadStoreDB - Looking for item : [" + itemName + "]");
	    Set<String> keys = new HashSet<>();
	    for (Entry<String, itemClass> entry : storeStock.entrySet()) {
	        if (entry.getValue().getName().equals(itemName)) {
	            keys.add(entry.getKey());
	        }
	    }
	    return keys;
	}
	
	public itemClass getItemByID(String itemID){
		try{
			itemClass returnItem = storeStock.get(itemID);
			return returnItem;
		}
		catch(Exception e) {
			return null;
		}
	}

	public void purchaseItem(String itemID, itemClass myItem) {
		storeStock.replace(itemID, myItem);
		saveStoreDB();
	}
	
	public void queueItem(String itemID, String userID) {

		if(storeWaitList.containsKey(itemID)) {
			List<String> queue = storeWaitList.get(itemID);
			queue.add(userID);
			storeWaitList.remove(itemID);
			storeWaitList.put(itemID, queue);
		} else {
			List<String> queue = new ArrayList<String>();
			queue.add(userID);
			storeWaitList.put(itemID, queue);
		}
		saveStoreDB();
	}

	public void addItem(String itemID, String itemName,  int quantity, double price) {
		storeStock.put(itemID, new itemClass(itemName, quantity, price));
		saveStoreDB();		
	}

	public void removeItem(String itemID) {
		storeStock.remove(itemID);
		saveStoreDB();		
	}

	public String getItemDetailsByName(String itemName) {
		
		System.out.println(" + loadStoreDB - Getting details for item : [" + itemName + "]");
	    
		String r = "";
	    for (Entry<String, itemClass> entry : storeStock.entrySet()) {
//DEBUG	    	System.out.println(" + seek: " + entry.getValue().getName());
	        if (entry.getValue().getName().equals(itemName)) {
	            r = r.concat(entry.getKey() + " " + entry.getValue().getQuantity() + " " + entry.getValue().getPrice()+" ");
	        }
	    }
	    return r;
	}
	
}