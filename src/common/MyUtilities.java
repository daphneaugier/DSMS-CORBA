package common;

import java.util.Collections;
import java.util.Scanner;

public class MyUtilities {
	
	static Scanner in = new Scanner(System.in);
	

	public static String askString(String message) {
		
		System.out.println("Please enter the " + message +" :");
		String ret = in.nextLine();
		
		return ret;
	}
	
	public static void displayMessageBox(String message) {
			String bar = "-";
			int n = message.length();
			bar = String.join("", Collections.nCopies(n, bar));
			System.out.println("\n+-" + bar + "-+");
			System.out.println("| " + message + " |");
			System.out.println("+-" + bar + "-+\n");
	}
	
	public static int askManagerChoice(){
		displayMessageBox("Manager Menu");
		System.out.println("Select an operation\n");
		System.out.println("1 - Add Item");
		System.out.println("2 - Remove Item");
		System.out.println("3 - List Item Availability");
		
		return MyUtilities.getUserMenu("Operation #", 3);
	}
	public static int askCustomerChoice() {
		
		System.out.println("\nCustomer Menu\n--> Select an operation\n");
		System.out.println("1 - Purchase Item");
		System.out.println("2 - Find Item");
		System.out.println("3 - Return Item");
		System.out.println("4 - Exchange Item");
		
		return getUserMenu("Operation #", 4);

	}
	
	public static int getUDPPort(String server) {
		
		int port = 0;
		switch(server)
		{
		case "BC":
			port = 201;
			break;
		case "ON":
			port = 202;
			break;
		case "QC":
			port = 203;
			break;
		}
		return port;
	}

	public static int getPortID(String server) {
		
		int port = 0;
		switch(server)
		{
		case "BC":
			port = 2001;
			break;
		case "ON":
			port = 2002;
			break;
		case "QC":
			port = 2003;
			break;
		}
		return port;
	}

	public static String getServerID() {

		System.out.println("Server Store");
		System.out.println("------------");
		System.out.println(" 1 - British Colombia (BC)");
		System.out.println(" 2 - Ontario          (ON)");
		System.out.println(" 3 - Quebec           (QC)");

		int numRole = getUserMenu("Select the store server to start", 3);
		
		String server = "";
		switch(numRole)
		{
		case 1:
			server = "BC";
			break;
		case 2:
			server = "ON";
			break;
		case 3:
			server = "QC";
			break;
		}
		
		return server;
	}
	
	public static int getUserMenu(String message, int max){

		int n = 0;
		while(n<1 || n>max)
		{
			System.out.print("\n" + message +  " => ");
	        String store = (in.nextLine()).trim();      
	        
			n = Integer.parseInt(store);				
		}
		
		return n;
		
	}
	
}
