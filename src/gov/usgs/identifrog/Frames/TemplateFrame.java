package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.ImageManipFrame;
import gov.usgs.identifrog.SharedUI;
import gov.usgs.identifrog.DataObjects.DateLabelFormatter;
import gov.usgs.identifrog.DataObjects.Location;
import gov.usgs.identifrog.DataObjects.Template;
import gov.usgs.identifrog.DataObjects.User;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.cellrenderers.TemplateListCellRenderer;
import gov.usgs.identifrog.cellrenderers.UserListCellRenderer;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.LayerUI;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

/**
 * <p>
 * Title: FrogEditor.java
 * <p>
 * Description: displays window 'Frog Information' for the user to fill out the
 * fields, adds filled entries to table Frog, Observer, EntryPerson,
 * CaptureLocation
 * 
 * @author Michael J. Perez 2015 (significant rewrite)
 * @author Hidayatullah Ahsan 2012
 * @author Oksana V. Kelly 2008
 * @author Oksana V. Kelly modified the code written by Mike Ramshaw from
 *         IdentiFrog 2005
 */
@SuppressWarnings("serial")
public class TemplateFrame extends JDialog implements ListSelectionListener {
	private boolean shouldSave = false;
	private DefaultListModel<Template> templateModel = new DefaultListModel<>();
	private Preferences root = Preferences.userRoot();
	//private Preferences node = root.node("edu/isu/aadis/defaults");

	private Font level1TitleFont = new Font("MS Sans Serif", Font.BOLD, 14);
	private Font level2TitleFont = new Font("MS Sans Serif", Font.BOLD, 12);
	private ImageIcon imageNew16 = new ImageIcon(MainFrame.class.getResource("/resources/IconNew16.png"));
	private ImageIcon imageImage16 = new ImageIcon(MainFrame.class.getResource("/resources/IconImage16.png"));
	private ImageIcon imageDiscriminators16 = new ImageIcon(MainFrame.class.getResource("/resources/IconDiscriminator16.png"));

	String surveyID;
	//protected int frogID;
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
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private int activeSample = 0;

	public TemplateFrame(JFrame frame) {
		IdentiFrog.LOGGER.writeMessage("Opening template manager");
		try {
			init2();
			setLocationRelativeTo(frame);
			setVisible(true);
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("Exception while starting template editor", e);
		}
	}

	public TemplateFrame(JDialog dialog) {
		IdentiFrog.LOGGER.writeMessage("Opening template manager");
		try {
			init2();
			setLocationRelativeTo(dialog);
			setVisible(true);
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("Exception while starting template editor", e);
		}
	}

	private ActionListener radButtonAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (((JRadioButton) e.getSource()).getText() == "Lat/Long") {
				//locCoorType = "Lat/Long";
				textZone.setVisible(false);
				labZone.setVisible(false);
				labX.setText("Latitude");
				labY.setText("Longitude");
			}
			if (((JRadioButton) e.getSource()).getText() == "UTM") {
				//locCoorType = "UTM";
				textZone.setVisible(true);
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
	JButton butNewTemplate = new JButton();
	JButton butEditDiscriminators = new JButton();
	JButton butEditTemplates = new JButton();
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
	JTextField textNextFrog_ID = new JTextField();
	JLabel labSpecies = new JLabel("Species");
	JTextField textDatum = new JTextField();
	JTextField textSurveyID = new JTextField();
	JTextField textTemplateName = new JTextField();
	JLabel labSurveyID = new JLabel("Survey ID");
	JLabel labTemplateName = new JLabel("Template Name");

	JLabel labCapturedate = new JLabel();
	JLabel labEntrydate = new JLabel();
	String[] sexStrings = { "M", "F", "J", "Unknown" };
	JDatePickerImpl captureDatePicker;
	//JCheckBox checkAdditionalDescriptor = new JCheckBox("Additional Discriminator");
	JButton butDiscriminators = new JButton("Discriminators", imageDiscriminators16);
	DecimalFormat integerFormat = new DecimalFormat("#");
	JFormattedTextField textMass = new JFormattedTextField(IdentiFrog.decimalFormat);
	JLabel labFrogComments = new JLabel();
	JTextField textComments = new JTextField();
	JLabel labCoorType = new JLabel();
	JLabel labX = new JLabel();
	JTextField textX = new JTextField();
	JLabel labY = new JLabel();
	JTextField textY = new JTextField();
	JButton butSave = new JButton();
	JRadioButton LatLongButton = new JRadioButton("Lat/Long", false);
	JRadioButton UTMButton = new JRadioButton("UTM", false);
	ButtonGroup Butgroup = new ButtonGroup();
	JFormattedTextField textZone = new JFormattedTextField(integerFormat);
	JDatePickerImpl entryDatePicker;
	JLabel labZone = new JLabel();
	JTextField textLocDesc = new JTextField();

	JLabel labDatum = new JLabel();
	JLabel labLocationName = new JLabel();
	JLabel labMass = new JLabel();
	JLabel labLocDesc = new JLabel();
	JLabel labCoordinates = new JLabel();
	JFormattedTextField textLength = new JFormattedTextField(IdentiFrog.decimalFormat);
	JLabel labLength = new JLabel();
	JButton butClose = new JButton();
	JLabel labMassUnit = new JLabel();
	JLabel labLengthUnit = new JLabel();
	JButton butClearAll = new JButton();
	ArrayList<LocInfo> locList = new ArrayList<LocInfo>();
	DefaultComboBoxModel<User> recorderListModel, observerListModel;

	JComboBox<User> comboObserver;
	JComboBox<User> comboRecorder;
	JButton usersButton;
	JComboBox<Location> comboLocationName = new JComboBox<Location>();

	private DisabledGlassPane templateEditorDisabler;
	private JList<Template> templateList;

	private void init2() throws Exception {
		setModal(true);
		setIconImages(IdentiFrog.ICONS);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Template Manager");

		//Data
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

		panelAllInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panelAllInfo.setLayout(new BoxLayout(panelAllInfo, BoxLayout.PAGE_AXIS));

		//Images panel
		JPanel panelTopButtons = new JPanel();
		//site survey panel
		JPanel panelSiteSurvey = new JPanel();
		panelSiteSurvey.setLayout(new GridBagLayout());
		//panelSiteSurvey.setAlignmentX(Component.CENTER_ALIGNMENT);
		TitledBorder siteSurveyBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Template Data");
		siteSurveyBorder.setTitleFont(level1TitleFont);
		panelSiteSurvey.setBorder(siteSurveyBorder);

		JPanel panelDataEntry = new JPanel(new GridBagLayout());
		TitledBorder entryPersonBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Data Entry");
		entryPersonBorder.setTitleFont(level2TitleFont);
		panelDataEntry.setBorder(entryPersonBorder);
		//recPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

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
		butNewTemplate.setText("New Template");
		butNewTemplate.setVisible(true);
		butNewTemplate.setIcon(new ImageIcon(MainFrame.class.getResource("/resources/IconBookmark32.png")));
		butNewTemplate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//butFillPreviousFrogInfo_actionPerformed(e);
				for (int i = 0; i < templateModel.getSize(); i++) {
					Template t = templateModel.getElementAt(i);
					if (t.getName().equalsIgnoreCase("New Template")) {
						JOptionPane
								.showMessageDialog(
										TemplateFrame.this,
										"Templates must be uniquely named.\nChange your template named \"New Survey\" to something else before adding a new one.",
										"Duplicate template not allowed", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				templateEditorDisabler.setOverlayEnabled(false);
				Template template = new Template();
				template.setName("New Template");
				templateModel.addElement(template);
				templateList.setSelectedIndex(templateModel.getSize() - 1);
				assert templateList.getSelectedIndex() == templateModel.getSize() - 1;
				repaint();
			}
		});

		//Manage Users
		ImageIcon imageUser = new ImageIcon(TemplateFrame.class.getResource("/resources/IconUsers32.png"));
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

		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.2;
		c.weighty = 0;

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
		c.weightx = 0;
		c.gridy = 3;
		//panelDataEntry.add(entryDatePicker,c);
		//c.gridx = 1;
		panelDataEntry.add(comboRecorder, c);
		c.insets = leftSpaceInsets;
		c.gridx = 1;
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
		textSurveyID.setColumns(5);

		labFrogComments.setText("Comments");
		textComments.setColumns(210);

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

		//row2
		c.insets = noInsets;
		c.gridx = 0;
		c.gridy = 1;
		panelBiometrics.add(textMass, c);
		c.insets = leftSpaceInsets;
		c.gridx = 1;
		panelBiometrics.add(textLength, c);

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
		panelBiometrics.add(textComments, c);

		// PANEL LOCATION INFO
		labLocationName.setText("Location Name");
		//comboLocationName.setSelectedIndex(0);
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

		Insets topInsets = new Insets(3, 5, 0, 5);
		Insets bottomInsets = new Insets(0, 5, 3, 5);
		
		JPanel surveyNamePanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = topInsets;
		surveyNamePanel.add(labTemplateName, c);
		c.gridx++;
		surveyNamePanel.add(labSurveyID, c);
		
		c.gridy++;
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = bottomInsets;
		surveyNamePanel.add(textTemplateName, c);
		c.gridx++;
		surveyNamePanel.add(textSurveyID, c);
		
		
		//LatLongButton.setBounds(new Rectangle(288, 100, 80, 20));
		//UTMButton.setBounds(new Rectangle(375, 100, 80, 20));
		Butgroup.add(LatLongButton);
		Butgroup.add(UTMButton);
		LatLongButton.addActionListener(radButtonAction);
		UTMButton.addActionListener(radButtonAction);
		labDatum.setText("Datum");
		labZone.setText("Zone");
		textZone.setColumns(200);
		butSave.setIcon(new ImageIcon(MainFrame.class.getResource("/resources/IconSave32.png")));
		butSave.setText("Save Template");
		butSave.setToolTipText("Saves this template to the database.");
		butSave.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (commitChanges()) {
					IdentiFrog.LOGGER.writeMessage("Saving active template");
					Template templateData = new Template();
				}
			}
		});
		//labImage.setBounds(new Rectangle(8, 6, 165, 133));
		butClose.setVerifyInputWhenFocusTarget(true);
		butClose.setIcon(new ImageIcon(MainFrame.class.getResource("/resources/IconCancel32.png")));
		butClose.setText("Close");
		butClose.setToolTipText("Closes template manager");
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
		panelTopButtons.add(butNewTemplate);
		panelTopButtons.add(Box.createRigidArea(new Dimension(10, 10)));
		panelTopButtons.add(usersButton);
		//panelTopButtons.add(Box.createRigidArea(new Dimension(10, 10)));
		//panelTopButtons.add(butEditDiscriminators);
		if (IdentiFrog.DEBUGGING_BUILD) {
			panelTopButtons.add(Box.createRigidArea(new Dimension(10, 10)));
			panelTopButtons.add(butDebugPopulate);
		}
		panelTopButtons.add(Box.createHorizontalGlue());
		c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		panelSiteSurvey.add(surveyNamePanel, c);
		c.gridy++;
		panelSiteSurvey.add(panelDataEntry, c);
		c.gridy++;
		panelSiteSurvey.add(panelLocation, c);
		c.gridy++;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		panelSiteSurvey.add(panelBiometrics, c);

		//PANEL LOCATION INFO
		c = new GridBagConstraints();
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
		c.insets = noInsets;
		c.gridy = 2;
		c.gridx = 0;
		panelLocation.add(LatLongButton, c);
		c.gridx = 1;
		panelLocation.add(UTMButton, c);

		//row 4
		c.insets = topInsets;
		c.gridx = 0;
		c.gridy = 3;
		panelLocation.add(labX, c);
		c.gridx = 1;
		panelLocation.add(labY, c);

		//row 5
		c.insets = bottomInsets;
		c.gridy = 4;
		c.gridx = 0;
		panelLocation.add(textX, c);
		c.gridx = 1;
		panelLocation.add(textY, c);

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
		panelBottomButtons.add(butClose);
		panelBottomButtons.add(Box.createRigidArea(new Dimension(10, 10)));
		panelBottomButtons.add(butClearAll);
		panelBottomButtons.add(Box.createRigidArea(new Dimension(10, 10)));

		panelBottomButtons.add(butSave);
		panelBottomButtons.add(Box.createHorizontalGlue());
		//panelAllInfo.add(labImage);
		//panelAllInfo.add(panelTopButtons);
		//panelAllInfo.add(panelFrogInfo);
		panelAllInfo.add(panelSiteSurvey);
		//panelAllInfo.add(labFRO);

		panelAllInfo.add(Box.createVerticalGlue());
		JPanel blankInfoPanel = new JPanel();
		blankInfoPanel.setLayout(new BoxLayout(blankInfoPanel, BoxLayout.LINE_AXIS));
		blankInfoPanel.add(Box.createHorizontalGlue());
		blankInfoPanel.add(new JLabel("Blank fields will not overwrite existing values when applying the template."));
		blankInfoPanel.add(Box.createHorizontalGlue());

		panelAllInfo.add(blankInfoPanel);
		panelAllInfo.add(panelBottomButtons);

		templateEditorDisabler = new DisabledGlassPane();
		JLayer<JComponent> jlayer = new JLayer<JComponent>(panelAllInfo, templateEditorDisabler);

		//TEMPLATE LIST
		templateList = new JList<Template>();
		templateList.setModel(templateModel);
		templateList.setCellRenderer(new TemplateListCellRenderer());
		JPanel templateSelectionPanel = new JPanel(new BorderLayout());
		templateSelectionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Templates"));
		templateSelectionPanel.add(new JScrollPane(templateList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, templateSelectionPanel, jlayer);
		sp.setDividerLocation(120);
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(sp, BorderLayout.CENTER);
		contentPanel.add(panelTopButtons, BorderLayout.NORTH);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		//getContentPane().add(panelAllInfo, BorderLayout.CENTER);
		add(contentPanel);
		setMinimumSize(new Dimension(660, 640));
		setPreferredSize(new Dimension(660, 640));
		pack();

		loadTemplates();
	}

	private void loadTemplates() {
		templateModel.clear();
		ArrayList<Template> templates = XMLFrogDatabase.getTemplates();
		IdentiFrog.LOGGER.writeMessage("Loading templates for template manager frame. DB has " + templates.size() + " templates in it");
		for (Template template : templates) {
			templateModel.addElement(template);
		}
	}

	/**
	 * Saves all data in the window to the frog assigned to this frogeditor
	 * window (copy of the original that was passed in if edit mode) and assigns
	 * it to the this.frog object (through a sitesample)
	 * 
	 * @return
	 */
	private boolean commitChanges() {
		if (validateData()) {
			try {
				Date eDate = addMonthToDate((Date) entryDatePicker.getModel().getValue());
				entrydate = IdentiFrog.dateFormat.format(eDate);
				Date d = addMonthToDate((Date) captureDatePicker.getModel().getValue());
				String capturedate = IdentiFrog.dateFormat.format(d);
				// mass
				// length
				String locationName = (String) comboLocationName.getSelectedItem().toString();
				String locationDescription = textLocDesc.getText().trim();

				// longitude
				// latitude
				// datum
				// zone
				String locCoorType;
				if (LatLongButton.isSelected()) {
					locCoorType = "Lat/Long";
				} else if (UTMButton.isSelected()) {
					locCoorType = "UTM";
				} else {
					locCoorType = "";
				}
				int zone = Location.EMPTY_ZONE;
				try {
					zone = Integer.parseInt(textZone.getText().trim());
				} catch (NumberFormatException e) {
					//not a zone
				}
				String datum = textDatum.getText();
				Location lc = new Location(locationName, locationDescription, locCoorType, textY.getText().trim(), textX.getText().trim(), datum,
						zone);

				//Generate Template
				/*
				 * SiteSample sample = new SiteSample();
				 * sample.setSurveyID(textSurveyID.getText().trim());
				 * sample.setMass(textMass.getText().trim());
				 * sample.setLength(textLength.getText().trim());
				 * sample.setDateCapture(capturedate);
				 * sample.setDateEntry(entrydate); User selectedObs = (User)
				 * comboObserver.getSelectedItem(); User selectedRec = (User)
				 * comboRecorder.getSelectedItem();
				 * sample.setRecorder(XMLFrogDatabase
				 * .getRecorderByID(selectedRec.getID())); //we use IDs instead
				 * of assigning values because
				 * sample.setObserver(XMLFrogDatabase
				 * .getObserverByID(selectedObs.getID())); //these may be out of
				 * sync with the XML database (say a user fools with the file in
				 * the background)
				 * sample.setComments(textComments.getText().trim());
				 * sample.setLocation(lc);
				 */

				//update copyfrog's sample

				IdentiFrog.LOGGER.writeMessage("Template Changes were commited");
				return true;
			} catch (Exception e) {
				IdentiFrog.LOGGER.writeExceptionWithMessage("Failed to commit changes, exception occured.", e);
				return false;
			}
		} else {
			return false;
		}
	}

	private Date addMonthToDate(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MONTH, 1); // number of days to add
		return c.getTime();
	}

	/**
	 * Loads a sitesample into the interface overriding any existing values
	 * 
	 * @param sample
	 *
	 *            protected void loadSiteSample(int index) { activeSample =
	 *            index;
	 * 
	 *            try { Date entryDate; entryDate =
	 *            IdentiFrog.dateFormat.parse(sample.getDateEntry());
	 *            //ridiculous... thanks oracle Calendar cal =
	 *            Calendar.getInstance(); cal.setTime(entryDate); int year =
	 *            cal.get(Calendar.YEAR); int month = cal.get(Calendar.MONTH);
	 *            int day = cal.get(Calendar.DAY_OF_MONTH);
	 *            entryDatePicker.getModel().setDate(year, month - 1, day); //-1
	 *            cause it's 0 indexed
	 *            entryDatePicker.getModel().setSelected(true); } } catch
	 *            (ParseException e) {
	 *            IdentiFrog.LOGGER.writeExceptionWithMessage
	 *            ("Failed to parse entry date when loading sitesample!", e); }
	 * 
	 *            try { Date captureDate; captureDate =
	 *            IdentiFrog.dateFormat.parse(sample.getDateCapture());
	 *            //ridiculous... thanks oracle Calendar cal =
	 *            Calendar.getInstance(); cal.setTime(captureDate); int year =
	 *            cal.get(Calendar.YEAR); int month = cal.get(Calendar.MONTH);
	 *            int day = cal.get(Calendar.DAY_OF_MONTH);
	 *            captureDatePicker.getModel().setDate(year, month - 1, day);
	 *            //-1 cause it's 0 indexed
	 *            captureDatePicker.getModel().setSelected(true); } } catch
	 *            (ParseException e) {
	 *            IdentiFrog.LOGGER.writeExceptionWithMessage
	 *            ("Failed to parse capture date when loading sitesample!", e);
	 *            }
	 * 
	 *            textSurveyID.setText(sample.getSurveyID());
	 *            comboRecorder.setSelectedItem(sample.getRecorder());
	 *            comboObserver.setSelectedItem(sample.getObserver());
	 * 
	 *            if (sample.getLocation() != null &&
	 *            sample.getLocation().getCoordinateType() != null) { if
	 *            (sample.getLocation().getCoordinateType().equals("UTM")) {
	 *            textX.setText(sample.getLocation().getLatitude());
	 *            textY.setText(sample.getLocation().getLongitude());
	 *            UTMButton.setSelected(true);
	 *            textZone.setText(Integer.toString(
	 *            sample.getLocation().getZone())); labZone.setVisible(true);
	 *            textZone.setVisible(true); } else {
	 *            textY.setText(sample.getLocation().getLongitude());
	 *            textX.setText(sample.getLocation().getLatitude());
	 *            LatLongButton.setSelected(true); labZone.setVisible(false);
	 *            textZone.setVisible(false); }
	 *            textLocDesc.setText(sample.getLocation().getDescription());
	 *            textDatum.setText(sample.getLocation().getDatum()); }
	 * 
	 *            for (Location l : XMLFrogDatabase.getAllLocations()) {
	 *            comboLocationName.addItem(l); }
	 * 
	 *            textMass.setText(sample.getMass());
	 *            textLength.setText(sample.getLength());
	 *            textComments.setText(sample.getComments()); } if
	 *            (!LatLongButton.isSelected() && !UTMButton.isSelected()) {
	 *            LatLongButton.setSelected(true); labZone.setVisible(false);
	 *            textZone.setVisible(false);
	 * 
	 *            } }
	 */

	/**
	 * Opens the Discriminators Management window and stalls execution of this
	 * thread until completed
	 */
	protected void openDiscriminatorsWindow() {
		DiscriminatorFrame dFrame = new DiscriminatorFrame(this);
		dFrame.setVisible(true);
	}

	/**
	 * Opens the User management window, and stalls execution of this thread
	 * until it completes. Once complete it reloads the user lists.
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
		if (observerListModel.getIndexOf(obsUser) >= 0) {
			comboObserver.setSelectedIndex(observerListModel.getIndexOf(obsUser));
		}
		if (recorderListModel.getIndexOf(recUser) >= 0) {
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
		 * 
		 * textFrog_ID.setText(Integer.toString(lastFrog.getID())); //
		 * textFrog_ID.setText(nextAvailFrogId);
		 * textSurveyID.setText(lastFrog.getSurveyID());
		 * textSpecies.setText(lastFrog.getSpecies());
		 * sexComboBox.setSelectedItem(lastFrog.getGender()); String dateCapture
		 * = lastFrog.getDateCapture();
		 * IdentiFrog.LOGGER.writeMessage(dateCapture);
		 * IdentiFrog.LOGGER.writeMessage(dateCapture.indexOf('-'));
		 * IdentiFrog.LOGGER.writeMessage(dateCapture.lastIndexOf('-'));
		 * IdentiFrog.LOGGER.writeMessage((String)
		 * dateCapture.subSequence(dateCapture.indexOf('-') + 1,
		 * dateCapture.lastIndexOf('-'))); //int m = new Integer((String)
		 * dateCapture.subSequence(dateCapture.indexOf('-') + 1,
		 * dateCapture.lastIndexOf('-'))).intValue() - 1;
		 * //dayComboBox.setSelectedItem
		 * (dateCapture.subSequence(dateCapture.lastIndexOf('-') + 1,
		 * dateCapture.length())); //monthComboBox.setSelectedItem(month[m]);
		 * //yearComboBox.setSelectedItem(dateCapture.subSequence(0,
		 * dateCapture.indexOf('-')));
		 * 
		 * textMass.setText(lastFrog.getMass());
		 * textLength.setText(lastFrog.getLength());
		 * textFrogComments.setText(lastFrog.getComments());
		 * comboLocationName.setSelectedItem(lastFrog.getLocation().getName());
		 * textLocDesc.setText(lastFrog.getLocation().getDescription());
		 * textX.setText(lastFrog.getLocation().getLongitude());
		 * textY.setText(lastFrog.getLocation().getLatitude());
		 * textDatum.setText(lastFrog.getLocation().getDatum()); if
		 * (lastFrog.getDiscriminator().equals("Yes")) {
		 * additDiscrNo.setEnabled(false); additDiscrYes.setEnabled(true); }
		 * else { additDiscrNo.setEnabled(true);
		 * additDiscrYes.setEnabled(false); }
		 */
	}

	protected void setLastValues() {
		IdentiFrog.LOGGER.writeError("setLastValues() is no longer implemented.");

		//Personel ob = new Personel("observer", (String) comboObserverFirstName.getSelectedItem(), (String) comboObserverLastName.getSelectedItem());
		//Personel rc = new Personel("recorder", (String) comboEntryFirstName.getSelectedItem(), (String) comboEntryLastName.getSelectedItem());
		/*
		 * String obsvr = "Observer String"; String rcrdr = "Recorder String";
		 * String coordinateType; if (LatLongButton.isSelected()) {
		 * coordinateType = "Lat/Long"; } else { coordinateType = "UTM"; }
		 * coordinateType = null; Date dateCaptureObj = (Date)
		 * captureDatePicker.getModel().getValue(); DateFormat df = new
		 * SimpleDateFormat("MM-dd-yyyy"); String dateCapture =
		 * df.format(dateCaptureObj); //String dateCapture =
		 * DateFormat.dateCaptureObj // String dateCapture = (String)
		 * yearComboBox.getSelectedItem() + "-" + (String)
		 * monthComboBox.getSelectedItem() + "-" + (String)
		 * dayComboBox.getSelectedItem();
		 * 
		 * Location lc = new Location((String)
		 * comboLocationName.getSelectedItem(), textLocDesc.getText(),
		 * coordinateType, textX.getText(), textY.getText(),
		 * textDatum.getText(), textZone.getText()); lastFrog = new Frog(frogID,
		 * textSurveyID.getText(), textSpecies.getText(), (String)
		 * sexComboBox.getSelectedItem(), textMass.getText(),
		 * textLength.getText(), dateCapture, "", obsvr, rcrdr, "",
		 * textFrogComments.getText(), lc);
		 * 
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
		} /*
		 * else { textX.setText(""); textY.setText(""); }
		 */
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
		} else if (!captureDatePicker.getModel().isSelected()) {
			captureDatePicker.requestFocus(true);
			errorMessage = "Capture date cannot be empty";
		} else if (!entryDatePicker.getModel().isSelected()) {
			entryDatePicker.requestFocus(true);
			errorMessage = "Entry date cannot be empty";
		} else if (isEmptyString(textMass.getText())) {
			textMass.requestFocus(true);
			errorMessage = "Frog's mass cannot be empty";
		} else if (isEmptyString(textSurveyID.getText())) {
			textSurveyID.requestFocus(true);
			errorMessage = "Survey ID cannot be empty";
		} else if (comboLocationName.getSelectedItem() == null || isEmptyString(comboLocationName.getSelectedItem().toString())) {
			comboLocationName.requestFocus(true);
			errorMessage = "Survey location name cannot be empty";
		} else if (!LatLongButton.isSelected() && !UTMButton.isSelected()) {
			LatLongButton.requestFocus(true);
			errorMessage = "Select between Lat/Long and UTM";
			/*
			 * Remember that x is longitude (East/West), and y is latitude
			 * (North/South). In this code however, for human readability, the
			 * UI is laid out as Lat, Long, which is actually Y, X
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
			//fields are populated
			System.out.println("Fields are populated.");
			isError = false;
			try {
				mass = Double.parseDouble(textMass.getText().trim());
			} catch (NumberFormatException nfe) {
				errorMessage = "Sample's mass must be a number";
				isError = true;
			}
			try {
				length = Double.parseDouble(textLength.getText().trim());
			} catch (NumberFormatException nfe) {
				errorMessage = "Sample's length must be a number";
				isError = true;
			}
			int zone = Location.EMPTY_ZONE;
			if (UTMButton.isSelected()) {
				try {
					zone = Integer.parseInt(textZone.getText().trim());
					if (zone > 60 || zone < 1) {
						errorMessage = "Sample's capture location zone must be between 1 and 60";
						isError = true;
					}
				} catch (NumberFormatException nfe) {
					errorMessage = "Sample's capture location zone must be a number";
					isError = true;
				}
			}
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

	void butClearAll_actionPerformed(ActionEvent e) {
		if (JOptionPane.showConfirmDialog(this, "This will clear all entered information on this screen.", "Clear Information",
				JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
			captureDatePicker.getModel().setSelected(false);
			textMass.setText("");
			textLength.setText("");
			textComments.setText("");
			textSurveyID.setText("");
			comboLocationName.setSelectedItem(null);
			textLocDesc.setText("");
			textX.setText("");
			textY.setText("");
			Butgroup.clearSelection(); // set radio LatLongButton, UTMButton to false
			textDatum.setText("");
			textZone.setText("");
		}
	}

	private void butDebugAutopopulate_actionPerformed(ActionEvent e) {
		LocalDate now = LocalDate.now();
		captureDatePicker.getModel().setDate(now.getYear(), now.getMonthValue() - 1, now.getDayOfMonth()); //-1 cause it's 0 indexed
		captureDatePicker.getModel().setSelected(true);
		textMass.setText("28");
		textLength.setText("64");
		textComments.setText("Really hoppy this one was");
		textSurveyID.setText("X");
		comboLocationName.getEditor().setItem("Area 51");
		textLocDesc.setText("Hoppy Pond");
		textX.setText("115.8111");
		textY.setText("37.2350");
		Butgroup.setSelected(LatLongButton.getModel(), true); // set radio LatLongButton, UTMButton to false
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

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public boolean shouldSave() {
		return shouldSave;
	}

	public void setShouldSave(boolean shouldSave) {
		this.shouldSave = shouldSave;
	}

	class DisabledGlassPane extends LayerUI<JComponent> {
		private boolean isOverlayEnabled = true;

		public boolean isOverlayEnabled() {
			return isOverlayEnabled;
		}

		public void setOverlayEnabled(boolean isEnabled) {
			this.isOverlayEnabled = isEnabled;
		}

		@Override
		public void paint(Graphics g, JComponent c) {
			super.paint(g, c);
			if (!isOverlayEnabled)
				return;
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .75f));
			g2.setPaint(new GradientPaint(0, 0, Color.gray, 0, c.getHeight(), Color.black));
			g2.fillRect(0, 0, c.getWidth(), c.getHeight());

			g2.setColor(Color.white);
			SharedUI.drawCenteredString(g2, "Select a template", SwingUtilities.getLocalBounds(c), new Font("Segoe UI", Font.PLAIN, 25));
			g2.dispose();
		}

		@Override
		public void eventDispatched(AWTEvent e, JLayer l) {
			if (isOverlayEnabled && e instanceof InputEvent) {
				((InputEvent) e).consume();
			}
		}

		@Override
		public void installUI(JComponent c) {
			super.installUI(c);
			JLayer jlayer = (JLayer) c;
			jlayer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
		}

		@Override
		public void uninstallUI(JComponent c) {
			JLayer jlayer = (JLayer) c;
			jlayer.setLayerEventMask(0);
			super.uninstallUI(c);
		}
	}

}