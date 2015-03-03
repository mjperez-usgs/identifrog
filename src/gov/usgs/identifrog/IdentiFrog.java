package gov.usgs.identifrog;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import gov.usgs.identifrog.Handlers.FolderHandler;
import gov.usgs.identifrog.Handlers.XMLHandler;

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

	// construct the application
	public IdentiFrog() {
	  // if the application is already open, then verify that the user wants to open another instance of the application
		boolean alreadyOpen = node.getBoolean("alreadyOpen", false);
		if (alreadyOpen) {
			if (ChoiceDialog.choiceMessage("Another instance of IdentiFrog may already running.\n" + "Open Anyway?") == 0) {
				node.putBoolean("alreadyOpen", true);
			} else {
				System.exit(5);
			}
		} else {
			node.putBoolean("alreadyOpen", true);
		}
		// set splash screen
		SplashScreen splash = new SplashScreen("SplashScreen.png");
		splash.splash();
		/*try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}*/
		// setup data folders and database xml file
		//TODO: MOVE THIS INTO A CREATE WORKSPACE
		FolderHandler fh = new FolderHandler();
		XMLHandler file = new XMLHandler(fh.getFileNamePath());
		if (!fh.FoldersExist()) {
			fh.CreateFolders();
			file.CreateXMLFile();
		}
		File f = new File(fh.getFileNamePath());
		if (f.exists() && f.length() == 0) {
			file.CreateXMLFile();
		}
		// create an instance of the MainFrame
		MainFrame frame = new MainFrame(fh);
		// validate frames that have preset sizes
		// pack frames that have useful preferred size info, e.g. from their layout
		if (packFrame) {
			frame.pack();
		} else {
			frame.validate();
		}
		// center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		frame.setLocation(0, 0);
		// close Splash Screen
		splash.dispose();
		frame.setVisible(true);
	}

	// main method
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		new IdentiFrog();
	}
}