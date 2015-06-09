package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.ChoiceDialog;
import gov.usgs.identifrog.DialogImageFileChooser;
import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.ImageViewer;
import gov.usgs.identifrog.MarkExport;
import gov.usgs.identifrog.MatchingDialog;
import gov.usgs.identifrog.SaveAsDialog;
import gov.usgs.identifrog.SearchCriteriaDialog;
import gov.usgs.identifrog.Site;
import gov.usgs.identifrog.WorkingAreaPanel;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.Operations.XLSXTemplateGeneratorFrame;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

/**
 * <p>
 * Title: MainFrame.java
 * <p>
 * Description: Main Frame of the IdentiFrog GUI
 * 
 * @author Hidayatullah Ahsan 2011
 * @author Oksana V. Kelly 2008
 * @author Steven P. Miller for IdentiFrog Team 2005
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	private Frog matchFrog = new Frog();
	private boolean changesMade = false;

	private String workingFolder;
	private File currentFile;

	private Preferences root = Preferences.userRoot();
	public final Preferences node = root.node("edu/isu/aadis/defaults");

	private ImageIcon imageNew = new ImageIcon(MainFrame.class.getResource("/resources/IconNew32.png"));
	private ImageIcon imageDelete = new ImageIcon(MainFrame.class.getResource("/resources/IconDelete32.png"));
	private ImageIcon imageHelp = new ImageIcon(MainFrame.class.getResource("/resources/IconHelp32.png"));

	private ImageIcon imageFind = new ImageIcon(MainFrame.class.getResource("/resources/IconFind32.png"));
	private ImageIcon imageEdit = new ImageIcon(MainFrame.class.getResource("/resources/IconEdit32.png"));

	private JButton bFind = new JButton("", imageFind); // disabled icons are
														// set in init()

	private JButton bEdit = new JButton("", imageEdit);
	private JButton bDelete = new JButton("", imageDelete);
	private JButton bOpen = new JButton("", imageNew);
	private JButton bHelp = new JButton("", imageHelp);
	//
	@SuppressWarnings("unused")
	private JButton btnPrevious = new JButton("Previous Page", new ImageIcon(WorkingAreaPanel.class.getResource("IconButtonPrevious32.png")));
	//
	private JToolBar barButtons = new JToolBar("", SwingConstants.HORIZONTAL);

	private MarkExport markExport = new MarkExport();
	//private ThumbnailCreator thumbnailCreator;
	private SaveAsDialog saveAsDialog;
	private ParametersDialog parametersDialog;
	private SearchCriteriaDialog searchCriteriaDialog;
	private MatchingDialog matchingDialog;

	private JPanel contentPanel;
	private WorkingAreaPanel workingAreaPanel;

	private JMenuBar mainMenu = new JMenuBar();
	private JMenu menuFile = new JMenu("File");
	private JMenuItem menuItemCreateXLSX = new JMenuItem("Create Batch Template", new ImageIcon(
			MainFrame.class.getResource("/resources/IconXLS16.png")));
	private JMenuItem MenuItemNew = new JMenuItem("New Frog Image", new ImageIcon(MainFrame.class.getResource("/resources/IconNew16.png")));
	private JMenuItem MenuItemMarkExport = new JMenuItem("Export to MARK");
	private JMenuItem menuItemFileExit = new JMenuItem("Exit");
	private JMenu menuHelp = new JMenu("Help");
	private JMenuItem menuItemHelpAbout = new JMenuItem("About");
	private JMenu menuDatabase = new JMenu("Project");
	private JMenuItem MenuItemSearch = new JMenuItem("Search for a Match", new ImageIcon(MainFrame.class.getResource("/resources/IconFind16.png")));
	private JMenuItem menuItemProjectManager = new JMenuItem("Project Manager");
	// private JMenuItem menuItemOpenSite = new JMenuItem("Open Existing Site");
	private JMenuItem menuItemSaveSiteAs = new JMenuItem("Save Site As");
	private JMenuItem MenuItemEdit = new JMenuItem("Edit Frog", new ImageIcon(MainFrame.class.getResource("/resources/IconEdit16.png")));
	private JMenuItem MenuItemDelete = new JMenuItem("Delete Frog", new ImageIcon(MainFrame.class.getResource("/resources/IconDelete16.png")));
	private JMenuItem MenuItemHelp = new JMenuItem("User Manual", new ImageIcon(MainFrame.class.getResource("/resources/IconHelp16.png")));
	private JCheckBoxMenuItem CheckBoxMenuItemShowThumbs = new JCheckBoxMenuItem("Show Thumbnails", true);
	private JMenuItem MenuItemParams = new JMenuItem("Rows per Page");
	private JMenuItem MenuItemSearchCriteria = new JMenuItem("Search Criteria");

	private JLabel lStatusBar = new JLabel("STATUS: Up to 10 Frogs Per Page");

	private BorderLayout borderLayout1 = new BorderLayout();
	private TitledBorder titledBorder1 = new TitledBorder("");
	private int viewerX = 0;
	private int viewerY = 0;

	/**
	 * MainFrame Constructor
	 */
	public MainFrame() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		setIconImage(getToolkit().getImage(getClass().getResource("/resources/IconFrog.png")));
		workingFolder = XMLFrogDatabase.getMainFolder();
		//thumbnailCreator = new ThumbnailCreator(thumbnailFolder);
		this.setTitle("IdentiFrog - " + XMLFrogDatabase.getFileNamePath());
		try {
			init();
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeMessage("MainFrame.MainFrame() Exception");
			IdentiFrog.LOGGER.writeException(e);
		}
	}

	/**
	 * Sets buttons on or off, depending if a row is selected.
	 * 
	 * @param state
	 */
	public void setButtonState(boolean state) {
		bFind.setEnabled(state);
		bEdit.setEnabled(state);
		bDelete.setEnabled(state);
		MenuItemDelete.setEnabled(state);
		MenuItemEdit.setEnabled(state);
		MenuItemSearch.setEnabled(state);
	}

	/**
	 * Loads the XML DB into memory and updates the recent projects list.
	 * Additionally causes the UI to refresh.
	 */
	private void read() {
		XMLFrogDatabase.setFile(new File(XMLFrogDatabase.getFileNamePath()));
		XMLFrogDatabase.loadXMLFile();
		//ArrayList<Frog> frogs = new XMLFrogDatabase(XMLFrogDatabase.getFileNamePath())
		//		.loadXMLFile();
		updateRecentlyOpened(XMLFrogDatabase.getFileNamePath());

		//workingAreaPanel.setFrogsData(frogData);
		workingAreaPanel.refreshRows();

		// update cells
		updateCells();

		this.setTitle("IdentiFrog " + XMLFrogDatabase.getFileNamePath());
	}

	private void updateRecentlyOpened(String fileNamePath) {
		// gather site info
		Site newSite = new Site();
		newSite.setDatafilePath(fileNamePath);
		newSite.setLastModified(new Date());
		File file = new File(fileNamePath);
		String siteName = file.getParent();
		siteName = siteName.substring(siteName.lastIndexOf(File.separator) + 1, siteName.length());
		newSite.setSiteName(siteName);

		// load recent site info for parsing
		ArrayList<Site> recentSites = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(ProjectManagerFrame.RECENT_SITES_FILE));
			recentSites = (ArrayList<Site>) in.readObject();
			in.close();
		} catch (Exception e) {
			recentSites = new ArrayList<Site>(); // empty
		}

		boolean updated = false;
		// update existing entry if it exists.
		if (recentSites.contains(newSite)) {
			recentSites.set(recentSites.indexOf(newSite), newSite);
			updated = true;
		}

		Site leastRecentSite = null;
		if (recentSites.size() < 3 && !updated) {
			// list not full
			recentSites.add(newSite);
			updated = true;
		}

		// sites are full, existing one not available. find the oldest one and
		// replace it.
		if (!updated) {
			for (Site site : recentSites) {
				if (leastRecentSite == null || leastRecentSite.getLastModified().before(site.getLastModified())) {
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
	}

	/**
	 * Component Initialization
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		// startup
		//XMLFrogDatabase.setFile(new File(XMLFrogDatabase.getFileNamePath()));
		XMLFrogDatabase.loadXMLFile();

		contentPanel = (JPanel) getContentPane();
		workingAreaPanel = new WorkingAreaPanel(MainFrame.this);

		contentPanel.setLayout(borderLayout1);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

		setSize((int) (screen.getWidth() * 0.95), (int) (screen.getHeight() * 0.92));
		// setTitle("IdentiFrog Beta");

		lStatusBar.setBorder(titledBorder1);
		// put menu bar here
		menuItemFileExit.addActionListener(new MainFrame_menuItemFileExit_ActionAdapter(this));
		menuItemHelpAbout.addActionListener(new MainFrame_menuItemHelpAbout_ActionAdapter(this));
		MenuItemNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke('N', InputEvent.CTRL_MASK, false));
		MenuItemNew.addActionListener(new MainFrame_MenuItemNew_actionAdapter(this));
		MenuItemMarkExport.addActionListener(new MainFrame_MenuItemMarkExport_actionAdapter(this));
		menuItemCreateXLSX.addActionListener(new MainFrame_menuItemCreateXLSX_actionAdapter(this));
		// find button verify input when focus target
		bFind.setVerifyInputWhenFocusTarget(true);
		// buttons tool tip text
		bFind.setToolTipText("Search for a Match...");
		bEdit.setToolTipText("Edit Frog...");
		bDelete.setToolTipText("Delete Frog...");
		bOpen.setToolTipText("Enter New Frog Image...");
		bHelp.setToolTipText("Help...");
		// action listeners for the buttons
		bFind.addActionListener(new MainFrame_butFind_actionAdapter(this));
		bEdit.addActionListener(new MainFrame_butEdit_actionAdapter(this));
		bDelete.addActionListener(new MainFrame_butDelete_actionAdapter(this));
		bOpen.addActionListener(new MainFrame_butOpen_actionAdapter(this));
		bHelp.addActionListener(new MainFrame_butHelp_actionAdapter(this));
		//
		MenuItemSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke('S', InputEvent.CTRL_MASK, false));
		MenuItemEdit.setAccelerator(javax.swing.KeyStroke.getKeyStroke('E', InputEvent.CTRL_MASK, false));
		CheckBoxMenuItemShowThumbs.setAccelerator(javax.swing.KeyStroke.getKeyStroke('T', InputEvent.CTRL_MASK, false));
		MenuItemParams.setAccelerator(javax.swing.KeyStroke.getKeyStroke('P', InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK, false));
		MenuItemSearchCriteria.setAccelerator(javax.swing.KeyStroke.getKeyStroke('S', InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK, false));
		MenuItemDelete.setAccelerator(javax.swing.KeyStroke.getKeyStroke('D', InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK, false));
		//
		MenuItemSearch.addActionListener(new MainFrame_MenuItemSearch_actionAdapter(this));
		MenuItemEdit.addActionListener(new MainFrame_MenuItemEdit_actionAdapter(this));
		MenuItemDelete.addActionListener(new MainFrame_MenuItemDelete_actionAdapter(this));
		MenuItemHelp.addActionListener(new MainFrame_MenuItemHelp_actionAdapter(this));
		CheckBoxMenuItemShowThumbs.addActionListener(new MainFrame_CheckBoxMenuItemShowThumbs_actionAdapter(this));
		MenuItemParams.addActionListener(new MainFrame_MenuItemParams_actionAdapter(this));
		MenuItemSearchCriteria.addActionListener(new MainFrame_MenuItemSearchCriteria_actionAdapter(this));

		// ActionListeners for Add Site Functionality
		menuItemProjectManager.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				/*
				 * // open file chooser in user's home directory JFileChooser
				 * fileChooser = new JFileChooser(System
				 * .getProperty("user.home")); // if user selects the OK button
				 * then perform the following: // XXX Add comments here if
				 * (fileChooser.showSaveDialog(null) ==
				 * JFileChooser.APPROVE_OPTION) { String sitePath =
				 * fileChooser.getSelectedFile() .getAbsolutePath(); // setup
				 * data folders and database xml file FolderHandler fhLocal =
				 * new FolderHandler(sitePath); XMLHandler file = new
				 * XMLHandler(fhLocal.getFileNamePath()); if
				 * (!fhLocal.FoldersExist()) { fhLocal.createFolders();
				 * file.CreateXMLFile(); File f = new
				 * File(fhLocal.getFileNamePath()); if (f.exists() && f.length()
				 * == 0) { file.CreateXMLFile(); } fh = fhLocal; read(); } else
				 * { String message = "Site name " +
				 * fileChooser.getSelectedFile().getName() + " already exists.";
				 * JOptionPane.showConfirmDialog(null, message,
				 * "Site Already Exists", JOptionPane.OK_OPTION); } }
				 */
				ProjectManagerFrame pmf = new ProjectManagerFrame(MainFrame.this);
				pmf.setLocationRelativeTo(MainFrame.this);
				pmf.setVisible(true);
			}
		});

		/*
		 * menuItemOpenSite.addActionListener(new ActionListener() {
		 * 
		 * @Override public void actionPerformed(ActionEvent arg0) { // open
		 * file chooser in user's home directory JFileChooser fileChooser = new
		 * JFileChooser(System .getProperty("user.home")); // if user selects
		 * the OK button then perform the following: // XXX Add comments here if
		 * (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		 * String sitePath = fileChooser.getSelectedFile().getParent();
		 * FolderHandler fhLocal = new FolderHandler(sitePath); fh = fhLocal;
		 * read(); } } });
		 */

		menuItemSaveSiteAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// open file chooser in user's home directory
				JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
				// if user selects the OK button then perform the following:
				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					String sitePath = fileChooser.getSelectedFile().getAbsolutePath();
					// setup data folders and database xml file
					if (new File(sitePath).exists()) {
						String message = "Site with the name/path " + fileChooser.getSelectedFile().getName() + " already exists.";
						JOptionPane.showConfirmDialog(null, message, "Site Exists", JOptionPane.OK_OPTION);
					} else {
						try {
							XMLFrogDatabase.createCopy(new File(sitePath));
							//copyFolder(new File(XMLFrogDatabase.getMainFolder().toString()),
							//		new File(sitePath));new File(sitePath + File.separator+ fh.getFileName()));
							/*
							 * XMLFrogDatabase copyHandler = new
							 * XMLFrogDatabase() sitePath + File.separator +
							 * fh.getFileName()), getFrogData() .getFrogs());
							 * copyHandler.WriteXMLFile();
							 */
							read();
						} catch (IOException e) {
							IdentiFrog.LOGGER.writeException(e);
						}
					}
				}
			}
		});

		// add buttons to the toolbar
		barButtons.add(bOpen, null);
		barButtons.add(new JToolBar.Separator(new Dimension(0, 8)), null);
		barButtons.add(bFind, null);
		barButtons.add(bEdit, null);
		barButtons.add(bDelete, null);
		barButtons.add(new JToolBar.Separator(new Dimension(0, 8)), null);
		// barButtons.add(btnPrevious);
		barButtons.add(new JToolBar.Separator(new Dimension(0, 8)), null);
		barButtons.add(bHelp);

		// add menu items
		menuFile.add(menuItemCreateXLSX);
		menuFile.add(MenuItemMarkExport);
		menuFile.addSeparator();
		menuFile.add(menuItemProjectManager);
		menuFile.add(menuItemSaveSiteAs);
		menuFile.addSeparator();
		menuFile.add(menuItemFileExit);

		menuHelp.add(MenuItemHelp);
		menuHelp.addSeparator();
		menuHelp.add(menuItemHelpAbout);
		mainMenu.add(menuFile);
		menuDatabase.add(MenuItemSearchCriteria);
		menuDatabase.addSeparator();

		menuDatabase.add(MenuItemNew);
		menuDatabase.add(MenuItemSearch);
		menuDatabase.add(MenuItemEdit);
		menuDatabase.add(MenuItemDelete);
		menuDatabase.addSeparator();
		menuDatabase.add(CheckBoxMenuItemShowThumbs);
		menuDatabase.add(MenuItemParams);
		mainMenu.add(menuDatabase);
		mainMenu.add(menuHelp);
		setJMenuBar(mainMenu);

		// icon states
		bFind.setEnabled(false); // default to false cause nothing is selected
									// by default
		bEdit.setEnabled(false);
		bDelete.setEnabled(false);
		MenuItemDelete.setEnabled(false);
		MenuItemEdit.setEnabled(false);
		MenuItemSearch.setEnabled(false);

		//
		contentPanel.add(barButtons, BorderLayout.NORTH);
		contentPanel.add(workingAreaPanel, BorderLayout.CENTER);
	}

	// File | Exit action performed
	public void menuItemFileExit_actionPerformed(ActionEvent e) {
		closeAction();
	}

	/**
	 * Add frog biometrics information
	 * 
	 * @param image
	 */
	private void addFrog(File image) {
		// step 1: make sure that the same image is not being recorded more than
		// once
		String localFilename = image.getName();
		localFilename = localFilename.substring(0, (localFilename.length() - 4)) + ".png";
		File testFile = new File(XMLFrogDatabase.getThumbnailFolder() + localFilename);
		if (testFile.exists()) {
			JOptionPane.showMessageDialog(this, "The image '" + image.getName() + "' has already been entered.\n"
					+ "To re-enter the image, first delete it from the database.");
			return;
		}
		FrogEditor addFrogWindow = new FrogEditor(MainFrame.this, "Frog Information", image);
		addFrogWindow.pack();
		addFrogWindow.setLocationRelativeTo(this);
		addFrogWindow.setVisible(true);
		Frog newFrog = addFrogWindow.getFrog();
		if (newFrog != null) {
			XMLFrogDatabase.addFrog(newFrog);
			XMLFrogDatabase.writeXMLFile();
		}
		updateCells();
	}

	/**
	 * Edit frog from the selected row
	 * 
	 * @throws Exception
	 */
	private void editFrog() throws Exception {
		int localID = workingAreaPanel.getSelectedFrog_Id();
		if (localID == -1) {
			return;
		}
		Frog localFrog = XMLFrogDatabase.searchFrogByID(localID);
		FrogEditor editFrogFrame = new FrogEditor(this, "Edit Frog", localFrog);

		/*
		 * EditFrog editFrogFrame = new EditFrog(MainFrame.this,
		 * "Edit Frog Information", true, localFrog);
		 */
		editFrogFrame.pack();
		// garbage collector
		//System.gc();
		// TODO center edit frog frame
		// editFrogFrame.setLocation(getX(), getY());
		editFrogFrame.setVisible(true);
		localFrog = editFrogFrame.getFrog();
		// frogData.replaceFrog(localID, localFrog);
		// update cells
		updateCells();
		// write xml file
		XMLFrogDatabase.writeXMLFile();
	}

	/**
	 * Delete frog from the selected row
	 * 
	 * @throws Exception
	 */
	private void deleteFrog() throws Exception {
		IdentiFrog.LOGGER.writeError("Deleting frog is not yet fully implemented!");
		int localID = workingAreaPanel.getSelectedFrog_Id();
		if (localID == -1) {
			return;
		}
		if (ChoiceDialog.choiceMessage("Deleting this frog will remove all images,\n"
				+ "signatures, and sitesamples for this frog.\nDelete this frog?") == 0) {
			Frog localFrog = XMLFrogDatabase.searchFrogByID(localID);
			//String localImageName = localFrog.getGenericImageName();
			//String localSignatureName = localFrog.getPathSignature();
			// check if exists then delete
			/*
			new File(XMLFrogDatabase.getImagesFolder() + localImageName).delete();
			new File(XMLFrogDatabase.getDorsalFolder() + localImageName).delete();
			new File(XMLFrogDatabase.getThumbnailFolder() + localImageName).delete();
			new File(XMLFrogDatabase.getBinaryFolder() + localImageName).delete();
			new File(XMLFrogDatabase.getSignaturesFolder() + localSignatureName).delete();*/
			XMLFrogDatabase.removeFrog(localID);
			// garbage collector
			System.gc();
			if (XMLFrogDatabase.getFrogs().size() == 1) {
				workingAreaPanel.refreshRows();
			}
			// update cells
			updateCells();
			// write xml file
			XMLFrogDatabase.writeXMLFile();
		}
	}

	/**
	 * Help | About action performed
	 */
	public void menuItemHelpAbout_actionPerformed(ActionEvent e) {
		AboutDialog dlg = new AboutDialog(this);
		// About dlg = new About(this);

		Dimension dialogSize = dlg.getPreferredSize();
		Dimension frameSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frameSize.width - dialogSize.width) / 2 + loc.x, (frameSize.height - dialogSize.height) / 2 + loc.y);
		dlg.setModal(true);
		dlg.pack();
		dlg.setVisible(true);
	}

	// Overridden so we can exit when window is closed
	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			menuItemFileExit_actionPerformed(null);
		}
	}

	public void setStatusBar(String status) {
		lStatusBar.setText(status);
	}

	protected void butClose_actionPerformed(ActionEvent e) {
		closeAction();
	}

	private void closeAction() {
		node.putBoolean("alreadyOpen", false);
		node.put("workingDir", workingFolder);
		node.putInt("maxPageRows", workingAreaPanel.getMaxPageRows());
		node.putDouble("threshold", workingAreaPanel.getThreshold());
		System.exit(0);
	}

	protected void butHelp_actionPerformed(ActionEvent e) {
		runManual();
	}

	/*
	 * public Image getIcon() { return
	 * getToolkit().getImage(getClass().getResource("/resources/IconFrog.png"));
	 * }
	 */

	protected void MenuItemNew_actionPerformed(ActionEvent e) {
		DialogImageFileChooser imageChooser = new DialogImageFileChooser(MainFrame.this, "Choose Frog Photograph...", false,
				System.getProperty("user.home"));
		String filename = imageChooser.getName();
		if (filename != null) {
			currentFile = new File(filename);
			workingFolder = currentFile.getParent();
			addFrog(currentFile);
		}
	}

	@SuppressWarnings("static-access")
	protected void MenuItemMarkExport_actionPerformed(ActionEvent e) {
		saveAsDialog = new SaveAsDialog(MainFrame.this, "Save As...", false, workingFolder);
		String filePath = saveAsDialog.getName();
		if (filePath != null) {
			try {
				// markExport.saveToFile(filePath);
				markExport.saveToMark(XMLFrogDatabase.getFrogs(), filePath);
			} catch (Exception ex) {
				new ErrorDialog("Cannot save the file.");
				ex.printStackTrace();
			}
		}
	}

	protected void menuItemCreateBatchXLSX_actionPerformed(ActionEvent e) {
		new XLSXTemplateGeneratorFrame(this);
		// String filePath = saveAsDialog.getName();

	}

	protected void MenuItemEdit_actionPerformed(ActionEvent e) {
		butEdit_actionPerformed(e);
	}

	protected void MenuItemSearch_actionPerformed(ActionEvent e) {
		butFind_actionPerformed(e);
	}

	protected void MenuItemDelete_actionPerformed(ActionEvent e) {
		butDelete_actionPerformed(e);
	}

	protected void butOpen_actionPerformed(ActionEvent e) {
		MenuItemNew_actionPerformed(e);
	}

	/* Match Button pressed */
	protected void butFind_actionPerformed(ActionEvent e) {
		IdentiFrog.LOGGER.writeMessage("Find/match is not implemented yet!");
		/*
		// simulation
		// ConfusionMatrix confmatrix = new ConfusionMatrix();
		// confmatrix.simulateIdentification();
		// Find Hamming Dist Threshold
		// ThresholdHammingDistance Threshold = new ThresholdHammingDistance();
		// double thresholdHammingDistance = Threshold.getThresholdHamDist();
		Integer[] tfrog = getSelectedRowDbId();
		if (tfrog.length == 0) {
			new ErrorDialog("You must select a row first.");
			return;
		}
		// TODO fix this
		// File dorspath = workingAreaPanel.getDorsalImageFileFromSelectedRow();
		File dorsalImage = new File(XMLFrogDatabase.getDorsalFolder()
				+ XMLFrogDatabase.searchFrogByID(workingAreaPanel.getSelectedFrog_Id()).getGenericImageName());
		setButtonsOn(false);
		// IdentiFrog.LOGGER.writeMessage("In butFind Action Performed tfrog[0].intValue() is "
		// +
		// tfrog[0].intValue());
		OpenMatchingDialog(dorsalImage, tfrog[0].intValue());
		*/
	}

	protected void butEdit_actionPerformed(ActionEvent e) {
		try {
			editFrog();
		} catch (Exception ex) {
			IdentiFrog.LOGGER.writeMessage("MainFrame.butEdit_actionPerfomed() Exception");
			IdentiFrog.LOGGER.writeMessage(ex.getMessage());
		}
	}

	protected void butDelete_actionPerformed(ActionEvent e) {
		try {
			deleteFrog();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void MenuItemHelp_actionPerformed(ActionEvent e) {
		runManual();
	}

	private void runManual() {
		String url = "http://code.google.com/p/identifrog/";
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (IOException e) {
				new ErrorDialog("Cannot open " + url + " IO exception.");
				IdentiFrog.LOGGER.writeException(e);
			} catch (URISyntaxException e) {
				new ErrorDialog("Cannot open " + url + ", invalid URI.");
				IdentiFrog.LOGGER.writeException(e);
			}
		} else {
			new ErrorDialog("Desktop services not supported on this OS. You can view the manual at " + url + ".");
		}
	}

	protected void CheckBoxMenuItemShowThumbs_actionPerformed(ActionEvent e) {
		workingAreaPanel.setShowThumbnails(CheckBoxMenuItemShowThumbs.isSelected());
	}

	public void setDBRows(int rows) {
		workingAreaPanel.setMaxPageRows(rows);
	}

	protected void MenuItemParams_actionPerformed(ActionEvent e) {
		OpenParametersDialog();
	}

	private void OpenParametersDialog() {
		if (parametersDialog == null) {
			parametersDialog = new ParametersDialog(MainFrame.this, "Database", false);
			Dimension dlgSize = parametersDialog.getPreferredSize();
			Dimension frmSize = getSize();
			Point loc = getLocation();
			parametersDialog.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
			parametersDialog.pack();
		}
		parametersDialog.setVisible(true);
	}

	protected void MenuItemSearchCriteria_actionPerformed(ActionEvent e) {
		OpenSearchCriteriaDialog();
	}

	private void OpenSearchCriteriaDialog() {
		if (searchCriteriaDialog == null) {
			searchCriteriaDialog = new SearchCriteriaDialog(MainFrame.this, "Database", false);
			Dimension dlgSize = searchCriteriaDialog.getPreferredSize();
			Dimension frmSize = getSize();
			Point loc = getLocation();
			searchCriteriaDialog.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
			searchCriteriaDialog.pack();
		}
		searchCriteriaDialog.setVisible(true);
	}

	public void updateCells() {
		workingAreaPanel.updateCells();
	}

	public void updateCells(int type, boolean ascending) {
		workingAreaPanel.updateCells(type, ascending, true);
	}

	protected Integer[] getSelectedRowDbId() {
		Integer[] dbid = workingAreaPanel.getSelectedRowDbId();
		return dbid;
	}

	protected int getSelectedRowVisitID() {
		int visitid = workingAreaPanel.getSelectedRowVisitID();
		return visitid;
	}

	/*
	public ThumbnailCreator getThumbnailCreator() {
		return thumbnailCreator;
	}*/

	public void OpenImageViewer(int localFrogID, String title, File imageFile, boolean displayallimages) {
		if (imageFile.exists()) {
			ImageViewer imageViewer = new ImageViewer(localFrogID, MainFrame.this, title, false, imageFile, true, displayallimages);
			imageViewer.setLocation(viewerX, viewerY);
			imageViewer.setVisible(true);
			viewerX += 20;
			viewerY += 20;
			if (viewerX >= 200) {
				viewerX = 0;
				viewerY = 0;
			}
		} else {
			IdentiFrog.LOGGER.writeMessage("Unable to find image file while opening image viewer: " + imageFile.getAbsolutePath());
			new ErrorDialog("Cannot find image file: " + imageFile.getAbsolutePath());
		}
	}

	private void OpenMatchingDialog(File imageFile, int DbID) {
		workingAreaPanel.setLastFrogID(DbID);
		if (matchingDialog == null || !matchingDialog.isOpen()) {
			matchingDialog = new MatchingDialog(MainFrame.this, imageFile, DbID);
		}
	}

	public WorkingAreaPanel getWorkingAreaPanel() {
		return workingAreaPanel;
	}

	/**
	 * Turns buttons on or off. Does not apply to buttons that depend on a
	 * selected item in the lists.
	 * 
	 * @param on
	 */
	public void setButtonsOn(boolean on) {
		// bDelete.setEnabled(on);
		// bEdit.setEnabled(on);
		// bFind.setEnabled(on);
		bOpen.setEnabled(on);
		MenuItemMarkExport.setEnabled(on);
		MenuItemDelete.setEnabled(on);
		MenuItemEdit.setEnabled(on);
		MenuItemNew.setEnabled(on);
		MenuItemSearch.setEnabled(on);
	}

	public JButton getButEdit() {
		return bEdit;
	}

	public JMenuItem getMenuItemEdit() {
		return MenuItemEdit;
	}

	public void setChangesMade(boolean changesMade) {
		this.changesMade = changesMade;
	}

	public boolean getChangesMade() {
		return changesMade;
	}

	public void setMatchForg(Frog matchFrog) {
		this.matchFrog = matchFrog;
	}

	public Frog getMatchFrog() {
		return matchFrog;
	}
}

class MainFrame_menuItemFileExit_ActionAdapter implements ActionListener {
	MainFrame adaptee;

	MainFrame_menuItemFileExit_ActionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.menuItemFileExit_actionPerformed(e);
	}
}

class MainFrame_menuItemHelpAbout_ActionAdapter implements ActionListener {
	MainFrame adaptee;

	MainFrame_menuItemHelpAbout_ActionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.menuItemHelpAbout_actionPerformed(e);
	}
}

class MainFrame_butHelp_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_butHelp_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butHelp_actionPerformed(e);
	}
}

class MainFrame_MenuItemNew_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemNew_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemNew_actionPerformed(e);
	}
}

class MainFrame_MenuItemMarkExport_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemMarkExport_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemMarkExport_actionPerformed(e);
	}
}

class MainFrame_menuItemCreateXLSX_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_menuItemCreateXLSX_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.menuItemCreateBatchXLSX_actionPerformed(e);
	}
}

class MainFrame_butOpen_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_butOpen_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butOpen_actionPerformed(e);
	}
}

class MainFrame_butFind_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_butFind_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butFind_actionPerformed(e);
	}
}

class MainFrame_butEdit_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_butEdit_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butEdit_actionPerformed(e);
	}
}

class MainFrame_butDelete_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_butDelete_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butDelete_actionPerformed(e);
	}
}

class MainFrame_MenuItemEdit_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemEdit_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemEdit_actionPerformed(e);
	}
}

class MainFrame_MenuItemSearch_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemSearch_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemSearch_actionPerformed(e);
	}
}

class MainFrame_MenuItemDelete_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemDelete_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemDelete_actionPerformed(e);
	}
}

class MainFrame_MenuItemHelp_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemHelp_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemHelp_actionPerformed(e);
	}
}

class MainFrame_CheckBoxMenuItemShowThumbs_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_CheckBoxMenuItemShowThumbs_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.CheckBoxMenuItemShowThumbs_actionPerformed(e);
	}
}

class MainFrame_MenuItemParams_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemParams_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemParams_actionPerformed(e);
	}
}

class MainFrame_MenuItemSearchCriteria_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemSearchCriteria_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemSearchCriteria_actionPerformed(e);
	}
}