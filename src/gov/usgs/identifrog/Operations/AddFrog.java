package gov.usgs.identifrog.Operations;

import gov.usgs.identifrog.ChoiceDialog;
import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.ImageManipFrame;
import gov.usgs.identifrog.MainFrame;
import gov.usgs.identifrog.DataObjects.DateLabelFormatter;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.Location;
import gov.usgs.identifrog.DataObjects.Personel;
import gov.usgs.identifrog.Handlers.DataHandler;
import gov.usgs.identifrog.Handlers.FolderHandler;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

/**
 * <p>
 * Title: AddFrog.java
 * <p>
 * Description: displays window 'Frog Information' for the user to fill out the fields, adds filled
 * entries to table Frog, Observer, EntryPerson, CaptureLocation
 * @author Michael J. Perez 2015
 * @author Hidayatullah Ahsan 2012
 * @author Oksana V. Kelly 2008
 * @author Oksana V. Kelly modified the code written by Mike Ramshaw from IdentiFrog 2005
 */
@SuppressWarnings("serial")
public class AddFrog extends JDialog {
	protected MainFrame parentFrame;

	private Preferences root = Preferences.userRoot();
	private Preferences node = root.node("edu/isu/aadis/defaults");

	int frogDbId, entpersDbId, obsDbId, caplocDbId, zone, frogidNum;

	String surveyID;
	private boolean isNewImage = true;
	protected String frogID;
	protected String species;
	protected String pathSignature;
	protected String pathImage;
	protected String pathBinary;
	protected String formerID;
	protected String locationDescription;
	protected String locationName;
	protected String locCoorType;
	protected String obsLastName;
	protected String obsFirstName;
	protected String comments;
	protected String gender;
	protected String entryLastName;
	protected String entryFirstName;
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
	protected File image;
	protected ImageManipFrame iFrame;
	private String nextAvailFrogId;
	private String thumbnailFilename;
	private Frog frog; //null if not in edit mode
	private FolderHandler fh;
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * This is the standard add-frog window (not edit)
	 * @param frame Parent frame
	 * @param fh Folder handler that is associated with the project
	 * @param title Window title
	 * @param modal if this window should block others
	 * @param image Image to load for this frog since it's not in the DB yet.
	 */
	public AddFrog(MainFrame frame,FolderHandler fh, String title, boolean modal, File image) {
		super((Frame) frame, title, modal);
		this.image = image;
		parentFrame = frame;
		this.fh = fh;
		try {
			init();
			LocalDate now = LocalDate.now();
			IdentiFrog.LOGGER.writeMessage("New Frog, current date from LocalDate: "+now.getYear()+"-"+now.getMonthValue()+"-"+now.getDayOfMonth());
			entryDatePicker.getModel().setDate(now.getYear(), now.getMonthValue()-1, now.getDayOfMonth()); //-1 cause it's 0 indexed
			entryDatePicker.getModel().setSelected(true);
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("Exception while starting add frog.",e);
		}
	}
	
	/**
	 * This is the edit version of the add frog window.
	 * @param frame parent frame
	 * @param fh folderhandler
	 * @param title Title of the page
	 * @param frog Frog to edita and populate the interface with
	 */
	public AddFrog(MainFrame frame,FolderHandler fh, String title, Frog frog) {
		super((Frame) frame, title);
		parentFrame = frame;
		this.fh = fh;
		this.frog = frog;
		
		try {
			init();
			//load frog
			textSurveyID.setText(frog.getSurveyID());
			textFrog_ID.setText(frog.getID());
			Calendar entryCal = Calendar.getInstance();
			entryCal.setTime(df.parse(frog.getDateEntry()));
			entryDatePicker.getModel().setDate(entryCal.get(Calendar.YEAR),entryCal.get(Calendar.MONTH), entryCal.get(Calendar.DAY_OF_MONTH));
			entryDatePicker.getModel().setSelected(true);
			textSpecies.setText(frog.getSpecies());
			textMass.setText(frog.getMass());
			textLength.setText(frog.getLength());
			textFrogComments.setText(frog.getComments());
			sexComboBox.setSelectedItem(frog.getGender());
			//load datepicker data
			Calendar captureCal = Calendar.getInstance();
			captureCal.setTime(df.parse(frog.getDateCapture()));
			IdentiFrog.LOGGER.writeMessage(captureCal.get(Calendar.YEAR)+"-"+captureCal.get(Calendar.MONTH)+"-"+captureCal.get(Calendar.DAY_OF_MONTH));
			captureDatePicker.getModel().setDate(captureCal.get(Calendar.YEAR),captureCal.get(Calendar.MONTH), captureCal.get(Calendar.DAY_OF_MONTH));
			captureDatePicker.getModel().setSelected(true);
			comboEntryFirstName.setSelectedItem(frog.getRecorder().getFirstName());
			comboObserverFirstName.setSelectedItem(frog.getObserver().getFirstName());
			comboEntryLastName.setSelectedItem(frog.getRecorder().getLastName());
			comboObserverLastName.setSelectedItem(frog.getObserver().getLastName());
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("Exception while starting add frog.",e);
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
				
			}
			if (((JRadioButton) e.getSource()).getText() == "UTM") {
				locCoorType = "UTM";
				textDatum.setVisible(true);
				textZone.setVisible(true);
				labDatum.setVisible(true);
				labZone.setVisible(true);
			}
		}
	};
	private ActionListener radDiscrButtonAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			addDiscriminator = ((JRadioButton) e.getSource()).getText();
			/*
			 * if (((JRadioButton) e.getSource()).getText() == "Yes") { addDiscriminator = "Yes"; }
			 * if (((JRadioButton) e.getSource()).getText() == "No") { addDiscriminator = "No"; }
			 */
		}
	};
	JButton butFillPreviousFrogInfo = new JButton();
	JButton butDebugPopulate = new JButton();
	JLabel labEntryPersonTitle = new JLabel();
	JLabel labObserverTitle = new JLabel();
	JLabel labFrogTitle = new JLabel();
	JLabel labCapLocationTitle = new JLabel();
	JPanel panelAllInfo = new JPanel();
	JLabel labEntryLastName = new JLabel();
	JLabel labEntryFirstName = new JLabel();
	JLabel labObserverLastName = new JLabel();
	JLabel labObserverFirstName = new JLabel();
	JLabel labSex = new JLabel();
	JLabel labNextFrog_ID = new JLabel();
	JLabel lab2NextFrog_ID = new JLabel();
	JTextField textNextFrog_ID = new JTextField();
	JLabel labSpecies = new JLabel();
	JTextField textDatum = new JTextField();
	JTextField textSurveyID = new JTextField();
	JLabel labSurveyID = new JLabel();
	JPanel panelEntryPersonInfo = new JPanel();
	JPanel panelObserverInfo = new JPanel();
	JLabel labCapturedate = new JLabel();
	JLabel labEntrydate = new JLabel();
	String[] sexStrings = { "M", "F", "J", "Unknown" };
	//String[] day = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" };
	//String[] month = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	//String[] year;
	//JComboBox<?> dayComboBox = new JComboBox<Object>(day);
	//JComboBox<?> monthComboBox = new JComboBox<Object>(month);
	//JComboBox<?> yearComboBox;
	JDatePickerImpl captureDatePicker;
	JComboBox<?> sexComboBox = new JComboBox<Object>(sexStrings);
	JRadioButton additDiscrNo = new JRadioButton("No", false);
	JRadioButton additDiscrYes = new JRadioButton("Yes", false);
	JLabel additDiscr = new JLabel("Add. Discriminator");
	ButtonGroup ButAdditDiscrGroup = new ButtonGroup();
	boolean isAdditDiscr = false;
	JTextField textMass = new JTextField();
	JPanel panelButtons = new JPanel();
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
	JLabel labImage = new JLabel("", SwingConstants.CENTER);
	//JFormattedTextField textEntrydate = new JFormattedTextField();
	JDatePickerImpl entryDatePicker;
	JLabel labZone = new JLabel();
	JTextField textLocDesc = new JTextField();
	JTextField textSpecies = new JTextField();
	JTextField textFrog_ID = new JTextField();
	JLabel labFrog_ID = new JLabel();
	JLabel labFRO = new JLabel();
	JPanel panelLocation = new JPanel();
	JPanel panelFrogInfo = new JPanel();
	JLabel labDatum = new JLabel();
	JLabel labLocationName = new JLabel();
	JLabel labMass = new JLabel();
	JLabel labLocDesc = new JLabel();
	JLabel labCoordinates = new JLabel();
	JTextField textLength = new JTextField();
	JLabel labLength = new JLabel();
	BorderLayout borderLayout1 = new BorderLayout();
	JButton butCancel = new JButton();
	JLabel labMassUnit = new JLabel();
	JLabel labLengthUnit = new JLabel();
	JButton butClearAll = new JButton();
	String[] ObsLastNameList = { "" };
	String[] ObsFirstNameList = { "" };
	String[] EntryLastNameList = { "" };
	String[] EntryFirstNameList = { "" };
	String[] LocNameList = { "" };
	ArrayList<String> obsFirstNameCorrespondToList = new ArrayList<String>();
	ArrayList<String> entryFirstNameCorrespondToList = new ArrayList<String>();
	ArrayList<LocInfo> locList = new ArrayList<LocInfo>();
	JComboBox<String> comboEntryLastName = new JComboBox<String>(EntryLastNameList);
	JComboBox<String> comboEntryFirstName = new JComboBox<String>(EntryFirstNameList);
	JComboBox<String> comboObserverLastName = new JComboBox<String>(ObsLastNameList);
	JComboBox<String> comboObserverFirstName = new JComboBox<String>(ObsFirstNameList);
	JComboBox<?> comboLocationName = new JComboBox<Object>(LocNameList);
	DataHandler frogData = new DataHandler();
	private Frog lastFrog;

	

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
		String coordinateType;
		if (node.get("lastLatLong", "").equals("true")) {
			coordinateType = "Lat/Long";
		} else {
			coordinateType = "UTM";
		}
		String dateCapture = node.get("lastCapyear", "") + node.get("lastCapmonth", "") + node.get("lastCapday", "");
		Personel ob = new Personel("observer", node.get("lastObserverFirstName", ""), node.get("lastObserverLastName", ""));
		Personel rc = new Personel("recorder", node.get("lastEntryFirstName", ""), node.get("lastEntryLastName", ""));
		Location lc = new Location(node.get("lastLocationName", ""), node.get("lastLocationDesc", ""), coordinateType, node.get("lastX", ""), node.get("lastY", ""), node.get("lastDatum", ""), node
				.get("lastZone", ""));
		lastFrog = new Frog(node.get("lastFrog_ID", ""), node.get("lastFormerID", ""), node.get("lastSurveyID", ""), node.get("lastSpecies", ""), node.get("lastSex", ""), node.get("lastMass", ""),
				node.get("lastLength", ""), dateCapture, "", ob, rc, node.get("lastDiscriminator", ""), node.get("lastFrogComments", ""), lc, "");
		frogData = parentFrame.getFrogData();

		// find max FrogId
		// int maxFrogId = 0;
		// maxFrogId = frogData.getFrogs().size();
		// find max FormerFrogId
		// LATER do something about maxFormerFrogId
		// int maxFormerFrogId = -1;
		// Next Available Frog ID
		/*
		 * if (maxFrogId >= maxFormerFrogId) { nextAvailFrogId = "" + (maxFrogId + 1); } else {
		 * nextAvailFrogId = "" + (maxFormerFrogId + 1); }
		 */
		nextAvailFrogId = Integer.toString(frogData.getNextAvailableID());
		// combobox for ObsLastName
		ArrayList<Personel> obArray = frogData.uniquePersonels("observer");
		for (int k = 0; k < obArray.size(); k++) {
			if(((DefaultComboBoxModel)comboObserverLastName.getModel()).getIndexOf(obArray.get(k).getLastName()) == -1 ) {
				comboObserverLastName.addItem(obArray.get(k).getLastName());
			}
			if(((DefaultComboBoxModel)comboObserverFirstName.getModel()).getIndexOf(obArray.get(k).getFirstName()) == -1 ) {
				comboObserverFirstName.addItem(obArray.get(k).getFirstName());
			}
			//comboObserverLastName.addItem(obArray.get(k).getLastName());
			//comboObserverFirstName.addItem(obArray.get(k).getFirstName());
			
			if (!obsFirstNameCorrespondToList.contains(obArray.get(k).getFirstName())) {
				obsFirstNameCorrespondToList.add(obArray.get(k).getFirstName()); //TODO This might cause bugs if there are multiple same first names but different last names! e.g. Jack John and Jack Josh
			}
		}
		// combobox for EntryLastName
		ArrayList<Personel> rcArray = frogData.uniquePersonels("recorder");
		for (int k = 0; k < rcArray.size(); k++) {
			if(((DefaultComboBoxModel)comboEntryLastName.getModel()).getIndexOf(rcArray.get(k).getLastName()) == -1 ) {
				comboEntryLastName.addItem(rcArray.get(k).getLastName());
			}
			if(((DefaultComboBoxModel)comboEntryFirstName.getModel()).getIndexOf(rcArray.get(k).getFirstName()) == -1 ) {
				comboEntryFirstName.addItem(rcArray.get(k).getFirstName());
			}
						
			//comboEntryLastName.addItem(rcArray.get(k).getLastName());
			if (!entryFirstNameCorrespondToList.contains(rcArray.get(k).getFirstName())) {
				entryFirstNameCorrespondToList.add(rcArray.get(k).getFirstName());
			}
		}
		// combobox for CapLocName
		/*
		 * ArrayList<Location> lcArray = frogData.uniqueLocations(); for (int k = 0; k <
		 * lcArray.size(); k++) { comboLocationName.addItem(lcArray.get(k).getName());
		 * Point2D.Double xy = new Point2D.Double(new Double(lcArray.get(k).getLatitude()), new
		 * Double(lcArray.get(k).getLongitude())); Integer locid = new Integer(-1); LocInfo locinfo
		 * = new LocInfo(locid.intValue(), lcArray.get(k).getName(), xy); locList.add(locinfo); }
		 */
		getContentPane().setLayout(borderLayout1);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		panelAllInfo.setLayout(null);
		panelEntryPersonInfo.setLayout(null);
		panelObserverInfo.setBorder(null);
		panelObserverInfo.setLayout(null);
		panelFrogInfo.setBorder(null);
		panelFrogInfo.setLayout(null);
		panelLocation.setLayout(null);
		panelAllInfo.setPreferredSize(new Dimension(800, 695));
		panelEntryPersonInfo.setBounds(new Rectangle(169, 0, 540, 170));
		panelObserverInfo.setBounds(new Rectangle(169, 180, 440, 70));
		panelFrogInfo.setBounds(new Rectangle(169, 260, 640, 180));
		panelLocation.setBounds(new Rectangle(169, 450, 440, 190));
		panelButtons.setBounds(new Rectangle(169, 645, 440, 50));
		// PANEL ENTRY PERSON INFO
		panelEntryPersonInfo.setFont(new java.awt.Font("MS Sans Serif", Font.BOLD, 14));
		butFillPreviousFrogInfo.setBounds(new Rectangle(180, 10, 180, 35));// 8
		butFillPreviousFrogInfo.setText("Populate from History");
		butFillPreviousFrogInfo.setVisible(true);
		butFillPreviousFrogInfo.setIcon(new ImageIcon(MainFrame.class.getResource("IconRefresh32.png")));
		butFillPreviousFrogInfo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				butFillPreviousFrogInfo_actionPerformed(e);
			}
		});
		//Debugging button
		butDebugPopulate.setBounds(new Rectangle(350, 10, 180, 35));// 8
		butDebugPopulate.setText("Debug: Autopopulate");
		butDebugPopulate.setIcon(new ImageIcon(MainFrame.class.getResource("IconDebug32.png")));
		butDebugPopulate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				butDebugAutopopulate_actionPerformed(e);
			}
		});
		if (IdentiFrog.DEBUGGING_BUILD) {
			butDebugPopulate.setVisible(true);
		}
		
		labEntryPersonTitle.setFont(new Font("MS Sans Serif", Font.BOLD, 14));
		labEntryPersonTitle.setBounds(new Rectangle(13, 43, 60, 20));
		labEntryPersonTitle.setText("Entry");
		labEntrydate.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labEntrydate.setBounds(new Rectangle(13, 65, 90, 20));
		labEntrydate.setText("Entry Date");
		
		UtilDateModel entryDateModel = new UtilDateModel();
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl entryDatePanel = new JDatePanelImpl(entryDateModel,p);
		entryDatePicker = new JDatePickerImpl(entryDatePanel,new DateLabelFormatter());
		entryDatePicker.setBounds(new Rectangle(13, 85, 160, 25));
		
		/*
		textEntrydate.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		textEntrydate.setBounds(new Rectangle(13, 85, 100, 25));
		textEntrydate.setColumns(10);
		textEntrydate.setValue(new SimpleDateFormat("yyyy-M-dd").format(new Date()));
		*/
		labEntryLastName.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labEntryLastName.setBounds(new Rectangle(13, 120, 80, 20));
		labEntryLastName.setText("Last Name");
		comboEntryLastName.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		comboEntryLastName.setBounds(new Rectangle(13, 140, 200, 25));
		comboEntryLastName.setSelectedIndex(0);
		comboEntryLastName.setEditable(true);
		comboEntryLastName.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				entrylastnameComboBox_actionPerformed(e);
			}
		});
		labEntryFirstName.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labEntryFirstName.setBounds(new Rectangle(223, 120, 80, 20));
		labEntryFirstName.setText("First Name");
		comboEntryFirstName.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		comboEntryFirstName.setBounds(new Rectangle(223, 140, 200, 25));
		comboEntryFirstName.setSelectedIndex(0);
		comboEntryFirstName.setEditable(true);
		comboEntryFirstName.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				entryfirstnameComboBox_actionPerformed(e);
			}
		});
		// PANEL OBSERVER INFO
		labObserverTitle.setFont(new Font("MS Sans Serif", Font.BOLD, 14));
		labObserverTitle.setBounds(new Rectangle(13, 0, 90, 20));
		labObserverTitle.setText("Observer");
		labObserverLastName.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labObserverLastName.setBounds(new Rectangle(13, 20, 90, 20));
		labObserverLastName.setText("Last Name");
		comboObserverLastName.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		comboObserverLastName.setBounds(new Rectangle(13, 40, 200, 25));
		comboObserverLastName.setSelectedIndex(0);
		comboObserverLastName.setEditable(true);
		comboObserverLastName.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				obslastnameComboBox_actionPerformed(e);
			}
		});
		labObserverFirstName.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labObserverFirstName.setBounds(new Rectangle(223, 20, 80, 20));
		labObserverFirstName.setText("First Name");
		comboObserverFirstName.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		comboObserverFirstName.setBounds(new Rectangle(223, 40, 200, 25));
		comboObserverFirstName.setSelectedIndex(0);
		comboObserverFirstName.setEditable(true);
		comboObserverFirstName.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				obsfirstnameComboBox_actionPerformed(e);
			}
		});
		// PANEL FROG INFO
		labFrogTitle.setFont(new Font("MS Sans Serif", Font.BOLD, 14));
		labFrogTitle.setBounds(new Rectangle(13, 0, 70, 20));
		labFrogTitle.setText("Frog");
		labFrog_ID.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labFrog_ID.setBounds(new Rectangle(13, 22, 15, 20));
		labFrog_ID.setText("ID");
		labFRO.setText("<html><div style=\"width:115px;\">If you want to add an image to an existing frog, use the right click menu for that frog to add one.</div></html>");
		labFRO.setFont(new Font("MS Sans Serif", Font.PLAIN, 11));
		labFRO.setBounds(new Rectangle(10, 102, 149, 140));
		
		textFrog_ID.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		textFrog_ID.setBounds(new Rectangle(13, 42, 143, 25));
		textFrog_ID.setColumns(143);
		textFrog_ID.setText(nextAvailFrogId);
		textFrog_ID.setEnabled(false);
		/*labNextFrog_ID.setText("Next Free:");
		labNextFrog_ID.setFont(new Font("MS Sans Serif", Font.PLAIN, 11));
		labNextFrog_ID.setBounds(new Rectangle(33, 22, 53, 20));
		lab2NextFrog_ID.setFont(new Font("MS Sans Serif", Font.PLAIN, 12));
		lab2NextFrog_ID.setBounds(new Rectangle(87, 22, 150, 20));
		lab2NextFrog_ID.setBounds(new Rectangle(125, 22, 90, 20));
		lab2NextFrog_ID.setText(nextAvailFrogId);*/
		labSpecies.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labSpecies.setBounds(new Rectangle(168, 22, 80, 20));
		labSpecies.setText("Species");
		textSpecies.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		textSpecies.setBounds(new Rectangle(168, 42, 163, 25));
		textSpecies.setColumns(163);
		labSex.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labSex.setBounds(new Rectangle(343, 22, 80, 20));
		labSex.setText("Gender");
		sexComboBox.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		sexComboBox.setSelectedIndex(-1);
		sexComboBox.setBounds(new Rectangle(343, 42, 78, 25));
		sexComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sexComboBox_actionPerformed(e);
			}
		});
		// Selection Addit. discriminator
		JRadioButton additDiscrNo = new JRadioButton("No", true);
		JRadioButton additDiscrYes = new JRadioButton("Yes", false);
		addDiscriminator = "Yes";
		JLabel additDiscr = new JLabel("Add. Discriminator");
		additDiscr.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		additDiscr.setBounds(new Rectangle(440, 22, 120, 20));
		ButtonGroup ButAdditDiscrGroup = new ButtonGroup();
		additDiscrNo.setFont(new Font("MS Sans Serif", Font.PLAIN, 12));
		additDiscrYes.setFont(new Font("MS Sans Serif", Font.PLAIN, 12));
		additDiscrYes.setBounds(new Rectangle(445, 45, 55, 20));
		additDiscrNo.setBounds(new Rectangle(500, 45, 55, 20));
		ButAdditDiscrGroup.add(additDiscrNo);
		ButAdditDiscrGroup.add(additDiscrYes);
		additDiscrNo.addActionListener(radDiscrButtonAction);
		additDiscrYes.addActionListener(radDiscrButtonAction);
		LatLongButton.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		UTMButton.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		LatLongButton.setBounds(new Rectangle(288, 100, 80, 20));
		UTMButton.setBounds(new Rectangle(375, 100, 80, 20));
		Butgroup.add(LatLongButton);
		Butgroup.add(UTMButton);
		LatLongButton.addActionListener(radButtonAction);
		UTMButton.addActionListener(radButtonAction);
		labCapturedate.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labCapturedate.setBounds(new Rectangle(13, 77, 100, 20));
		labCapturedate.setText("Capture Date");
		
		UtilDateModel captureDateModel = new UtilDateModel();
		/*Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");*/
		JDatePanelImpl captureDatePanel = new JDatePanelImpl(captureDateModel,p);
		captureDatePicker = new JDatePickerImpl(captureDatePanel,new DateLabelFormatter());
		captureDatePicker.setBounds(new Rectangle(13, 97, 160, 25));
		/*dayComboBox.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		dayComboBox.setBounds(new Rectangle(13, 97, 48, 25));
		dayComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dayComboBox_actionPerformed(e);
			}
		});
		monthComboBox.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		monthComboBox.setBounds(new Rectangle(68, 97, 55, 25));
		monthComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				monthComboBox_actionPerformed(e);
			}
		});
		yearComboBox.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		yearComboBox.setBounds(new Rectangle(130, 97, 63, 25));
		yearComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				yearComboBox_actionPerformed(e);
			}
		});
		dayComboBox.setSelectedItem(null);
		monthComboBox.setSelectedItem(null);
		yearComboBox.setSelectedItem(null);*/
		labMass.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labMass.setBounds(new Rectangle(206, 77, 90, 20));
		labMass.setText("Mass, g");
		textMass.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		textMass.setBounds(new Rectangle(206, 97, 103, 25));
		textMass.setColumns(5);
		labLength.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labLength.setText("Length, mm");
		labLength.setBounds(new Rectangle(321, 77, 90, 20));
		textLength.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		textLength.setColumns(5);
		textLength.setBounds(new Rectangle(321, 97, 103, 25));
		labSurveyID.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labSurveyID.setBounds(new Rectangle(13, 132, 90, 20));
		labSurveyID.setText("Survey ID");
		textSurveyID.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		textSurveyID.setBounds(new Rectangle(13, 152, 103, 25));
		textSurveyID.setColumns(50);
		labFrogComments.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labFrogComments.setBounds(new Rectangle(127, 132, 90, 20));
		labFrogComments.setText("Comment");
		textFrogComments.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		textFrogComments.setBounds(new Rectangle(127, 152, 300, 25));
		textFrogComments.setColumns(210);
		// PANEL LOCATION INFO
		labCapLocationTitle.setFont(new Font("MS Sans Serif", Font.BOLD, 14));
		labCapLocationTitle.setBounds(new Rectangle(13, 0, 140, 20));
		labCapLocationTitle.setText("Capture Location");
		labLocationName.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labLocationName.setBounds(new Rectangle(13, 22, 90, 20));
		labLocationName.setText("Location Name");
		comboLocationName.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		comboLocationName.setBounds(new Rectangle(13, 42, 200, 25));
		comboLocationName.setSelectedIndex(0);
		comboLocationName.setEditable(true);
		comboLocationName.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				locnameComboBox_actionPerformed(e);
			}
		});
		labLocDesc.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labLocDesc.setBounds(new Rectangle(223, 22, 140, 20));
		labLocDesc.setText("Location Description");
		textLocDesc.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		textLocDesc.setBounds(new Rectangle(223, 42, 200, 25));
		textLocDesc.setColumns(200);
		labX.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labX.setBounds(new Rectangle(151, 77, 120, 20));
		labX.setText("Longitude or Easting");
		textX.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		textX.setBounds(new Rectangle(151, 97, 128, 25));
		textX.setColumns(128);
		
		labY.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labY.setBounds(new Rectangle(13, 77, 120, 20));
		labY.setText("Latitude or Northing");
		textY.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		textY.setBounds(new Rectangle(13, 97, 128, 25));
		textY.setColumns(128);
		LatLongButton.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		UTMButton.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		LatLongButton.setBounds(new Rectangle(288, 100, 80, 20));
		UTMButton.setBounds(new Rectangle(375, 100, 80, 20));
		Butgroup.add(LatLongButton);
		Butgroup.add(UTMButton);
		LatLongButton.addActionListener(radButtonAction);
		UTMButton.addActionListener(radButtonAction);
		labDatum.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labDatum.setText("Datum");
		labDatum.setBounds(new Rectangle(13, 132, 90, 20));
		textDatum.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		textDatum.setBounds(new Rectangle(13, 152, 200, 25));
		textDatum.setColumns(200);
		labZone.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		labZone.setText("Zone");
		labZone.setBounds(new Rectangle(223, 132, 50, 20));
		textZone.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		textZone.setBounds(new Rectangle(223, 152, 200, 25));
		textZone.setColumns(200);
		butNewEntry.setIcon(new ImageIcon(MainFrame.class.getResource("IconSave32.png")));
		butNewEntry.setText("Save Entry");
		butNewEntry.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				butNewEntry_actionPerformed(e);
			}
		});
		labImage.setFocusable(false);
		labImage.setBorder(BorderFactory.createEtchedBorder());
		labImage.setBounds(new Rectangle(8, 6, 165, 133));
		butCancel.setVerifyInputWhenFocusTarget(true);
		butCancel.setIcon(new ImageIcon(MainFrame.class.getResource("IconCancel32.png")));
		butCancel.setText("Cancel");
		butCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				butCancel_actionPerformed(e);
			}
		});
		butClearAll.setIcon(new ImageIcon(MainFrame.class.getResource("IconBlank32.png")));
		butClearAll.setText("Clear All");
		butClearAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				butClearAll_actionPerformed(e);
			}
		});
		getContentPane().add(panelAllInfo, BorderLayout.CENTER);
		panelEntryPersonInfo.add(butFillPreviousFrogInfo, null);
		panelEntryPersonInfo.add(butDebugPopulate, null);
		panelEntryPersonInfo.add(labEntryPersonTitle, null);
		//panelEntryPersonInfo.add(textEntrydate, null);
		panelEntryPersonInfo.add(entryDatePicker, null);
		panelEntryPersonInfo.add(labEntrydate, null);
		panelEntryPersonInfo.add(labEntryLastName, null);
		panelEntryPersonInfo.add(comboEntryLastName, null);
		panelEntryPersonInfo.add(labEntryFirstName, null);
		panelEntryPersonInfo.add(comboEntryFirstName, null);
		panelObserverInfo.add(labObserverTitle, null);
		panelObserverInfo.add(labObserverLastName, null);
		panelObserverInfo.add(comboObserverLastName, null);
		panelObserverInfo.add(labObserverFirstName, null);
		panelObserverInfo.add(comboObserverFirstName, null);
		panelFrogInfo.add(labFrogTitle, null);
		panelFrogInfo.add(labFrog_ID, null);
		panelFrogInfo.add(labNextFrog_ID, null);
		// panelFrogInfo.add(labFRO, null);
		panelFrogInfo.add(lab2NextFrog_ID, null);
		panelFrogInfo.add(textFrog_ID, null);
		panelFrogInfo.add(labSpecies, null);
		panelFrogInfo.add(textSpecies, null);
		panelFrogInfo.add(labSex, null);
		panelFrogInfo.add(sexComboBox, null);
		panelFrogInfo.add(sexComboBox, null);
		panelFrogInfo.add(additDiscr, null);
		panelFrogInfo.add(additDiscrNo, null);
		panelFrogInfo.add(additDiscrYes, null);
		panelFrogInfo.add(labCapturedate, null);
		panelFrogInfo.add(captureDatePicker,null);
		//panelFrogInfo.add(dayComboBox, null);
		//panelFrogInfo.add(monthComboBox, null);
		//panelFrogInfo.add(yearComboBox, null);
		// panelFrogInfo.add(textCapturedate, null);
		panelFrogInfo.add(labMass, null);
		panelFrogInfo.add(textMass, null);
		panelFrogInfo.add(labLength, null);
		panelFrogInfo.add(textLength, null);
		// panelFrogInfo.add(labMassUnit, null);
		panelFrogInfo.add(labSurveyID, null);
		panelFrogInfo.add(textSurveyID, null);
		panelFrogInfo.add(labFrogComments, null);
		panelFrogInfo.add(textFrogComments, null);
		// panelFrogInfo.add(labLengthUnit, null);
		panelLocation.add(labCapLocationTitle, null);
		panelLocation.add(labLocationName, null);
		panelLocation.add(comboLocationName, null);
		panelLocation.add(labLocDesc, null);
		panelLocation.add(textLocDesc, null);
		panelLocation.add(labX, null);
		panelLocation.add(textX, null);
		panelLocation.add(labY, null);
		panelLocation.add(textY, null);
		// panelLocation.add(labCoorType, null);
		panelLocation.add(LatLongButton, null);
		panelLocation.add(UTMButton, null);
		panelLocation.add(labDatum, null);
		panelLocation.add(textDatum, null);
		panelLocation.add(labZone, null);
		panelLocation.add(textZone, null);
		panelButtons.add(butCancel, null);
		panelButtons.add(butClearAll, null);
		panelButtons.add(butNewEntry, null);
		panelAllInfo.add(labImage, null);
		panelAllInfo.add(panelEntryPersonInfo, null);
		panelAllInfo.add(panelObserverInfo, null);
		panelAllInfo.add(labFRO, null);
		panelAllInfo.add(panelFrogInfo, null);
		panelAllInfo.add(panelLocation, null);
		panelAllInfo.add(panelButtons, null);
	}

	/**
	 * Populates the add frog interface with old values from the last frog data.
	 */
	protected void imposeLastValues() {
		DefaultComboBoxModel<String> celnBoxModel = (DefaultComboBoxModel<String>) comboEntryLastName.getModel();
		if (celnBoxModel.getIndexOf(lastFrog.getRecorder().getLastName()) == -1) {
			//just set the data as it does not yet exist.
			comboEntryLastName.setSelectedItem(lastFrog.getRecorder().getLastName());
			IdentiFrog.LOGGER.writeMessage("CELN: NO MATCH ON OLD POPULATE.");
		} else {
			//does exists - use it instead of adding it
			IdentiFrog.LOGGER.writeMessage("");
			comboEntryLastName.setSelectedIndex(celnBoxModel.getIndexOf(lastFrog.getRecorder().getLastName()));
		}
		
		comboEntryFirstName.setSelectedItem(lastFrog.getRecorder().getFirstName());
		comboObserverLastName.setSelectedItem(lastFrog.getObserver().getLastName());
		comboObserverFirstName.setSelectedItem(lastFrog.getObserver().getFirstName());
		textFrog_ID.setText(lastFrog.getID());
		// textFrog_ID.setText(nextAvailFrogId);
		textSurveyID.setText(lastFrog.getSurveyID());
		textSpecies.setText(lastFrog.getSpecies());
		sexComboBox.setSelectedItem(lastFrog.getGender());
		String dateCapture = lastFrog.getDateCapture();
		IdentiFrog.LOGGER.writeMessage(dateCapture);
		IdentiFrog.LOGGER.writeMessage(dateCapture.indexOf('-'));
		IdentiFrog.LOGGER.writeMessage(dateCapture.lastIndexOf('-'));
		IdentiFrog.LOGGER.writeMessage((String) dateCapture.subSequence(dateCapture.indexOf('-') + 1, dateCapture.lastIndexOf('-')));
		int m = new Integer((String) dateCapture.subSequence(dateCapture.indexOf('-') + 1, dateCapture.lastIndexOf('-'))).intValue() - 1;
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
		}
	}

	protected void setLastValues() {
		Personel ob = new Personel("observer", (String) comboObserverFirstName.getSelectedItem(), (String) comboObserverLastName.getSelectedItem());
		Personel rc = new Personel("recorder", (String) comboEntryFirstName.getSelectedItem(), (String) comboEntryLastName.getSelectedItem());
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

		Location lc = new Location((String) comboLocationName.getSelectedItem(), textLocDesc.getText(), coordinateType, textX.getText(), textY.getText(), textDatum.getText(), textZone.getText());
		lastFrog = new Frog(textFrog_ID.getText(), "", textSurveyID.getText(), textSpecies.getText(), (String) sexComboBox.getSelectedItem(), textMass.getText(), textLength.getText(), dateCapture,
				"", ob, rc, "", textFrogComments.getText(), lc, "");
	}

	void butFillPreviousFrogInfo_actionPerformed(ActionEvent e) {
		imposeLastValues();
	}

	void entrylastnameComboBox_actionPerformed(ActionEvent e) {
		int entryind = comboEntryLastName.getSelectedIndex();
		// set to EntryFirstName
		IdentiFrog.LOGGER.writeMessage("lastname combo performed w/ index: "+entryind);
		if (entryind > 0) {
			--entryind;
			comboEntryFirstName.setSelectedItem(entryFirstNameCorrespondToList.get(entryind));
		}
	}

	void entryfirstnameComboBox_actionPerformed(ActionEvent e) {
		entryFirstName = (String) comboEntryFirstName.getSelectedItem();
	}

	private void obslastnameComboBox_actionPerformed(ActionEvent e) {
		int obsind = comboObserverLastName.getSelectedIndex();
		if (obsind > 0) {
			--obsind;
			// set to ObsFirstName
			comboObserverFirstName.setSelectedItem(obsFirstNameCorrespondToList.get(obsind));
		}
	}

	void obsfirstnameComboBox_actionPerformed(ActionEvent e) {
		obsFirstName = (String) comboObserverFirstName.getSelectedItem();
	}

	void locnameComboBox_actionPerformed(ActionEvent e) {
		int locind = comboLocationName.getSelectedIndex();
		// set to original coordinates
		if (locind > 0) { // in comboBox Location List index = 0 for a new entry
			--locind;
			textX.setText(Double.toString(locList.get(locind).loccoor.getX()));
			textY.setText(Double.toString(locList.get(locind).loccoor.getY()));
		} else {
			textX.setText("");
			textY.setText("");
		}
	}

	void sexComboBox_actionPerformed(ActionEvent e) {
		gender = (String) sexComboBox.getSelectedItem();
	}

	void dayComboBox_actionPerformed(ActionEvent e) {
	}

	void monthComboBox_actionPerformed(ActionEvent e) {
	}

	void yearComboBox_actionPerformed(ActionEvent e) {
	}
	
	private boolean validateField() {
	  boolean isError = true;
	  String errorMessage = "Error message at validate field";
	  // validate entry person's last name
	  if (!entryDatePicker.getModel().isSelected()) {
		entryDatePicker.requestFocus(true);
	    errorMessage = "Entry date cannot be empty";
	  } else if (isEmptyString((String) comboEntryLastName.getSelectedItem())) {
	    comboEntryLastName.requestFocus(true);
	    errorMessage = "Entry person's last name cannot be empty";
	  } else if  (isEmptyString((String) comboEntryFirstName.getSelectedItem())) {
      comboEntryFirstName.requestFocus(true);
      errorMessage = "Entry person's first name cannot be empty";
    } else if (isEmptyString((String) comboObserverLastName.getSelectedItem())) {
      comboObserverLastName.requestFocus(true);
      errorMessage = "Observer's last name cannot be empty";
    } else if  (isEmptyString((String) comboObserverFirstName.getSelectedItem())) {
      comboObserverFirstName.requestFocus(true);
      errorMessage = "Observer's first name cannot be empty";
    } else if (isEmptyString(textFrog_ID.getText())) {
      textFrog_ID.requestFocus(true);
      errorMessage = "Frog ID cannot be empty";
    } else if (isEmptyString(textSpecies.getText())) {
      textSpecies.requestFocus(true);
      errorMessage = "Frog species cannot be empty";
    } else if (isEmptyString((String) sexComboBox.getSelectedItem())) {
      sexComboBox.requestFocus(true);
      errorMessage = "Select frog's gender";
    } else if (!entryDatePicker.getModel().isSelected()) {
		entryDatePicker.requestFocus(true);
	    errorMessage = "Entry date cannot be empty";
    /*} else if (isEmptyString((String) dayComboBox.getSelectedItem())) {
      dayComboBox.requestFocus(true);
      errorMessage = "Capture Day cannot be empty";
    } else if (isEmptyString((String) monthComboBox.getSelectedItem())) {
      monthComboBox.requestFocus(true);
      errorMessage = "Capture Month cannot be empty";
    } else if (isEmptyString((String) yearComboBox.getSelectedItem())) {
      yearComboBox.requestFocus(true);
      errorMessage = "Capture Year cannot be empty";*/
    } else if (isEmptyString(textMass.getText())) {
      textMass.requestFocus(true);
      errorMessage = "Frog's mass cannot be empty";
    } else if (isEmptyString(textSurveyID.getText())) {
      textSurveyID.requestFocus(true);
      errorMessage = "Survey ID cannot be empty";
    } else if (isEmptyString((String) comboLocationName.getSelectedItem())) {
      comboLocationName.requestFocus(true);
      errorMessage = "Survey location name cannot be empty";
    } 
    /*
    else if (isEmptyString(locCoorType)) {
      LatLongButton.requestFocus(true);
      errorMessage = "Select between Lat/Long and UTM";
    } else if (isEmptyString(textX.getText())) {
      textX.requestFocus(true);
      errorMessage = "Longitude information cannot be empty";
    } else if (isEmptyString(textY.getText())) {
      textY.requestFocus(true);
      errorMessage = "Latitude information cannot be empty";
    } else if (isEmptyString(textDatum.getText())) {
      textDatum.requestFocus(true);
      errorMessage = "Datum information cannot be empty";
    } else if (!isEmptyString(locCoorType) && locCoorType.equals("UTM") && isEmptyString(textZone.getText())) {
      textZone.requestFocus(true);
      errorMessage = "Zone cannot be empty when UTM is selected";
    } 
    */
    else {
      isError = false;
      try {
        mass = Double.parseDouble(textMass.getText());
      } catch (NumberFormatException nfe) {
        errorMessage = "Frog's mass must be a number";
        isError = true;
      }
      try {
        length = Double.parseDouble(textLength.getText());
      } catch (NumberFormatException nfe) {
        errorMessage = "Frog's length must be a number";
        isError = true;
      }
      /*
      try {
        x = Double.parseDouble(textX.getText());
      } catch (NumberFormatException nfe) {
        errorMessage = "Longitude must be a number";
        isError = true;
      }
      try {
        y = Double.parseDouble(textY.getText());
      } catch (NumberFormatException nfe) {
        errorMessage = "Latitude must be a number";
        isError = true;
      }
      if (!isEmptyString(locCoorType) && locCoorType.equals("UTM") && !isEmptyString(textZone.getText())) {
        try {
          zone = Integer.parseInt(textZone.getText());
        } catch (NumberFormatException nfe) {
          errorMessage = "Zone be a number";
          isError = true;
        }
      }
      */
	  }
	  if (isError) {
	    JOptionPane.showMessageDialog(this, errorMessage, "Data Validation Error", JOptionPane.ERROR_MESSAGE);
	    return false;
	  } else {
	    return true;
	  }
	}
	
	private boolean isEmptyString(String input) {
	  if (input!= null && input.trim().length()!=0) {
	    return false;
	  } else {
	    return true;
	  }
	}

	// ***************** NEW ENTRY *************************** //
	protected void butNewEntry_actionPerformed(ActionEvent e) {

		// ******************** VALIDATE FIELD ******************** //
		if (validateField()) {
			Date eDate = (Date) entryDatePicker.getModel().getValue();
			entrydate = df.format(eDate);
			entryLastName = (String) comboEntryLastName.getSelectedItem();
			entryFirstName = (String) comboEntryFirstName.getSelectedItem();
			obsLastName = (String) comboObserverLastName.getSelectedItem();
			obsFirstName = (String) comboObserverFirstName.getSelectedItem();
			frogID = textFrog_ID.getText().trim();
			species = textSpecies.getText().trim();
			gender = (String) sexComboBox.getSelectedItem();
			// Additional Discriminator
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
			// longitude
			// latitude
			// datum
			// zone
			pathImage = image.getAbsolutePath();
			if (LatLongButton.isSelected()) {
				locCoorType = "Lat/Long";
			} else if (UTMButton.isSelected()) {
				locCoorType = "UTM";
			}
			formerID = Integer.toString(parentFrame.getFrogData().getNextAvailableID());
			if (datum == null) {
				datum = "";
			}
			Personel ob = new Personel("observer", obsFirstName, obsLastName);
			Personel rc = new Personel("recorder", entryFirstName,
					entryLastName);
			Location lc = new Location(locationName, locationDescription,
					locCoorType, textX.getText(), textY.getText(), datum,
					Integer.toString(zone));
			Frog f = new Frog(frogID, formerID, textSurveyID.getText(),
					species, gender, textMass.getText(), textLength.getText(),
					capturedate, entrydate, ob, rc, addDiscriminator, comments,
					lc);
			if (frog != null) {
				this.frog = f;
				//it's in edit mode
			} else {
				//it's in add mode
				
				
				frogData.getFrogs().add(f);
				parentFrame.setFrogData(frogData);
				setLastValues();
	
				node.put("lastEntryLastName", f.getRecorder().getLastName());
				node.put("lastFormerID", f.getFormerID());
				node.put("lastEntryFirstName", f.getRecorder().getFirstName());
				node.put("lastObserverLastName", f.getObserver().getLastName());
				node.put("lastObserverFirstName", f.getObserver().getFirstName());
				node.put("lastFrog_ID", f.getID());
				node.put("lastSurveyID", f.getSurveyID());
				node.put("lastSpecies", f.getSpecies());
				node.put("lastSex", f.getGender());
				String dateCapture = f.getDateCapture();
				node.put(
						"lastCapday",
						(String) dateCapture.subSequence(
								dateCapture.lastIndexOf('-'), dateCapture.length()));
				node.put("lastCapmonth", (String) dateCapture.subSequence(
						dateCapture.indexOf('-'), dateCapture.lastIndexOf('-')));
				node.put(
						"lastCapyear",
						(String) dateCapture.subSequence(0,
								dateCapture.indexOf('-')));
				node.put("lastMass", f.getMass());
				node.put("lastLength", f.getLength());
				node.put("lastFrogComments", f.getComments());
				node.put("lastLocationName", f.getLocation().getName());
				if (f.getLocation().getDescription() != null) {
					node.put("lastLocationDesc", f.getLocation().getDescription());
				} else {
					node.put("lastLocationDesc", "");
				}
				node.put("lastDiscriminator", f.getDiscriminator());
				if (f.getLocation().getCoordinateType() != null) {
					node.put("lastX", f.getLocation().getLongitude());
					node.put("lastY", f.getLocation().getLatitude());
					node.put("lastDatum", f.getLocation().getDatum());
					if (f.getLocation().getCoordinateType().equals("Lat/Long")) {
						node.put("lastLatLong", "true");
						node.put("lastUTM", "false");
						node.put("lastZone", "");
					} else if (f.getLocation().getCoordinateType().equals("UTM")) {
						node.put("lastLatLong", "false");
						node.put("lastUTM", "true");
						node.put("lastZone", f.getLocation().getZone());
					}
				} else {
					node.put("lastX", "");
					node.put("lastY", "");
					node.put("lastDatum", "");
					node.put("lastLatLong", "false");
					node.put("lastUTM", "false");
					node.put("lastZone", "");
				}
			}
			// close dialog
			dispose();
			if (isNewImage) {
				openDigSigFrame(maxfrogdbid);
			}
			//edit mode will continue execution once this frame is dumped from the stack
		}
	} // end of ActionPerformed for new entry button

	void butCancel_actionPerformed(ActionEvent e) {
		File tnImageFile = new File(thumbnailFilename);
		tnImageFile.delete();
		dispose();
	}

	public JLabel getButImage() {
		return labImage;
	}

	public void setButImage(File imageFile) {
		thumbnailFilename = imageFile.getAbsolutePath();
		labImage.setIcon(new ImageIcon(thumbnailFilename));
		//butImage.setM
	}

	private void openDigSigFrame(int frogdbid) {
		// Garbage Collector
		System.gc();
		iFrame = new ImageManipFrame(parentFrame, image, frogdbid,fh);
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
		IdentiFrog.LOGGER.writeMessage("I was here");
	}

	void butClearAll_actionPerformed(ActionEvent e) {
		if (ChoiceDialog.choiceMessage("This will clear all entered information on this screen.\n" + "Do you want to continue?") == 0) {
			comboEntryLastName.setSelectedIndex(0);
			comboEntryFirstName.setSelectedIndex(0);
			comboObserverLastName.setSelectedIndex(0);
			comboObserverFirstName.setSelectedIndex(0);
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
			ButAdditDiscrGroup.clearSelection();
			textDatum.setText("");
			textZone.setText("");
		}
	}
	
	private void butDebugAutopopulate_actionPerformed(ActionEvent e) {
		String[] firstnames = {
				"Haley",
				"Jim",
				"Deonna", 
				"Sam",
				"Elaine",
				"Aiko",
				"Ashlee",
				"Chiquita",
				"Chrystal",
				"Tinisha",
				"Mabel",
				"Ronni",
				"Clinton",
				"Monica",
				"Earleen",
				"Margret",
				"Jackson",
				"Pamella",  
				"Clifton",  
				"Kory"  
		};
		comboEntryLastName.addItem("Lastname");
		comboEntryLastName.getEditor().setItem("Lastname");
		Random rand = new Random();
	    int randomNum = rand.nextInt(firstnames.length);
		comboEntryFirstName.getEditor().setItem(firstnames[randomNum]);
		comboObserverLastName.getEditor().setItem("Perez");
		comboObserverFirstName.getEditor().setItem("Mike");
		textFrog_ID.setText(Integer.toString(frogData.getNextAvailableID()));
		textSpecies.setText("Jumpy");
		sexComboBox.setSelectedItem("M");
		//dayComboBox.setSelectedItem("16");
		//monthComboBox.setSelectedItem("Mar");
		//yearComboBox.setSelectedItem("2015");
		textMass.setText("28");
		textLength.setText("64");
		textFrogComments.setText("Really hoppy this one was");
		textSurveyID.setText("");
		comboLocationName.getEditor().setItem("Area 51");
		textLocDesc.setText("Hoppy Pond");
		textX.setText("115.8111");
		textY.setText("37.2350");
		Butgroup.setSelected(LatLongButton.getModel(), true); // set radio LatLongButton, UTMButton to false
		ButAdditDiscrGroup.clearSelection();
		textDatum.setText("");
		textZone.setText("");
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
	 * Returns frog data this frame is holding. In normal cases this is the edited frog from the one passed in when this frame as originally displayed in edit mode.
	 * @return Frog object this frame is describing
	 */
	public Frog getFrog() {
		return frog;
	}
}