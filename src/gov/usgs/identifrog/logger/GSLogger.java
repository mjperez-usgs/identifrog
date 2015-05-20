package gov.usgs.identifrog.logger;

import gov.usgs.identifrog.IdentiFrog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.exception.ExceptionUtils;

/** Adapted from source code used in ME3CMM 3.0 (DebugLogger.java)
 * @author Michael J. Perez 2015
 *
 */
public class GSLogger {
	File logFile;
	String logFileName = "IdentiFrogSession.log";
	FileWriter fw;
	Calendar cal = Calendar.getInstance();
	SimpleDateFormat sdf;
	
	public GSLogger(){
		sdf = new SimpleDateFormat("MM/dd/YYYY HH:mm:ss");
		try {
			fw = new FileWriter(logFileName);
		} catch (IOException e) {
			System.out.println("Unable to write log to file! Will write to console only.");
			IdentiFrog.LOGGER.writeException(e);
		}
	}
	
	/**
	 * Called if you want to use the debug logger.
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
	
	public void writeMessage(String message){
		if (IdentiFrog.LOGGING){
			try {
				System.out.println(message);
				cal.getTime();
				fw.write(sdf.format(cal.getTime()));
				fw.write(": ");
				fw.write(message);
				fw.write(System.getProperty("line.separator"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Cannot write to log file due to IOException:");
				IdentiFrog.LOGGER.writeException(e);
			}
		}
	}

	public void writeException(Exception e) {
		if (IdentiFrog.LOGGING){
			try {
				cal.getTime();
				System.err.println(ExceptionUtils.getStackTrace(e));
				fw.write(sdf.format(cal.getTime()));
				fw.write(ExceptionUtils.getStackTrace(e));
				fw.write(System.getProperty("line.separator"));
				fw.flush();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				System.err.println("Cannot write to log file due to IOException:");
				ex.printStackTrace();
			}
		}
	}

	public void writeExceptionWithMessage(String message, Exception e) {
		writeMessage(message);
		writeException(e);
	}

	public void writeMessage(int intVal) {
		writeMessage(Integer.toString(intVal));
		
	}

	public void writeError(String message) {
		if (IdentiFrog.LOGGING){
			try {
				System.err.println(message);
				cal.getTime();
				fw.write(sdf.format(cal.getTime()));
				fw.write(" ERROR: ");
				fw.write(message);
				fw.write(System.getProperty("line.separator"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("Cannot write to log file due to IOException:");
				IdentiFrog.LOGGER.writeException(e);
			}
		}
	}
}