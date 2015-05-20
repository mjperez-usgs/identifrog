package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.ExtensionFileFilter;
import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.MainFrame;
import gov.usgs.identifrog.Site;
import gov.usgs.identifrog.Handlers.FolderHandler;
import gov.usgs.identifrog.Handlers.XMLHandler;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class ProjectManagerFrame extends JDialog implements ActionListener {
	public static final String RECENT_SITES_FILE = "recentsites.idf";
	private Image icon = Toolkit.getDefaultToolkit().getImage("IconFrog.png");
	private JButton openSite, createSite;
	private ArrayList<Site> recentSites;
	private MainFrame mf;
	
	public ProjectManagerFrame(){
		setupFrame();
		//setVisible(true);
	}
	
	public ProjectManagerFrame(MainFrame mf){
		this.mf = mf;
		setupFrame();
		//setVisible(true);
	}
	
	

	private void setupFrame() {
		// TODO Auto-generated method stub
		setMinimumSize(new Dimension(400,300));
		setTitle("IdentiFrog Project Manager");
		setIconImage(icon);
		if (mf == null) {
			//close if this is opened by main()
			addWindowListener(new WindowAdapter() { 
			    @Override public void windowClosing(WindowEvent e) { 
			      System.exit(0);
			    }
			  });
		} else {
			setModalityType(ModalityType.APPLICATION_MODAL);
		}
		ImageIcon openIcon = new ImageIcon(this.getClass().getResource("/resources/IconSite128.png"));
		openSite = new JButton("Open existing site", openIcon);
		openSite.setVerticalTextPosition(SwingConstants.BOTTOM);
		openSite.setHorizontalTextPosition(SwingConstants.CENTER);
		openSite.setMinimumSize(new Dimension(132,132));
		openSite.addActionListener(this);
	    
	    //openSite.setIcon(new ImageIcon(img));
		ImageIcon createIcon = new ImageIcon(this.getClass().getResource("/resources/IconBook128.png"));
		createSite = new JButton("Create new site", createIcon);
		createSite.setVerticalTextPosition(SwingConstants.BOTTOM);
		createSite.setHorizontalTextPosition(SwingConstants.CENTER);
		createSite.setMinimumSize(new Dimension(132,132));
		createSite.addActionListener(this);
		//img = Toolkit.getDefaultToolkit().getImage("IconBook128.png");
	    //createSite.setIcon(new ImageIcon(img));
		
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(openSite);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(createSite);
		buttonPanel.add(Box.createHorizontalGlue());
		
		
		
		JPanel verticalPanel = new JPanel();
		verticalPanel.add(Box.createVerticalGlue());
		verticalPanel.setLayout(new BoxLayout(verticalPanel,BoxLayout.PAGE_AXIS));
		verticalPanel.add(buttonPanel);
		verticalPanel.add(Box.createVerticalGlue());
		
		//Recent sites panel
		JPanel recentSitesPanel = new JPanel();
		Border border = BorderFactory.createEtchedBorder();
		TitledBorder title = BorderFactory.createTitledBorder(border,"Recently opened sites");
		title.setTitleJustification(TitledBorder.CENTER);
		recentSitesPanel.setBorder(title);
		recentSitesPanel.setLayout(new BoxLayout(recentSitesPanel,BoxLayout.LINE_AXIS));
		recentSitesPanel.add(Box.createHorizontalGlue());
		
		//start population
		getRecentSites();
		for (Site site : recentSites) {
			JButton recentSite = createRecentSiteButton(site);
			recentSitesPanel.add(recentSite);
			recentSitesPanel.add(Box.createHorizontalGlue());
		}
		
		if (recentSites.size() <= 0) {
			JLabel noSites = new JLabel ("No recently opened sites");
			noSites.setEnabled(false);
			recentSitesPanel.add(noSites);
		}
		
		//end population
		recentSitesPanel.add(Box.createHorizontalGlue());
		verticalPanel.add(recentSitesPanel);
		
		//version
		JPanel versionPanel = new JPanel();
		versionPanel.setLayout(new BoxLayout(versionPanel,BoxLayout.LINE_AXIS));
		versionPanel.add(Box.createHorizontalGlue());
		JLabel versionInfo = new JLabel("IdentiFrog "+IdentiFrog.HR_VERSION);
		versionInfo.setEnabled(false);
		versionPanel.add(versionInfo);
		versionPanel.add(Box.createHorizontalGlue());
		
		verticalPanel.add(versionPanel);
		add(verticalPanel);
		pack();
	}

	private void getRecentSites() {
		recentSites = null;
	    try {
	        ObjectInputStream in = new ObjectInputStream(new FileInputStream(RECENT_SITES_FILE));
	        recentSites = (ArrayList<Site>) in.readObject(); 
	        in.close();
	    }
	    catch(Exception e) {
	    	recentSites = new ArrayList<Site>(); //empty
	    }
	}

	private JButton createRecentSiteButton(Site site) {
		String text = "<html><center>"+site.getSiteName()+"<br>"+Site.dateFormat.format(site.getLastModified())+"</center></html>";
		JButton siteButton = new JButton(text);
		siteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				//IdentiFrog.LOGGER.writeMessage("Clicked on a recent site.");
				loadSite(site.getDatafilePath());
			}
		});
		return siteButton;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == createSite) {
			NewSiteDialog nsd = new NewSiteDialog(this);
			nsd.setVisible(true);
		} else
		if (e.getSource() == openSite) {
			JFileChooser f = new JFileChooser();
			ExtensionFileFilter xmlFilter = new ExtensionFileFilter();
			xmlFilter.addExactFile(IdentiFrog.DB_FILENAME);
			xmlFilter.setDescription("IdentiFrog Site Files ("+IdentiFrog.DB_FILENAME+")");
	        f.setFileFilter(xmlFilter);
	        int result = f.showOpenDialog(this);
	        if (result == JFileChooser.APPROVE_OPTION) {
	        	if (f.getSelectedFile().getName().equals(IdentiFrog.DB_FILENAME)) {
	        		//valid
	        		loadSite(f.getCurrentDirectory().getAbsolutePath());
	        		dispose();
	        	} else {
	        		new ErrorDialog("The selected XML file is not an IdentiFrog site file.");
	        	}
	        } else {
	        	dispose();
	        }
		}
	}
	
	public void createSite(String location, String siteName){
		FolderHandler fh = new FolderHandler(location+File.separator+siteName);
		XMLHandler file = new XMLHandler(fh.getFileNamePath());
		if (!fh.FoldersExist()) {
			fh.createFolders();
			file.CreateXMLFile();
		}
		File f = new File(fh.getFileNamePath());
		if (f.exists() && f.length() == 0) {
			file.CreateXMLFile();
		}
		loadSite(fh.getFileNamePath());
		/*
		// create an instance of the MainFrame
		MainFrame frame = new MainFrame(fh);
		// validate frames that have preset sizes
		// pack frames that have useful preferred size info, e.g. from their layout
		
		/*if (packFrame) {
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
		//splash.dispose();
		dispose();
		frame.setVisible(true);*/
	}
	
	public void loadSite(String dataFilePath){
		IdentiFrog.LOGGER.writeMessage("Loading site datafile: "+dataFilePath);
		
		
		File dfile = new File(dataFilePath);
		if (!dfile.exists()) {
			//does not exist
		    JOptionPane.showMessageDialog(null, "Could not open site, the datafile.xml file does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		FolderHandler fh = new FolderHandler(dataFilePath);
		// create an instance of the MainFrame
		MainFrame frame = new MainFrame(fh);
		
		//gathersiteinfo
		IdentiFrog.LOGGER.writeMessage("Gathering information to update most recent list");
		Site newSite = new Site();
		newSite.setDatafilePath(dataFilePath);
		newSite.setLastModified(new Date());
		//String siteName = dfile.getParent();
		String siteName = dfile.getAbsolutePath();
		//remove final slash if there is one for some reason.
		if (Character.toString(siteName.charAt(siteName.length()-1)).equals(File.separator)) {
			IdentiFrog.LOGGER.writeMessage("Sitename final char is a slash.");
			siteName = siteName.substring(0, siteName.length() - 2);
		}
		//remove datafile from sitename
		if (siteName.endsWith(File.separator + IdentiFrog.DB_FILENAME)) {
		    siteName = siteName.substring(0, siteName.lastIndexOf(File.separator));
		}
	    siteName = siteName.substring(siteName.lastIndexOf(File.separator) + 1, siteName.length());
		newSite.setSiteName(siteName);
		
		IdentiFrog.LOGGER.writeMessage("Inject list with site: "+newSite);
		
		//update recents list
	    boolean updated = false;
	    //update existing entry if it exists.
		if (recentSites.contains(newSite)) {
			recentSites.set(recentSites.indexOf(newSite), newSite);
			updated = true;
		}
		
		Site leastRecentSite = null;
		if (recentSites.size() < 3 && !updated) {
			//list not full
			recentSites.add(newSite);
			updated = true;
		}
		
		//sites are full, existing one not available. find the oldest one and replace it.
		if (!updated) {
			for (Site site : recentSites) {
				if (leastRecentSite == null || site.getLastModified().before(leastRecentSite.getLastModified())) {
					leastRecentSite = site;
				}
			}
			recentSites.set(recentSites.indexOf(leastRecentSite), newSite);
		}
		
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(ProjectManagerFrame.RECENT_SITES_FILE));
			out.writeObject(recentSites);
			out.close();
			out.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			IdentiFrog.LOGGER.writeException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			IdentiFrog.LOGGER.writeException(e);
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
		dispose();
		if (mf != null) {
			mf.dispose();
		}
		frame.setVisible(true);
	}
}
