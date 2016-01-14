package gov.usgs.identifrog.logger;

import gov.usgs.identifrog.IdentiFrog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.exception.ExceptionUtils;

/** Adapted from source code used in ME3CMM 3.0 (DebugLogger.java)
 * GSLogger is a logging class that writes messages to Standard Out and a file, as well as errors and exceptions to standard error and a file with datestamps.
 * It should be used in place of System.out.println();.
 * @author Michael J. Perez 2015
 *
 */
public class GSLogger {
	private File logFile;
	private String logFileName = "IdentiFrogSession.log";
	private FileWriter fw;
	private Calendar cal = Calendar.getInstance();
	private SimpleDateFormat sdf;
	private int currentPendingMessages = 0;
	private final int maxPendingMessage = 10;
	
	/**
	 * Initializes a new GSLogger object using the object's predefined logFileName. Writes an exception if unable to open a file.
	 */
	public GSLogger(){
		sdf = new SimpleDateFormat("MM/dd/YYYY HH:mm:ss");
		try {
			fw = new FileWriter(logFileName);
		} catch (IOException e) {
			System.out.println("Unable to write log to file! Will write to console only.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Call if you want to use the debug logger.
	 */
	public void initialize(){
		logFile = new File(logFileName);
		try {
			/*if (logFile.exists()){
				logFile.delete();
			}
			*/
			logFile.createNewFile();
			//we now have write permissions
			fw = new FileWriter(logFile);
			fw.write("-------Logger initialized: ");
			cal.getTime();
			fw.write(sdf.format(cal.getTime()));
			fw.write("-------");
			fw.write(System.getProperty("line.separator"));
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
			        try {
			        	System.out.println("Shutting down logger.");
			        	writeMessage("Shutting down logger");
						fw.close();
					} catch (IOException e) {
						System.out.println("Cannot close filewriter. Giving up due to exception:");
						IdentiFrog.LOGGER.writeException(e);
					}
			    }
			});	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logFile = null;
			IdentiFrog.LOGGING = false;
			System.out.println("Log failed to write! Cannot write log due to IOException:");
			IdentiFrog.LOGGER.writeException(e);
		}
	}
	
	/**
	 * Writes a string into the log file.
	 * @param message Message to write into the log file
	 */
	public void writeMessage(String message){
		if (IdentiFrog.LOGGING){
			try {
				System.out.println(message);
				cal.getTime();
				fw.write(sdf.format(cal.getTime()));
				fw.write(": ");
				fw.write(message);
				fw.write(System.getProperty("line.separator"));
				incrementPendingMessages();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Cannot write to log file due to IOException:");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Writes the stack trace of the passed exception into the log file.
	 * @param e Exception to print stack trace of
	 */
	public void writeException(Exception e) {
		if (IdentiFrog.LOGGING){
			try {
				cal.getTime();
				System.err.println(ExceptionUtils.getStackTrace(e));
				fw.write(sdf.format(cal.getTime()));
				fw.write(ExceptionUtils.getStackTrace(e));
				fw.write(System.getProperty("line.separator"));
				incrementPendingMessages();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				System.err.println("Cannot write to log file due to IOException:");
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Writes an exception with a preceeding message to describe what the exception is occuring from.
	 * @param message Message to display
	 * @param e exception to print stack trace of
	 */
	public void writeExceptionWithMessage(String message, Exception e) {
		writeMessage(message);
		writeException(e);
	}

	/**
	 * Writes an integer to the console and log file.
	 * @param intVal int value to write
	 */
	public void writeMessage(int intVal) {
		writeMessage(Integer.toString(intVal));
		
	}

	/**
	 * Writes a message to Standard Error and writes a special  ERROR: tag in the log file.
	 * @param message
	 */
	public void writeError(String message) {
		if (IdentiFrog.LOGGING){
			try {
				System.err.println(message);
				cal.getTime();
				fw.write(sdf.format(cal.getTime()));
				fw.write(" ERROR: ");
				fw.write(message);
				fw.write(System.getProperty("line.separator"));
				incrementPendingMessages();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Cannot write to log file due to IOException:");
				IdentiFrog.LOGGER.writeException(e);
			}
		}
	}
	
	private void incrementPendingMessages(){
		currentPendingMessages++;
		if (currentPendingMessages > maxPendingMessage) {
			//System.err.println("Flushing messages");
			try {
				fw.flush();
			} catch (IOException e) {
				System.err.println("Failed to flush messages of logger");
				e.printStackTrace();
			}
			currentPendingMessages = 0;
		}
	}
}