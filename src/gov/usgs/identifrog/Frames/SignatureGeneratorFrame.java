package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.DigSigThread;
import gov.usgs.identifrog.DigSignature;
import gov.usgs.identifrog.FillSpot;
import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.ImagePanel;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.extendedclasses.DilationSteppingSlider;
import gov.usgs.identifrog.extendedclasses.EdgeSteppingSlider;
import gov.usgs.identifrog.extendedclasses.NoiseSteppingSlider;
import gov.usgs.identifrog.signaturegenerator.SignatureFlowState;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>
 * Title: ImageManipFrame.java
 * <p>
 * Description: frame for image processing, Color Fingerprint and Spot
 * Extraction Tools
 * 
 * @author Oksana V. Kelly 2008
 * @author Oksana V. Kelly used image rotation/alignment by Steven P. Miller
 *         from <b>IdentiFrog</b> <i>2005</i>
 */
@SuppressWarnings("serial")
public class SignatureGeneratorFrame extends JDialog {
	//IMAGES
	private ImageIcon imageSave32 = new ImageIcon(MainFrame.class.getResource("/resources/IconFloppy32.png"));
	private ImageIcon imagePrevious32 = new ImageIcon(MainFrame.class.getResource("/resources/IconPrevious32.png"));
	private ImageIcon imageNext32 = new ImageIcon(MainFrame.class.getResource("/resources/IconNext32.png"));
	private ImageIcon imageUndo32 = new ImageIcon(MainFrame.class.getResource("/resources/IconUndo32.png"));
	private ImageIcon imageCancel32 = new ImageIcon(MainFrame.class.getResource("/resources/IconCancel32.png"));

	//IMAGE STEP EXAMPLES
	private ImageIcon imageExampleVent = new ImageIcon(MainFrame.class.getResource("/resources/exampleVent.gif"));
	private ImageIcon imageExampleDorsalLateral = new ImageIcon(MainFrame.class.getResource("/resources/exampleRidge.gif"));
	private ImageIcon imageExampleEyes = new ImageIcon(MainFrame.class.getResource("/resources/exampleEyeClicks.gif"));
	private ImageIcon imageExampleSnout = new ImageIcon(MainFrame.class.getResource("/resources/exampleSnout.gif"));
	private ImageIcon imageExampleTopY = new ImageIcon(MainFrame.class.getResource("/resources/exampleLSide.gif"));
	private ImageIcon imageExampleBottomY = new ImageIcon(MainFrame.class.getResource("/resources/exampleRSide.gif"));

	//WORKFLOW
	private int CURRENT_STEP = 0;
	private final int VENT_STEP = 0;
	private final int SNOUT_STEP = 1;
	private final int TOPY_STEP = 2;
	private final int BOTTOMY_STEP = 3;
	private final int EYE_STEP = 4;
	private final int DORSALLATERAL_STEP = 5;
	private final int SPOT_STEP = 6;
	private final int FINAL_STEP = SPOT_STEP; //> this step = saving

	private JButton butQuit = new JButton("Cancel Signature Generation");
	private JButton butBack = new JButton("Previous Step");
	private JButton butNext = new JButton("Next Step");
	private LinkedList<SignatureFlowState> signatureFlowStack = new LinkedList<SignatureFlowState>();

	//OLD GENERATOR
	private Preferences root = Preferences.userRoot();
	@SuppressWarnings("unused")
	private final Preferences node = root.node("edu/isu/aadis/defaults");
	public String installDir;
	private SliderListener listener = new SliderListener();
	//private BorderLayout borderLayout1 = new BorderLayout();
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

	// extract shape 1-14-09
	private JButton butExtract = new JButton();
	//private BorderLayout borderLayout2 = new BorderLayout();
	//private JMenuBar jMenuBar1 = new JMenuBar();
	private JMenu menuFile = new JMenu();
	private JMenu menuEdit = new JMenu();
	private JMenuItem MenuItemUndo = new JMenuItem();
	private JMenuItem MenuItemExit = new JMenuItem();
	public int step = 0;
	private boolean changeMade = false;
	private JButton butEraser = new JButton();
	private FrogEditor parentFrame;
	private SiteImage image;
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
	private JSlider SliderThreshold_edges = new EdgeSteppingSlider();
	private JSlider SliderDilation_radius = new DilationSteppingSlider();
	private JSlider SliderNoise_radius = new NoiseSteppingSlider();
	private JLabel SliderLabel_edges = new JLabel();
	private JLabel SliderLabel_radius = new JLabel();
	private JLabel SliderLabel_noise = new JLabel();
	private JLabel label_numSpots = new JLabel("Number of spots defined: 0");
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
	JButton butDustFrame = new JButton();
	FlowLayout flowLayout1 = new FlowLayout();
	private Color sliderToolBoxColor = new Color(224, 223, 227);
	public int imageInEllipse_width = 0;
	public int imageInEllipse_heigth = 0;

	private Icon iamgeRestart32 = new ImageIcon(MainFrame.class.getResource("/resources/IconRefresh32.png"));

	private JButton butClear = new JButton();
	private JButton butOverlayToggle = new JButton();
	private Icon imageErase32 = new ImageIcon(MainFrame.class.getResource("/resources/IconEraser32.png"));
	private JButton butDetect = new JButton();
	private Icon imageRadar32 = new ImageIcon(MainFrame.class.getResource("/resources/IconRadar32.png"));
	private JLabel directionsLabel;
	private JLabel exampleIconLabel;
	private JLabel exampleLabel;
	private JLabel directionsStepLabel;
	private JPanel authoringPanel;

	/**
	 * Creates a new Digital Signature Window using the specified parent and
	 * SiteImage.
	 * 
	 * @param parent
	 * @param imageFile
	 */
	public SignatureGeneratorFrame(FrogEditor parent, SiteImage image) {
		parentFrame = parent;
		this.image = image;
		//DbId = db_id;
		parentFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		try {
			init2();

			inflateDirectionsForStep(CURRENT_STEP);
			inflateAuthoringFlowStep(CURRENT_STEP);
			inflateAuthoringStep(CURRENT_STEP);
			//at this point we can import an old stack from disk
			setVisible(true);
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("ImageManipFrame failed init()", e);
		}
		parentFrame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Initializes Signature Generator 2's base-state UI.
	 */
	private void init2() {
		JPanel directionsPanel = new JPanel(new GridBagLayout()); //left side directions
		GridBagConstraints directionsConstraints = new GridBagConstraints();
		authoringPanel = new JPanel(); //main content
		JPanel authorflowPanel = new JPanel(); //bottom buttons for next, back, etc.

		//INSTRUCTIONS
		JLabel instructionsHeaderLabel = new JLabel("Instructions");
		directionsStepLabel = new JLabel();
		directionsLabel = new JLabel();
		setDirectionsText("Directions will be placed here.");
		exampleLabel = new JLabel("Example");
		exampleIconLabel = new JLabel("An example image will be shown here.");

		directionsConstraints.gridy++;
		directionsPanel.add(instructionsHeaderLabel, directionsConstraints);
		directionsConstraints.gridy++;

		directionsPanel.add(directionsStepLabel, directionsConstraints);
		directionsConstraints.gridy++;

		directionsPanel.add(directionsLabel, directionsConstraints);
		directionsConstraints.gridy++;

		directionsPanel.add(exampleLabel, directionsConstraints);
		directionsConstraints.gridy++;
		directionsConstraints.weighty = 1;
		directionsConstraints.fill = GridBagConstraints.BOTH;
		directionsPanel.add(exampleIconLabel, directionsConstraints);

		//AUTHOR FLOW
		butBack.setIcon(imagePrevious32);
		butQuit.setIcon(imageCancel32);
		butNext.setIcon(imageNext32);
		butNext.setHorizontalTextPosition(SwingConstants.LEFT);
		
		butNext.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				advanceStep(getStepSerializedValues());
			}
		});
		butBack.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				previousStep();
			}
		});

		authorflowPanel.setLayout(new BoxLayout(authorflowPanel, BoxLayout.LINE_AXIS));
		authorflowPanel.add(butBack);
		authorflowPanel.add(Box.createHorizontalGlue());
		authorflowPanel.add(butQuit);
		authorflowPanel.add(Box.createHorizontalGlue());
		authorflowPanel.add(butNext);

		JSplitPane contentFlowSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, authoringPanel, authorflowPanel);
		JSplitPane directionsContentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, directionsPanel, contentFlowSplitPane);

		contentFlowSplitPane.setResizeWeight(1.0);
		directionsContentSplitPane.setResizeWeight(1.0);
		add(directionsContentSplitPane);

		setPreferredSize(new Dimension(1280,720));
		pack();
		contentFlowSplitPane.setDividerLocation(0.9);
		directionsContentSplitPane.setDividerLocation(0.1);

		setModal(true);
	}

	/**
	 * Serializes the user input from the current step into a list of avlues that will be pushed onto the stack when advancing the step.
	 * @return
	 */
	protected ArrayList<Point2D> getStepSerializedValues() {

		return new ArrayList<>();
	}

	/**
	 * Sets the directions panel up for the specified step.
	 * 
	 * @param step
	 *            Step to setup.
	 */
	private void inflateDirectionsForStep(int step) {
		exampleIconLabel.setText("");
		switch (step) {
		case VENT_STEP:
			directionsLabel.setText("Click on the vent of the frog.");
			exampleIconLabel.setIcon(imageExampleVent);
			break;
		case SNOUT_STEP:
			directionsLabel.setText("Click on the vent of the frog.");
			exampleIconLabel.setIcon(imageExampleSnout);
			break;
		case TOPY_STEP:
			directionsLabel.setText("Place the line at the leftmost part of the main frog body.");
			exampleIconLabel.setIcon(imageExampleTopY);
			break;
		case BOTTOMY_STEP:
			directionsLabel.setText("Place the line at the rightmost part of the main frog body.");
			exampleIconLabel.setIcon(imageExampleBottomY);
			break;
		case EYE_STEP:
			directionsLabel.setText("Click on the lateral part of the frog's eyes (leftmost part of eye, relative to the image)");
			exampleIconLabel.setIcon(imageExampleEyes);
			break;
		case DORSALLATERAL_STEP:
			directionsLabel.setText("Drag the handles to the inner parts of the dorsal later of the frog.");
			exampleIconLabel.setIcon(imageExampleDorsalLateral);
			break;
		case SPOT_STEP:
			directionsLabel
					.setText("Draw the boundary of the spots of the frog, then fill them in. Confirm that the displayed frog is accurate, then click Save to generate the signature.");
			exampleIconLabel.setIcon(null);
			exampleIconLabel.setText("Step has no image right now.");
			break;
		default:
			directionsLabel.setText("Unknown step: " + step);
			exampleIconLabel.setText("¯\\_(ツ)_/¯");
			exampleIconLabel.setIcon(null);
		}
	}

	/**
	 * Advances the signature flow forward one step, pushing values onto the
	 * stack and then inflating the next step.
	 */
	private void advanceStep(ArrayList<Point2D> stackItems) {
		CURRENT_STEP++;
		if (CURRENT_STEP > FINAL_STEP) {
			//Saving.
			showMessage("Signature will save here");
			return;
		}
		addItemsToStack(stackItems);
		inflateDirectionsForStep(CURRENT_STEP);
		inflateAuthoringFlowStep(CURRENT_STEP);
		inflateAuthoringStep(CURRENT_STEP);
	}

	/**
	 * Inflates the Authoring Flow (back, cancel, next) buttons for the specified step.
	 * @param Step to configure panel for
	 */
	private void inflateAuthoringFlowStep(int step) {
		switch (step) {
		case VENT_STEP:
			butBack.setText("Previous Step");
			butBack.setVisible(false);
			butBack.setEnabled(false);
			butBack.setIcon(imagePrevious32);

			butNext.setText("Auto Advancing");
			butNext.setToolTipText("Signature Generator will automatically advance when this step is completed");
			butNext.setVisible(true);
			butNext.setEnabled(false);
			butNext.setIcon(imageNext32);
			break;
		case SNOUT_STEP:
			butBack.setText("Previous Step");
			butBack.setVisible(true);
			butBack.setEnabled(true);
			butBack.setIcon(imagePrevious32);

			butNext.setText("Auto Advancing");
			butNext.setToolTipText("Signature Generator will automatically advance when this step is completed");
			butNext.setVisible(true);
			butNext.setEnabled(false);
			butNext.setIcon(imageNext32);
			break;
		case TOPY_STEP:
			butBack.setText("Previous Step");
			butBack.setVisible(true);
			butBack.setEnabled(true);
			butBack.setIcon(imagePrevious32);

			butNext.setText("Auto Advancing");
			butNext.setToolTipText("Signature Generator will automatically advance when this step is completed");
			butNext.setVisible(true);
			butNext.setEnabled(false);
			butNext.setIcon(imageNext32);
			break;
		case BOTTOMY_STEP:
			butBack.setText("Previous Step");
			butBack.setVisible(true);
			butBack.setEnabled(true);
			butBack.setIcon(imagePrevious32);

			butNext.setText("Auto Advancing");
			butNext.setToolTipText("Signature Generator will automatically advance when this step is completed");
			butNext.setVisible(true);
			butNext.setEnabled(false);
			butNext.setIcon(imageNext32);
			break;
		case EYE_STEP:
			butBack.setText("Previous Step");
			butBack.setVisible(true);
			butBack.setEnabled(true);
			butBack.setIcon(imagePrevious32);

			butNext.setText("Auto Advancing");
			butNext.setToolTipText("Signature Generator will automatically advance when this step is completed");
			butNext.setVisible(true);
			butNext.setEnabled(false);
			butNext.setIcon(imageNext32);
			break;
		case DORSALLATERAL_STEP:
			butBack.setText("Previous Step");
			butBack.setVisible(true);
			butBack.setEnabled(true);
			butBack.setIcon(imagePrevious32);

			butNext.setText("Next Step");
			butNext.setToolTipText("Click when finished aligning the box");
			butNext.setVisible(true);
			butNext.setEnabled(true);
			butNext.setIcon(imageNext32);
			break;
		case SPOT_STEP:
			butBack.setText("Previous Step");
			butBack.setVisible(true);
			butBack.setEnabled(true);
			butBack.setIcon(imagePrevious32);

			butNext.setText("Generate Signature");
			butNext.setToolTipText("Click to generate signature");
			butNext.setVisible(true);
			butNext.setEnabled(true);
			butNext.setIcon(imageSave32);
			break;
		default:
			directionsLabel.setText("Unknown step: " + step);
			exampleIconLabel.setText("¯\\_(ツ)_/¯");
			exampleIconLabel.setIcon(null);
		}
	}

	/**
	 * Pushes a set of values onto the stack as a SignatureFlowState object.
	 * 
	 * @param stackItems
	 */
	private void addItemsToStack(ArrayList<Point2D> stackItems) {
		signatureFlowStack.add(new SignatureFlowState(stackItems));
	}

	/**
	 * Inflates the authoring area for the current step.
	 * 
	 * @param step Step to inflate
	 */
	private void inflateAuthoringStep(int step) {
		// TODO Auto-generated method stub
		authoringPanel.removeAll();
		JButton advanceButton = new JButton("Debug: Advance to "+(CURRENT_STEP+2));
		advanceButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				advanceStep(getStepSerializedValues());
			}
		});
		authoringPanel.add(advanceButton);
	}

	/**
	 * Placeholder method for GUI mockup
	 * 
	 * @param string
	 */
	private void showMessage(String string) {
		JOptionPane.showMessageDialog(null, string);
	}

	/**
	 * Moves the signature flow back one step, popping the last item off the
	 * stack, and then reinflating the interface by iterating through the
	 * signature flow.
	 */
	private void previousStep() {

	}

	private void setDirectionsText(String text) {
		directionsLabel.setText("<html>" + text + "</html>");
	}

	// graphical user interface initialization
	private void init() throws Exception {
		setModal(true);
		installDir = XMLFrogDatabase.getMainFolder();
		setIconImages(IdentiFrog.ICONS);
		//setJMenuBar(jMenuBar1);
		imagePanel = new ImagePanel(SignatureGeneratorFrame.this, image);
		digSignature = new DigSignature();
		getContentPane().setLayout(new BorderLayout());
		panelStatus.setLayout(new BorderLayout());
		panelStatus.setFont(new java.awt.Font("MS Sans Serif", 0, 14)); // 11
		panelStatus.setPreferredSize(new Dimension(44, 55)); // 35
		butClearImage.setPreferredSize(new Dimension(90, 28));
		butClearImage.setText("Clear Image");
		butClearImage.addActionListener(new ImageManipFrame_butClearImage_actionAdapter(this));
		butPencil.setPreferredSize(new Dimension(41, 41));
		butPencil.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("IconPencil32.png")));
		//butPencil.setText("");// Pencil
		butPencil.setToolTipText("Pencil");
		butPencil.addActionListener(new ImageManipFrame_butPencil_actionAdapter(this));
		butUndoPencil.setPreferredSize(new Dimension(41, 41));
		butUndoPencil.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("/resources/IconUndo32.png")));
		//butUndoPencil.setText("");
		butUndoPencil.addActionListener(new ImageManipFrame_butUndoPencil_actionAdapter(this));
		butUndoPencil.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		butUndoPencil.setToolTipText("Erase Pencil");
		butFillSpot.setPreferredSize(new Dimension(41, 41));
		butFillSpot.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("IconFill32.png")));
		//butFillSpot.setText("");
		butFillSpot.setToolTipText("Spot Filler");
		butFillSpot.addActionListener(new ImageManipFrame_butFillSpot_actionAdapter(this));
		butUndoFillSpot.setPreferredSize(new Dimension(41, 41));
		butUndoFillSpot.setIcon(imageUndo32);
		//butUndoFillSpot.setText("");
		butUndoFillSpot.setToolTipText("Undo Fill Spot");
		butUndoFillSpot.addActionListener(new ImageManipFrame_butUndoFillSpot_actionAdapter(this));

		butClear.setPreferredSize(new Dimension(41, 41));
		butClear.setIcon(imageErase32);
		//butUndoFillSpot.setText("");
		butClear.setToolTipText("Wipe Spot Extraction Slate");
		butClear.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				//imagePanel.
				SliderThreshold_edges.setEnabled(false);
				SliderNoise_radius.setEnabled(false);
				SliderDilation_radius.setEnabled(false);
				SliderLabel_edges.setEnabled(false);
				SliderLabel_noise.setEnabled(false);
				SliderLabel_radius.setEnabled(false);
				TextFieldDilation_radius.setEnabled(false);
				TextFieldNoise_radius.setEnabled(false);
				TextFieldThreshold_edges.setEnabled(false);

				imagePanel.standardRectColor = IdentiFrog.copyImage(imagePanel.standardRectColorCopy); //make copy
				//imagePanel.standardRectGray = new BufferedImage(ImagePanel.rect_width, ImagePanel.rect_height, BufferedImage.TYPE_3BYTE_BGR);
				imagePanel.standardRectBinary = new BufferedImage(ImagePanel.rect_width, ImagePanel.rect_height, BufferedImage.TYPE_3BYTE_BGR);
				imagePanel.standardRectEdgesDilated = new BufferedImage(ImagePanel.rect_width, ImagePanel.rect_height, BufferedImage.TYPE_3BYTE_BGR);
				imagePanel.standardRectFilled = new BufferedImage(ImagePanel.rect_width, ImagePanel.rect_height, BufferedImage.TYPE_3BYTE_BGR);
				setNumSpotsUI(0);
				imagePanel.filledSpotNumber = 0;
				repaint();
			}
		});

		butDetect.setPreferredSize(new Dimension(41, 41));
		butDetect.setIcon(imageRadar32);
		//butUndoFillSpot.setText("");
		butDetect.setToolTipText("Detect Edges");
		butDetect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				SliderThreshold_edges.setEnabled(true);
				SliderNoise_radius.setEnabled(true);
				SliderDilation_radius.setEnabled(true);
				SliderLabel_edges.setEnabled(true);
				SliderLabel_noise.setEnabled(true);
				SliderLabel_radius.setEnabled(true);
				TextFieldDilation_radius.setEnabled(true);
				TextFieldNoise_radius.setEnabled(true);
				TextFieldThreshold_edges.setEnabled(true);

				imagePanel.detectEdges();
				repaint();
			}
		});

		/*
		 * butOverlayToggle.setPreferredSize(new Dimension(41, 41));
		 * butOverlayToggle.setIcon(imageUndo32); //butUndoFillSpot.setText("");
		 * butOverlayToggle.setToolTipText("Toggle Spot Extraction Overlay");
		 * butOverlayToggle.addActionListener(new ActionListener() {
		 * 
		 * @Override public void actionPerformed(ActionEvent arg0) { // TODO
		 * Auto-generated method stub
		 * 
		 * } });
		 */

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
		TextAreaStatus.setMinimumSize(new Dimension(332, 80));
		TextAreaStatus.setPreferredSize(new Dimension(332, 80));
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
		butStartOver.setIcon(new ImageIcon(MainFrame.class.getResource("/resources/IconRefresh32.png")));
		butStartOver.setText("Restart");
		butStartOver.addActionListener(new ImageManipFrame_butStartOver_actionAdapter(this));
		butQuit.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("/resources/IconCancel32.png")));
		butQuit.setText("Quit");
		butQuit.addActionListener(new ImageManipFrame_butQuit_actionAdapter(this));
		butBack.setEnabled(false);
		butBack.setIcon(imagePrevious32);
		butBack.setText("Back");
		butBack.addActionListener(new ImageManipFrame_butBack_actionAdapter(this));
		butNext.setIcon(imageNext32);
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
		butEraser.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("/resources/IconBlank32.png")));
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
		 * butDustFrame.setIcon(new ImageIcon(ImageManipFrame.class.getResource(
		 * "duster.gif"))); butDustFrame.setText("Dust Image");
		 * butDustFrame.addActionListener(new
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
		SliderDilation_radius.setMajorTickSpacing(5);
		SliderLabel_radius.setPreferredSize(new Dimension(220, 13));
		SliderLabel_radius.setMaximumSize(new Dimension(220, 13));
		SliderLabel_radius.setMinimumSize(new Dimension(220, 13));
		SliderLabel_radius.setFont(new Font(curFont.getFontName(), curFont.getStyle(), 11));
		SliderLabel_radius.setText("Dilation Radius");
		SliderNoise_radius.setOrientation(JSlider.HORIZONTAL);
		/*
		 * SliderNoise_radius.setMaximum(100); SliderNoise_radius.setMinimum(0);
		 */
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
		SliderTools.add(butClear);
		SliderTools.add(butDetect);
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
		//colorFingerprintTool.add(butOverlayToggle);
		SpotExtractionPanelTools.setVisible(false);
		SpotExtractionPanelTools.setBackground(sliderToolBoxColor);
		SpotExtractionPanelTools.setLayout(null);
		colorFingerprintTool.setBounds(0, 40, 147, 80);
		SpotExtractionPanelTools.add(colorFingerprintTool);
		SliderTools.setBounds(0, 261, 295, 180);
		SpotExtractionPanelTools.add(SliderTools);
		SliderNoiseTool.setBounds(0, 481, 280, 120);
		label_numSpots.setBounds(0, 510, 280, 120);
		SpotExtractionPanelTools.add(label_numSpots);
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
		labelExampleRidge.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("exampleRidge.gif")));
		labelExampleEyeClicks.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("exampleEyeClicks.gif")));
		labelExampleSnout.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("exampleSnout.gif")));
		labelExampleVent.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("exampleVent.gif")));
		labelExampleLSide.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("exampleLSide.gif")));
		labelExampleRSide.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("exampleRSide.gif")));
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
		//System.gc(); // Garbage Collector
		//ArrayList<Frog> localFrogs = XMLFrogDatabase.getFrogs();
		//localFrogs.remove(localFrogs.size() - 1);
		//parentFrame.getFrogData().setFrogs(localFrogs);
		IdentiFrog.LOGGER.writeError("Quitting the Signature Generation tool");
		this.image = null;
		//parentFrame.updateCells();
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
		Image pencilimage = Toolkit.getDefaultToolkit().getImage(SignatureGeneratorFrame.class.getResource("MousePencel.png"));
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

	/**
	 * Deletes all temp files, removes all interface elements and then cleans up
	 * memory.
	 */
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
			try {
				imagePanel.mapOntoRectangle();
			} catch (ArrayIndexOutOfBoundsException ex) {
				IdentiFrog.LOGGER.writeExceptionWithMessage("User likely drew an invalid rectangle: ", ex);
				JOptionPane.showMessageDialog(this,
						"Error: Invalid selection.\nMake sure you have drawn a figure without overlaps\nand the line are all within the image.",
						"Error generating dorsal image", JOptionPane.ERROR_MESSAGE);
				return;
			}

			ExamplePanelContainer.setVisible(true);
			ExampleVent.setVisible(false);
			ExampleSnout.setVisible(false);
			ExampleLSide.setVisible(false);
			ExampleRSide.setVisible(false);
			ExampleRidge.setVisible(false);
			inPanelToolsInstructions.setVisible(true);
			TextAreaStep.setText("Step 7 of 7: BINARY FINGERPRINT CREATION");
			TextAreaStatus
					.setText("Use the Tools to extract a spot pattern into\nthe Binary Fingerprint.\nDo not include any spots that do not have a\nmajority in the image.\nClick Save to save the signature.");
			TextAreaTools
					.setText("Use the Spot Filler and click inside each\nspot contour.\nFilled spots are added to the Binary Fingerprint.\n\nAdjust the Edge Threshold and Dilation Radius\nfor edge detection.\n\nUse the Pencil on the Color Fingerprint to\nconnect any broken contours.");
			setSpotExtractionPanelTools(true);
			imagePanel.setImageinEllipse(false);
			// ready to map fingerprint onto Standard Rectangle

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
			butNext.setText("Save");
			butNext.setIcon(imageSave32);
			// imagePanel.setNoise_slider_Active(false);
			increaseStep();
			imagePanel.newOperationStep();
			imagePanel.repaint();
			break;
		case 2:// when final Next button pressed
				// binaryImage 256x128 is used to create signature
				//parentFrame.setChangesMade(true);
			image.generateHash();
			String fileName = image.isProcessed() ? image.getImageFileName() : image.createUniqueDBFilename();
			imagePanel.exportDownsampledImage(fileName);
			BufferedImage binaryImage = imagePanel.saveBinaryImage(XMLFrogDatabase.getBinaryFolder(), fileName);

			String sigFileLocation = XMLFrogDatabase.getSignaturesFolder() + fileName;
			if (binaryImage != null) {
				digSignature.makeSignature(binaryImage, sigFileLocation);
				DigSigThread digSigThread = new DigSigThread(new File(sigFileLocation));
				digSigThread.start();
			} else {
				IdentiFrog.LOGGER.writeError("Could not obtain binary image, variable was null (in case 2)");
				JOptionPane.showMessageDialog(null, "Could not obtain binary image");
			}
			image.setSignatureGenerated(true);
			image.processImageIntoDB(true);
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
			butNext.setText("Next");
			butNext.setIcon(imageNext32);

			//IdentiFrog.LOGGER.writeMessage("case 1 before back " + step);
			decreaseStep();
			//IdentiFrog.LOGGER.writeMessage("case 1 afer back " + step);
			break;
		case 2: // from binary fingerprint creation back to clicking on the frog's eyes
			//IdentiFrog.LOGGER.writeMessage("back case 2");
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
			butNext.setText("Next");
			butNext.setIcon(imageNext32);
			butStartOver.setEnabled(true);
			//IdentiFrog.LOGGER.writeMessage("case 2 before back " + step);
			decreaseStep();
			//IdentiFrog.LOGGER.writeMessage("case 2 afer back " + step);
			imagePanel.backOperationStep();
			imagePanel.deleteTempFiles();
			imagePanel.repaint();
			setNumSpotsUI(0);
			break;
		}
	}

	protected void setNumSpotsUI(int i) {
		label_numSpots.setText("Number of spots defined: " + i);
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
		//IdentiFrog.LOGGER.writeMessage("incr step " + step);
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
			butEraser.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("Eraser.gif")));
		} else {// Mask mode
			// imagePanel.setMaskOn(false);
			imagePanel.setEraserOn(true);
			TextFieldThreshold_edges.setEnabled(false);
			SliderThreshold.setEnabled(false);
			imagePanel.setUndoPoint();
			TextAreaStatus.setText("Click and drag the eraser over an area you want to erase,\n" + "or click Next>>");
			butEraser.setText("Mask");
			butEraser.setIcon(new ImageIcon(SignatureGeneratorFrame.class.getResource("mask.gif")));
		}
	}

	/**
	 * This class listens for changes to the sliders. It redraws the interfaces.
	 * Requires sliders to be at certain modulos to prevent the system from
	 * locking up.
	 * 
	 * @author mjperez
	 *
	 */
	class SliderListener implements ChangeListener {
		//private ThresholdWorker thresholdWorker = new ThresholdWorker();

		public void stateChanged(ChangeEvent e) {
			boolean redraw = true;
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
				if (SliderThreshold_edges.getValue() % 50 != 0) {
					redraw = false;
				}
			} else if (slider == SliderDilation_radius) {
				TextFieldDilation_radius.setText("" + SliderDilation_radius.getValue());
				imagePanel.setDilation_radius(SliderDilation_radius.getValue());
				imagePanel.setNoise_slider_Active(false);
				noise_radius_active = false;
				if (SliderDilation_radius.getValue() % 5 != 0) {
					redraw = false;
				}
			} else if (slider == SliderNoise_radius) {
				TextFieldNoise_radius.setText("" + SliderNoise_radius.getValue());
				imagePanel.setNoise_radius(SliderNoise_radius.getValue());
				imagePanel.setNoise_slider_Active(true);
				noise_radius_active = true;
			}
			//thresholdWorker.cancel(true);
			if (redraw) {
				new ThresholdWorker().execute();
			}
		}
	}

	/**
	 * @return int the value of the threshold slider
	 */
	public int getSliderThreshold() {
		return SliderThreshold_edges.getValue();
	}

	// WORKER
	private class ThresholdWorker extends SwingWorker<BufferedImage, Object> {
		// long-running code to be run in a worker thread
		@Override
		public BufferedImage doInBackground() throws Exception {
			@SuppressWarnings("static-access")
			BufferedImage img = new BufferedImage(imagePanel.rect_width, imagePanel.rect_height, BufferedImage.TYPE_3BYTE_BGR);
			img = imagePanel.detectEdges();
			return img;
		} // end method doInBackground

		// code to run on the event dispatch thread when doInBackground returns
		@Override
		protected void done() {
			try {
				@SuppressWarnings("static-access")
				BufferedImage Img = new BufferedImage(imagePanel.rect_width, imagePanel.rect_height, BufferedImage.TYPE_3BYTE_BGR);
				Img = get();
				imagePanel.standardRectEdgesDilated = Img;
				imagePanel.repaint();
			} catch (InterruptedException ignore) {
			} catch (ExecutionException ex) {
				IdentiFrog.LOGGER.writeException(ex);
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
				JOptionPane.showMessageDialog(null, "Invalid Entry, Must be an integer between 1 - 300");
			} else {
				SliderThreshold_edges.setValue(c);
				imagePanel.setThreshold_edges(c);
			}
		} catch (Exception ex) {
			IdentiFrog.LOGGER.writeException(ex);
			TextFieldThreshold_edges.setText("" + c);
			JOptionPane.showMessageDialog(null, "Invalid Entry, Must be an integer between 1 - 300");
		}
	}

	private void setSliderFromText_dilation() {
		int w = SliderDilation_radius.getValue();
		try {
			w = Integer.parseInt(TextFieldDilation_radius.getText());
			if (w < 0 || w > 10) {
				JOptionPane.showMessageDialog(null, "Invalid Entry, Must be an integer between 0 - 30");
			} else {
				SliderDilation_radius.setValue(w);
				imagePanel.setDilation_radius(w);
			}
		} catch (Exception ex) {
			TextFieldDilation_radius.setText("" + w);
			JOptionPane.showMessageDialog(null, "Invalid Entry, Must be an integer between 0 - 30");
		}
	}

	private void setSliderFromText_noise() {
		int w = SliderNoise_radius.getValue();
		try {
			w = Integer.parseInt(TextFieldNoise_radius.getText());
			if (w < 0 || w > 100) {
				JOptionPane.showMessageDialog(null, "Invalid Entry, Must be an integer between 0 - 100");
			} else {
				SliderNoise_radius.setValue(w);
				imagePanel.setNoise_radius(w);
			}
		} catch (Exception ex) {
			IdentiFrog.LOGGER.writeException(ex);
			TextFieldNoise_radius.setText("" + w);
			JOptionPane.showMessageDialog(null, "Invalid Entry, Must be an integer between 0 - 100");
		}
	}

	/**
	 * @return boolean returns true if changes have been made since last step,
	 *         false if not
	 */
	public boolean isChangeMade() {
		return changeMade;
	}

	/**
	 * @param changeMade
	 *            boolean sets true if changes have been made since last step,
	 *            false if not
	 */
	public void setChangeMade(boolean changeMade) {
		this.changeMade = changeMade;
	}

	// Returns the string value of the button text
	public String getEraserButtonText() {
		return butEraser.getText();
	}

	public SiteImage getImage() {
		return image;
	}

	public FrogEditor getParentFrame() {
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
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_butClearImage_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butClearImage_actionPerformed(e);
	}
}

class ImageManipFrame_butPencil_actionAdapter implements java.awt.event.ActionListener {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_butPencil_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butPencil_actionPerformed(e);
	}
}

class ImageManipFrame_butUndoPencil_actionAdapter implements java.awt.event.ActionListener {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_butUndoPencil_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butUndoPencil_actionPerformed(e);
		adaptee.butPencil_actionPerformed(e);
	}
}

class ImageManipFrame_butFillSpot_actionAdapter implements java.awt.event.ActionListener {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_butFillSpot_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butFillSpot_actionPerformed(e);
	}
}

class ImageManipFrame_butUndoFillSpot_actionAdapter implements java.awt.event.ActionListener {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_butUndoFillSpot_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butUndoFillSpot_actionPerformed(e);
		adaptee.butFillSpot_actionPerformed(e);
	}
}

class ImageManipFrame_butQuit_actionAdapter implements java.awt.event.ActionListener {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_butQuit_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butQuit_actionPerformed(e);
	}
}

class ImageManipFrame_menuFile_actionAdapter implements java.awt.event.ActionListener {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_menuFile_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.menuFile_actionPerformed(e);
	}
}

class ImageManipFrame_MenuItemUndo_actionAdapter implements java.awt.event.ActionListener {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_MenuItemUndo_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.MenuItemUndo_actionPerformed(e);
	}
}

class ImageManipFrame_butNext_actionAdapter implements java.awt.event.ActionListener {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_butNext_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butNext_actionPerformed(e);
	}
}

class ImageManipFrame_butBack_actionAdapter implements java.awt.event.ActionListener {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_butBack_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butBack_actionPerformed(e);
	}
}

class ImageManipFrame_butStartOver_actionAdapter implements java.awt.event.ActionListener {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_butStartOver_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butStartOver_actionPerformed(e);
	}
}

class ImageManipFrame_butEraser_actionAdapter implements java.awt.event.ActionListener {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_butEraser_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butEraser_actionPerformed(e);
	}
}

class ImageManipFrame_TextFieldThreshold_edges_keyAdapter extends java.awt.event.KeyAdapter {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_TextFieldThreshold_edges_keyAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e) {
		adaptee.TextFieldThreshold_edges_keyPressed(e);
	}
}

class ImageManipFrame_TextFieldDilation_radius_keyAdapter extends java.awt.event.KeyAdapter {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_TextFieldDilation_radius_keyAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e) {
		adaptee.TextFieldDilation_radius_keyPressed(e);
	}
}

class ImageManipFrame_TextFieldNoise_radius_keyAdapter extends java.awt.event.KeyAdapter {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_TextFieldNoise_radius_keyAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e) {
		adaptee.TextFieldNoise_radius_keyPressed(e);
	}
}

class ImageManipFrame_TextFieldThreshold_edges_focusAdapter extends java.awt.event.FocusAdapter {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_TextFieldThreshold_edges_focusAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e) {
		adaptee.TextFieldThreshold_edges_focusLost(e);
	}
}

class ImageManipFrame_TextFieldDilation_radius_focusAdapter extends java.awt.event.FocusAdapter {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_TextFieldDilation_radius_focusAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e) {
		adaptee.TextFieldDilation_radius_focusLost(e);
	}
}

class ImageManipFrame_TextFieldNoise_radius_focusAdapter extends java.awt.event.FocusAdapter {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_TextFieldNoise_radius_focusAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e) {
		adaptee.TextFieldNoise_radius_focusLost(e);
	}
}

class ImageManipFrame_butDustFrame_actionAdapter implements java.awt.event.ActionListener {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_butDustFrame_actionAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butDustFrame_actionPerformed(e);
	}
}

class ImageManipFrame_this_windowAdapter extends java.awt.event.WindowAdapter {
	SignatureGeneratorFrame adaptee;

	ImageManipFrame_this_windowAdapter(SignatureGeneratorFrame adaptee) {
		this.adaptee = adaptee;
	}

	public void windowClosing(WindowEvent e) {
		adaptee.this_windowClosing(e);
	}
}
