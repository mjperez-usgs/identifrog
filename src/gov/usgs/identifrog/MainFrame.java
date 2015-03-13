package gov.usgs.identifrog;

import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.Frames.AboutDialog;
import gov.usgs.identifrog.Frames.ErrorDialog;
import gov.usgs.identifrog.Frames.ParametersDialog;
import gov.usgs.identifrog.Handlers.DataHandler;
import gov.usgs.identifrog.Handlers.FolderHandler;
import gov.usgs.identifrog.Handlers.XMLHandler;
import gov.usgs.identifrog.Operations.AddFrog;
import gov.usgs.identifrog.Operations.EditFrog;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
	private DataHandler frogData = new DataHandler();
	private Frog matchForg = new Frog();
	private boolean changesMade = false;

	private String workingFolder;
	private File thumbnailFolder;
	private File currentFile;

	private Preferences root = Preferences.userRoot();
	public final Preferences node = root.node("edu/isu/aadis/defaults");

	private Image icon = Toolkit.getDefaultToolkit().getImage("IconFrog.png");
	private ImageIcon imageNew = new ImageIcon(
			MainFrame.class.getResource("IconNew32.png"));
	private ImageIcon imageDelete = new ImageIcon(
			MainFrame.class.getResource("IconDelete32.png"));
	private ImageIcon imageHelp = new ImageIcon(
			MainFrame.class.getResource("IconHelp32.png"));
	private ImageIcon imageFind = new ImageIcon(
			MainFrame.class.getResource("IconFind32.png"));
	private ImageIcon imageEdit = new ImageIcon(
			MainFrame.class.getResource("IconEdit32.png"));
	private JButton bFind = new JButton("", imageFind);
	private JButton bEdit = new JButton("", imageEdit);
	private JButton bDelete = new JButton("", imageDelete);
	private JButton bOpen = new JButton("", imageNew);
	private JButton bHelp = new JButton("", imageHelp);
	//
	@SuppressWarnings("unused")
	private JButton btnPrevious = new JButton("Previous Page", new ImageIcon(
			WorkingAreaPanel.class.getResource("IconButtonPrevious32.png")));
	//
	private JToolBar barButtons = new JToolBar("", SwingConstants.HORIZONTAL);

	private MarkExport markExport = new MarkExport();
	private ThumbnailCreator thumbnailCreator;
	private SaveAsDialog saveAsDialog;
	private ParametersDialog parametersDialog;
	private SearchCriteriaDialog searchCriteriaDialog;
	private MatchingDialog matchingDialog;

	private JPanel contentPanel;
	private WorkingAreaPanel workingAreaPanel;

	private JMenuBar mainMenu = new JMenuBar();
	private JMenu menuFile = new JMenu("File");
	private JMenuItem MenuItemNew = new JMenuItem("New Frog Image",
			new ImageIcon(MainFrame.class.getResource("IconNew16.png")));
	private JMenuItem MenuItemMarkExport = new JMenuItem("Export to MARK");
	private JMenuItem menuItemFileExit = new JMenuItem("Exit");
	private JMenu menuHelp = new JMenu("Help");
	private JMenuItem menuItemHelpAbout = new JMenuItem("About");
	private JMenu menuDatabase = new JMenu("Database");
	private JMenuItem MenuItemSearch = new JMenuItem("Search for a Match",
			new ImageIcon(MainFrame.class.getResource("IconFind16.png")));
	private JMenuItem menuItemAddSite = new JMenuItem("Add New Site");
	private JMenuItem menuItemOpenSite = new JMenuItem("Open Existing Site");
	private JMenuItem menuItemSaveSiteAs = new JMenuItem("Save Site As");
	private JMenuItem MenuItemEdit = new JMenuItem("Edit Frog", new ImageIcon(
			MainFrame.class.getResource("IconEdit16.png")));
	private JMenuItem MenuItemDelete = new JMenuItem("Delete Frog",
			new ImageIcon(MainFrame.class.getResource("IconDelete16.png")));
	private JMenuItem MenuItemHelp = new JMenuItem("User Manual",
			new ImageIcon(MainFrame.class.getResource("IconHelp16.png")));
	private JCheckBoxMenuItem CheckBoxMenuItemShowThumbs = new JCheckBoxMenuItem(
			"Show Thumbnails", true);
	private JMenuItem MenuItemParams = new JMenuItem("Rows per Page");
	private JMenuItem MenuItemSearchCriteria = new JMenuItem("Search Criteria");

	private JLabel lStatusBar = new JLabel("STATUS: Up to 10 Frogs Per Page");

	private BorderLayout borderLayout1 = new BorderLayout();
	private TitledBorder titledBorder1 = new TitledBorder("");
	private int viewerX = 0;
	private int viewerY = 0;

	private FolderHandler fh;

	/**
	 * MainFrame Constructor
	 */
	public MainFrame(FolderHandler fh) {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		setIconImage(icon);
		this.fh = fh;
		workingFolder = fh.getMainFolder();
		thumbnailFolder = new File(fh.getThumbnailFolder());
		thumbnailCreator = new ThumbnailCreator(thumbnailFolder);
		this.setTitle("IdentiFrog Beta " + fh.getFileNamePath());
		try {
			init();
		} catch (Exception e) {
			System.out.println("MainFrame.MainFrame() Exception");
			e.printStackTrace();
		}
	}

	private void read() {
		ArrayList<Frog> frogs = new XMLHandler(fh.getFileNamePath())
				.ReadXMLFile();
		frogData.setFrogs(frogs);

		workingAreaPanel.setFrogs(frogs);
		workingAreaPanel.setFrogsData(frogData);
		workingAreaPanel.refreshRows();

		setFrogData(frogData);
		// update cells
		updateCells();

		this.setTitle("IdentiFrog Beta " + fh.getFileNamePath());
	}

	/**
	 * Component Initialization
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		ArrayList<Frog> frogs = new XMLHandler(fh.getFileNamePath())
				.ReadXMLFile();
		frogData.setFrogs(frogs);

		contentPanel = (JPanel) getContentPane();
		workingAreaPanel = new WorkingAreaPanel(MainFrame.this, fh);
		setFrogData(workingAreaPanel.getFrogsData());

		contentPanel.setLayout(borderLayout1);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

		setSize((int) (screen.getWidth() * 0.95),
				(int) (screen.getHeight() * 0.92));
		// setTitle("IdentiFrog Beta");

		lStatusBar.setBorder(titledBorder1);
		// put menu bar here
		menuItemFileExit
				.addActionListener(new MainFrame_menuItemFileExit_ActionAdapter(
						this));
		menuItemHelpAbout
				.addActionListener(new MainFrame_menuItemHelpAbout_ActionAdapter(
						this));
		MenuItemNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke('N',
				InputEvent.CTRL_MASK, false));
		MenuItemNew.addActionListener(new MainFrame_MenuItemNew_actionAdapter(
				this));
		MenuItemMarkExport
				.addActionListener(new MainFrame_MenuItemMarkExport_actionAdapter(
						this));

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
		MenuItemSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke('S',
				InputEvent.CTRL_MASK, false));
		MenuItemEdit.setAccelerator(javax.swing.KeyStroke.getKeyStroke('E',
				InputEvent.CTRL_MASK, false));
		CheckBoxMenuItemShowThumbs.setAccelerator(javax.swing.KeyStroke
				.getKeyStroke('T', InputEvent.CTRL_MASK, false));
		MenuItemParams.setAccelerator(javax.swing.KeyStroke.getKeyStroke('P',
				InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK, false));
		MenuItemSearchCriteria.setAccelerator(javax.swing.KeyStroke
				.getKeyStroke('S',
						InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK, false));
		MenuItemDelete.setAccelerator(javax.swing.KeyStroke.getKeyStroke('D',
				InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK, false));
		//
		MenuItemSearch
				.addActionListener(new MainFrame_MenuItemSearch_actionAdapter(
						this));
		MenuItemEdit
				.addActionListener(new MainFrame_MenuItemEdit_actionAdapter(
						this));
		MenuItemDelete
				.addActionListener(new MainFrame_MenuItemDelete_actionAdapter(
						this));
		MenuItemHelp
				.addActionListener(new MainFrame_MenuItemHelp_actionAdapter(
						this));
		CheckBoxMenuItemShowThumbs
				.addActionListener(new MainFrame_CheckBoxMenuItemShowThumbs_actionAdapter(
						this));
		MenuItemParams
				.addActionListener(new MainFrame_MenuItemParams_actionAdapter(
						this));
		MenuItemSearchCriteria
				.addActionListener(new MainFrame_MenuItemSearchCriteria_actionAdapter(
						this));

		// ActionListeners for Add Site Functionality
		menuItemAddSite.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// open file chooser in user's home directory
				JFileChooser fileChooser = new JFileChooser(System
						.getProperty("user.home"));
				// if user selects the OK button then perform the following:
				// XXX Add comments here
				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					String sitePath = fileChooser.getSelectedFile()
							.getAbsolutePath();
					// setup data folders and database xml file
					FolderHandler fhLocal = new FolderHandler(sitePath);
					XMLHandler file = new XMLHandler(fhLocal.getFileNamePath());
					if (!fhLocal.FoldersExist()) {
						fhLocal.createFolders();
						file.CreateXMLFile();
						File f = new File(fhLocal.getFileNamePath());
						if (f.exists() && f.length() == 0) {
							file.CreateXMLFile();
						}
						fh = fhLocal;
						read();
					} else {
						String message = "Site name "
								+ fileChooser.getSelectedFile().getName()
								+ " already exists.";
						JOptionPane.showConfirmDialog(null, message,
								"Site Already Exists", JOptionPane.OK_OPTION);
					}
				}
			}
		});

		menuItemOpenSite.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// open file chooser in user's home directory
				JFileChooser fileChooser = new JFileChooser(System
						.getProperty("user.home"));
				// if user selects the OK button then perform the following:
				// XXX Add comments here
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String sitePath = fileChooser.getSelectedFile().getParent();
					FolderHandler fhLocal = new FolderHandler(sitePath);
					fh = fhLocal;
					read();
				}
			}
		});

		menuItemSaveSiteAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// open file chooser in user's home directory
				JFileChooser fileChooser = new JFileChooser(System
						.getProperty("user.home"));
				// if user selects the OK button then perform the following:
				// XXX Add comments here
				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					String sitePath = fileChooser.getSelectedFile()
							.getAbsolutePath();
					// setup data folders and database xml file
					if (new File(sitePath).exists()) {
						String message = "Site with the name/path "
								+ fileChooser.getSelectedFile().getName()
								+ " already exists.";
						JOptionPane.showConfirmDialog(null, message,
								"Site Exists", JOptionPane.OK_OPTION);
					} else {
						try {
							copyFolder(new File(fh.getMainFolder().toString()),
									new File(sitePath));
							XMLHandler copyHandler = new XMLHandler(new File(
									sitePath + File.separator
											+ fh.getFileName()), getFrogData()
									.getFrogs());
							copyHandler.WriteXMLFile();
							fh = new FolderHandler(sitePath);
							read();
						} catch (IOException e) {
							e.printStackTrace();
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
		menuFile.add(MenuItemNew);
		menuFile.addSeparator();
		menuFile.add(MenuItemMarkExport);
		menuFile.addSeparator();
		menuFile.add(menuItemFileExit);
		menuHelp.add(MenuItemHelp);
		menuHelp.addSeparator();
		menuHelp.add(menuItemHelpAbout);
		mainMenu.add(menuFile);
		menuDatabase.add(MenuItemSearchCriteria);
		menuDatabase.addSeparator();
		menuDatabase.add(menuItemAddSite);
		menuDatabase.add(menuItemOpenSite);
		menuDatabase.add(menuItemSaveSiteAs);
		menuDatabase.addSeparator();
		menuDatabase.add(MenuItemSearch);
		menuDatabase.add(MenuItemEdit);
		menuDatabase.add(MenuItemDelete);
		menuDatabase.addSeparator();
		menuDatabase.add(CheckBoxMenuItemShowThumbs);
		menuDatabase.add(MenuItemParams);
		mainMenu.add(menuDatabase);
		mainMenu.add(menuHelp);
		setJMenuBar(mainMenu);
		//
		contentPanel.add(barButtons, BorderLayout.NORTH);
		contentPanel.add(workingAreaPanel, BorderLayout.CENTER);
	}

	public static void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
				// System.out.println("Directory copied from " + src + "  to " +
				// dest);
			}
			// list all the directory contents
			String files[] = src.list();
			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}
		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[2048];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
			// System.out.println("File copied from " + src + " to " + dest);
		}
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
		localFilename = localFilename
				.substring(0, (localFilename.length() - 4)) + ".png";
		File testFile = new File(fh.getThumbnailFolder() + localFilename);
		if (testFile.exists()) {
			JOptionPane
					.showMessageDialog(
							this,
							"The image '"
									+ image.getName()
									+ "' has already been entered.\n"
									+ "To re-enter the image, first delete it from the database.");
			return;
		}
		AddFrog addFrogObject = new AddFrog(MainFrame.this, fh,
				"Frog Information", true, image);
		addFrogObject.pack();
		// garbage collector
		System.gc();
		File temporaryThumbnail = new File(
				System.getProperty("java.io.tmpdir"), "tempThumb.jpg");
		if (temporaryThumbnail.exists()) {
			temporaryThumbnail.delete();
		}
		System.out.println(System.getProperty("java.io.tmpdir")
				+ "tempThumb.jpg");
		File thumbnailImageFile = thumbnailCreator.createThumbnailforEntry(
				image, addFrogObject.getButImage().getWidth(), addFrogObject
						.getButImage().getWidth(), temporaryThumbnail, true);
		if (thumbnailImageFile != null) {
			addFrogObject.setButImage(thumbnailImageFile);
			// add biometrics information window on the screen
			int move = (int) (getWidth() * 0.28);
			addFrogObject.setLocation((getX() + move), getY());
			addFrogObject.setVisible(true);
		} else {
			new ErrorDialog("Did not import image!");
		}
		temporaryThumbnail.deleteOnExit();
		updateCells();
	}

	/**
	 * Edit frog from the selected row
	 * 
	 * @throws Exception
	 */
	private void editFrog() throws Exception {
		String localID = workingAreaPanel.getSelectedFrog_Id();
		if (localID == null) {
			return;
		}
		Frog localFrog = frogData.searchFrog(localID);
		EditFrog editFrogObject = new EditFrog(MainFrame.this,
				"Edit Frog Information", true, localFrog);
		editFrogObject.pack();
		// garbage collector
		System.gc();
		// TODO center edit frog frame
		editFrogObject.setLocation(getX(), getY());
		editFrogObject.setVisible(true);
		localFrog = editFrogObject.getFrog();
		frogData.replaceFrog(localID, localFrog);
		// update cells
		updateCells();
		// write xml file
		XMLHandler file = new XMLHandler(new File(fh.getFileNamePath()),
				getFrogData().getFrogs());
		file.WriteXMLFile();
	}

	/**
	 * Delete frog from the selected row
	 * 
	 * @throws Exception
	 */
	private void deleteFrog() throws Exception {
		String localID = workingAreaPanel.getSelectedFrog_Id();
		if (localID == null) {
			return;
		}
		if (ChoiceDialog.choiceMessage("Do you want to delete this row?") == 0) {
			Frog localFrog = frogData.searchFrog(localID);
			String localImageName = localFrog.getPathImage();
			String localSignatureName = localFrog.getPathSignature();
			// check if exists then delete
			new File(fh.getImagesFolder() + localImageName).delete();
			new File(fh.getDorsalFolder() + localImageName).delete();
			new File(fh.getThumbnailFolder() + localImageName).delete();
			new File(fh.getBinaryFolder() + localImageName).delete();
			new File(fh.getSignaturesFolder() + localSignatureName).delete();
			frogData.removeFrog(localID);
			// garbage collector
			System.gc();
			if (frogData.getFrogs().size() == 1) {
				workingAreaPanel.refreshRows();
			}
			// update cells
			updateCells();
			// write xml file
			XMLHandler file = new XMLHandler(new File(fh.getFileNamePath()),
					getFrogData().getFrogs());
			file.WriteXMLFile();
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
		dlg.setLocation((frameSize.width - dialogSize.width) / 2 + loc.x,
				(frameSize.height - dialogSize.height) / 2 + loc.y);
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

	public Image getIcon() {
		return icon;
	}

	protected void MenuItemNew_actionPerformed(ActionEvent e) {
		DialogImageFileChooser imageChooser = new DialogImageFileChooser(
				MainFrame.this, "Choose Frog Photograph...", false,
				workingFolder);
		String filename = imageChooser.getName();
		if (filename != null) {
			currentFile = new File(filename);
			workingFolder = currentFile.getParent();
			addFrog(currentFile);
		}
	}

	@SuppressWarnings("static-access")
	protected void MenuItemMarkExport_actionPerformed(ActionEvent e) {
		saveAsDialog = new SaveAsDialog(MainFrame.this, "Save As...", false,
				workingFolder);
		String filePath = saveAsDialog.getName();
		if (filePath != null) {
			try {
				// markExport.saveToFile(filePath);
				markExport.saveToMark(frogData.getFrogs(), filePath);
			} catch (Exception ex) {
				new ErrorDialog("Cannot save the file.");
				ex.printStackTrace();
			}
		}
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
		File dorsalImage = new File(fh.getDorsalFolder()
				+ frogData.searchFrog(workingAreaPanel.getSelectedFrog_Id())
						.getPathImage());
		setButtonsOn(false);
		// System.out.println("In butFind Action Performed tfrog[0].intValue() is "
		// +
		// tfrog[0].intValue());
		OpenMatchingDialog(dorsalImage, tfrog[0].intValue());

	}

	protected void butEdit_actionPerformed(ActionEvent e) {
		try {
			editFrog();
		} catch (Exception ex) {
			System.out.println("MainFrame.butEdit_actionPerfomed() Exception");
			System.out.println(ex.getMessage());
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
				e.printStackTrace();
			} catch (URISyntaxException e) {
				new ErrorDialog("Cannot open " + url + ", invalid URI.");
				e.printStackTrace();
			}
		} else {
			new ErrorDialog("Desktop services not supported on this OS. You can view the manual at "+url+".");
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
			parametersDialog = new ParametersDialog(MainFrame.this, "Database",
					false);
			Dimension dlgSize = parametersDialog.getPreferredSize();
			Dimension frmSize = getSize();
			Point loc = getLocation();
			parametersDialog.setLocation((frmSize.width - dlgSize.width) / 2
					+ loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
			parametersDialog.pack();
		}
		parametersDialog.setVisible(true);
	}

	protected void MenuItemSearchCriteria_actionPerformed(ActionEvent e) {
		OpenSearchCriteriaDialog();
	}

	private void OpenSearchCriteriaDialog() {
		if (searchCriteriaDialog == null) {
			searchCriteriaDialog = new SearchCriteriaDialog(MainFrame.this,
					"Database", false);
			Dimension dlgSize = searchCriteriaDialog.getPreferredSize();
			Dimension frmSize = getSize();
			Point loc = getLocation();
			searchCriteriaDialog.setLocation((frmSize.width - dlgSize.width)
					/ 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
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

	public ThumbnailCreator getThumbnailCreator() {
		return thumbnailCreator;
	}

	public void OpenImageViewer(String frogId, String title, File imageFile,
			boolean displayallimages) {
		if (imageFile.exists()) {
			ImageViewer imageViewer = new ImageViewer(fh, frogId,
					MainFrame.this, title, false, imageFile, true,
					displayallimages);
			imageViewer.setLocation(viewerX, viewerY);
			imageViewer.setVisible(true);
			viewerX += 20;
			viewerY += 20;
			if (viewerX >= 200) {
				viewerX = 0;
				viewerY = 0;
			}
		} else {
			new ErrorDialog("Cannot find image file for " + title);
		}
	}

	private void OpenMatchingDialog(File imageFile, int DbID) {
		workingAreaPanel.setLastFrogID(DbID);
		if (matchingDialog == null || !matchingDialog.isOpen()) {
			matchingDialog = new MatchingDialog(MainFrame.this, imageFile,
					DbID, fh);
		}
	}

	public WorkingAreaPanel getWorkingAreaPanel() {
		return workingAreaPanel;
	}

	protected void setButtonsOn(boolean on) {
		bDelete.setEnabled(on);
		bEdit.setEnabled(on);
		bFind.setEnabled(on);
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

	public DataHandler getFrogData() {
		return frogData;
	}

	public void setFrogData(DataHandler frogData) {
		this.frogData = frogData;
	}

	public void setMatchForg(Frog matchForg) {
		this.matchForg = matchForg;
	}

	public Frog getMatchForg() {
		return matchForg;
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

class MainFrame_MenuItemNew_actionAdapter implements
		java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemNew_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemNew_actionPerformed(e);
	}
}

class MainFrame_MenuItemMarkExport_actionAdapter implements
		java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemMarkExport_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemMarkExport_actionPerformed(e);
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

class MainFrame_butDelete_actionAdapter implements
		java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_butDelete_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butDelete_actionPerformed(e);
	}
}

class MainFrame_MenuItemEdit_actionAdapter implements
		java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemEdit_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemEdit_actionPerformed(e);
	}
}

class MainFrame_MenuItemSearch_actionAdapter implements
		java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemSearch_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemSearch_actionPerformed(e);
	}
}

class MainFrame_MenuItemDelete_actionAdapter implements
		java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemDelete_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemDelete_actionPerformed(e);
	}
}

class MainFrame_MenuItemHelp_actionAdapter implements
		java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemHelp_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemHelp_actionPerformed(e);
	}
}

class MainFrame_CheckBoxMenuItemShowThumbs_actionAdapter implements
		java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_CheckBoxMenuItemShowThumbs_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.CheckBoxMenuItemShowThumbs_actionPerformed(e);
	}
}

class MainFrame_MenuItemParams_actionAdapter implements
		java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemParams_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemParams_actionPerformed(e);
	}
}

class MainFrame_MenuItemSearchCriteria_actionAdapter implements
		java.awt.event.ActionListener {
	MainFrame adaptee;

	MainFrame_MenuItemSearchCriteria_actionAdapter(MainFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemSearchCriteria_actionPerformed(e);
	}
}