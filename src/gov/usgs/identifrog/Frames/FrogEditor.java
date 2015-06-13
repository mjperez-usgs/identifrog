package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.ChoiceDialog;
import gov.usgs.identifrog.DialogImageFileChooser;
import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.ImageManipFrame;
import gov.usgs.identifrog.MainFrogBrowserPanel;
import gov.usgs.identifrog.DataObjects.DateLabelFormatter;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.Location;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.DataObjects.SiteSample;
import gov.usgs.identifrog.DataObjects.User;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.Operations.UITest1;
import gov.usgs.identifrog.cellrenderers.FrogEditorImageRenderer;
import gov.usgs.identifrog.cellrenderers.UserListCellRenderer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

/**
 * <p>
 * Title: AddFrog.java
 * <p>
 * Description: displays window 'Frog Information' for the user to fill out the
 * fields, adds filled entries to table Frog, Observer, EntryPerson,
 * CaptureLocation
 * 
 * @author Michael J. Perez 2015
 * @author Hidayatullah Ahsan 2012
 * @author Oksana V. Kelly 2008
 * @author Oksana V. Kelly modified the code written by Mike Ramshaw from
 *         IdentiFrog 2005
 */
@SuppressWarnings("serial")
public class FrogEditor extends JDialog implements ListSelectionListener {
	protected MainFrame parentFrame;

	private Preferences root = Preferences.userRoot();
	//private Preferences node = root.node("edu/isu/aadis/defaults");

	//	private int DEFAULT_WIDTH = 170, DEFAULT_HEIGHT = 25;
	private Dimension DEFAULT_MAX_ELEM_SIZE = new Dimension(170, 25);
	private Font level1TitleFont = new Font("MS Sans Serif", Font.BOLD, 14);
	private Font level2TitleFont = new Font("MS Sans Serif", Font.BOLD, 12);

	int frogDbId, entpersDbId, obsDbId, caplocDbId, zone, frogidNum;

	String surveyID;
	private boolean isNewImage = true;
	protected int frogID;
	protected String species;
	protected String pathSignature;
	protected String pathImage;
	protected String pathBinary;
	protected String formerID;
	protected String locationDescription;
	protected String locationName;
	protected String locCoorType;
	//protected String obsLastName;
	//protected String obsFirstName;
	protected String observer, recorder;
	protected String comments;
	protected String gender;
	//protected String entryLastName;
	//protected String entryFirstName;
	protected String datum;
	protected String addDiscriminator = "";
	protected String entrydate, capturedate;
	protected double x;
	protected double y;
	protected double length;
	protected double mass;
	protected int maxfrogdbid = 0;
	protected int maxobsdbid = 0;
	protected int maxlocdbid = 0;
	protected int maxentrypersondbid = 0;
	protected ImageManipFrame iFrame;
	private String nextAvailFrogId;
	private String thumbnailFilename;
	private Frog frog; //null if not in edit mode
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	//private boolean MEMORY_COPY = true; //Discard the base frog if this is true when closing
	private ArrayList<SiteImage> images;

	/**
	 * This is the standard add-frog window that comes up when a "New Frog" is
	 * entered.
	 * 
	 * @param frame
	 *            Parent frame
	 * @param title
	 *            Window title
	 * @param modal
	 *            if this window should block others
	 * @param image
	 *            Image to load for this frog since it's not in the DB yet.
	 */
	public FrogEditor(MainFrame frame, String title, File image) {
		super((Frame) frame, title, true); //make modal (true)
		IdentiFrog.LOGGER.writeMessage("Opening FrogEditor's NEW FROG frog editor");
		parentFrame = frame;

		try {
			images = new ArrayList<SiteImage>();
			SiteImage simage = new SiteImage();
			simage.setProcessed(false);
			simage.setSourceFilePath(image.getAbsolutePath());
			//generate thumbnail in memory
			simage.createListThumbnail();
			images.add(simage);
			init();
			frogID = XMLFrogDatabase.getNextAvailableFrogID();
			textFrog_ID.setText(Integer.toString(frogID));
			LocalDate now = LocalDate.now();
			entryDatePicker.getModel().setDate(now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()); //-1 cause it's 0 indexed
			entryDatePicker.getModel().setSelected(true);
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("Exception while starting add frog.", e);
		}
	}

	/**
	 * This is the edit version of the add frog window.
	 * 
	 * @param frame
	 *            parent frame
	 * @param fh
	 *            folderhandler
	 * @param title
	 *            Title of the page
	 * @param frog
	 *            Frog to edita and populate the interface with
	 */
	public FrogEditor(MainFrame frame, String title, Frog frog) {
		super((Frame) frame, title);
		IdentiFrog.LOGGER.writeMessage("Opening FrogEditor's EXISTING FROG frog editor");
		IdentiFrog.LOGGER.writeError("This constructor is not optimized for DB 2.0! [Edit Mode]"); 

		parentFrame = frame;
		this.frog = frog;
		images = frog.getAllSiteImages();
		try {
			init();
			//load frog... TODO
			textFrog_ID.setText(Integer.toString(frog.getID()));
			textSpecies.setText(frog.getSpecies());
			sexComboBox.setSelectedItem(frog.getGender());
			//descriminator todo
			
			/*
			Calendar entryCal = Calendar.getInstance();
			entryDatePicker.getModel().setDate(entryCal.get(Calendar.YEAR), entryCal.get(Calendar.MONTH), entryCal.get(Calendar.DAY_OF_MONTH));
			entryDatePicker.getModel().setSelected(true);
			
			//load datepicker data
			Calendar captureCal = Calendar.getInstance();
			IdentiFrog.LOGGER.writeMessage(captureCal.get(Calendar.YEAR) + "-" + captureCal.get(Calendar.MONTH) + "-"
					+ captureCal.get(Calendar.DAY_OF_MONTH));
			captureDatePicker.getModel()
					.setDate(captureCal.get(Calendar.YEAR), captureCal.get(Calendar.MONTH), captureCal.get(Calendar.DAY_OF_MONTH));
			captureDatePicker.getModel().setSelected(true);*/
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("Exception while starting add frog.", e);
		}
	}

	private ActionListener radButtonAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (((JRadioButton) e.getSource()).getText() == "Lat/Long") {
				locCoorType = "Lat/Long";
				textDatum.setVisible(false);
				textZone.setVisible(false);
				labDatum.setVisible(false);
				labZone.setVisible(false);
				labX.setText("Latitude");
				labY.setText("Longitude");
			}
			if (((JRadioButton) e.getSource()).getText() == "UTM") {
				locCoorType = "UTM";
				textDatum.setVisible(true);
				textZone.setVisible(true);
				labDatum.setVisible(true);
				labZone.setVisible(true);
				labX.setText("Northing");
				labY.setText("Easting");
			}
		}
	};
	/*
	 * private ActionListener radDiscrButtonAction = new ActionListener() {
	 * public void actionPerformed(ActionEvent e) { addDiscriminator =
	 * ((JRadioButton) e.getSource()).getText(); /* if (((JRadioButton)
	 * e.getSource()).getText() == "Yes") { addDiscriminator = "Yes"; } if
	 * (((JRadioButton) e.getSource()).getText() == "No") { addDiscriminator =
	 * "No"; }
	 * 
	 * } };
	 */
	JList<SiteImage> imageList = new JList<SiteImage>();
	DefaultListModel<SiteImage> listModel = new DefaultListModel<SiteImage>();
	JButton butFillPreviousFrogInfo = new JButton();
	JButton butEditDiscriminators = new JButton();
	JButton butDebugPopulate = new JButton();
	JLabel labEntryPersonTitle = new JLabel();
	JLabel labObserverTitle = new JLabel();
	JLabel labFrogTitle = new JLabel();
	//JLabel labCapLocationTitle = new JLabel();

	JPanel panelAllInfo = new JPanel();

	JPanel panelSiteSampleInfo = new JPanel();
	//JPanel recPanel = new JPanel();
	JPanel panelDataEntry;
	JPanel panelObserverInfo = new JPanel();
	//JPanel panelLocation = new JPanel();

	JPanel panelBottomButtons = new JPanel();

	JLabel labEntryLastName = new JLabel();
	JLabel labEntryFirstName = new JLabel();
	JLabel labObserverLastName = new JLabel();
	JLabel labObserverFirstName = new JLabel();
	JLabel labSex = new JLabel("Gender");
	JLabel labNextFrog_ID = new JLabel();
	JLabel lab2NextFrog_ID = new JLabel();
	JTextField textNextFrog_ID = new JTextField();
	JLabel labSpecies = new JLabel("Species");
	JTextField textDatum = new JTextField();
	JTextField textSurveyID = new JTextField();
	JLabel labSurveyID = new JLabel();

	JLabel labCapturedate = new JLabel();
	JLabel labEntrydate = new JLabel();
	String[] sexStrings = { "M", "F", "J", "Unknown" };
	JDatePickerImpl captureDatePicker;
	JComboBox<?> sexComboBox = new JComboBox<Object>(sexStrings);
	JRadioButton additDiscrNo = new JRadioButton("No", false);
	JRadioButton additDiscrYes = new JRadioButton("Yes", false);
	JCheckBox checkAdditionalDescriptor = new JCheckBox("Additional Discriminator");
	DecimalFormat decimalFormat = new DecimalFormat("#.00");
	JFormattedTextField textMass = new JFormattedTextField(decimalFormat);
	JLabel labFrogComments = new JLabel();
	JTextField textFrogComments = new JTextField();
	JLabel labCoorType = new JLabel();
	JLabel labX = new JLabel();
	JTextField textX = new JTextField();
	JLabel labY = new JLabel();
	JTextField textY = new JTextField();
	JButton butNewEntry = new JButton();
	JRadioButton LatLongButton = new JRadioButton("Lat/Long", false);
	JRadioButton UTMButton = new JRadioButton("UTM", false);
	ButtonGroup Butgroup = new ButtonGroup();
	JTextField textZone = new JTextField();
	JButton addImageButton = new JButton("Add Image");
	JDatePickerImpl entryDatePicker;
	JLabel labZone = new JLabel();
	JTextField textLocDesc = new JTextField();
	JTextField textSpecies = new JTextField();
	JTextField textFrog_ID = new JTextField();
	JLabel labFrog_ID = new JLabel("ID");
	JLabel labFRO = new JLabel();

	JLabel labDatum = new JLabel();
	JLabel labLocationName = new JLabel();
	JLabel labMass = new JLabel();
	JLabel labLocDesc = new JLabel();
	JLabel labCoordinates = new JLabel();
	JFormattedTextField textLength = new JFormattedTextField(decimalFormat);
	JLabel labLength = new JLabel();
	JButton butCancel = new JButton();
	JLabel labMassUnit = new JLabel();
	JLabel labLengthUnit = new JLabel();
	JButton butClearAll = new JButton();
	String[] ObsLastNameList = { "" };
	String[] ObsFirstNameList = { "" };
	String[] EntryLastNameList = { "" };
	String[] EntryFirstNameList = { "" };
	String[] LocNameList = { "" };
	ArrayList<LocInfo> locList = new ArrayList<LocInfo>();
	DefaultComboBoxModel<User> recorderListModel, observerListModel;

	JComboBox<User> comboObserver;
	JComboBox<User> comboRecorder;
	JButton usersButton;
	JComboBox<?> comboLocationName = new JComboBox<Object>(LocNameList);

	private void init() throws Exception {
		//load objects that can't be done at compile time.
		ArrayList<String> years = new ArrayList<String>();
		int curYear = Calendar.getInstance().get(Calendar.YEAR);
		int parsingYear = 2005; //you can enter frogs from this date and after till the current year.
		while (parsingYear <= curYear) {
			years.add(Integer.toString(parsingYear));
			parsingYear++;
		}
		//year = years.toArray(new String[years.size()]);
		//yearComboBox = new JComboBox<Object>(year);

		//start init
		//Images
		for (SiteImage simage : images) {
			listModel.addElement(simage);
		}

		imageList = new JList<SiteImage>(listModel);
		imageList.addListSelectionListener(this);
		imageList.setFont(new Font("Arial", Font.BOLD, 35));
		imageList.setCellRenderer(new FrogEditorImageRenderer());
		imageList.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
			           //-- select a row
			           final int idx = imageList.locationToIndex(e.getPoint());
			           final SiteImage img = imageList.getModel().getElementAt(idx);
			           imageList.setSelectedIndex(idx);
			            //codeModel.setSelectedFileName(table.getValueAt(table.getSelectedRow(), 0).toString());
			            JPopupMenu popup = new JPopupMenu();
						JMenuItem popupGenerateSignature;
						popupGenerateSignature = new JMenuItem();
						if (img.isSignatureGenerated()) {
							popupGenerateSignature.setEnabled(false);
							popupGenerateSignature.setText("Signature already generated");
						} else {
							popupGenerateSignature.setEnabled(true);
							popupGenerateSignature.setText("Create signature");
						}
						popupGenerateSignature.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								IdentiFrog.LOGGER.writeMessage("User generating signature for siteimage "+img);
								SiteImage newImg = openDigSigFrame(img);
								IdentiFrog.LOGGER.writeMessage("Continuing execution of FrogEditor. Signature Generator has closed.");
								newImg.createListThumbnail();
								listModel.set(idx,newImg); //update the SiteImage object.
							}
						});
						
						popup.add(popupGenerateSignature);
			            popup.show(e.getComponent(), e.getX(), e.getY());
			       }
			    }
		});
		//Data
		nextAvailFrogId = Integer.toString(XMLFrogDatabase.getNextAvailableFrogID());
		ArrayList<User> observers = XMLFrogDatabase.getObservers();
		ArrayList<User> recorders = XMLFrogDatabase.getRecorders();
		comboObserver = new JComboBox<User>();
		comboRecorder = new JComboBox<User>();
		recorderListModel = new DefaultComboBoxModel<User>();
		observerListModel = new DefaultComboBoxModel<User>();
		
		comboRecorder.setModel(recorderListModel);
		comboObserver.setModel(observerListModel);
		comboObserver.setRenderer(new UserListCellRenderer());
		comboRecorder.setRenderer(new UserListCellRenderer());

		//Initial loading of recorder, observers
		for (User user : observers) {
			observerListModel.addElement(user);
		}
		for (User user : recorders) {
			recorderListModel.addElement(user);
		}

		setLayout(new BorderLayout());
		panelAllInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		panelAllInfo.setLayout(new BoxLayout(panelAllInfo, BoxLayout.PAGE_AXIS));

		//Images panel
		JPanel imagesPanel = new JPanel(new BorderLayout());
		imagesPanel.setMinimumSize(new Dimension(100, 50));

		addImageButton.setMaximumSize(new Dimension(160, 15));
		addImageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SiteImage newImage = addImage();
				if (newImage != null) {
					listModel.addElement(newImage);
				}
			}
		});
		imagesPanel.add(imageList, BorderLayout.CENTER);
		imagesPanel.add(addImageButton, BorderLayout.SOUTH);

		JPanel panelTopButtons = new JPanel();
		//site survey panel
		JPanel panelSiteSurvey = new JPanel();
		panelSiteSurvey.setLayout(new BoxLayout(panelSiteSurvey, BoxLayout.PAGE_AXIS));
		//panelSiteSurvey.setAlignmentX(Component.CENTER_ALIGNMENT);
		TitledBorder siteSurveyBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Site Survey");
		siteSurveyBorder.setTitleFont(level1TitleFont);
		panelSiteSurvey.setBorder(siteSurveyBorder);

		JPanel panelDataEntry = new JPanel(new GridBagLayout());
		TitledBorder entryPersonBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Data Entry");
		entryPersonBorder.setTitleFont(level2TitleFont);
		panelDataEntry.setBorder(entryPersonBorder);
		//recPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel panelFrogInfo = new JPanel(new GridBagLayout());
		TitledBorder staticFrogBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Static Frog Information");
		staticFrogBorder.setTitleFont(level1TitleFont);
		panelFrogInfo.setBorder(staticFrogBorder);

		JPanel panelBiometrics = new JPanel(new GridBagLayout());
		TitledBorder biometricsBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Biometrics");
		biometricsBorder.setTitleFont(level2TitleFont);
		panelBiometrics.setBorder(biometricsBorder);

		JPanel panelLocation = new JPanel(new GridBagLayout());
		TitledBorder locationBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Capture Location");
		locationBorder.setTitleFont(level2TitleFont);
		panelLocation.setBorder(locationBorder);
		//panelLocation.setAlignmentX(Component.LEFT_ALIGNMENT);
		panelAllInfo.setPreferredSize(new Dimension(800, 695));

		//top buttons==============
		butFillPreviousFrogInfo.setText("Fill From Template");
		butFillPreviousFrogInfo.setVisible(true);
		butFillPreviousFrogInfo.setIcon(new ImageIcon(MainFrame.class.getResource("/resources/IconRefresh32.png")));
		butFillPreviousFrogInfo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				butFillPreviousFrogInfo_actionPerformed(e);
			}
		});

		//Manage Users
		ImageIcon imageUser = new ImageIcon(FrogEditor.class.getResource("/resources/IconUser32.png"));
		usersButton = new JButton("Manage Users", imageUser);
		//Icon from: http://www.softicons.com/business-icons/dragon-soft-icons-by-artua.com/user-icon
		//License: Free for non-commercial use, commercial use not allowed

		usersButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openUsersWindow();
			}
		});
		
		//Manage Discriminators
		butEditDiscriminators.setText("Edit Discriminators");
		butEditDiscriminators.setIcon(new ImageIcon(MainFrame.class.getResource("/resources/IconDiscriminator32.png")));
		butEditDiscriminators.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openDiscriminatorsWindow();
			}
		});

		
		//Debugging button
		butDebugPopulate.setText("Debug: Autopopulate");
		butDebugPopulate.setVisible(false);
		butDebugPopulate.setIcon(new ImageIcon(MainFrame.class.getResource("/resources/IconDebug32.png")));
		butDebugPopulate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				butDebugAutopopulate_actionPerformed(e);
			}
		});
		if (IdentiFrog.DEBUGGING_BUILD) {
			butDebugPopulate.setVisible(true);
		}

		Insets leftSpaceInsets = new Insets(0, 10, 0, 0);
		Insets noInsets = new Insets(0, 0, 0, 0);
		GridBagConstraints c = new GridBagConstraints();

		//FROG INFO
		// PANEL FROG INFO
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.2;
		c.weighty = 0;

		//row1
		panelFrogInfo.add(labFrog_ID, c);
		c.gridx = 1;
		c.weightx = 1;
		c.insets = leftSpaceInsets;
		panelFrogInfo.add(labSpecies, c);
		c.gridx = 2;
		panelFrogInfo.add(labSex, c);

		//row 2
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.2;
		c.insets = noInsets;
		panelFrogInfo.add(textFrog_ID, c);
		textFrog_ID.setEnabled(false);
		c.weightx = 1;
		c.insets = leftSpaceInsets;
		c.gridx = 1;
		panelFrogInfo.add(textSpecies, c);
		c.gridx = 2;
		panelFrogInfo.add(sexComboBox, c);

		c.gridx = 3;
		c.gridy = 1;
		panelFrogInfo.add(checkAdditionalDescriptor, c);

		panelFrogInfo.setMinimumSize(new Dimension(100, 60));
		panelFrogInfo.setMaximumSize(new Dimension(10000, 60));

		sexComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sexComboBox_actionPerformed(e);
			}
		});

		//Data Entry Panel======================
		c = new GridBagConstraints();
		JLabel recorderLabel = new JLabel("Recorder");
		JLabel observerLabel = new JLabel("Observer");

		labEntrydate.setText("Entry Date");
		labCapturedate.setText("Capture Date");

		UtilDateModel entryDateModel = new UtilDateModel();
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl entryDatePickerPanel = new JDatePanelImpl(entryDateModel, p);
		entryDatePicker = new JDatePickerImpl(entryDatePickerPanel, new DateLabelFormatter());

		UtilDateModel captureDateModel = new UtilDateModel();
		JDatePanelImpl captureDatePanel = new JDatePanelImpl(captureDateModel, p);
		captureDatePicker = new JDatePickerImpl(captureDatePanel, new DateLabelFormatter());

		//1st row
		//c.weightx = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		//c.fill = GridBagConstraints.HORIZONTAL;
		panelDataEntry.add(labEntrydate, c);
		c.insets = leftSpaceInsets;
		c.gridx = 1;
		panelDataEntry.add(labCapturedate, c);

		//2nd row
		c.insets = noInsets;
		c.gridx = 0;
		c.gridy = 1;
		panelDataEntry.add(entryDatePicker, c);
		c.insets = leftSpaceInsets;
		c.gridx = 1;
		panelDataEntry.add(captureDatePicker, c);

		//3nd row
		c.insets = noInsets;
		c.gridx = 0;
		c.gridy = 2;
		panelDataEntry.add(recorderLabel, c);
		c.insets = leftSpaceInsets;
		c.gridx = 1;
		panelDataEntry.add(observerLabel, c);

		//4th row
		c.insets = noInsets;
		c.gridx = 0;
		c.gridy = 3;
		//panelDataEntry.add(entryDatePicker,c);
		//c.gridx = 1;
		panelDataEntry.add(comboRecorder, c);
		c.insets = leftSpaceInsets;
		c.gridx = 1;
		c.weightx = 1;
		panelDataEntry.add(comboObserver, c);
		panelDataEntry.setMaximumSize(new Dimension(10000, 120));

		Butgroup.add(LatLongButton);
		Butgroup.add(UTMButton);
		LatLongButton.addActionListener(radButtonAction);
		UTMButton.addActionListener(radButtonAction);
		labCapturedate.setText("Capture Date");

		//PANEL BIOMETRICS INFO
		labMass.setText("Mass, g");
		textMass.setColumns(5);
		labLength.setText("Length, mm");
		textLength.setColumns(5);
		labSurveyID.setText("Survey ID");
		textSurveyID.setColumns(50);
		labFrogComments.setText("Comments");
		textFrogComments.setColumns(210);

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		panelBiometrics.add(labMass, c);
		c.insets = leftSpaceInsets;
		c.gridx = 1;
		panelBiometrics.add(labLength, c);
		c.gridx = 2;
		panelBiometrics.add(labSurveyID, c);
		//row2
		c.insets = noInsets;
		c.gridx = 0;
		c.gridy = 1;
		panelBiometrics.add(textMass, c);
		c.insets = leftSpaceInsets;
		c.gridx = 1;
		panelBiometrics.add(textLength, c);
		c.gridx = 2;
		panelBiometrics.add(textSurveyID, c);

		c.insets = noInsets;
		c.gridy = 2;
		c.gridx = 0;
		panelBiometrics.add(labFrogComments, c);

		//row 4
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 3;
		c.gridheight = 2;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		panelBiometrics.add(textFrogComments, c);

		// PANEL LOCATION INFO
		labLocationName.setText("Location Name");
		comboLocationName.setSelectedIndex(0);
		comboLocationName.setEditable(true);
		comboLocationName.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				locnameComboBox_actionPerformed(e);
			}
		});
		labLocDesc.setText("Location Description");
		textLocDesc.setColumns(200);
		labX.setText("Latitude");
		textX.setColumns(128);
		labY.setText("Longitude");
		textY.setColumns(128);
		//LatLongButton.setBounds(new Rectangle(288, 100, 80, 20));
		//UTMButton.setBounds(new Rectangle(375, 100, 80, 20));
		Butgroup.add(LatLongButton);
		Butgroup.add(UTMButton);
		LatLongButton.addActionListener(radButtonAction);
		UTMButton.addActionListener(radButtonAction);
		labDatum.setText("Datum");
		labZone.setText("Zone");
		textZone.setColumns(200);
		butNewEntry.setIcon(new ImageIcon(MainFrame.class.getResource("/resources/IconSave32.png")));
		butNewEntry.setText("Save Entry");
		butNewEntry.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				butNewEntry_actionPerformed(e);
			}
		});
		//labImage.setBounds(new Rectangle(8, 6, 165, 133));
		butCancel.setVerifyInputWhenFocusTarget(true);
		butCancel.setIcon(new ImageIcon(MainFrame.class.getResource("/resources/IconCancel32.png")));
		butCancel.setText("Cancel");
		butCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				butCancel_actionPerformed(e);
			}
		});
		butClearAll.setIcon(new ImageIcon(MainFrame.class.getResource("/resources/IconBlank32.png")));
		butClearAll.setText("Clear All");
		butClearAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				butClearAll_actionPerformed(e);
			}
		});

		//Add all items to interface

		//top buttons
		panelTopButtons.setLayout(new BoxLayout(panelTopButtons, BoxLayout.LINE_AXIS));
		panelTopButtons.add(Box.createHorizontalGlue());
		panelTopButtons.add(butFillPreviousFrogInfo);
		panelTopButtons.add(Box.createRigidArea(new Dimension(10, 10)));
		panelTopButtons.add(usersButton);
		panelTopButtons.add(Box.createRigidArea(new Dimension(10, 10)));
		panelTopButtons.add(butEditDiscriminators);
		if (IdentiFrog.DEBUGGING_BUILD) {
			panelTopButtons.add(Box.createRigidArea(new Dimension(10, 10)));
			panelTopButtons.add(butDebugPopulate);
		}
		panelTopButtons.add(Box.createHorizontalGlue());

		panelSiteSurvey.add(panelDataEntry);
		panelSiteSurvey.add(panelBiometrics);
		panelSiteSurvey.add(panelLocation);

		//PANEL LOCATION INFO
		c = new GridBagConstraints();
		Insets topInsets = new Insets(3, 5, 0, 5);
		Insets bottomInsets = new Insets(0, 5, 3, 5);
		c.insets = topInsets;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		panelLocation.add(labLocationName, c);
		c.gridx = 1;
		panelLocation.add(labLocDesc, c);

		//row2
		c.insets = bottomInsets;
		c.gridx = 0;
		c.gridy = 1;
		panelLocation.add(comboLocationName, c);
		c.gridx = 1;
		panelLocation.add(textLocDesc, c);

		//row 3
		c.insets = topInsets;
		c.gridx = 0;
		c.gridy = 2;
		panelLocation.add(labX, c);
		c.gridx = 1;
		panelLocation.add(labY, c);

		//row 4
		c.insets = bottomInsets;
		c.gridy = 3;
		c.gridx = 0;
		panelLocation.add(textX, c);
		c.gridx = 1;
		panelLocation.add(textY, c);

		//row 5
		c.insets = noInsets;
		c.gridy = 4;
		c.gridx = 0;
		panelLocation.add(LatLongButton, c);
		c.gridx = 1;
		panelLocation.add(UTMButton, c);

		//row 6
		c.insets = topInsets;
		c.gridy = 5;
		c.gridx = 0;
		panelLocation.add(labDatum, c);
		c.gridx = 1;
		panelLocation.add(labZone, c);

		//row 7
		c.insets = bottomInsets;
		c.gridy = 6;
		c.gridx = 0;
		panelLocation.add(textDatum, c);
		c.gridx = 1;
		c.weighty = 1;
		panelLocation.add(textZone, c);
		panelLocation.setMaximumSize(new Dimension(10000, 200));
		// panelLocation.add(labCoorType, null);

		//order panels
		panelBottomButtons.add(Box.createHorizontalGlue());
		panelBottomButtons.add(butCancel);
		panelBottomButtons.add(Box.createRigidArea(new Dimension(10, 10)));
		panelBottomButtons.add(butClearAll);
		panelBottomButtons.add(Box.createRigidArea(new Dimension(10, 10)));

		panelBottomButtons.add(butNewEntry);
		panelBottomButtons.add(Box.createHorizontalGlue());
		//panelAllInfo.add(labImage);
		panelAllInfo.add(panelTopButtons);
		panelAllInfo.add(panelFrogInfo);
		panelAllInfo.add(panelSiteSurvey);
		//panelAllInfo.add(labFRO);

		panelAllInfo.add(Box.createVerticalGlue());
		panelAllInfo.add(panelBottomButtons);
		//getContentPane().add(panelAllInfo, BorderLayout.CENTER);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagesPanel, panelAllInfo);
		splitPane.setDividerLocation(110);
		add(splitPane);
		setMinimumSize(new Dimension(650, 640));
		setPreferredSize(new Dimension(650, 640));
		pack();
	}

	/**
	 * Opens the Discriminators Management window and stalls execution of this thread until completed
	 */
	protected void openDiscriminatorsWindow() {
		// Keep user selection so when refresh we can set the proper one
		//TODO - get list of selected discriminators
		
		DiscriminatorFrame dFrame = new DiscriminatorFrame(this);
		dFrame.setVisible(true);
		
		//Reload
		/*observerListModel.removeAllElements();
		for (User user : XMLFrogDatabase.getObservers()) {
			observerListModel.addElement(user);
		}
		for (User user : XMLFrogDatabase.getRecorders()) {
			recorderListModel.addElement(user);
		}
		
		//Restore existing selection if it exists
		if (observerListModel.getIndexOf(obsUser) >= 0){
			comboObserver.setSelectedIndex(observerListModel.getIndexOf(obsUser));
		}
		if (recorderListModel.getIndexOf(recUser) >= 0){
			comboRecorder.setSelectedIndex(recorderListModel.getIndexOf(recUser));
		}*/
	}

	/**
	 * Opens the User management window, and stalls execution of this thread until it completes. Once complete it reloads the user lists.
	 */
	protected void openUsersWindow() {
		// Keep user selection so when refresh we can set the proper one
		User recUser = (User) comboRecorder.getSelectedItem();
		User obsUser = (User) comboObserver.getSelectedItem();
		
		UsersFrame uFrame = new UsersFrame(this);
		uFrame.setVisible(true);
		
		//Reload
		observerListModel.removeAllElements();
		recorderListModel.removeAllElements();
		for (User user : XMLFrogDatabase.getObservers()) {
			observerListModel.addElement(user);
		}
		for (User user : XMLFrogDatabase.getRecorders()) {
			recorderListModel.addElement(user);
		}
		
		//Restore existing selection if it exists
		if (observerListModel.getIndexOf(obsUser) >= 0){
			comboObserver.setSelectedIndex(observerListModel.getIndexOf(obsUser));
		}
		if (recorderListModel.getIndexOf(recUser) >= 0){
			comboRecorder.setSelectedIndex(recorderListModel.getIndexOf(recUser));
		}
	}

	/**
	 * Populates the add frog interface with old values from the last frog data.
	 */
	protected void imposeLastValues() {
		IdentiFrog.LOGGER.writeError("imposeLastValues() is no longer implemented."); 
		/*
		 * DefaultComboBoxModel<String> celnBoxModel =
		 * (DefaultComboBoxModel<User>) comboRecorder.getModel(); if
		 * (celnBoxModel.getIndexOf(lastFrog.getRecorder().getLastName()) == -1)
		 * { //just set the data as it does not yet exist.
		 * comboRecorder.setSelectedItem(lastFrog.getRecorder().getLastName());
		 * } else { //does exists - use it instead of adding it
		 * comboRecorder.setSelectedIndex
		 * (celnBoxModel.getIndexOf(lastFrog.getRecorder().getLastName())); }
		 */

		/*
		 * comboEntryFirstName.setSelectedItem(lastFrog.getRecorder().getFirstName
		 * ());
		 * comboObserverLastName.setSelectedItem(lastFrog.getObserver().getLastName
		 * ()); comboObserverFirstName.setSelectedItem(lastFrog.getObserver().
		 * getFirstName());
		 
		textFrog_ID.setText(Integer.toString(lastFrog.getID()));
		// textFrog_ID.setText(nextAvailFrogId);
		textSurveyID.setText(lastFrog.getSurveyID());
		textSpecies.setText(lastFrog.getSpecies());
		sexComboBox.setSelectedItem(lastFrog.getGender());
		String dateCapture = lastFrog.getDateCapture();
		IdentiFrog.LOGGER.writeMessage(dateCapture);
		IdentiFrog.LOGGER.writeMessage(dateCapture.indexOf('-'));
		IdentiFrog.LOGGER.writeMessage(dateCapture.lastIndexOf('-'));
		IdentiFrog.LOGGER.writeMessage((String) dateCapture.subSequence(dateCapture.indexOf('-') + 1, dateCapture.lastIndexOf('-')));
		//int m = new Integer((String) dateCapture.subSequence(dateCapture.indexOf('-') + 1, dateCapture.lastIndexOf('-'))).intValue() - 1;
		//dayComboBox.setSelectedItem(dateCapture.subSequence(dateCapture.lastIndexOf('-') + 1, dateCapture.length()));
		//monthComboBox.setSelectedItem(month[m]);
		//yearComboBox.setSelectedItem(dateCapture.subSequence(0, dateCapture.indexOf('-')));

		textMass.setText(lastFrog.getMass());
		textLength.setText(lastFrog.getLength());
		textFrogComments.setText(lastFrog.getComments());
		comboLocationName.setSelectedItem(lastFrog.getLocation().getName());
		textLocDesc.setText(lastFrog.getLocation().getDescription());
		textX.setText(lastFrog.getLocation().getLongitude());
		textY.setText(lastFrog.getLocation().getLatitude());
		textDatum.setText(lastFrog.getLocation().getDatum());
		if (lastFrog.getDiscriminator().equals("Yes")) {
			additDiscrNo.setEnabled(false);
			additDiscrYes.setEnabled(true);
		} else {
			additDiscrNo.setEnabled(true);
			additDiscrYes.setEnabled(false);
		}*/
	}

	protected void setLastValues() {
		IdentiFrog.LOGGER.writeError("setLastValues() is no longer implemented."); 

		//Personel ob = new Personel("observer", (String) comboObserverFirstName.getSelectedItem(), (String) comboObserverLastName.getSelectedItem());
		//Personel rc = new Personel("recorder", (String) comboEntryFirstName.getSelectedItem(), (String) comboEntryLastName.getSelectedItem());
		/*String obsvr = "Observer String";
		String rcrdr = "Recorder String";
		String coordinateType;
		if (LatLongButton.isSelected()) {
			coordinateType = "Lat/Long";
		} else {
			coordinateType = "UTM";
		}
		coordinateType = null;
		Date dateCaptureObj = (Date) captureDatePicker.getModel().getValue();
		DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
		String dateCapture = df.format(dateCaptureObj);
		//String dateCapture = DateFormat.dateCaptureObj
		//		String dateCapture = (String) yearComboBox.getSelectedItem() + "-" + (String) monthComboBox.getSelectedItem() + "-" + (String) dayComboBox.getSelectedItem();

		Location lc = new Location((String) comboLocationName.getSelectedItem(), textLocDesc.getText(), coordinateType, textX.getText(),
				textY.getText(), textDatum.getText(), textZone.getText());
		lastFrog = new Frog(frogID, textSurveyID.getText(), textSpecies.getText(), (String) sexComboBox.getSelectedItem(), textMass.getText(),
				textLength.getText(), dateCapture, "", obsvr, rcrdr, "", textFrogComments.getText(), lc);
		
		 * this.ID = ID; this.surveyID = surveyID; this.species = species;
		 * this.gender = gender; this.mass = mass; this.length = length;
		 * this.dateCapture = dateCapture; this.dateEntry = dateEntry;
		 * this.observer = observer; this.recorder = recorder;
		 * this.discriminator = discriminator; this.comments = comments;
		 * this.location = location;
		 */
	}

	void butFillPreviousFrogInfo_actionPerformed(ActionEvent e) {
		imposeLastValues();
	}

	void locnameComboBox_actionPerformed(ActionEvent e) {
		int locind = comboLocationName.getSelectedIndex();
		// set to original coordinates
		if (locind > 0) { // in comboBox Location List index = 0 for a new entry
			--locind;
			textX.setText(Double.toString(locList.get(locind).loccoor.getX()));
			textY.setText(Double.toString(locList.get(locind).loccoor.getY()));
		} /*else {
			textX.setText("");
			textY.setText("");
		}*/
	}

	void sexComboBox_actionPerformed(ActionEvent e) {
		gender = (String) sexComboBox.getSelectedItem();
	}
	/**
	 * Validates all data for the currently displayed Site Sample and static
	 * frog info
	 * 
	 * @return True if validated, false otherwise
	 */
	private boolean validateData() {
		boolean isError = true;
		String errorMessage = "Error message at validate field";
		// validate entry person's last name
		if (comboObserver.getSelectedItem() == null || comboRecorder.getSelectedItem() == null) {
			errorMessage = "Site Surveys must have both a recorder and an observer.\nYou can add users that fill these roles via the Manager Users button.";
		} else if (!entryDatePicker.getModel().isSelected()) {
			entryDatePicker.requestFocus(true);
			errorMessage = "Entry date cannot be empty";
		} else 	if (!captureDatePicker.getModel().isSelected()) {
			captureDatePicker.requestFocus(true);
			errorMessage = "Capture date cannot be empty";
		} else if (isEmptyString(textSpecies.getText())) {
			textSpecies.requestFocus(true);
			errorMessage = "Frog species cannot be empty";
		} else if (isEmptyString((String) sexComboBox.getSelectedItem())) {
			sexComboBox.requestFocus(true);
			errorMessage = "A frog must entry must have a gender.";
		} else if (!entryDatePicker.getModel().isSelected()) {
			entryDatePicker.requestFocus(true);
			errorMessage = "Entry date cannot be empty";
		} else if (isEmptyString(textMass.getText())) {
			textMass.requestFocus(true);
			errorMessage = "Frog's mass cannot be empty";
		} else if (isEmptyString(textSurveyID.getText())) {
			textSurveyID.requestFocus(true);
			errorMessage = "Survey ID cannot be empty";
		} else if (isEmptyString((String) comboLocationName.getSelectedItem())) {
			comboLocationName.requestFocus(true);
			errorMessage = "Survey location name cannot be empty";
		} else if (!LatLongButton.isSelected() && !UTMButton.isSelected()) {
			LatLongButton.requestFocus(true);
			errorMessage = "Select between Lat/Long and UTM";
			/*
			 * Remember that x is longitude (East/West), and y is latitude
			 * (North/South).
			 * In this code however, for human readability, the UI is laid out as 
			 * Lat, Long, which is actually Y, X
			 */
		} else if (isEmptyString(textX.getText())) {
			textX.requestFocus(true);
			if (LatLongButton.isSelected()) {
				errorMessage = "Latitude cannot be empty";
			} else {
				errorMessage = "Easting cannot be empty";
			}
		} else if (isEmptyString(textY.getText())) {
			textY.requestFocus(true);
			if (LatLongButton.isSelected()) {
				errorMessage = "Longitude cannot be empty";
			} else {
				errorMessage = "Northing cannot be empty";
			}
		} else if (isEmptyString(textDatum.getText())) {
			textDatum.requestFocus(true);
			errorMessage = "Datum information cannot be empty";
		} else if (UTMButton.isSelected() && isEmptyString(textZone.getText())) {
			textZone.requestFocus(true);
			errorMessage = "Zone cannot be empty when UTM is selected";
		} else {
			isError = false;
			try {
				mass = Double.parseDouble(textMass.getText());
			} catch (NumberFormatException nfe) {
				errorMessage = "Sample's mass must be a number";
				isError = true;
			}
			try {
				length = Double.parseDouble(textLength.getText());
			} catch (NumberFormatException nfe) {
				errorMessage = "Sample's length must be a number";
				isError = true;
			}
			/*
			 * try { x = Double.parseDouble(textX.getText()); } catch
			 * (NumberFormatException nfe) { errorMessage =
			 * "Longitude must be a number"; isError = true; } try { y =
			 * Double.parseDouble(textY.getText()); } catch
			 * (NumberFormatException nfe) { errorMessage =
			 * "Latitude must be a number"; isError = true; } if
			 * (!isEmptyString(locCoorType) && locCoorType.equals("UTM") &&
			 * !isEmptyString(textZone.getText())) { try { zone =
			 * Integer.parseInt(textZone.getText()); } catch
			 * (NumberFormatException nfe) { errorMessage = "Zone be a number";
			 * isError = true; } }
			 */
		}
		if (isError) {
			JOptionPane.showMessageDialog(this, errorMessage, "Data Validation Error", JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Checks if the given input string is empty or null
	 * 
	 * @param input
	 *            String to check
	 * @return true if string is empty or null (after trimming!), false
	 *         otherwise
	 */
	private boolean isEmptyString(String input) {
		if (input != null && input.trim().length() != 0) {
			return false;
		} else {
			return true;
		}
	}

	// ***************** NEW ENTRY *************************** //
	protected void butNewEntry_actionPerformed(ActionEvent e) {

		// ******************** VALIDATE FIELD ******************** //
		if (validateData()) {
			Date eDate = (Date) entryDatePicker.getModel().getValue();
			entrydate = df.format(eDate);
			species = textSpecies.getText().trim();
			gender = (String) sexComboBox.getSelectedItem();
			// Additional Discriminator
			String discriminator = (checkAdditionalDescriptor.isSelected()) ? "true": "false";
			//int m = monthComboBox.getSelectedIndex() + 1;
			capturedate = df.format(captureDatePicker.getModel().getValue());

			//yearComboBox.getSelectedItem() + "-" + m + "-"
			//+ dayComboBox.getSelectedItem();

			// mass
			// length
			surveyID = textSurveyID.getText();
			comments = textFrogComments.getText();
			locationName = (String) comboLocationName.getSelectedItem();
			locationDescription = textLocDesc.getText().trim();
			zone = Integer.parseInt(textZone.getText().trim());
			// longitude
			// latitude
			// datum
			// zone
			if (LatLongButton.isSelected()) {
				locCoorType = "Lat/Long";
			} else if (UTMButton.isSelected()) {
				locCoorType = "UTM";
			}
			if (datum == null) {
				datum = "1";
			}
			Location lc = new Location(locationName, locationDescription, locCoorType, textX.getText(), textY.getText(), datum,
					Integer.toString(zone));

			//Generate sitesample
			SiteSample firstSample = new SiteSample();
			firstSample.setSurveyID(textSurveyID.getText());
			firstSample.setMass(textMass.getText());
			firstSample.setLength(textLength.getText());
			firstSample.setDateCapture(capturedate);
			firstSample.setDateEntry(entrydate);
			User selectedObs = (User) comboObserver.getSelectedItem();
			User selectedRec = (User) comboRecorder.getSelectedItem();
			firstSample.setRecorder(XMLFrogDatabase.getRecorderByID(selectedRec.getID())); //we use IDs instead of assigning values because 
			firstSample.setObserver(XMLFrogDatabase.getObserverByID(selectedObs.getID())); //these may be out of sync with the XML database (say a user fools with the file in the background)
			firstSample.setComments(comments);
			firstSample.setLocation(lc);
			
			ArrayList<SiteImage> images = new ArrayList<SiteImage>();
			ListModel<SiteImage> imgList = imageList.getModel();
			for (int i = 0; i < imgList.getSize(); i++){
				//prepare frog images for DB storing
				SiteImage img = imgList.getElementAt(i);
				
				//once processed an image is finalized
				if (!img.isProcessed()){
					if (img.getSourceImageHash() == null) {
						img.generateHash();
					}
					img.processImageIntoDB();
					
				}
				images.add(img);
			}
			firstSample.setSiteImages(images);

			Frog f = new Frog(frogID, species, gender, firstSample);
			f.setDiscriminator(discriminator);
			IdentiFrog.LOGGER.writeMessage("FrogEditor is closing, assigning new/modified frog to return method...");
			this.frog = f;
			// close dialog
			//new UITest1(frog).setVisible(true);
			dispose();
			//openDigSigFrame(maxfrogdbid);
		}
	} // end of ActionPerformed for new entry button

	void butCancel_actionPerformed(ActionEvent e) {
		dispose();
	}

	/**
	 * This opens the signature generator window.
	 * 
	 * @param frogdbid
	 */
	private SiteImage openDigSigFrame(SiteImage image) {
		IdentiFrog.LOGGER.writeMessage("Opening signature generation tool... This shouldn't work yet...");
		iFrame = new ImageManipFrame(this, image);
		iFrame.setTitle("Signature Creation");
		iFrame.pack();
		// Signature Creation Window
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// Make the frame size of screen.
		int fwt = (int) screen.getWidth();
		int fht = (int) (screen.getHeight() * 0.92);
		iFrame.setSize(fwt, fht);
		iFrame.setLocation(0, 0);
		iFrame.setVisible(true);
		SiteImage modifiedImg = iFrame.getImage();
		if (modifiedImg != null) {
			//if its not modified, we just return the same image object
			image = modifiedImg;
		}
		return image;
	}

	void butClearAll_actionPerformed(ActionEvent e) {
		if (ChoiceDialog.choiceMessage("This will clear all entered information on this screen.\n" + "Do you want to continue?") == 0) {
			/*
			 * comboEntryLastName.setSelectedIndex(0);
			 * comboEntryFirstName.setSelectedIndex(0);
			 * comboObserverLastName.setSelectedIndex(0);
			 * comboObserverFirstName.setSelectedIndex(0);
			 */
			textFrog_ID.setText("");
			textSpecies.setText("");
			sexComboBox.setSelectedItem(-1);
			//dayComboBox.setSelectedItem(null);
			//monthComboBox.setSelectedItem(null);
			//yearComboBox.setSelectedItem(null);
			captureDatePicker.getModel().setSelected(false);
			sexComboBox.setSelectedItem(null);
			textMass.setText("");
			textLength.setText("");
			textFrogComments.setText("");
			textSurveyID.setText("");
			comboLocationName.setSelectedItem(null);
			textLocDesc.setText("");
			textX.setText("");
			textY.setText("");
			Butgroup.clearSelection(); // set radio LatLongButton, UTMButton to false
			checkAdditionalDescriptor.setSelected(false);
			textDatum.setText("");
			textZone.setText("");
		}
	}

	private void butDebugAutopopulate_actionPerformed(ActionEvent e) {
		textFrog_ID.setText(Integer.toString(XMLFrogDatabase.getNextAvailableFrogID()));
		textSpecies.setText("Jumpy");
		sexComboBox.setSelectedItem("M");
		//dayComboBox.setSelectedItem("16");
		//monthComboBox.setSelectedItem("Mar");
		//yearComboBox.setSelectedItem("2015");
		LocalDate now = LocalDate.now();
		captureDatePicker.getModel().setDate(now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()); //-1 cause it's 0 indexed
		captureDatePicker.getModel().setSelected(true);
		textMass.setText("28");
		textLength.setText("64");
		textFrogComments.setText("Really hoppy this one was");
		textSurveyID.setText("X");
		comboLocationName.getEditor().setItem("Area 51");
		textLocDesc.setText("Hoppy Pond");
		textX.setText("115.8111");
		textY.setText("37.2350");
		Butgroup.setSelected(LatLongButton.getModel(), true); // set radio LatLongButton, UTMButton to false
		checkAdditionalDescriptor.setSelected(false);
		textDatum.setText("1");
		textZone.setText("1");
	}

	// end of search for previous entries
	private class LocInfo {
		@SuppressWarnings("unused")
		public int dbid;
		@SuppressWarnings("unused")
		public String locname;
		public Point2D.Double loccoor;

		@SuppressWarnings("unused")
		public LocInfo(int db_id, String loc_name, Point.Double coor) {
			dbid = db_id;
			locname = loc_name;
			loccoor = coor;
		}
	}

	/**
	 * Returns frog data this frame is holding. In normal cases this is the
	 * edited frog from the one passed in when this frame as originally
	 * displayed in edit mode.
	 * 
	 * @return Frog object this frame is describing
	 */
	public Frog getFrog() {
		return frog;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * This method is called when the add image button is clicked.
	 * 
	 * @return
	 */
	public SiteImage addImage() {
		String home = System.getProperty("user.home");
		DialogImageFileChooser imageChooser = new DialogImageFileChooser("Choose Frog Photograph...", false, home);
		String filename = imageChooser.getName();
		if (filename != null) {
			SiteImage image = new SiteImage();
			image.setSourceFilePath(imageChooser.getName());
			image.setProcessed(false);
			//TODO - check for duplicates
			image.createListThumbnail();
			return image;
		}
		return null;
	}

}