package gov.usgs.identifrog;

import gov.usgs.identifrog.Frames.ProjectManagerFrame;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.logger.GSLogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.prefs.Preferences;

import javax.swing.UIManager;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;

/**
 * <p>
 * Title: IdentiFrog.java
 * <p>
 * Description: An Automated Pattern Recognition Program for Leopard Frogs (Lithobates pipiens).
 * 
 * @author Hidayatullah Ahsan 2011
 * @author Oksana V. Kelly 2008
 * @author Steven P. Miller for IdentiFrog Team 2005
 */
public class IdentiFrog {
	boolean packFrame = false;
	private Preferences root = Preferences.userRoot();
	private final Preferences node = root.node("edu/isu/aadis/defaults");
	public final static boolean DEBUGGING_BUILD = true; //when building the app, change this to false and debugging items will be hidden
	public static final String DB_FILENAME = "datafile.xml"; //filename for the DB, can possibly change.
	public static final String HR_VERSION = "1.0 Alpha";
	protected static final String THUMBNAIL_DIR = "Thumbnail";
	public static boolean LOGGING = true;
	public static GSLogger LOGGER;
	public static XMLFrogDatabase DB;
	// construct the application
	public IdentiFrog() throws FileNotFoundException, ParseException, IOException {
		LOGGER = new GSLogger();
	  // if the application is already open, then verify that the user wants to open another instance of the application
		boolean alreadyOpen = node.getBoolean("alreadyOpen", false);
		
		
		/*
		if (alreadyOpen) {
			if (ChoiceDialog.choiceMessage("Another instance of IdentiFrog may already running.\n" + "Open Anyway?") == 0) {
				node.putBoolean("alreadyOpen", true);
			} else {
				System.exit(5);
			}
		} else {
			node.putBoolean("alreadyOpen", true);
		}*/
		
		
		//splash screen ==== likely removed
		// set splash screen
		//SplashScreen splash = new SplashScreen("SplashScreen.png");
		//splash.splash();
		/*try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}*/
		// setup data folders and database xml file
		
		
		ProjectManagerFrame startupFrame = new ProjectManagerFrame();
		// center the window
		/*Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = startupFrame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}*/
		startupFrame.setLocationRelativeTo(null);
		// close Splash Screen
		//splash.dispose();
		startupFrame.setVisible(true);
		//TODO: MOVE THIS INTO STARTUP FRAME
	}

	// main method
	public static void main(String[] args) throws FileNotFoundException, ParseException, IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeMessage(e.getLocalizedMessage());
		}
		new IdentiFrog();
	}
	
	/**
	 * Gets a reference to the singular DB that is currently in use by IdentiFrog. May need loading before first use.
	 * @return
	 */
	public static XMLFrogDatabase getDB(){
		return DB;
	}
	
	public static String elementToXMLStr(Element elem) {
		String str = "error occured.";
		try {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer;
		transformer = transFactory.newTransformer();
		
		StringWriter buffer = new StringWriter();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(elem),new StreamResult(buffer));
		str = buffer.toString();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
}