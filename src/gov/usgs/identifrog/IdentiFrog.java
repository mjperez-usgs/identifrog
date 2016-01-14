package gov.usgs.identifrog;

import gov.usgs.identifrog.DataObjects.ProcessResult;
import gov.usgs.identifrog.Frames.MainFrame;
import gov.usgs.identifrog.Frames.ProjectManagerFrame;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.logger.GSLogger;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.ToolTipManager;
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
 * Description: An Automated Pattern Recognition Program for Leopard Frogs
 * (Lithobates pipiens).
 * 
 * @author Hidayatullah Ahsan 2011
 * @author Oksana V. Kelly 2008
 * @author Steven P. Miller for IdentiFrog Team 2005
 */
public class IdentiFrog {
	boolean packFrame = false;
	private Preferences root = Preferences.userRoot();
	//private final Preferences node = root.node("edu/isu/aadis/defaults");
	public final static boolean DEBUGGING_BUILD = true; //when building the app, change this to false and debugging items will be hidden
	public static final String DB_FILENAME = "datafile.xml"; //filename for the DB, can possibly change.
	public static final String HR_VERSION = "0.1.0";
	public static final int INT_VERSION = 1;
	public static final String BUILD_DATE = "10/26/2015";

	protected static final String THUMBNAIL_DIR = "Thumbnail";
	public static final String SIGNATURE_EXTENSION = ".dsg";
	public static final String BINARY_EXTENSION = ".jpg";
	public static boolean LOGGING = true;
	public static GSLogger LOGGER;
	public static XMLFrogDatabase DB;
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static DecimalFormat decimalFormat = new DecimalFormat("#.00");
	public static MainFrame activeMainFrame;
	public static ArrayList<Image> ICONS;

	// construct the application
	public IdentiFrog() throws FileNotFoundException, ParseException, IOException {
		//Set UI defaults before any UI is loaded
		System.setProperty("apple.laf.useScreenMenuBar", "true"); //makes mac use the system bar instead of windows style bar
		//removes the weird padding aroudn tab contents
		Insets insets = (Insets) UIManager.getDefaults().get("TabbedPane.contentBorderInsets");
		UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, insets.left, 0, insets.right));
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE); //tooltip timeout

		initLogger();
		ICONS = new ArrayList<Image>();
		ICONS.add(Toolkit.getDefaultToolkit().getImage(IdentiFrog.class.getResource("/resources/IconFrog2.png")));

		// if the application is already open, then verify that the user wants to open another instance of the application
		//boolean alreadyOpen = node.getBoolean("alreadyOpen", false);

		/*
		 * if (alreadyOpen) { if (ChoiceDialog.choiceMessage(
		 * "Another instance of IdentiFrog may already running.\n" +
		 * "Open Anyway?") == 0) { node.putBoolean("alreadyOpen", true); } else
		 * { System.exit(5); } } else { node.putBoolean("alreadyOpen", true); }
		 */

		//splash screen ==== likely removed
		// set splash screen
		//SplashScreen splash = new SplashScreen("SplashScreen.png");
		//splash.splash();
		/*
		 * try { Thread.sleep(1); } catch (InterruptedException ex) {
		 * ex.printStackTrace(); }
		 */
		// setup data folders and database xml file

		ProjectManagerFrame startupFrame = new ProjectManagerFrame();
		// center the window
		/*
		 * Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		 * Dimension frameSize = startupFrame.getSize(); if (frameSize.height >
		 * screenSize.height) { frameSize.height = screenSize.height; } if
		 * (frameSize.width > screenSize.width) { frameSize.width =
		 * screenSize.width; }
		 */
		startupFrame.setLocationRelativeTo(null);
		// close Splash Screen
		//splash.dispose();
		startupFrame.setVisible(true);
		//TODO: MOVE THIS INTO STARTUP FRAME
	}

	public static void initLogger() {
		if (LOGGER == null) {
			LOGGER = new GSLogger();
		}
	}

	// main method
	public static void main(String[] args) throws FileNotFoundException, ParseException, IOException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeException(e);
		}
		new IdentiFrog();
	}

	public static String getUpdatesFolder() {
		return getDataDir() + "updater"+File.separator;
	}

	public static String getUpdaterScript() {
		File f = new File(getUpdatesFolder());
		f.mkdirs();
		return getUpdatesFolder() + "update.cmd";
	}

	public static String getUpdateFileExtractedDestination() {
		File f = new File(getUpdatesFolder() + "extracted");
		f.mkdirs();
		return getUpdatesFolder() + "extracted";
	}

	public static String getUpdateFileLocation() {
		File f = new File(getUpdatesFolder());
		f.mkdirs();
		return getUpdatesFolder() + "update.zip";
	}
	
	/**
	 * Compares two version strings. 
	 * 
	 * Use this instead of String.compareTo() for a non-lexicographical 
	 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
	 * 
	 * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
	 * 
	 * @param str1 a string of ordinal numbers separated by decimal points. 
	 * @param str2 a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if str1 is _numerically_ less than str2. 
	 *         The result is a positive integer if str1 is _numerically_ greater than str2. 
	 *         The result is zero if the strings are _numerically_ equal.
	 */
	public static Integer versionCompare(String str1, String str2)
	{
	    String[] vals1 = str1.split("\\.");
	    String[] vals2 = str2.split("\\.");
	    int i = 0;
	    // set index to first non-equal ordinal or length of shortest version string
	    while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) 
	    {
	      i++;
	    }
	    // compare first non-equal ordinal number
	    if (i < vals1.length && i < vals2.length) 
	    {
	        int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
	        return Integer.signum(diff);
	    }
	    // the strings are equal or one string is a substring of the other
	    // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
	    else
	    {
	        return Integer.signum(vals1.length - vals2.length);
	    }
	}

	/**
	 * Gets a reference to the singular DB that is currently in use by
	 * IdentiFrog. May need loading before first use.
	 * 
	 * @return
	 */
	public static XMLFrogDatabase getDB() {
		return DB;
	}

	/**
	 * Appends a slash onto the end of a string if not already there.
	 * 
	 * @param string
	 *            Original string
	 * @return Original string with a slash on the end if it was not there
	 *         previously.
	 */
	public static String appendSlash(String string) {
		if (string.charAt(string.length() - 1) == File.separatorChar) {
			return string;
		} else {
			return string + File.separator;
		}
	}

	/**
	 * Gets the data/ folder, returning with an appended slash
	 * 
	 * @return
	 */
	public static String getDataDir() {
		return appendSlash(System.getProperty("user.dir")) + "data"+File.separator;
	}

	public static String getToolsDir() {
		File file = new File(getDataDir() + "tools"+File.separator);
		file.mkdirs();
		return appendSlash(file.getAbsolutePath());
	}

	/**
	 * Runs a process already build via processbuilder, prints timing info and
	 * returns the result
	 * 
	 * @param p
	 *            Process to build and run
	 * @return ProcessResult, with code if successful, or exception as not-null
	 *         if one occurred
	 */
	public static ProcessResult runProcess(ProcessBuilder p) {
		try {
			long startTime = System.currentTimeMillis();
			Process process = p.start();
			int returncode = process.waitFor();
			long endTime = System.currentTimeMillis();
			IdentiFrog.LOGGER.writeMessage("Process finished with code " + returncode + ", took " + (endTime - startTime) + " ms.");
			return new ProcessResult(returncode, null);
		} catch (IOException | InterruptedException e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("Process exception occured:", e);
			return new ProcessResult(0, e);
		}
	}

	public static String elementToXMLStr(Element elem) {
		String str = "error occured.";
		try {
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = transFactory.newTransformer();

			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(elem), new StreamResult(buffer));
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

	public static BufferedImage copyImage(Image source) {
		if (source == null) {
			return null;
		}
		BufferedImage copyOfImage = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics g = copyOfImage.createGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();
		return copyOfImage;
	}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img
	 *            The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}
}