package common;

import java.io.*;
import java.util.*;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * This Logger class inputs the actions performed on the system. 
 * 
 * @author Daphné Augier - 40036123
 */

public class Logger {
	
	private static FileOutputStream file;
	private PrintWriter printWriter;

	final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd:hh-mm-ss");

	public Logger(String filePath) {
		try {
			file = new FileOutputStream(filePath, true);
			printWriter = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			System.out.println("Logger Error!");
			e.printStackTrace();
		}
	}

	public synchronized void logInfo(String message) {
		printWriter.println(formatter.format(new Date()) + ": " + message);
		flush();
		System.out.println(message);
	}

	private void flush() {
		this.printWriter.flush();
	}

}