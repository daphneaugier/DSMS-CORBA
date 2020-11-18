/**
 * This class is the Server of the stores of the DSMS project.
 * 
 * @author Daphné Augier - 40036123
 */


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import common.*;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import StoreApp.*;

public class BCStoreServer {

	static Scanner in = new Scanner(System.in);

	public static void main(String[] args) throws NumberFormatException, IOException {
		String server = null;
		try
		{
			// create and initialize the ORB
			ORB orb= ORB.init(args, null);
			// get reference to rootpoa& activate the POAManager
			POA rootpoa= POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();
			
			System.out.println("+------------------------------------------------------+");
			System.out.println("|  Welcome to DSMS Server for Bristish Colombia Store  |");
			System.out.println("+------------------------------------------------------+\n");
	
			// server = MyUtilities.getServerID();
			server = "BC";

			// create servant and register it with the ORB
			StoreServerImpl StoreServerImpl= new StoreServerImpl(server);
			StoreServerImpl.setORB(orb);
			
			//get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(StoreServerImpl);
			Store href= (Store) StoreHelper.narrow(ref);
			
			//get the root naming context
			//NameServiceinvokes the name service
			org.omg.CORBA.Object objRef= orb.resolve_initial_references("NameService");
			//Use NamingContextExtwhich is part of the Interoperable Naming Service (INS) specification.
			NamingContextExt ncRef= NamingContextExtHelper.narrow(objRef);
						
			System.out.println("Preparing  Server for " + server  + " Region");
			
			NameComponent path[] = ncRef.to_name(server);
		    ncRef.rebind(path, href);
				
			System.out.println("+--------------------------------+");
			System.out.println("| Server " + server  + " started successfully |");
			System.out.println("+--------------------------------+\n");
			//wait for invocations from clients
			orb.run();
		}
		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
		System.out.println("+------------------------+");
		System.out.println("| Server " + server  + " Exiting |");
		System.out.println("+------------------------+\n");
	}

}
