package gov.usgs.identifrog;

import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.Frames.ErrorDialog;
import gov.usgs.identifrog.Frames.MainFrame;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>
 * Title: ImageManipFrame.java
 * <p>
 * Description: frame for image processing, Color Fingerprint and Spot Extraction Tools
 * 
 * @author Oksana V. Kelly 2008
 * @author Oksana V. Kelly used image rotation/alignment by Steven P. Miller from <b>IdentiFrog</b>
 *         <i>2005</i>
 */
@SuppressWarnings("serial")
public class ImageManipFrame extends JFrame {
	private Preferences root = Preferences.userRoot();
	@SuppressWarnings("unused")
	private final Preferences node = root.node("edu/isu/aadis/defaults");
	public String installDir;
	private SliderListener listener = new SliderListener();
	private BorderLayout borderLayout1 = new BorderLayout();
	private Image icon = Toolkit.getDefaultToolkit().getImage("/resources/IconFrog.png");
	private ImagePanel imagePanel;
	private FillSpot fillSpot;
	private DigSignature digSignature;
	private JScrollPane ScrollPaneScroller = new JScrollPane();
	public JPanel ToolBarTools = new JPanel();
	private JButton butZoomIn = new JButton();
	private JButton butZoomOut = new JButton();
	private JPanel panelStatus = new JPanel();
	// private JButton butEdge = new JButton();
	private JButton butLight = new JButton();
	private JButton butDarker = new JButton();
	private JButton butSharpen = new JButton();
	private JButton butBlur = new JButton();
	private JButton butClearImage = new JButton();
	private JButton butPencil = new JButton();
	private JButton butFillSpot = new JButton();
	private JButton butUndoPencil = new JButton();
	private JButton butUndoFillSpot = new JButton();
	private JButton butDone = new JButton();
	private JPanel inPanelButtons = new JPanel();
	public JButton butStartOver = new JButton();
	private JButton butQuit = new JButton();
	private JButton butBack = new JButton();
	private JButton butNext = new JButton();
	// extract shape 1-14-09
	private JButton butExtract = new JButton();
	private BorderLayout borderLayout2 = new BorderLayout();
	private JMenuBar jMenuBar1 = new JMenuBar();
	private JMenu menuFile = new JMenu();
	private JMenu menuEdit = new JMenu();
	private JMenuItem MenuItemUndo = new JMenuItem();
	private JMenuItem MenuItemExit = new JMenuItem();
	public int step = 0;
	private boolean changeMade = false;
	private JButton butEraser = new JButton();
	private MainFrame parentFrame;
	private File Image;
	// Threshold Slider from Identifrog to dust an image
	private JSlider SliderThreshold = new JSlider();
	// Sliders for Spot extraction: Canny edge detection and dilation
	// for edge detection and dilation radius sliders
	private JPanel SpotExtractionPanelTools = new JPanel();
	// tool box for Color Fingerprint image
	private JPanel colorFingerprintTool = new JPanel();
	// tool box for Spot Extraction Edge image
	public JPanel SliderTools = new JPanel();
	// tool box for Final Binary image
	private JPanel SliderNoiseTool = new JPanel();
	private JSlider SliderThreshold_edges = new JSlider();
	private JSlider SliderDilation_radius = new JSlider();
	private JSlider SliderNoise_radius = new JSlider();
	private JLabel SliderLabel_edges = new JLabel();
	private JLabel SliderLabel_radius = new JLabel();
	private JLabel SliderLabel_noise = new JLabel();
	// Panels to demonstrate examples
	public JPanel ExamplePanelContainer = new JPanel();
	public JPanel ExampleRidge = new JPanel();
	public JPanel ExampleSnout = new JPanel();
	public JPanel ExampleVent = new JPanel();
	public JPanel ExampleLSide = new JPanel();
	public JPanel ExampleRSide = new JPanel();
	private JPanel inPanelInstructions = new JPanel();
	private JPanel inPanelToolsInstructions = new JPanel();
	public JTextArea TextAreaStep = new JTextArea();
	public JTextArea TextAreaStatus = new JTextArea();
	public JTextArea TextAreaTools = new JTextArea();
	public String instructions;
	private JLabel labelExampleRidge = new JLabel();
	private JLabel labelExampleEyeClicks = new JLabel();
	public JLabel labelExampleSnout = new JLabel();
	public JLabel labelExampleVent = new JLabel();
	private JLabel labelExampleLSide = new JLabel();
	private JLabel labelExampleRSide = new JLabel();
	@SuppressWarnings("unused")
	private GridLayout gridLayout1 = new GridLayout();
	private JTextField TextFieldThreshold_edges = new JTextField();
	private JTextField TextFieldDilation_radius = new JTextField();
	private JTextField TextFieldNoise_radius = new JTextField();
	@SuppressWarnings("unused")
	private boolean noise_radius_active = false;
	@SuppressWarnings("unused")
	private boolean searchDB; // this is currently unutilized
	@SuppressWarnings("unused")
	private int DbId = -1;
	JButton butDustFrame = new JButton();
	FlowLayout flowLayout1 = new FlowLayout();
	private Color sliderToolBoxColor = new Color(224, 223, 227);
	public int imageInEllipse_width = 0;
	public int imageInEllipse_heigth = 0;
	

	/**
	 * Frame Constructor
	 * 
	 * @param parent
	 *            MainFrame the parent (or calling) Frame
	 * @param imageFile
	 *            File The .jpg or .png image that will be manipulated
	 * @param frogId
	 *            int The Database ID that uniquely identifies the frog
	 */
	public ImageManipFrame(MainFrame parent, File imageFile, int db_id) {
		parentFrame = parent;
		Image = imageFile;
		DbId = db_id;
		parentFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		try {
			init();
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeMessage("ImageManipFrame.ImageManipFrame() Exception");
			IdentiFrog.LOGGER.writeException(e);
		}
		parentFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	// graphical user interface initialization
	private void init() throws Exception {
		if (true) {
			JOptionPane.showMessageDialog(null, "This feature is currently disabled.", "Feature disabled", JOptionPane.ERROR_MESSAGE);
			return;
		} 
		
		installDir = XMLFrogDatabase.getMainFolder();
		setIconImage(icon);
		setJMenuBar(jMenuBar1);
		imagePanel = new ImagePanel(ImageManipFrame.this, Image);
		digSignature = new DigSignature();
		getContentPane().setLayout(borderLayout1);
		panelStatus.setLayout(borderLayout2);
		panelStatus.setFont(new java.awt.Font("MS Sans Serif", 0, 14)); // 11
		panelStatus.setPreferredSize(new Dimension(44, 55)); // 35
		butClearImage.setPreferredSize(new Dimension(90, 28));
		butClearImage.setText("Clear Image");
		butClearImage.addActionListener(new ImageManipFrame_butClearImage_actionAdapter(this));
		butPencil.setPreferredSize(new Dimension(41, 41));
		butPencil.setIcon(new ImageIcon(ImageManipFrame.class.getResource("IconPencil32.png")));
		butPencil.setText("");// Pencil
		butPencil.setToolTipText("Pencil");
		butPencil.addActionListener(new ImageManipFrame_butPencil_actionAdapter(this));
		butUndoPencil.setPreferredSize(new Dimension(41, 41));
		butUndoPencil.setIcon(new ImageIcon(ImageManipFrame.class.getResource("IconUndo32.png")));
		butUndoPencil.setText("");
		butUndoPencil.addActionListener(new ImageManipFrame_butUndoPencil_actionAdapter(this));
		butUndoPencil.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		butUndoPencil.setToolTipText("Erase Pencil");
		butFillSpot.setPreferredSize(new Dimension(41, 41));
		butFillSpot.setIcon(new ImageIcon(ImageManipFrame.class.getResource("IconFill32.png")));
		butFillSpot.setText("");
		butFillSpot.setToolTipText("Spot Filler");
		butFillSpot.addActionListener(new ImageManipFrame_butFillSpot_actionAdapter(this));
		butUndoFillSpot.setPreferredSize(new Dimension(41, 41));
		butUndoFillSpot.setIcon(new ImageIcon(ImageManipFrame.class.getResource("IconUndo32.png")));
		butUndoFillSpot.setText("");
		butUndoFillSpot.setToolTipText("Clear All");
		butUndoFillSpot.addActionListener(new ImageManipFrame_butUndoFillSpot_actionAdapter(this));
		TextAreaStep.setFont(new java.awt.Font("SansSerif", Font.BOLD, 14));
		TextAreaStep.setForeground(new Color(90, 0, 120));
		TextAreaStep.setBackground(new Color(224, 223, 227));
		TextAreaStep.setMinimumSize(new Dimension(332, 30));
		TextAreaStep.setPreferredSize(new Dimension(332, 30));
		TextAreaStep.setEditable(false);
		TextAreaStep.setMargin(new Insets(0, 0, 0, 0));
		TextAreaStep.setLineWrap(true);
		TextAreaStep.setLocation(0, 1);
		TextAreaStatus.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 14));
		TextAreaStatus.setForeground(new Color(90, 0, 120));
		TextAreaStatus.setBackground(new Color(224, 223, 227));
		TextAreaStatus.setMinimumSize(new Dimension(332, 70));
		TextAreaStatus.setPreferredSize(new Dimension(332, 70));
		TextAreaStatus.setEditable(false);
		TextAreaStatus.setMargin(new Insets(0, 10, 0, 0));
		TextAreaStatus.setLineWrap(true);
		TextAreaStatus.setLocation(1, 1);
		TextAreaTools.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 14));
		TextAreaTools.setForeground(new Color(80, 0, 120));
		TextAreaTools.setBackground(new Color(224, 223, 227));
		TextAreaTools.setMinimumSize(new Dimension(332, 170));
		TextAreaTools.setPreferredSize(new Dimension(332, 170));
		TextAreaTools.setEditable(false);
		TextAreaTools.setMargin(new Insets(0, 10, 0, 0));
		TextAreaTools.setLineWrap(true);
		TextAreaTools.setLocation(1, 1);
		butStartOver.setIcon(new ImageIcon(ImageManipFrame.class.getResource("IconRestart32.png")));
		butStartOver.setText("Restart");
		butStartOver.addActionListener(new ImageManipFrame_butStartOver_actionAdapter(this));
		butQuit.setIcon(new ImageIcon(ImageManipFrame.class.getResource("IconCancel32.png")));
		butQuit.setText("Quit");
		butQuit.addActionListener(new ImageManipFrame_butQuit_actionAdapter(this));
		butBack.setEnabled(false);
		butBack.setIcon(new ImageIcon(ImageManipFrame.class.getResource("IconButtonPrevious32.png")));
		butBack.setText("Back");
		butBack.addActionListener(new ImageManipFrame_butBack_actionAdapter(this));
		butNext.setIcon(new ImageIcon(ImageManipFrame.class.getResource("IconButtonNext32.png")));
		butNext.setText("Next");
		butNext.addActionListener(new ImageManipFrame_butNext_actionAdapter(this));
		// button Extract shape
		// butExtract.setIcon(new ImageIcon(ImageManipFrame.class.getResource("ArrowRight.gif")));
		butExtract.setText("Extract");
		butExtract.addActionListener(new ImageManipFrame_butNext_actionAdapter(this));
		menuFile.setText("File");
		menuFile.addActionListener(new ImageManipFrame_menuFile_actionAdapter(this));
		menuEdit.setRolloverEnabled(false);
		menuEdit.setText("Edit");
		MenuItemUndo.setText("Undo");
		MenuItemUndo.setEnabled(false);
		MenuItemUndo.setAccelerator(javax.swing.KeyStroke.getKeyStroke('Z', java.awt.event.KeyEvent.CTRL_MASK, false));
		MenuItemUndo.addActionListener(new ImageManipFrame_MenuItemUndo_actionAdapter(this));
		// MenuItemRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke('Z',
		// java.awt.event.KeyEvent.CTRL_MASK |
		// java.awt.event.KeyEvent.SHIFT_MASK, false));
		MenuItemExit.setText("Exit");
		butEraser.setText("Eraser");
		butEraser.setPreferredSize(new Dimension(107, 33));
		butEraser.setIcon(new ImageIcon(ImageManipFrame.class.getResource("IconBlank32.png")));
		butEraser.addActionListener(new ImageManipFrame_butEraser_actionAdapter(this));
		SliderThreshold.setOrientation(JSlider.VERTICAL);
		SliderThreshold.setMajorTickSpacing(1000);
		SliderThreshold.setMaximum(10000);
		SliderThreshold.setMinimum(0);
		SliderThreshold.setMinorTickSpacing(500);
		SliderThreshold.setPaintLabels(true);
		SliderThreshold.setPaintTicks(true);
		SliderThreshold.setPaintTrack(false);
		SliderThreshold.setMaximumSize(new Dimension(55, 300));
		SliderThreshold.setMinimumSize(new Dimension(55, 300));
		SliderThreshold.setOpaque(true);
		SliderThreshold.setPreferredSize(new Dimension(55, 300));
		TextFieldThreshold_edges.setMaximumSize(new Dimension(30, 20));
		TextFieldThreshold_edges.setMinimumSize(new Dimension(30, 20));
		TextFieldThreshold_edges.setPreferredSize(new Dimension(30, 20));
		TextFieldThreshold_edges.setText("" + imagePanel.getdefaultThreshold_edges());
		TextFieldThreshold_edges.addFocusListener(new ImageManipFrame_TextFieldThreshold_edges_focusAdapter(this));
		TextFieldThreshold_edges.addKeyListener(new ImageManipFrame_TextFieldThreshold_edges_keyAdapter(this));
		TextFieldDilation_radius.setMaximumSize(new Dimension(30, 20));
		TextFieldDilation_radius.setMinimumSize(new Dimension(30, 20));
		TextFieldDilation_radius.setPreferredSize(new Dimension(30, 20));
		TextFieldDilation_radius.setText("" + imagePanel.getdefaultDilation_radius());
		TextFieldDilation_radius.addFocusListener(new ImageManipFrame_TextFieldDilation_radius_focusAdapter(this));
		TextFieldDilation_radius.addKeyListener(new ImageManipFrame_TextFieldDilation_radius_keyAdapter(this));
		TextFieldNoise_radius.setMaximumSize(new Dimension(30, 20));
		TextFieldNoise_radius.setMinimumSize(new Dimension(30, 20));
		TextFieldNoise_radius.setPreferredSize(new Dimension(30, 20));
		TextFieldNoise_radius.setText("" + imagePanel.getNoise_radius());
		TextFieldNoise_radius.addFocusListener(new ImageManipFrame_TextFieldNoise_radius_focusAdapter(this));
		TextFieldNoise_radius.addKeyListener(new ImageManipFrame_TextFieldNoise_radius_keyAdapter(this));
		// inPanelButtons.setMinimumSize(new Dimension(381, 85)); // 55
		// inPanelButtons.setPreferredSize(new Dimension(381, 85)); // 55
		/*
		 * butDustFrame.setIcon(new ImageIcon(ImageManipFrame.class.getResource( "duster.gif")));
		 * butDustFrame.setText("Dust Image"); butDustFrame.addActionListener(new
		 * ImageManipFrame_butDustFrame_actionAdapter(this));
		 */
		SliderThreshold_edges.setOrientation(JSlider.HORIZONTAL);
		SliderThreshold_edges.setMaximum(300);
		SliderThreshold_edges.setMinimum(100);
		// SliderThreshold_edges.setMajorTickSpacing(50);
		// SliderThreshold_edges.setMinorTickSpacing(10);
		// SliderThreshold_edges.setPaintTicks(false);
		SliderThreshold_edges.setPaintTrack(true);
		SliderThreshold_edges.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);
		SliderThreshold_edges.setBackground(sliderToolBoxColor);
		SliderThreshold_edges.setPreferredSize(new Dimension(220, 20));
		SliderThreshold_edges.setMaximumSize(new Dimension(220, 20));
		SliderThreshold_edges.setMinimumSize(new Dimension(220, 20));
		// SliderThreshold_edges.setOpaque(true);
		imagePanel.setBackground(sliderToolBoxColor);
		imagePanel.setLayout(null);
		SliderThreshold_edges.setValue(imagePanel.getdefaultThreshold_edges());
		SliderThreshold_edges.addChangeListener(listener);
		SliderTools.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		SliderLabel_edges.setPreferredSize(new Dimension(220, 13));
		SliderLabel_edges.setMaximumSize(new Dimension(220, 13));
		SliderLabel_edges.setMinimumSize(new Dimension(220, 13));
		Font curFont = SliderLabel_edges.getFont();
		SliderLabel_edges.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 11));
		SliderLabel_edges.setText("Edge Threshold");
		SliderDilation_radius.setOrientation(JSlider.HORIZONTAL);
		SliderDilation_radius.setMaximum(30);
		SliderDilation_radius.setMinimum(5);
		SliderDilation_radius.setBackground(sliderToolBoxColor);
		SliderDilation_radius.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);
		SliderDilation_radius.setValue(imagePanel.getdefaultDilation_radius());
		SliderDilation_radius.addChangeListener(listener);
		SliderDilation_radius.setPreferredSize(new Dimension(220, 20));
		SliderDilation_radius.setMaximumSize(new Dimension(220, 20));
		SliderDilation_radius.setMinimumSize(new Dimension(220, 20));
		SliderLabel_radius.setPreferredSize(new Dimension(220, 13));
		SliderLabel_radius.setMaximumSize(new Dimension(220, 13));
		SliderLabel_radius.setMinimumSize(new Dimension(220, 13));
		SliderLabel_radius.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 11));
		SliderLabel_radius.setText("Dilation Radius");
		SliderNoise_radius.setOrientation(JSlider.HORIZONTAL);
		SliderNoise_radius.setMaximum(100);
		SliderNoise_radius.setMinimum(0);
		SliderNoise_radius.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);
		SliderNoise_radius.setValue(imagePanel.getNoise_radius());
		SliderNoise_radius.addChangeListener(listener);
		SliderNoise_radius.setPreferredSize(new Dimension(220, 20));
		SliderNoise_radius.setMaximumSize(new Dimension(220, 20));
		SliderNoise_radius.setMinimumSize(new Dimension(220, 20));
		SliderLabel_noise.setPreferredSize(new Dimension(220, 13));
		SliderLabel_noise.setMaximumSize(new Dimension(220, 13));
		SliderLabel_noise.setMinimumSize(new Dimension(220, 13));
		SliderLabel_noise.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 11));
		SliderLabel_noise.setText("Noise Radius");
		// Adding to SliderTools
		SpotExtractionPanelTools.setMaximumSize(new Dimension(300, 700));
		SpotExtractionPanelTools.setMinimumSize(new Dimension(300, 700));
		SpotExtractionPanelTools.setPreferredSize(new Dimension(300, 700));
		TitledBorder border = new TitledBorder("Spot Extraction Tools");
		border.setTitleFont(new java.awt.Font("MS Sans Serif", 0, 12));
		border.setTitleColor(Color.black);
		SliderTools.setBorder(border);
		SliderTools.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
		SliderTools.setMaximumSize(new Dimension(300, 180));
		SliderTools.setMinimumSize(new Dimension(300, 180));
		SliderTools.setPreferredSize(new Dimension(300, 180));
		SliderTools.setBackground(sliderToolBoxColor);
		SliderTools.add(butFillSpot, null);
		SliderTools.add(butUndoFillSpot);
		SliderTools.add(SliderLabel_edges, null);
		SliderTools.add(SliderThreshold_edges, null);
		SliderTools.add(TextFieldThreshold_edges, null);
		SliderTools.add(SliderLabel_radius, null);
		SliderTools.add(SliderDilation_radius, null);
		SliderTools.add(TextFieldDilation_radius, null);
		TitledBorder border1 = new TitledBorder("Binary Image Tools");
		SliderNoiseTool.setBorder(border1);
		border1.setTitleColor(Color.black);
		SliderNoiseTool.setLayout(new FlowLayout(FlowLayout.LEFT));
		SliderNoiseTool.setMaximumSize(new Dimension(280, 120));
		SliderNoiseTool.setMinimumSize(new Dimension(280, 120));
		SliderNoiseTool.setPreferredSize(new Dimension(280, 120));
		SliderNoiseTool.add(butClearImage);
		SliderNoiseTool.add(SliderLabel_noise, null);
		SliderNoiseTool.add(SliderNoise_radius, null);
		SliderNoiseTool.add(TextFieldNoise_radius, null);
		TitledBorder border2 = new TitledBorder("Color Fingerprint Tools");
		border2.setTitleFont(new java.awt.Font("MS Sans Serif", 0, 12));
		colorFingerprintTool.setBorder(border2);
		border2.setTitleColor(Color.black);
		colorFingerprintTool.setBackground(sliderToolBoxColor);
		colorFingerprintTool.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
		colorFingerprintTool.setMaximumSize(new Dimension(146, 80));
		colorFingerprintTool.setMinimumSize(new Dimension(146, 80));
		colorFingerprintTool.setPreferredSize(new Dimension(146, 80));
		colorFingerprintTool.add(butPencil);
		colorFingerprintTool.add(butUndoPencil);
		SpotExtractionPanelTools.setVisible(false);
		SpotExtractionPanelTools.setBackground(sliderToolBoxColor);
		SpotExtractionPanelTools.setLayout(null);
		colorFingerprintTool.setBounds(0, 40, 147, 80);
		SpotExtractionPanelTools.add(colorFingerprintTool);
		SliderTools.setBounds(0, 261, 295, 180);
		SpotExtractionPanelTools.add(SliderTools);
		SliderNoiseTool.setBounds(0, 481, 280, 120);
		// p.add(SliderNoiseTool);
		// Panel with Examples
		getContentPane().add(ExamplePanelContainer, BorderLayout.WEST);
		// Panel with Instructions
		inPanelInstructions.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		inPanelToolsInstructions.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		inPanelInstructions.setMinimumSize(new Dimension(360, 150));
		inPanelInstructions.setMaximumSize(new Dimension(360, 150));
		inPanelInstructions.setPreferredSize(new Dimension(360, 150));
		inPanelInstructions.setBackground(new Color(224, 223, 227));
		inPanelToolsInstructions.setMinimumSize(new Dimension(360, 240));
		inPanelToolsInstructions.setMaximumSize(new Dimension(360, 240));
		inPanelToolsInstructions.setPreferredSize(new Dimension(360, 240));
		inPanelToolsInstructions.setBackground(new Color(224, 223, 227));
		// Ridge Alignment Example
		TitledBorder borderExample = new TitledBorder(" Example ");
		borderExample.setTitleFont(new java.awt.Font("MS Sans Serif", 0, 14));
		ExampleVent.setBorder(borderExample);
		ExampleSnout.setBorder(borderExample);
		ExampleLSide.setBorder(borderExample);
		ExampleRSide.setBorder(borderExample);
		ExampleRidge.setBorder(borderExample);
		borderExample.setTitleColor(Color.black);
		borderExample.setTitleJustification(TitledBorder.CENTER);
		ExampleRidge.setBackground(new Color(224, 223, 227));
		ExampleRidge.setMinimumSize(new Dimension(360, 349));
		ExampleRidge.setPreferredSize(new Dimension(360, 349));
		ExampleRidge.add(labelExampleEyeClicks, null);
		ExampleRidge.add(labelExampleRidge, null);
		ExampleSnout.setBackground(new Color(224, 223, 227));
		ExampleSnout.setMaximumSize(new Dimension(360, 189));
		ExampleSnout.setMinimumSize(new Dimension(360, 189));
		ExampleSnout.setPreferredSize(new Dimension(360, 189));
		ExampleSnout.add(labelExampleSnout, null);
		ExampleVent.setBackground(new Color(224, 223, 227));
		ExampleVent.setMaximumSize(new Dimension(360, 189));
		ExampleVent.setMinimumSize(new Dimension(360, 189));
		ExampleVent.setPreferredSize(new Dimension(360, 189));
		ExampleVent.add(labelExampleVent, null);
		ExampleLSide.setBackground(new Color(224, 223, 227));
		ExampleLSide.setMaximumSize(new Dimension(360, 189));
		ExampleLSide.setMinimumSize(new Dimension(360, 189));
		ExampleLSide.setPreferredSize(new Dimension(360, 189));
		ExampleLSide.add(labelExampleLSide, null);
		ExampleRSide.setBackground(new Color(224, 223, 227));
		ExampleRSide.setMaximumSize(new Dimension(360, 189));
		ExampleRSide.setMinimumSize(new Dimension(360, 189));
		ExampleRSide.setPreferredSize(new Dimension(360, 189));
		ExampleRSide.add(labelExampleRSide, null);
		labelExampleRidge.setIcon(new ImageIcon(ImageManipFrame.class.getResource("exampleRidge.gif")));
		labelExampleEyeClicks.setIcon(new ImageIcon(ImageManipFrame.class.getResource("exampleEyeClicks.gif")));
		labelExampleSnout.setIcon(new ImageIcon(ImageManipFrame.class.getResource("exampleSnout.gif")));
		labelExampleVent.setIcon(new ImageIcon(ImageManipFrame.class.getResource("exampleVent.gif")));
		labelExampleLSide.setIcon(new ImageIcon(ImageManipFrame.class.getResource("exampleLSide.gif")));
		labelExampleRSide.setIcon(new ImageIcon(ImageManipFrame.class.getResource("exampleRSide.gif")));
		labelExampleRidge.setVisible(true);
		labelExampleEyeClicks.setVisible(true);
		labelExampleSnout.setVisible(true);
		labelExampleVent.setVisible(true);
		labelExampleLSide.setVisible(true);
		labelExampleRSide.setVisible(true);
		// Adding Examples to Panel
		ExamplePanelContainer.setLayout(new FlowLayout());
		ExamplePanelContainer.setMaximumSize(new Dimension(370, 570));
		ExamplePanelContainer.setMinimumSize(new Dimension(370, 570)); // 570
		ExamplePanelContainer.setPreferredSize(new Dimension(370, 570));
		ExamplePanelContainer.add(inPanelInstructions, null);
		ExamplePanelContainer.add(inPanelToolsInstructions, null);
		ExamplePanelContainer.add(ExampleRidge, null);
		ExamplePanelContainer.add(ExampleSnout, null);
		ExamplePanelContainer.add(ExampleVent, null);
		ExamplePanelContainer.add(ExampleLSide, null);
		ExamplePanelContainer.add(ExampleRSide, null);
		inPanelInstructions.setVisible(true);
		inPanelToolsInstructions.setVisible(false);
		ExamplePanelContainer.setVisible(true);
		TitledBorder borderInstruction = new TitledBorder(" Instructions ");
		borderInstruction.setTitleFont(new java.awt.Font("MS Sans Serif", 0, 14));
		borderInstruction.setTitleJustification(TitledBorder.CENTER);
		inPanelInstructions.setBorder(borderInstruction);
		borderInstruction.setTitleColor(Color.black);
		TitledBorder borderTools = new TitledBorder(" Tools ");
		borderTools.setTitleFont(new java.awt.Font("MS Sans Serif", 0, 14));
		borderTools.setTitleJustification(TitledBorder.CENTER);
		inPanelToolsInstructions.setBorder(borderTools);
		borderTools.setTitleColor(Color.black);
		ToolBarTools.setLayout(flowLayout1);
		ToolBarTools.setMaximumSize(new Dimension(100, 310));
		ToolBarTools.setMinimumSize(new Dimension(100, 310));
		ToolBarTools.setPreferredSize(new Dimension(120, 310));
		inPanelButtons.add(butQuit, null);
		ScrollPaneScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		ScrollPaneScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		getContentPane().add(ScrollPaneScroller, BorderLayout.CENTER);
		ScrollPaneScroller.setViewportView(imagePanel);
		SpotExtractionPanelTools.setBounds(460, 0, 300, 700);
		imagePanel.add(SpotExtractionPanelTools);
		// ImagePanel size to the size of the screen - 370
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// ImageManipFrame.ExamplePanelContainer is 370 wide
		int fw = (int) (screen.getWidth() - 390);
		int fh = (int) (screen.getHeight() * 0.85);
		imagePanel.setPreferredSize(new Dimension(fw, fh));
		imagePanel.setCenter();
		getContentPane().add(panelStatus, BorderLayout.SOUTH);
		inPanelInstructions.add(TextAreaStep, null);
		inPanelInstructions.add(TextAreaStatus, null);
		inPanelToolsInstructions.add(TextAreaTools, null);
		// panelStatus.add(inPanelInstructions, BorderLayout.CENTER);
		panelStatus.add(inPanelButtons, BorderLayout.EAST);
		ToolBarTools.setVisible(false);
		inPanelButtons.add(butStartOver, null);
		inPanelButtons.add(butBack, null);
		inPanelButtons.add(butNext, null);
		// jMenuBar1.add(menuFile);
		// jMenuBar1.add(menuEdit);
		// menuEdit.add(MenuItemUndo);
		// menuFile.add(MenuItemExit);
		// ToolBarTools.add(TextFieldThreshold_edges, null);
		ToolBarTools.add(butDustFrame, null);
		imagePanel.setCenter(getWidth(), getHeight());
		// Default Button Settings
		imagePanel.setRubberBandLineOn(false);
		imagePanel.setFirstRubberBoxOn(false);
		imagePanel.setShowBoundingBox(false);
		imagePanel.setSecondRubberBoxOn(false);
		imagePanel.setRidgeInBox(false);
		// imagePanel.setMaskOn(false);
		imagePanel.setEraserOn(false);
		// setModifierPackage(false);
		butEraser.setVisible(false);
		setThresholdPackage(false);
		setSpotExtractionPanelTools(false);
		imagePanel.setRidgeInBox(false);
		imagePanel.setTwoEyeClicks(false);
		imagePanel.setRidgeRect(false);
		ExampleVent.setVisible(true);
		ExampleSnout.setVisible(false);
		ExampleLSide.setVisible(false);
		ExampleRSide.setVisible(false);
		ExampleRidge.setVisible(false);
		TextAreaStep.setText("Step 1 of 7: BODY ALIGNMENT");
		TextAreaStatus.setText("Click on the frog's vent at the midline\nof the body.");
		imagePanel.setRubberBandLineOn(true);
		imagePanel.setImageinEllipse(false);
		butBack.setEnabled(false);
		butNext.setEnabled(false);
		butStartOver.setEnabled(false);
		step = 0;
		addWindowListener(new ImageManipFrame_this_windowAdapter(this));
		pack();
	}

	// Not Used
	protected void butCrop_actionPerformed(ActionEvent e) {
		changeMade = true;
		imagePanel.cropImage();
	}

	// Window closing action
	void this_windowClosing(WindowEvent e) {
		butQuit_actionPerformed(null);
	}

	// Quit button pressed
	protected void butQuit_actionPerformed(ActionEvent e) {
		// DBdeletefrog dbDeletefrog = new DBdeletefrog(DbId);
		System.gc(); // Garbage Collector
		ArrayList<Frog> localFrogs = XMLFrogDatabase.getFrogs();
		localFrogs.remove(localFrogs.size() - 1);
		//parentFrame.getFrogData().setFrogs(localFrogs);
		IdentiFrog.LOGGER.writeError("Passed a setFrogs() in butQuit ImageManipFrame. Possible side effects with the quit method...");
		parentFrame.updateCells();
		// XMLHandler file = new XMLHandler(new File(FolderHandler.getFileNamePath()),
		// parentFrame.getFrogData().getFrogs());
		// file.WriteXMLFile();
		closeAction();
	}

	// adding filled spot to an image
	protected void butClearImage_actionPerformed(ActionEvent e) {
		changeMade = true;
		// imagePanel.updateBinaryImage();
		imagePanel.setNoise_slider_Active(false);
		noise_radius_active = false;
		imagePanel.repaint();
	}

	// Pencil to draw on Color Image
	protected void butPencil_actionPerformed(ActionEvent e) {
		// Load an image for the cursor
		Image pencilimage = Toolkit.getDefaultToolkit().getImage(ImageManipFrame.class.getResource("MousePencel.png"));
		// Create the hotspot for the cursor
		Point hotSpot = new Point(1, 15);
		// Create the custom cursor
		Cursor pencilcursor = Toolkit.getDefaultToolkit().createCustomCursor(pencilimage, hotSpot, "Pencil");
		imagePanel.setCursor(pencilcursor);
		butPencil.getModel().setPressed(true);
		butFillSpot.getModel().setPressed(false);
		butUndoFillSpot.getModel().setPressed(false);
		butUndoPencil.getModel().setPressed(false);
		imagePanel.setPencilOn(true);
		imagePanel.setUndoPencilOn(false);
		imagePanel.setFillSpotOn(false);
		imagePanel.setUndoFillSpotOn(false);
	}

	// UndoPencil
	protected void butUndoPencil_actionPerformed(ActionEvent e) {
		butPencil.getModel().setPressed(true);
		butUndoPencil.getModel().setPressed(false);
		butFillSpot.getModel().setPressed(false);
		butUndoFillSpot.getModel().setPressed(false);
		imagePanel.setUndoPencilOn(true);
		imagePanel.setPencilOn(true);
		imagePanel.setFillSpotOn(false);
		imagePanel.setBacktoOriginalRectImage();
		imagePanel.setUndoFillSpotOn(false);
	}

	// Pencil to draw on Color Image
	protected void butFillSpot_actionPerformed(ActionEvent e) {
		imagePanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		butFillSpot.getModel().setPressed(true);
		butUndoFillSpot.getModel().setPressed(false);
		butPencil.getModel().setPressed(false);
		butUndoPencil.getModel().setPressed(false);
		imagePanel.setFillSpotOn(true);
		imagePanel.setPencilOn(false);
		imagePanel.setUndoPencilOn(false);
		imagePanel.setUndoFillSpotOn(false);
	}

	// Undo Fill Spot
	protected void butUndoFillSpot_actionPerformed(ActionEvent e) {
		butFillSpot.getModel().setPressed(true);
		butUndoFillSpot.getModel().setPressed(false);
		butPencil.getModel().setPressed(false);
		butUndoPencil.getModel().setPressed(false);
		imagePanel.setUndoFillSpotOn(true);
		imagePanel.setBacktoOriginalRectImage();
		imagePanel.setPencilOn(false);
		imagePanel.setUndoPencilOn(false);
		imagePanel.setFillSpotOn(false);
	}

	/**
	 * @param back
	 *            boolean true if Back button is enabled, false if not
	 */
	public void setBackOn(boolean back) {
		butBack.setEnabled(back);
	}

	/**
	 * @param next
	 *            boolean true if Next button is enabled, false if not
	 */
	public void setNextOn(boolean next) {
		butNext.setEnabled(next);
	}

	// Menu - Exit
	protected void menuFile_actionPerformed(ActionEvent e) {
		closeAction();
	}

	// Menu - Undo
	protected void MenuItemUndo_actionPerformed(ActionEvent e) {
		imagePanel.undoPaint();
	}

	/**
	 * @param undoOn
	 *            boolean true if Undo menu item is enabled, false if not
	 */
	public void setMenuItemUndo(boolean undoOn) {
		MenuItemUndo.setEnabled(undoOn);
	}

	// Clean up feature
	private void closeAction() {
		imagePanel.deleteTempFiles();
		imagePanel.removeAll();
		imagePanel = null;
		System.gc(); // Garbage Collector
		dispose();
	}

	/**
	 * Action taken when Next button is pressed
	 */
	protected void butNext_actionPerformed(ActionEvent e) {
		switch (step) {
			case 1:// to spot extraction window
				ExamplePanelContainer.setVisible(true);
				ExampleVent.setVisible(false);
				ExampleSnout.setVisible(false);
				ExampleLSide.setVisible(false);
				ExampleRSide.setVisible(false);
				ExampleRidge.setVisible(false);
				inPanelToolsInstructions.setVisible(true);
				TextAreaStep.setText("Step 7 of 7: BINARY FINGERPRINT CREATION");
				TextAreaStatus.setText("Use the Tools to extract a spot pattern into\nthe Binary Fingerprint.\nThen click Next>>");
				TextAreaTools
						.setText("Use the Spot Filler and click inside each\nspot contour.\nFilled spots are added to the Binary Fingerprint.\n\nAdjust the Edge Threshold and Dilation Radius\nfor edge detection.\n\nUse the Pencil on the Color Fingerprint to\nconnect any broken contours.");
				setSpotExtractionPanelTools(true);
				imagePanel.setImageinEllipse(false);
				// ready to map fingerprint onto Standard Rectangle
				imagePanel.mapOntoRectangle();
				imagePanel.standardRectEdgesDilated = imagePanel.detectEdges();
				imagePanel.trackOriginalRectImage();
				imagePanel.setSpotExtraction(true);
				imagePanel.reset = true;
				// imagePanel.filterMedian3x3();
				// imagePanel.adjustContrast();
				imagePanel.setRidgeInBox(false);
				imagePanel.setTwoEyeClicks(false);
				imagePanel.setRidgeRect(false);
				butStartOver.setEnabled(true);
				// imagePanel.setNoise_slider_Active(false);
				IdentiFrog.LOGGER.writeMessage("case 1 before next " + step);
				increaseStep();
				IdentiFrog.LOGGER.writeMessage("case 1 after next " + step);
				imagePanel.newOperationStep();
				imagePanel.repaint();
				break;
			case 2:// when final Next button pressed
				// binaryImage 256x128 is used to create signature
				parentFrame.setChangesMade(true);
				String imageName1 = imagePanel.exportDownsampledImage(XMLFrogDatabase.getBinaryFolder());
				BufferedImage binaryImage = imagePanel.saveBinaryImage(XMLFrogDatabase.getBinaryFolder());

				String sigFileLocarion = XMLFrogDatabase.getSignaturesFolder() + imageName1;
				String binaryImageLocation = XMLFrogDatabase.getBinaryFolder() + imageName1;
				IdentiFrog.LOGGER.writeMessage("imageName1 " + imageName1);
				IdentiFrog.LOGGER.writeMessage("sigFileLocation " + sigFileLocarion);
				IdentiFrog.LOGGER.writeMessage("binaryImageLocation " + binaryImageLocation);

				if (binaryImageLocation != null) {
					if (binaryImage != null) {
						digSignature.makeSignature(binaryImage, sigFileLocarion);
						DigSigThread digSigThread = new DigSigThread(new File(sigFileLocarion));
						digSigThread.start();
					} else {
						new ErrorDialog("Could not obtain binary image");
					}
				} else {
					new ErrorDialog("Could not create digital signature");
				}
				// ///////////////////////////////////////////////////////////////////////////////////
				parentFrame.updateCells();
				//XMLFrogDatabase.getFrogs().get(XMLFrogDatabase.getFrogs().size() - 1).setPathImage(imageName1);
				IdentiFrog.LOGGER.writeMessage(XMLFrogDatabase.getFrogs().size());
				XMLFrogDatabase.writeXMLFile();
				parentFrame.updateCells(0, false);
				closeAction();

				break;
		}
	}

	/**
	 * Action taken when Back button is pressed
	 */
	protected void butBack_actionPerformed(ActionEvent e) {
		parentFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		switch (step) {
			case 1:// from one to zero
				ExamplePanelContainer.setVisible(true);
				ExampleVent.setVisible(true);
				ExampleSnout.setVisible(false);
				ExampleLSide.setVisible(false);
				ExampleRSide.setVisible(false);
				ExampleRidge.setVisible(false);
				TextAreaStep.setText("Step 1 of 7: BODY ALIGNMENT");
				TextAreaStatus.setText("Click on the frog's vent at the midline\nof the body.");
				imagePanel.resetToOrigImage();
				imagePanel.setRubberBandLineOn(true);
				imagePanel.setImageinEllipse(false);
				butBack.setEnabled(false);
				butNext.setEnabled(false);
				butStartOver.setEnabled(false);
				ExampleRidge.setVisible(false);
				imagePanel.setFirstRubberBoxOn(false);
				imagePanel.setShowBoundingBox(false);
				imagePanel.setSecondRubberBoxOn(false);
				imagePanel.setShowCenterLine(false);
				imagePanel.setFirstClick(false);
				imagePanel.setRidgeInBox(false);
				imagePanel.setTwoEyeClicks(false);
				imagePanel.setRidgeRect(false);
				imagePanel.setSpotExtraction(false);
				imagePanel.setPencilOn(false);
				imagePanel.setFillSpotOn(false);
				imagePanel.setUndoPencilOn(false);
				imagePanel.setUndoFillSpotOn(false);
				IdentiFrog.LOGGER.writeMessage("case 1 before back " + step);
				decreaseStep();
				IdentiFrog.LOGGER.writeMessage("case 1 afer back " + step);
				break;
			case 2: // from binary fingerprint creation back to clicking on the frog's eyes
				IdentiFrog.LOGGER.writeMessage("back case 2");
				ExamplePanelContainer.setVisible(true);
				inPanelToolsInstructions.setVisible(false);
				ExampleVent.setVisible(false);
				ExampleSnout.setVisible(false);
				ExampleLSide.setVisible(false);
				ExampleRSide.setVisible(false);
				ExampleRidge.setVisible(true);
				imagePanel.setRubberBandLineOn(false);
				imagePanel.setFirstRubberBoxOn(false);
				imagePanel.setShowBoundingBox(false);
				imagePanel.setSecondRubberBoxOn(false);
				imagePanel.setShowCenterLine(false);
				imagePanel.setFirstClick(false);
				setSpotExtractionPanelTools(false);
				ExampleRidge.setVisible(false);
				imagePanel.setSpotExtraction(false);
				imagePanel.setuptoWorkOnRidges();
				imagePanel.setFirstClick(false);
				imagePanel.setSecondRubberBoxOn(false);
				imagePanel.setPencilOn(false);
				imagePanel.setFillSpotOn(false);
				imagePanel.setUndoPencilOn(false);
				imagePanel.setUndoFillSpotOn(false);
				imagePanel.setTwoEyeClicks(false);
				imagePanel.setRidgeRect(false);
				imagePanel.pencilCoor.clear();
				imagePanel.spotFilledCoor.clear();
				butBack.setEnabled(true);
				butNext.setEnabled(false);
				butStartOver.setEnabled(true);
				IdentiFrog.LOGGER.writeMessage("case 2 before back " + step);
				decreaseStep();
				IdentiFrog.LOGGER.writeMessage("case 2 afer back " + step);
				imagePanel.backOperationStep();
				imagePanel.deleteTempFiles();
				imagePanel.repaint();
				break;
		}
	}

	// All these are enabled or disabled at the same time, simply saving code
	private void setThresholdPackage(boolean vis) {
		SliderThreshold.setVisible(vis);
		butEraser.setVisible(vis);
		butDustFrame.setVisible(vis);
	}

	private void setSpotExtractionPanelTools(boolean visible) {
		SpotExtractionPanelTools.setVisible(visible);
		SliderThreshold_edges.setValue(imagePanel.getdefaultThreshold_edges());
		SliderDilation_radius.setValue(imagePanel.getdefaultDilation_radius());
		// SliderNoise_radius.setValue(0);
	}

	// Changes text in the status area
	public void setTextAreaStatus(String status) {
		TextAreaStatus.setText(status);
	}

	// Increments to the next step
	public void increaseStep() {
		changeMade = false;
		step++;
		IdentiFrog.LOGGER.writeMessage("incr step " + step);
	}

	// Decraments to the previous step
	public void decreaseStep() {
		step--;
	}

	// Re-start button pressed - Starts from the beginning of the process
	protected void butStartOver_actionPerformed(ActionEvent e) {
		ExamplePanelContainer.setVisible(true);
		inPanelToolsInstructions.setVisible(false);
		ExampleVent.setVisible(true);
		ExampleSnout.setVisible(false);
		ExampleLSide.setVisible(false);
		ExampleRSide.setVisible(false);
		ExampleRidge.setVisible(false);
		TextAreaStep.setText("Step 1 of 7: BODY ALIGNMENT");
		TextAreaStatus.setText("Click on the frog's vent at the midline\nof the body.");
		imagePanel.resetToOrigImage();
		imagePanel.setRubberBandLineOn(true);
		imagePanel.setImageinEllipse(false);
		butBack.setEnabled(false);
		butNext.setEnabled(false);
		butStartOver.setEnabled(false);
		imagePanel.setFirstClick(false);
		imagePanel.setFirstRubberBoxOn(false);
		imagePanel.setShowBoundingBox(false);
		imagePanel.setSecondRubberBoxOn(false);
		imagePanel.setShowCenterLine(false);
		imagePanel.setRidgeInBox(false);
		imagePanel.setTwoEyeClicks(false);
		imagePanel.setRidgeRect(false);
		imagePanel.setSpotExtraction(false);
		imagePanel.setPencilOn(false);
		imagePanel.setFillSpotOn(false);
		imagePanel.setUndoPencilOn(false);
		imagePanel.setUndoFillSpotOn(false);
		imagePanel.pencilCoor.clear();
		imagePanel.spotFilledCoor.clear();
		setSpotExtractionPanelTools(false);
		step = 0;
	}

	// Toggles between eraser mode and auto-masking mode
	protected void butEraser_actionPerformed(ActionEvent e) {
		if (imagePanel.isEraserOn()) {// Eraser mode
			// imagePanel.setMaskOn(true);
			imagePanel.setEraserOn(false);
			TextFieldThreshold_edges.setEnabled(true);
			SliderThreshold.setEnabled(true);
			setMenuItemUndo(false);
			TextAreaStatus.setText("Click on an area inside the mask, but outside the frog,\n" + "or click Next>>");
			butEraser.setText("Eraser");
			butEraser.setIcon(new ImageIcon(ImageManipFrame.class.getResource("Eraser.gif")));
		} else {// Mask mode
			// imagePanel.setMaskOn(false);
			imagePanel.setEraserOn(true);
			TextFieldThreshold_edges.setEnabled(false);
			SliderThreshold.setEnabled(false);
			imagePanel.setUndoPoint();
			TextAreaStatus.setText("Click and drag the eraser over an area you want to erase,\n" + "or click Next>>");
			butEraser.setText("Mask");
			butEraser.setIcon(new ImageIcon(ImageManipFrame.class.getResource("mask.gif")));
		}
	}

	class SliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			imagePanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			JSlider slider = (JSlider) e.getSource();
			butFillSpot.getModel().setPressed(false);
			butUndoFillSpot.getModel().setPressed(false);
			butPencil.getModel().setPressed(false);
			butUndoPencil.getModel().setPressed(false);
			if (slider == SliderThreshold_edges) {
				TextFieldThreshold_edges.setText("" + SliderThreshold_edges.getValue());
				imagePanel.setThreshold_edges(SliderThreshold_edges.getValue());
				noise_radius_active = false;
			} else if (slider == SliderDilation_radius) {
				TextFieldDilation_radius.setText("" + SliderDilation_radius.getValue());
				imagePanel.setDilation_radius(SliderDilation_radius.getValue());
				imagePanel.setNoise_slider_Active(false);
				noise_radius_active = false;
			} else if (slider == SliderNoise_radius) {
				TextFieldNoise_radius.setText("" + SliderNoise_radius.getValue());
				imagePanel.setNoise_radius(SliderNoise_radius.getValue());
				imagePanel.setNoise_slider_Active(true);
				noise_radius_active = true;
			}
			new thresholdWorker().execute();
		}
	}

	/**
	 * @return int the value of the threshold slider
	 */
	public int getSliderThreshold() {
		return SliderThreshold_edges.getValue();
	}

	// WORKER
	private class thresholdWorker extends SwingWorker<BufferedImage, Object> {
		public thresholdWorker() {
		}

		// long-running code to be run in a worker thread
		@Override
		public BufferedImage doInBackground() throws Exception {
			@SuppressWarnings("static-access")
			BufferedImage img = new BufferedImage(imagePanel.rect_width, imagePanel.rect_height, BufferedImage.TYPE_3BYTE_BGR);
			// if (!noise_radius_active){
			img = imagePanel.detectEdges();
			// } else img = imagePanel.removeNoiseInBinaryImage();
			return img;
		} // end method doInBackground

		// code to run on the event dispatch thread when doInBackground returns
		@Override
		protected void done() {
			try {
				@SuppressWarnings("static-access")
				BufferedImage Img = new BufferedImage(imagePanel.rect_width, imagePanel.rect_height, BufferedImage.TYPE_3BYTE_BGR);
				Img = get();
				// if (!noise_radius_active)
				imagePanel.standardRectEdgesDilated = Img;
				// else imagePanel.standardRectBinary = Img;
				imagePanel.repaint();
			} catch (InterruptedException ignore) {
			} catch (ExecutionException ex) {
				System.err.println("Error encountered while performing calculation.");
			}
		}
	}

	// Sets slider value text if input by the user, and the user presses enter
	@SuppressWarnings("static-access")
	protected void TextFieldThreshold_edges_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == e.VK_ENTER) {
			setSliderFromText_edges();
		}
	}

	// Sets slider value text if input by the user, and the user leaves this field
	protected void TextFieldThreshold_edges_focusLost(FocusEvent e) {
		setSliderFromText_edges();
	}

	@SuppressWarnings("static-access")
	protected void TextFieldDilation_radius_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == e.VK_ENTER) {
			setSliderFromText_dilation();
		}
	}

	// Sets slider value text if input by the user, and the user leaves this field
	protected void TextFieldDilation_radius_focusLost(FocusEvent e) {
		setSliderFromText_dilation();
	}

	@SuppressWarnings("static-access")
	protected void TextFieldNoise_radius_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == e.VK_ENTER) {
			setSliderFromText_noise();
		}
	}

	// Sets slider value text if input by the user, and the user leaves this field
	protected void TextFieldNoise_radius_focusLost(FocusEvent e) {
		setSliderFromText_noise();
	}

	// Sets slider from entry in the text box
	private void setSliderFromText_edges() {
		int c = SliderThreshold_edges.getValue();
		try {
			c = Integer.parseInt(TextFieldThreshold_edges.getText());
			if (c < 1 || c > 300) {
				new ErrorDialog("Invalid Entry, Must be an integer between 1 - 300");
			} else {
				SliderThreshold_edges.setValue(c);
				imagePanel.setThreshold_edges(c);
			}
		} catch (Exception ex) {
			TextFieldThreshold_edges.setText("" + c);
			new ErrorDialog("Invalid Entry, Must be an integer between 1 - 300");
		}
	}

	private void setSliderFromText_dilation() {
		int w = SliderDilation_radius.getValue();
		try {
			w = Integer.parseInt(TextFieldDilation_radius.getText());
			if (w < 0 || w > 10) {
				new ErrorDialog("Invalid Entry, Must be an integer between 0 - 30");
			} else {
				SliderDilation_radius.setValue(w);
				imagePanel.setDilation_radius(w);
			}
		} catch (Exception ex) {
			TextFieldDilation_radius.setText("" + w);
			new ErrorDialog("Invalid Entry, Must be an integer between 0 - 30");
		}
	}

	private void setSliderFromText_noise() {
		int w = SliderNoise_radius.getValue();
		try {
			w = Integer.parseInt(TextFieldNoise_radius.getText());
			if (w < 0 || w > 100) {
				new ErrorDialog("Invalid Entry, Must be an integer between 0 - 100");
			} else {
				SliderNoise_radius.setValue(w);
				imagePanel.setNoise_radius(w);
			}
		} catch (Exception ex) {
			TextFieldNoise_radius.setText("" + w);
			new ErrorDialog("Invalid Entry, Must be an integer between 0 - 100");
		}
	}

	/**
	 * @return boolean returns true if changes have been made since last step, false if not
	 */
	public boolean isChangeMade() {
		return changeMade;
	}

	/**
	 * @param changeMade
	 *            boolean sets true if changes have been made since last step, false if not
	 */
	public void setChangeMade(boolean changeMade) {
		this.changeMade = changeMade;
	}

	// Returns the string value of the button text
	public String getEraserButtonText() {
		return butEraser.getText();
	}

	public File getImage() {
		return Image;
	}

	public MainFrame getParentFrame() {
		return parentFrame;
	}

	void butDustFrame_actionPerformed(ActionEvent e) {
		imagePanel.dustImage();
	}

	void this_mouseEntered(MouseEvent e) {
		imagePanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void setFillSpot(FillSpot fillSpot) {
		this.fillSpot = fillSpot;
	}

	public FillSpot getFillSpot() {
		return fillSpot;
	}

	public void setButZoomIn(JButton butZoomIn) {
		this.butZoomIn = butZoomIn;
	}

	public JButton getButZoomIn() {
		return butZoomIn;
	}

	public void setButZoomOut(JButton butZoomOut) {
		this.butZoomOut = butZoomOut;
	}

	public JButton getButZoomOut() {
		return butZoomOut;
	}

	public void setButLight(JButton butLight) {
		this.butLight = butLight;
	}

	public JButton getButLight() {
		return butLight;
	}

	public void setButDarker(JButton butDarker) {
		this.butDarker = butDarker;
	}

	public JButton getButDarker() {
		return butDarker;
	}

	public void setButSharpen(JButton butSharpen) {
		this.butSharpen = butSharpen;
	}

	public JButton getButSharpen() {
		return butSharpen;
	}

	public void setButBlur(JButton butBlur) {
		this.butBlur = butBlur;
	}

	public JButton getButBlur() {
		return butBlur;
	}

	public void setButDone(JButton butDone) {
		this.butDone = butDone;
	}

	public JButton getButDone() {
		return butDone;
	}
}

class ImageManipFrame_butClearImage_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_butClearImage_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butClearImage_actionPerformed(e);
	}
}

class ImageManipFrame_butPencil_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_butPencil_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butPencil_actionPerformed(e);
	}
}

class ImageManipFrame_butUndoPencil_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_butUndoPencil_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butUndoPencil_actionPerformed(e);
		adaptee.butPencil_actionPerformed(e);
	}
}

class ImageManipFrame_butFillSpot_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_butFillSpot_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butFillSpot_actionPerformed(e);
	}
}

class ImageManipFrame_butUndoFillSpot_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_butUndoFillSpot_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butUndoFillSpot_actionPerformed(e);
		adaptee.butFillSpot_actionPerformed(e);
	}
}

class ImageManipFrame_butQuit_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_butQuit_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butQuit_actionPerformed(e);
	}
}

class ImageManipFrame_menuFile_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_menuFile_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.menuFile_actionPerformed(e);
	}
}

class ImageManipFrame_MenuItemUndo_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_MenuItemUndo_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemUndo_actionPerformed(e);
	}
}

class ImageManipFrame_butNext_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_butNext_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butNext_actionPerformed(e);
	}
}

class ImageManipFrame_butBack_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_butBack_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butBack_actionPerformed(e);
	}
}

class ImageManipFrame_butStartOver_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_butStartOver_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butStartOver_actionPerformed(e);
	}
}

class ImageManipFrame_butEraser_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_butEraser_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butEraser_actionPerformed(e);
	}
}

class ImageManipFrame_TextFieldThreshold_edges_keyAdapter extends java.awt.event.KeyAdapter {
	ImageManipFrame adaptee;

	ImageManipFrame_TextFieldThreshold_edges_keyAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e) {
		adaptee.TextFieldThreshold_edges_keyPressed(e);
	}
}

class ImageManipFrame_TextFieldDilation_radius_keyAdapter extends java.awt.event.KeyAdapter {
	ImageManipFrame adaptee;

	ImageManipFrame_TextFieldDilation_radius_keyAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e) {
		adaptee.TextFieldDilation_radius_keyPressed(e);
	}
}

class ImageManipFrame_TextFieldNoise_radius_keyAdapter extends java.awt.event.KeyAdapter {
	ImageManipFrame adaptee;

	ImageManipFrame_TextFieldNoise_radius_keyAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e) {
		adaptee.TextFieldNoise_radius_keyPressed(e);
	}
}

class ImageManipFrame_TextFieldThreshold_edges_focusAdapter extends java.awt.event.FocusAdapter {
	ImageManipFrame adaptee;

	ImageManipFrame_TextFieldThreshold_edges_focusAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e) {
		adaptee.TextFieldThreshold_edges_focusLost(e);
	}
}

class ImageManipFrame_TextFieldDilation_radius_focusAdapter extends java.awt.event.FocusAdapter {
	ImageManipFrame adaptee;

	ImageManipFrame_TextFieldDilation_radius_focusAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e) {
		adaptee.TextFieldDilation_radius_focusLost(e);
	}
}

class ImageManipFrame_TextFieldNoise_radius_focusAdapter extends java.awt.event.FocusAdapter {
	ImageManipFrame adaptee;

	ImageManipFrame_TextFieldNoise_radius_focusAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e) {
		adaptee.TextFieldNoise_radius_focusLost(e);
	}
}

class ImageManipFrame_butDustFrame_actionAdapter implements java.awt.event.ActionListener {
	ImageManipFrame adaptee;

	ImageManipFrame_butDustFrame_actionAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butDustFrame_actionPerformed(e);
	}
}

class ImageManipFrame_this_windowAdapter extends java.awt.event.WindowAdapter {
	ImageManipFrame adaptee;

	ImageManipFrame_this_windowAdapter(ImageManipFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void windowClosing(WindowEvent e) {
		adaptee.this_windowClosing(e);
	}
}
