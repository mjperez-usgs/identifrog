package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.ExtensionFileFilter;
import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.Site;
import gov.usgs.identifrog.DataObjects.GitHubRelease;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.ui.StatusBar;

import java.awt.Dimension;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

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
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class ProjectManagerFrame extends JDialog implements ActionListener {
	public static final String RECENT_SITES_FILE = IdentiFrog.getDataDir() + "recentsites.idf";
	private JButton openSite, createSite;
	private ArrayList<Site> recentSites;
	private MainFrame mf;
	private StatusBar statusBar;
	private ImageIcon imageWarning = new ImageIcon(MainFrame.class.getResource("/resources/IconWarning16.png"));
	private ImageIcon imageOK = new ImageIcon(MainFrame.class.getResource("/resources/IconOK16.png"));
	private ImageIcon imageUpdateAvailable = new ImageIcon(MainFrame.class.getResource("/resources/IconUpdateAvailable16.png"));
	private ImageIcon imageHeartbeat = new ImageIcon(MainFrame.class.getResource("/resources/IconHeartbeat16.png"));
	private ImageIcon imageGrayHeartbeat = new ImageIcon(MainFrame.class.getResource("/resources/IconGrayHeartbeat16.png"));

	public ProjectManagerFrame() {
		setupFrame();
		new UpdateCheckerThread().execute();
	}

	public ProjectManagerFrame(MainFrame mf) {
		this.mf = mf;
		setupFrame();
	}

	private void setupFrame() {
		// TODO Auto-generated method stub
		setMinimumSize(new Dimension(400, 300));
		setTitle("IdentiFrog Project Manager");
		//new ImageIcon(this.getClass().getClassLoader().getResource("/resources/IconFrog.png"));
		setIconImage(new ImageIcon(this.getClass().getResource("/resources/IconFrog.png")).getImage());
		if (mf == null) {
			//close if this is opened by main()
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
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
		openSite.setMinimumSize(new Dimension(132, 132));
		openSite.addActionListener(this);

		//openSite.setIcon(new ImageIcon(img));
		ImageIcon createIcon = new ImageIcon(this.getClass().getResource("/resources/IconBook128.png"));
		createSite = new JButton("Create new site", createIcon);
		createSite.setVerticalTextPosition(SwingConstants.BOTTOM);
		createSite.setHorizontalTextPosition(SwingConstants.CENTER);
		createSite.setMinimumSize(new Dimension(132, 132));
		createSite.addActionListener(this);
		//img = Toolkit.getDefaultToolkit().getImage("IconBook128.png");
		//createSite.setIcon(new ImageIcon(img));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(openSite);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(createSite);
		buttonPanel.add(Box.createHorizontalGlue());

		JPanel verticalPanel = new JPanel();
		verticalPanel.setLayout(new BoxLayout(verticalPanel, BoxLayout.PAGE_AXIS));
		verticalPanel.add(Box.createVerticalGlue());
		verticalPanel.add(buttonPanel);
		verticalPanel.add(Box.createVerticalGlue());

		//Recent sites panel
		JPanel recentSitesPanel = new JPanel();
		Border border = BorderFactory.createEtchedBorder();
		TitledBorder title = BorderFactory.createTitledBorder(border, "Recently opened sites");
		title.setTitleJustification(TitledBorder.CENTER);
		recentSitesPanel.setBorder(title);
		recentSitesPanel.setLayout(new BoxLayout(recentSitesPanel, BoxLayout.LINE_AXIS));
		recentSitesPanel.add(Box.createHorizontalGlue());

		//start population
		getRecentSites();
		for (Site site : recentSites) {
			JButton recentSite = createRecentSiteButton(site);
			File recentDBFile = new File(site.getDatafilePath());
			recentSite.setEnabled(recentDBFile.exists());
			if (!recentDBFile.exists()) {
				recentSite.setToolTipText("<html>This project no longer exists:<br>" + site.getDatafilePath() + "</html>");
			}
			recentSitesPanel.add(recentSite);
			recentSitesPanel.add(Box.createHorizontalGlue());
		}

		if (recentSites.size() <= 0) {
			JLabel noSites = new JLabel("No recently opened sites");
			noSites.setEnabled(false);
			recentSitesPanel.add(noSites);
		}

		//end population
		recentSitesPanel.add(Box.createHorizontalGlue());
		verticalPanel.add(recentSitesPanel);

		statusBar = new StatusBar();
		statusBar.setRightMessage("IdentiFrog " + IdentiFrog.HR_VERSION);

		verticalPanel.add(statusBar);
		add(verticalPanel);
		pack();
	}

	private void getRecentSites() {
		recentSites = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(RECENT_SITES_FILE));
			recentSites = (ArrayList<Site>) in.readObject();
			in.close();
			IdentiFrog.LOGGER.writeMessage("Loaded recent sites file, contents:");
			for (Site s : recentSites) {
				IdentiFrog.LOGGER.writeMessage(s.toString());
			}
		} catch (Exception e) {
			recentSites = new ArrayList<Site>(); //empty
		}
	}

	private JButton createRecentSiteButton(final Site site) {
		String text = "<html><center>" + site.getSiteName() + "<br>" + Site.dateFormat.format(site.getLastModified()) + "</center></html>";
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
		} else if (e.getSource() == openSite) {
			JFileChooser f = new JFileChooser();
			ExtensionFileFilter xmlFilter = new ExtensionFileFilter();
			xmlFilter.addExactFile(IdentiFrog.DB_FILENAME);
			xmlFilter.setDescription("IdentiFrog Site Files (" + IdentiFrog.DB_FILENAME + ")");
			f.setFileFilter(xmlFilter);
			f.setCurrentDirectory(new File(System.getProperty("user.home")));
			int result = f.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				if (f.getSelectedFile().getName().equals(IdentiFrog.DB_FILENAME)) {
					//valid
					loadSite(f.getSelectedFile().getAbsolutePath());
					dispose();
				} else {
					JOptionPane.showMessageDialog(ProjectManagerFrame.this, "The selected XML file is not an IdentiFrog site file.", "Invalid Project File", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	public void createSite(String location, String siteName) {
		IdentiFrog.LOGGER.writeMessage("Creating new site in folder " + location + " with name " + siteName);
		XMLFrogDatabase.setFile(new File(location + File.separator + siteName + File.separator + IdentiFrog.DB_FILENAME));
		if (!XMLFrogDatabase.siteFoldersExist()) {
			XMLFrogDatabase.createFolders();
		}
		XMLFrogDatabase.createXMLFile();
		loadSite(XMLFrogDatabase.getFileNamePath());
	}

	public void loadSite(String dataFilePath) {

		IdentiFrog.LOGGER.writeMessage("Project manager is preparing to load sitefile: " + dataFilePath);
		if (!dataFilePath.endsWith(IdentiFrog.DB_FILENAME)) {
			IdentiFrog.LOGGER.writeError("LOADING XML DB THAT IS NOT NAMED AS IDENTIFROG.DB_FILENAME: " + dataFilePath);
			return;
		}
		File dfile = new File(dataFilePath);
		if (!dfile.exists()) {
			//does not exist
			JOptionPane.showMessageDialog(null, "Could not open site, the datafile.xml file does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		XMLFrogDatabase.setFile(dfile);

		//gathersiteinfo
		IdentiFrog.LOGGER.writeMessage("Gathering information to update most recent list");
		Site newSite = new Site();
		newSite.setDatafilePath(dataFilePath);
		newSite.setLastModified(new Date());
		//String siteName = dfile.getParent();
		String siteName = dfile.getParent(); //datafile parent
		siteName = siteName.substring(siteName.lastIndexOf(File.separator) + 1, siteName.length());
		newSite.setSiteName(siteName);

		IdentiFrog.LOGGER.writeMessage("Inject list with site: " + newSite);

		updateRecentlyOpened(dfile.getAbsolutePath());
		/*
		 * //update recents list boolean updated = false; //update existing
		 * entry if it exists. if (recentSites.contains(newSite)) {
		 * recentSites.set(recentSites.indexOf(newSite), newSite); updated =
		 * true; }
		 * 
		 * Site leastRecentSite = null; if (recentSites.size() < 3 && !updated)
		 * { //list not full recentSites.add(newSite); updated = true; }
		 * 
		 * //sites are full, existing one not available. find the oldest one and
		 * replace it. if (!updated) { for (Site site : recentSites) { if
		 * (leastRecentSite == null ||
		 * site.getLastModified().before(leastRecentSite.getLastModified())) {
		 * leastRecentSite = site; } }
		 * recentSites.set(recentSites.indexOf(leastRecentSite), newSite); }
		 * 
		 * ObjectOutputStream out; try { out = new ObjectOutputStream(new
		 * FileOutputStream(ProjectManagerFrame.RECENT_SITES_FILE));
		 * out.writeObject(recentSites); out.close(); out.flush(); } catch
		 * (FileNotFoundException e) { // TODO Auto-generated catch block
		 * IdentiFrog.LOGGER.writeException(e); } catch (IOException e) { //
		 * TODO Auto-generated catch block IdentiFrog.LOGGER.writeException(e);
		 * }
		 */

		// create an instance of the MainFrame
		MainFrame frame = new MainFrame();
		IdentiFrog.activeMainFrame = frame;
		frame.setPreferredSize(new Dimension(600, 450));
		frame.pack();

		// center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//prevent oversizing

		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}

		frame.setLocationRelativeTo(null);
		//frame.setLocation(0, 0);
		dispose();
		if (mf != null) {
			//dispose of old project
			mf.dispose();
		}
		frame.setVisible(true);
	}

	/**
	 * Updates the file of recently opened sites
	 * 
	 * @param fileNamePath
	 */
	public static void updateRecentlyOpened(String fileNamePath) {
		IdentiFrog.LOGGER.writeMessage("Updating recent sites list with datapath" + fileNamePath);
		// gather site info
		Site newSite = new Site();
		newSite.setDatafilePath(fileNamePath);
		newSite.setLastModified(new Date());

		if (fileNamePath.endsWith(File.separator)) {
			IdentiFrog.LOGGER.writeMessage("Sitename final char is a slash.");
			fileNamePath = fileNamePath.substring(0, fileNamePath.length() - 2);
		}
		//remove \datafile from sitename
		if (fileNamePath.endsWith(File.separator + IdentiFrog.DB_FILENAME)) {
			fileNamePath = fileNamePath.substring(0, fileNamePath.lastIndexOf(File.separator));
		}
		String siteName = FilenameUtils.getBaseName(fileNamePath);

		siteName = siteName.substring(siteName.lastIndexOf(File.separator) + 1, siteName.length());
		newSite.setSiteName(siteName);

		// load recent site info for parsing
		ArrayList<Site> recentSites = null;
		File recentSitesFile = new File(ProjectManagerFrame.RECENT_SITES_FILE);
		if (recentSitesFile.exists()) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(ProjectManagerFrame.RECENT_SITES_FILE));
				recentSites = (ArrayList<Site>) in.readObject();
				in.close();
			} catch (Exception e) {
				IdentiFrog.LOGGER.writeExceptionWithMessage("Error reading recent sites list:", e);
				recentSites = new ArrayList<Site>(); // empty
			}
		} else {
			File dataFolder = recentSitesFile.getParentFile();
			dataFolder.mkdirs();
			recentSites = new ArrayList<Site>();
		}

		recentSites.add(newSite);
		HashSet<Site> filters = new HashSet<Site>(recentSites); //removes duplicates
		recentSites = new ArrayList<Site>(filters);
		while (recentSites.size() > 3) {
			recentSites.remove(3);
		}

		Collections.sort(recentSites);
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(ProjectManagerFrame.RECENT_SITES_FILE));
			out.writeObject(recentSites);
			out.close();
			out.flush();
		} catch (FileNotFoundException e) {
			IdentiFrog.LOGGER.writeException(e);
		} catch (IOException e) {
			IdentiFrog.LOGGER.writeException(e);
		}
	}

	/**
	 * Execute file download in a background thread and update the progress.
	 * 
	 * @author www.codejava.net
	 *
	 */
	class UpdateCheckerThread extends SwingWorker<Void, Void> {
		String serverResponse = null;
		private boolean hadError = false;

		public UpdateCheckerThread() {
			statusBar.setMessageWithIcon("Checking for updates", imageHeartbeat);
		}

		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() {
			try {
				serverResponse = IOUtils.toString(new URL("https://api.github.com/repos/mjperez-usgs/IdentiFrog/releases"));
			} catch (IOException e) {
				IdentiFrog.LOGGER.writeExceptionWithMessage("IOException checking for updates:", e);
				hadError = true;
			}
			return null;
		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			//Check for actual update.
			if (serverResponse != null) {
				boolean shouldShowUpdateText = false;
				Object obj = JSONValue.parse(serverResponse);
				if (obj instanceof JSONArray) {
					JSONArray releaseArray = (JSONArray) obj;
					IdentiFrog.SERVER_RELEASES_INFO = releaseArray;
					IdentiFrog.LOGGER.writeMessage("Number of releases on server: " + releaseArray.size());
					for (Object release : releaseArray) {
						if (release instanceof JSONObject) {
							GitHubRelease ghb = new GitHubRelease((JSONObject) release);
							IdentiFrog.LOGGER.writeMessage("Release from server: " + ghb);
							if (ghb.getAttachments().size() > 0) {
								String relHRVersion = ghb.getTagName();
								if (relHRVersion.startsWith("v")) {
									relHRVersion = relHRVersion.substring(1);
								}
								int versionComparison = IdentiFrog.versionCompare(relHRVersion, IdentiFrog.HR_VERSION);
								if (versionComparison > 0) {
									//update available
									shouldShowUpdateText = true;
									break;
								}
							}
						}
					}
					if (shouldShowUpdateText) {
						statusBar.setMessageWithIcon("Update available", imageUpdateAvailable);
					} else {
						statusBar.setMessageWithIcon("No available updates", imageOK);
					}
				} else {
					statusBar.setMessageWithIcon("Error checking for updates", imageWarning);
				}
			} else {
				statusBar.setMessageWithIcon("Error checking for updates", imageWarning);
			}
		}
	}
}
