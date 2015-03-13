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
	public final static boolean DEBUGGING_BUILD = true; //when building the app, change this to false and debugging items will be hidden
	// construct the application
	public IdentiFrog() {
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
		
		
		StartupFrame startupFrame = new StartupFrame();
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
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		new IdentiFrog();
	}
}