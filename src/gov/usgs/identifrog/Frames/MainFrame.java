package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.DialogFileChooser;
import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.MainFrogBrowserPanel;
import gov.usgs.identifrog.MarkExport;
import gov.usgs.identifrog.SaveAsDialog;
import gov.usgs.identifrog.Site;
import gov.usgs.identifrog.DataObjects.Discriminator;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.FrogMatch;
import gov.usgs.identifrog.DataObjects.ImageMatch;
import gov.usgs.identifrog.DataObjects.SearchPackage;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.DataObjects.SiteSample;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.Operations.SearchWorker;
import gov.usgs.identifrog.cellrenderers.FrogBrowserCellRenderer;
import gov.usgs.identifrog.cellrenderers.FrogSearchCellRenderer;
import gov.usgs.identifrog.ui.JCheckBoxList;
import gov.usgs.identifrog.ui.StatusBar;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * <p>
 * Title: MainFrame.java
 * <p>
 * Description: Main Frame of the IdentiFrog GUI
 * 
 * @author Michael J. Perez 2015
 * @author Hidayatullah Ahsan 2011
 * @author Oksana V. Kelly 2008
 * @author Steven P. Miller for IdentiFrog Team 2005
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {
	//private boolean changesMade = false;

	private String workingFolder;
	private SearchWorker sw;
	private SiteImage currentSearchImage;

	private ImageIcon imageNew32 = new ImageIcon(MainFrame.class.getResource("/resources/IconNew32.png"));
	private ImageIcon imageNew16 = new ImageIcon(MainFrame.class.getResource("/resources/IconNew16.png"));
	private ImageIcon imageDelete = new ImageIcon(MainFrame.class.getResource("/resources/IconDelete32.png"));
	private ImageIcon imageHelp = new ImageIcon(MainFrame.class.getResource("/resources/IconHelp32.png"));

	private ImageIcon imageSaveAs16 = new ImageIcon(MainFrame.class.getResource("/resources/IconFloppy16.png"));

	//private ImageIcon imageFind = new ImageIcon(MainFrame.class.getResource("/resources/IconSearch32.png"));
	private ImageIcon imageEdit = new ImageIcon(MainFrame.class.getResource("/resources/IconEdit32.png"));
	private ImageIcon imageUsers = new ImageIcon(MainFrame.class.getResource("/resources/IconUsers32.png"));
	private ImageIcon imageDiscriminators = new ImageIcon(MainFrame.class.getResource("/resources/IconDiscriminator32.png"));
	private ImageIcon imageTemplates = new ImageIcon(MainFrame.class.getResource("/resources/IconBookmark32.png"));

	//private ImageIcon imageImage16 = new ImageIcon(MainFrame.class.getResource("/resources/IconImage16.png"));
	private ImageIcon imageDelete16 = new ImageIcon(MainFrame.class.getResource("/resources/IconDelete16.png"));
	private ImageIcon imageEdit16 = new ImageIcon(MainFrame.class.getResource("/resources/IconEdit16.png"));
	private ImageIcon imageWarning16 = new ImageIcon(MainFrame.class.getResource("/resources/IconWarning16.png"));

	private JTabbedPane tabbedPane = new JTabbedPane();

	//private JButton bFind = new JButton("", imageFind);
	private JButton bEdit = new JButton("", imageEdit);
	private JButton bDelete = new JButton("", imageDelete);
	private JButton bNew = new JButton("", imageNew32);
	private JButton bUsers = new JButton("", imageUsers);
	private JButton bTemplates = new JButton("", imageTemplates);
	private JButton bDiscriminators = new JButton("", imageDiscriminators);
	private JButton bHelp = new JButton("", imageHelp);
	//
	@SuppressWarnings("unused")
	private JButton btnPrevious = new JButton("Previous Page", new ImageIcon(MainFrogBrowserPanel.class.getResource("IconButtonPrevious32.png")));
	//
	private JToolBar browserBarButtons = new JToolBar(), searchBarButtons = new JToolBar();
	private StatusBar statusBar = new StatusBar();
	private MarkExport markExport = new MarkExport();
	//private ThumbnailCreator thumbnailCreator;
	private SaveAsDialog saveAsDialog;
	//private MatchingDialog matchingDialog;

	//private JPanel contentPanel;
	//private MainFrogBrowserPanel workingAreaPanel;

	private JMenuBar mainMenu = new JMenuBar();
	private JMenu menuFile = new JMenu("File");
	private JMenuItem menuItemCreateXLSX = new JMenuItem("Batch Processing", new ImageIcon(MainFrame.class.getResource("/resources/IconXLS16.png")));
	private JMenuItem MenuItemNew = new JMenuItem("New Frog Image", imageNew16);
	private JMenuItem MenuItemMarkExport = new JMenuItem("Export to MARK");
	private JMenuItem menuItemFileExit = new JMenuItem("Exit");
	private JMenu menuHelp = new JMenu("Help");
	private JMenuItem menuItemHelpAbout = new JMenuItem("About");
	private JMenu menuProject = new JMenu("Project");
	private JMenuItem menuItemProjectManager = new JMenuItem("Project Manager");
	// private JMenuItem menuItemOpenSite = new JMenuItem("Open Existing Site");
	private JMenuItem menuItemSaveSiteAs = new JMenuItem("Save Site As", imageSaveAs16);
	private JMenuItem MenuItemEdit = new JMenuItem("Edit Frog", imageEdit16);
	private JMenuItem MenuItemDelete = new JMenuItem("Delete Frog", imageDelete16);
	private JMenuItem MenuItemHelp = new JMenuItem("User Manual", new ImageIcon(MainFrame.class.getResource("/resources/IconHelp16.png")));
	private JMenuItem MenuItemUsers = new JMenuItem("Users", new ImageIcon(MainFrame.class.getResource("/resources/IconUsers16.png")));
	private JMenuItem MenuItemTemplates = new JMenuItem("Data Templates", new ImageIcon(MainFrame.class.getResource("/resources/IconBookmark16.png")));
	private JMenuItem MenuItemDiscriminators = new JMenuItem("Discriminators", new ImageIcon(
			MainFrame.class.getResource("/resources/IconDiscriminator16.png")));
	private JCheckBoxMenuItem CheckBoxMenuItemShowThumbs = new JCheckBoxMenuItem("Show Thumbnails", true);
	private HashMap<JCheckBox, Discriminator> searchDiscriminatorMap = new HashMap<JCheckBox, Discriminator>();
	private JCheckBoxList discriminatorList = new JCheckBoxList();
	private final DefaultListModel<JCheckBox> discriminatorModel = new DefaultListModel<JCheckBox>();

	//private JLabel lStatusBar = new JLabel("STATUS: Up to 10 Frogs Per Page");

	//private TitledBorder titledBorder1 = new TitledBorder("");
	//private int viewerX = 0;
	//private int viewerY = 0;

	private JList<Frog> frogList;
	private DefaultListModel<Frog> frogModel;
	private JList<FrogMatch> frogSearchList;
	private DefaultListModel<FrogMatch> frogSearchModel;
	private JFormattedTextField massToleranceField;
	private JFormattedTextField massField;
	private JTextField speciesField;
	private JCheckBox genderCheckbox;
	private JComboBox<String> genderComboBox;
	private JCheckBox speciesCheckbox;
	private JCheckBox lengthCheckBox;
	private JFormattedTextField lengthField;
	private JLabel lengthToleranceLabel;
	private JFormattedTextField lengthToleranceField;
	private JLabel massToleranceLabel;
	private JCheckBox massCheckBox;
	private JLabel searchImageLabel;
	private JCheckBox searchImageCheckBox;
	private JButton searchButton;
	private JProgressBar searchProgress;

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
			statusBar.setRightStatus(XMLFrogDatabase.getFrogs().size() + " Frogs");
			statusBar.setMessage("Loaded project");
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
		bEdit.setEnabled(state);
		bDelete.setEnabled(state);
		MenuItemDelete.setEnabled(state);
		MenuItemEdit.setEnabled(state);
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

		Collections.sort(recentSites);
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

		//contentPanel = (JPanel) getContentPane();
		//workingAreaPanel = new MainFrogBrowserPanel(MainFrame.this);

		setLayout(new GridBagLayout());
		//setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// put menu bar here
		menuItemFileExit.addActionListener(new MainFrame_menuItemFileExit_ActionAdapter(this));
		menuItemHelpAbout.addActionListener(new MainFrame_menuItemHelpAbout_ActionAdapter(this));
		MenuItemNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke('N', InputEvent.CTRL_MASK, false));
		MenuItemNew.addActionListener(new MainFrame_MenuItemNew_actionAdapter(this));

		MenuItemUsers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new UsersFrame(MainFrame.this).setVisible(true);
			}
		});

		MenuItemTemplates.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Templates not defined yet");
			}
		});

		MenuItemDiscriminators.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new DiscriminatorFrame(MainFrame.this).setVisible(true);
			}
		});

		MenuItemMarkExport.addActionListener(new MainFrame_MenuItemMarkExport_actionAdapter(this));
		menuItemCreateXLSX.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new XLSXTemplateGeneratorFrame(MainFrame.this);
			}
		});
		// find button verify input when focus target
		// buttons tool tip text
		//bFind.setToolTipText("Search for a Match");
		bEdit.setToolTipText("Edit Frog");
		bDelete.setToolTipText("Delete Frog");
		bNew.setToolTipText("New Frog");
		bHelp.setToolTipText("Help");
		bDiscriminators.setToolTipText("Manage Discriminators List");
		bUsers.setToolTipText("Manage Observers and Recorders List");
		bTemplates.setToolTipText("Manage Templates");

		bEdit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					editFrog(frogList.getSelectedIndex());
				} catch (Exception ex) {
					IdentiFrog.LOGGER.writeException(ex);
				}
			}
		});
		bDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					deleteFrog(frogList.getSelectedValue());
				} catch (Exception ex) {
					IdentiFrog.LOGGER.writeException(ex);
				}
			}
		});
		bNew.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				MenuItemNew_actionPerformed(null);
			}
		});

		bUsers.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new UsersFrame(MainFrame.this).setVisible(true);
			}
		});

		bDiscriminators.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new DiscriminatorFrame(MainFrame.this).setVisible(true);
			}
		});

		bHelp.addActionListener(new MainFrame_butHelp_actionAdapter(this));
		//
		MenuItemEdit.setAccelerator(javax.swing.KeyStroke.getKeyStroke('E', InputEvent.CTRL_MASK, false));
		CheckBoxMenuItemShowThumbs.setAccelerator(javax.swing.KeyStroke.getKeyStroke('T', InputEvent.CTRL_MASK, false));
		MenuItemDelete.setAccelerator(javax.swing.KeyStroke.getKeyStroke('D', InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK, false));
		//
		MenuItemEdit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				editFrog(frogList.getSelectedIndex());
			}
		});
		MenuItemDelete.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				deleteFrog(frogList.getSelectedValue());
			}
		});
		MenuItemHelp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				runManual();
			}
		});
		CheckBoxMenuItemShowThumbs.addActionListener(new MainFrame_CheckBoxMenuItemShowThumbs_actionAdapter(this));

		// ActionListeners for Add Site Functionality
		menuItemProjectManager.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ProjectManagerFrame pmf = new ProjectManagerFrame(MainFrame.this);
				pmf.setLocationRelativeTo(MainFrame.this);
				pmf.setVisible(true);
			}
		});

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
							read();
						} catch (IOException e) {
							IdentiFrog.LOGGER.writeException(e);
						}
					}
				}
			}
		});

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

		menuProject.addSeparator();
		menuProject.add(MenuItemNew);
		menuProject.add(MenuItemEdit);
		menuProject.add(MenuItemDelete);
		menuProject.addSeparator();
		menuProject.add(MenuItemUsers);
		menuProject.add(MenuItemTemplates);
		menuProject.add(MenuItemDiscriminators);
		menuProject.addSeparator();
		menuProject.add(CheckBoxMenuItemShowThumbs);
		mainMenu.add(menuProject);
		mainMenu.add(menuHelp);
		setJMenuBar(mainMenu);

		// icon states
		bEdit.setEnabled(false);
		bDelete.setEnabled(false);
		MenuItemDelete.setEnabled(false);
		MenuItemEdit.setEnabled(false);

		setupBrowser();
		setupSearch();

		//frogListPane.set

		//Compile interface================
		GridBagConstraints cons = new GridBagConstraints();
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1;
		cons.weighty = 1;
		cons.gridx = 0;
		cons.gridy = 1;
		add(tabbedPane, cons);
		cons.gridy = 2;
		cons.weighty = 0;
		cons.fill = GridBagConstraints.HORIZONTAL;
		add(statusBar, cons);
	}

	private void setupSearch() {
		DecimalFormat biometricFormat = new DecimalFormat("#.00");

		JSplitPane searchSplitPane = new JSplitPane();
		searchSplitPane.setDividerLocation(180);
		JPanel criteriaPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JLabel searchBoxTitle = new JLabel("Search Critieria");
		searchBoxTitle.setFont(new Font("MS Sans Serif", Font.BOLD, 14));

		searchImageCheckBox = new JCheckBox("Image Signature");
		searchImageCheckBox.setEnabled(false);
		searchImageCheckBox
				.setToolTipText("<html>To use an image, open a frog in the browser<br>and right click the image you want to search, then choose search.</html>");
		searchImageCheckBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateCheckBoxes();
			}
		});
		searchImageLabel = new JLabel("No image selected");
		searchImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		searchImageLabel.setEnabled(false);

		genderCheckbox = new JCheckBox("Gender");
		genderCheckbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateCheckBoxes();
			}
		});
		String[] genders = new String[] { "M", "F", "J", "Unknown" };
		genderComboBox = new JComboBox<String>(genders);
		speciesCheckbox = new JCheckBox("Species");
		speciesCheckbox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateCheckBoxes();
			}
		});
		speciesField = new JTextField();

		massCheckBox = new JCheckBox("Mass (g)");
		massCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateCheckBoxes();
			}
		});
		massField = new JFormattedTextField(biometricFormat);
		massToleranceLabel = new JLabel("<html>Tolerance (g)&#177;</html>");
		massToleranceField = new JFormattedTextField(biometricFormat);

		lengthCheckBox = new JCheckBox("Length (mm)");
		lengthCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateCheckBoxes();
			}
		});
		lengthField = new JFormattedTextField(biometricFormat);
		lengthToleranceLabel = new JLabel("<html>Tolerance (mm)&#177;</html>");
		lengthToleranceField = new JFormattedTextField(biometricFormat);

		//Discriminator list
		JLabel discriminatorLabel = new JLabel("Discriminators");
		discriminatorList.setModel(discriminatorModel);
		updateDiscriminatorList();

		JCheckBox latestSampleCheckbox = new JCheckBox("Latest surveys only");

		searchProgress = new JProgressBar();

		searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//Gather discriminators
				SearchPackage sp = new SearchPackage();
				boolean hasOneCriteria = false;
				if (currentSearchImage != null && searchImageCheckBox.isSelected()) {
					sp.setImage(currentSearchImage);
					hasOneCriteria = true;
				}
				
				if (genderCheckbox.isSelected()) {
					sp.setGender((String) genderComboBox.getSelectedItem());
					hasOneCriteria = true;
				}

				if (speciesCheckbox.isSelected() && !speciesField.getText().trim().equals("")) {
					sp.setSpecies(speciesField.getText().trim());
					hasOneCriteria = true;
				} else {
					speciesCheckbox.setSelected(false);
					updateCheckBoxes();
				}
				
				if (massCheckBox.isSelected() && !massField.getText().trim().equals("") && !massToleranceField.getText().trim().equals("")) {
					try {
						double mass = Double.parseDouble(massField.getText().trim());
						double massTolerance = Double.parseDouble(massToleranceField.getText().trim());
						sp.setMass(mass);
						sp.setMassTolerance(massTolerance);
						hasOneCriteria = true;
					} catch (NumberFormatException nfe) {
						massCheckBox.setSelected(false);
						updateCheckBoxes();
					}
				} else {
					massCheckBox.setSelected(false);
					updateCheckBoxes();
				}
				
				if (lengthCheckBox.isSelected() && !lengthField.getText().trim().equals("") && !lengthToleranceField.getText().trim().equals("")) {
					try {
						double length = Double.parseDouble(lengthField.getText().trim());
						double lengthTolerance = Double.parseDouble(lengthToleranceField.getText().trim());
						sp.setLength(length);
						sp.setLengthTolerance(lengthTolerance);
						hasOneCriteria = true;
					} catch (NumberFormatException nfe) {
						lengthCheckBox.setSelected(false);
						updateCheckBoxes();
					}
				} else {
					lengthCheckBox.setSelected(false);
					updateCheckBoxes();
				}
				
				//get discriminators
				ArrayList<Discriminator> discriminators = new ArrayList<Discriminator>();
				for (Map.Entry<JCheckBox, Discriminator> entry : searchDiscriminatorMap.entrySet()) {
					JCheckBox key = entry.getKey();
					Discriminator value = entry.getValue();
					if (key.isSelected()) {
						discriminators.add(value);
					}
				}
				if (discriminators.size() > 0) {
					sp.setDiscriminators(discriminators);
					hasOneCriteria = true;
				}
				
				if (latestSampleCheckbox.isSelected()) {
					sp.setUseAllSurveys(false);
				}

				if (hasOneCriteria) {
					searchButton.setEnabled(false);
					searchProgress.setIndeterminate(true);
					statusBar.setMessage("Searching...");

					sw = new SearchWorker(sp, MainFrame.this);
					sw.execute();
				} else {
					statusBar.setMessageWithIcon("At least one criteria must be specified", imageWarning16);
				}

			}
		});

		int row = 0;

		//Title
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = row;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0;
		c.weighty = 0;
		c.gridwidth = 2;
		criteriaPanel.add(searchBoxTitle, c);

		//Image
		c.gridwidth = 1;
		c.gridy = ++row;
		criteriaPanel.add(searchImageCheckBox, c);
		c.gridy = ++row;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		criteriaPanel.add(searchImageLabel, c);
		c.anchor = GridBagConstraints.NORTHWEST;

		//Species
		c.gridwidth = 1;
		c.gridy = ++row;
		criteriaPanel.add(speciesCheckbox, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		criteriaPanel.add(speciesField, c);
		c.fill = GridBagConstraints.NONE;

		//Gender
		c.gridy = ++row;
		c.gridx = 0;
		criteriaPanel.add(genderCheckbox, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		criteriaPanel.add(genderComboBox, c);
		c.fill = GridBagConstraints.NONE;

		//Mass
		c.gridy = ++row;
		c.gridx = 0;
		criteriaPanel.add(massCheckBox, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		criteriaPanel.add(massField, c);
		c.fill = GridBagConstraints.NONE;

		c.gridy = ++row;
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		criteriaPanel.add(massToleranceLabel, c);
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		criteriaPanel.add(massToleranceField, c);
		c.fill = GridBagConstraints.NONE;

		//Length
		c.gridy = ++row;
		c.gridx = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		criteriaPanel.add(lengthCheckBox, c);
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		criteriaPanel.add(lengthField, c);
		c.fill = GridBagConstraints.NONE;

		c.gridy = ++row;
		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		criteriaPanel.add(lengthToleranceLabel, c);
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		criteriaPanel.add(lengthToleranceField, c);

		//Discrmininators
		c.gridy = ++row;
		c.gridx = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		criteriaPanel.add(discriminatorLabel, c);
		c.gridy = ++row;
		c.gridwidth = 2;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		JScrollPane discriminatorScrollPane = new JScrollPane(discriminatorList);
		discriminatorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		criteriaPanel.add(discriminatorScrollPane, c);

		//Surveys
		c.gridy = ++row;
		c.gridx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = 2;
		criteriaPanel.add(latestSampleCheckbox, c);

		//Search and button
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		c.gridy = ++row;
		c.weighty = 0;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		criteriaPanel.add(searchProgress, c);
		c.gridx = 1;
		c.weighty = 0;
		criteriaPanel.add(searchButton, c);

		//surveys

		//Frog list=====================
		frogSearchList = new JList<FrogMatch>();
		frogSearchModel = new DefaultListModel<FrogMatch>();
		frogSearchList.setModel(frogSearchModel);
		frogSearchList.setCellRenderer(new FrogSearchCellRenderer());
		//...
		frogSearchList.setVisibleRowCount(-1);
		//frogList.setFixedCellHeight(160);
		//frogList.setFixedCellWidth(150);
		frogSearchList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

		// add buttons to the toolbar
		searchBarButtons.setFloatable(false);
		searchBarButtons.setMargin(new Insets(2, 4, 2, 4));
		searchBarButtons.setMinimumSize(new Dimension(100000, 40));
		//searchBarButtons.add(bNew);
		//barButtons.add(new JToolBar.Separator(new Dimension(0, 8)), null);
		/*
		 * searchBarButtons.add(bEdit); searchBarButtons.add(bDelete);
		 * searchBarButtons.add(Box.createHorizontalGlue());
		 * searchBarButtons.add(bUsers); searchBarButtons.add(bTemplates);
		 * searchBarButtons.add(bDiscriminators);
		 */
		//barButtons.add(new JToolBar.Separator(new Dimension(0, 8)), null);
		// barButtons.add(btnPrevious);
		//barButtons.add(new JToolBar.Separator(new Dimension(0, 8)), null);

		JPanel searchPanel = new JPanel(new BorderLayout());
		//GridBagConstraints cons = new GridBagConstraints();
		/*
		 * cons.fill = GridBagConstraints.HORIZONTAL; cons.weightx = 1;
		 * cons.gridx = 0; cons.gridy = 0;
		 */
		searchPanel.add(searchBarButtons, BorderLayout.NORTH);
		//cons.gridy = 1;
		//cons.weighty = 1;
		//cons.fill = GridBagConstraints.BOTH;
		searchSplitPane.setLeftComponent(criteriaPanel);
		searchSplitPane.setRightComponent(new JScrollPane(frogSearchList));
		searchPanel.add(searchSplitPane, BorderLayout.CENTER);
		updateCheckBoxes();
		tabbedPane.addTab("Frog Matching", searchPanel);
	}

	protected void updateCheckBoxes() {
		if (searchImageCheckBox.isSelected()) {
			searchImageLabel.setEnabled(true);
		} else {
			searchImageLabel.setEnabled(false);
		}
		
		if (genderCheckbox.isSelected()) {
			genderComboBox.setEnabled(true);
		} else {
			genderComboBox.setEnabled(false);
		}

		if (speciesCheckbox.isSelected()) {
			speciesField.setEnabled(true);
		} else {
			speciesField.setEnabled(false);
		}

		if (massCheckBox.isSelected()) {
			massField.setEnabled(true);
			massToleranceField.setEnabled(true);
		} else {
			massField.setEnabled(false);
			massToleranceField.setEnabled(false);
		}
		
		if (lengthCheckBox.isSelected()) {
			lengthField.setEnabled(true);
			lengthToleranceField.setEnabled(true);
		} else {
			lengthField.setEnabled(false);
			lengthToleranceField.setEnabled(false);
		}
	}

	/**
	 * Called when SearchWorker finishes and publishes matches to the screen
	 * @param matches
	 */
	public void setMatches(ArrayList<FrogMatch> matches) {
		frogSearchModel.clear();
		for (FrogMatch m : matches) {
			frogSearchModel.addElement(m);
		}
		statusBar.setMessage("Search finished");
		searchProgress.setIndeterminate(false);
		searchButton.setEnabled(true);
	}

	private void updateDiscriminatorList() {
		//Preserve state of checked boxes
		ArrayList<Discriminator> selectedDiscrims = new ArrayList<Discriminator>();
		for (Map.Entry<JCheckBox, Discriminator> entry : searchDiscriminatorMap.entrySet()) {
			JCheckBox key = entry.getKey();
			Discriminator value = entry.getValue();
			if (key.isSelected()) {
				selectedDiscrims.add(value);
			}
		}

		discriminatorModel.clear();
		ArrayList<Discriminator> newDiscriminators = XMLFrogDatabase.getDiscriminators();

		for (Discriminator disc : newDiscriminators) {
			JCheckBox cb = new JCheckBox(disc.getText());
			searchDiscriminatorMap.put(cb, disc);
			if (selectedDiscrims.contains(disc)) {
				cb.setSelected(true);
			}
			discriminatorModel.addElement(cb);
		}
	}

	private void setupBrowser() {
		//Frog list=====================
		frogList = new JList<Frog>();
		frogModel = new DefaultListModel<Frog>();
		frogList.setModel(frogModel);
		frogList.setCellRenderer(new FrogBrowserCellRenderer());
		//...
		frogList.setVisibleRowCount(-1);
		//frogList.setFixedCellHeight(160);
		//frogList.setFixedCellWidth(150);
		frogList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

		frogList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					if (frogList.getSelectedIndex() != -1) {
						// icon states
						bEdit.setEnabled(true);
						bDelete.setEnabled(true);
						MenuItemDelete.setEnabled(true);
						MenuItemEdit.setEnabled(true);

					} else {
						//empty selection
						// icon states
						bEdit.setEnabled(false);
						bDelete.setEnabled(false);
						MenuItemDelete.setEnabled(false);
						MenuItemEdit.setEnabled(false);
					}
				}
			}
		});

		//match right click selection
		frogList.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					frogList.setSelectedIndex(frogList.locationToIndex(e.getPoint()));
				}
				if (e.isPopupTrigger()) {
					createFrogPopup(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					frogList.setSelectedIndex(frogList.locationToIndex(e.getPoint()));
				}
				if (e.isPopupTrigger()) {
					createFrogPopup(e);
				}
			}

		});

		for (Frog frog : XMLFrogDatabase.getFrogs()) {
			frogModel.addElement(frog);
		}

		// add buttons to the toolbar
		browserBarButtons.setFloatable(false);
		browserBarButtons.setMargin(new Insets(2, 4, 2, 4));
		browserBarButtons.setMinimumSize(new Dimension(100000, 36));
		browserBarButtons.add(bNew);
		//barButtons.add(new JToolBar.Separator(new Dimension(0, 8)), null);
		browserBarButtons.add(bEdit);
		browserBarButtons.add(bDelete);
		browserBarButtons.add(Box.createHorizontalGlue());
		browserBarButtons.add(bUsers);
		browserBarButtons.add(bTemplates);
		browserBarButtons.add(bDiscriminators);
		//barButtons.add(new JToolBar.Separator(new Dimension(0, 8)), null);
		// barButtons.add(btnPrevious);
		//barButtons.add(new JToolBar.Separator(new Dimension(0, 8)), null);
		browserBarButtons.add(bHelp);

		JPanel browserPanel = new JPanel(new BorderLayout());
		//GridBagConstraints cons = new GridBagConstraints();
		/*
		 * cons.fill = GridBagConstraints.HORIZONTAL; cons.weightx = 1;
		 * cons.gridx = 0; cons.gridy = 0;
		 */
		browserPanel.add(browserBarButtons, BorderLayout.NORTH);
		//cons.gridy = 1;
		//cons.weighty = 1;
		//cons.fill = GridBagConstraints.BOTH;
		browserPanel.add(new JScrollPane(frogList), BorderLayout.CENTER);
		tabbedPane.addTab("Frog Browser", browserPanel);
	}

	private void createFrogPopup(MouseEvent e) {
		System.out.println("IS POPUP TRIG");
		int frogIndex = frogList.locationToIndex(e.getPoint());
		final Frog f = frogModel.get(frogIndex);

		//codeModel.setSelectedFileName(table.getValueAt(table.getSelectedRow(), 0).toString());
		JPopupMenu popup = new JPopupMenu();
		JMenuItem popupEditInfo, popupDeleteFrog, popupSearch, popupAddSample;
		popupEditInfo = new JMenuItem("Edit frog information", imageEdit16);
		popupEditInfo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				IdentiFrog.LOGGER.writeMessage("Opening Frog Editor (EDIT FROG) via Right Click Menu: " + f.toString());
				editFrog(frogIndex);
			}
		});

		popupAddSample = new JMenuItem("Add Site Survey", imageNew16);
		popupAddSample.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				IdentiFrog.LOGGER.writeMessage("Opening Frog Editor (NEW SITE SAMPLE) via Right Click Menu: " + f.toString());
				FrogEditor editFrogWindow = new FrogEditor(MainFrame.this, "Edit Frog", f);
				editFrogWindow.createNewSample();
				editFrogWindow.pack();
				editFrogWindow.setVisible(true);
			}
		});

		popupDeleteFrog = new JMenuItem("Delete Frog", imageDelete16);
		popupDeleteFrog.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteFrog(f);
			}
		});
		popupSearch = new JMenuItem("Merge Frog...?");
		popup.add(popupAddSample);
		popup.add(popupEditInfo);
		popup.add(popupDeleteFrog);
		popup.add(popupSearch);
		popup.show(e.getComponent(), e.getX(), e.getY());
	}

	// File | Exit action performed
	public void menuItemFileExit_actionPerformed(ActionEvent e) {
		closeAction();
	}

	/**
	 * Oepns frog editor with the new image file
	 * 
	 * @param image
	 *            Image to open frog editor with
	 */
	private void addFrog(File image) {
		// step 1: make sure that the same image is not being recorded more than
		// once
		String localFilename = image.getName();
		localFilename = localFilename.substring(0, (localFilename.length() - 4)) + ".png";
		File testFile = new File(XMLFrogDatabase.getThumbnailFolder() + localFilename);
		if (testFile.exists()) {

			return;
		}

		SiteImage img = new SiteImage();
		img.setSourceFilePath(image.getAbsolutePath());
		img.generateHash();
		Frog owner = XMLFrogDatabase.findImageOwnerByHash(img.getSourceImageHash());
		if (owner != null) {
			JOptionPane.showMessageDialog(this, "The image '" + image.getName() + "' has already been entered (part of frog " + owner.getID()
					+ ").\n" + "To the image, first delete it from the database.", "Image already entered", JOptionPane.ERROR_MESSAGE);
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
			frogModel.addElement(newFrog);
		}
	}

	/**
	 * Opens the frog editor for the specified frog object
	 */
	private void editFrog(int frogIndex) {
		Frog f = frogModel.get(frogIndex);
		FrogEditor editFrogWindow = new FrogEditor(MainFrame.this, "Edit Frog", f);
		editFrogWindow.pack();
		editFrogWindow.setVisible(true);
		//we should check if the frog has changed
		Frog editedFrog = editFrogWindow.getFrog();
		if (editedFrog != null) {
			XMLFrogDatabase.updateFrog(f.getID(), editedFrog);
			frogModel.set(frogIndex, editedFrog);
			XMLFrogDatabase.writeXMLFile();
			statusBar.setMessage("Updated frog in database");
		}
	}

	/**
	 * Delete frog from the database
	 */
	private void deleteFrog(Frog f) {
		IdentiFrog.LOGGER.writeError("Deleting frog is not yet fully implemented!");
		int numImages = f.getAllSiteImages().size();
		int numSignatures = 0;
		for (SiteSample sample : f.getSiteSamples()) {
			for (SiteImage img : sample.getSiteImages()) {
				if (img.isSignatureGenerated()) {
					numSignatures++;
				}
			}
		}
		if (JOptionPane.showConfirmDialog(null, "Deleting this frog will remove " + numImages + " " + ((numImages == 1) ? "image" : "images")
				+ " and " + numSignatures + " " + ((numSignatures == 1) ? "signature" : "signatures") + ".\n" + "Delete this frog?", "Delete Frog",
				JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {

			frogModel.removeElement(f);
			XMLFrogDatabase.removeFrog(f.getID());
			XMLFrogDatabase.writeXMLFile();
			statusBar.setMessage("Deleted frog from database");
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

	protected void butClose_actionPerformed(ActionEvent e) {
		closeAction();
	}

	private void closeAction() {
		//node.putBoolean("alreadyOpen", false);
		//node.put("workingDir", workingFolder);
		//node.putInt("maxPageRows", workingAreaPanel.getMaxPageRows());
		//node.putDouble("threshold", workingAreaPanel.getThreshold());
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
		DialogFileChooser imageChooser = new DialogFileChooser(MainFrame.this, "Choose Frog Photograph...", System.getProperty("user.home"),
				DialogFileChooser.getImageFilter());
		String filename = imageChooser.getName();

		if (filename != null) {
			File imageFile = new File(filename);
			workingFolder = imageFile.getParent();
			addFrog(imageFile);
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

	/* Match Button pressed */
	protected void butFind_actionPerformed(ActionEvent e) {
		IdentiFrog.LOGGER.writeMessage("Find/match is not implemented yet!");
		/*
		 * // simulation // ConfusionMatrix confmatrix = new ConfusionMatrix();
		 * // confmatrix.simulateIdentification(); // Find Hamming Dist
		 * Threshold // ThresholdHammingDistance Threshold = new
		 * ThresholdHammingDistance(); // double thresholdHammingDistance =
		 * Threshold.getThresholdHamDist(); Integer[] tfrog =
		 * getSelectedRowDbId(); if (tfrog.length == 0) { new
		 * ErrorDialog("You must select a row first."); return; } // TODO fix
		 * this // File dorspath =
		 * workingAreaPanel.getDorsalImageFileFromSelectedRow(); File
		 * dorsalImage = new File(XMLFrogDatabase.getDorsalFolder() +
		 * XMLFrogDatabase
		 * .searchFrogByID(workingAreaPanel.getSelectedFrog_Id()).
		 * getGenericImageName()); setButtonsOn(false); //
		 * IdentiFrog.LOGGER.writeMessage
		 * ("In butFind Action Performed tfrog[0].intValue() is " // + //
		 * tfrog[0].intValue()); OpenMatchingDialog(dorsalImage,
		 * tfrog[0].intValue());
		 */
	}

	protected void MenuItemHelp_actionPerformed(ActionEvent e) {
		runManual();
	}

	private void runManual() {
		String url = "http://github.com/mjperez-usgs/IdentiFrog/";
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


	/*
	 * public ThumbnailCreator getThumbnailCreator() { return thumbnailCreator;
	 * }
	 */

	/*
	 * public void OpenImageViewer(int localFrogID, String title, File
	 * imageFile, boolean displayallimages) { if (imageFile.exists()) {
	 * ImageViewer imageViewer = new ImageViewer(localFrogID, MainFrame.this,
	 * title, false, imageFile, true, displayallimages);
	 * imageViewer.setLocation(viewerX, viewerY); imageViewer.setVisible(true);
	 * viewerX += 20; viewerY += 20; if (viewerX >= 200) { viewerX = 0; viewerY
	 * = 0; } } else { IdentiFrog.LOGGER.writeMessage(
	 * "Unable to find image file while opening image viewer: " +
	 * imageFile.getAbsolutePath()); new ErrorDialog("Cannot find image file: "
	 * + imageFile.getAbsolutePath()); } }
	 */

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
		bNew.setEnabled(on);
		MenuItemMarkExport.setEnabled(on);
		MenuItemDelete.setEnabled(on);
		MenuItemEdit.setEnabled(on);
		MenuItemNew.setEnabled(on);
	}

	public JButton getButEdit() {
		return bEdit;
	}

	public JMenuItem getMenuItemEdit() {
		return MenuItemEdit;
	}

	/**
	 * Sets a SiteImage in the search field and switches to that interface
	 * @param img Image to search
	 */
	public void setSearchImage(SiteImage img) {
		currentSearchImage = img;
		searchImageLabel.setIcon(new ImageIcon(img.getColorThumbnail()));
		searchImageLabel.setText("");
		searchImageLabel.setEnabled(true);
		searchImageCheckBox.setSelected(true);
		searchImageCheckBox.setEnabled(true);
		searchImageLabel.setBorder(new EmptyBorder(3,0,3,0));
	}

	/**
	 * Gets the tabbed pane so you can control the tab currently showing
	 * @return tabbedPane
	 */
	public JTabbedPane getTabbedPane() {
		return tabbedPane;
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

class MainFrame_CheckBoxMenuItemShowThumbs_actionAdapter implements java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_CheckBoxMenuItemShowThumbs_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		//	adaptee.CheckBoxMenuItemShowThumbs_actionPerformed(e);
	}
}