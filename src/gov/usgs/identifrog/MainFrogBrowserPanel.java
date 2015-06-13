package gov.usgs.identifrog;

import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.Frames.ErrorDialog;
import gov.usgs.identifrog.Frames.FrogEditor;
import gov.usgs.identifrog.Frames.MainFrame;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * <p>
 * Title: WorkingAreaPanel.java
 * <p>
 * Description: Lays out tables in AADIS GUI.
 * 
 * @author Oksana V. Kelly 2008
 * @author Steven P. Miller for IdentiFrog Team 2005
 */
@SuppressWarnings("serial")
public class MainFrogBrowserPanel extends JPanel {
	//private ArrayList<Frog> frogs = new ArrayList<Frog>();
	private static final int PREFERRED_WIDTH = 180;
	private static final int PREFERED_HEIGHT = 80;
	private static final int STANDARD_WIDTH = 75;
	private static final int STANDARD_HEIGHT = 16;
	private Preferences root = Preferences.userRoot();
	//private final Preferences node = root.node("edu/isu/aadis/defaults");
	private JScrollPane ScrollPaneDBPrintout;
	private JPanel panelNavigation = new JPanel();
	private JPanel panelButtons = new JPanel();
	//private JTable dbTable = new JTable();
	private JTextField textRange = new JTextField();

	private int columns;
	private int maxPageRows;
	private int page = 1;
	private boolean isMaxPages = false;
	private boolean showThumbnails = true;
	private boolean wasAscending;
	private boolean wasAllImagesDisplay;
	private int lastType;
	// private RetrieveTable retrieveTable;
	private TopTenMatches topTenMatches;
	private Object[][] cells;
	private Object[][] pageCells;
	private MainFrame parentFrame;
	private boolean firstChange = true;
	// SEARCH DESCRIMINATORS (SEX or SNOUT SPOT)
	public boolean discriminatorSex = false;
	public boolean additDiscriminator = false;
	public boolean includeQueryImg = false;
	private static int BLANK_FROG_ID = -1; //this is what shows up for frog id in the list if no rows exist
	private Object[][] emptyFrogCells = { { BLANK_FROG_ID, new ImageIcon(this.getClass().getResource("/resources/IconFrog.png")), "-", "-", "-", "-", "-", new Integer(0), new Double(0), new Double(0), "-", "-", "-", "-", "-" } };
	private Object[][] emptyEntryPersonCells = { { "-", "-", "-" } };
	private Object[][] emptyObserverCells = { { "-", "-", "-" } };
	private Object[][] emptyLocationCells = { { "-", "-", "-", new Double(0), new Double(0), "-", "-", "-" } };
	private ImageIcon frogIcon = new ImageIcon(this.getClass().getResource("/resources/IconFrog.png"));
	private Object[][] emptyMatchingCells = { { "-", frogIcon, "-", new Double(0), "-", "-" } };
	private JLabel labSort = new JLabel();
	// private JComboBox ComboBoxSortBy;
	// private JCheckBox CheckBoxAscending = new JCheckBox();
	private JLabel labViewBy = new JLabel();
	private Object[] viewByList = { "Frog", "Observer", "Capture Location", "Recorder" };
	private JComboBox ComboBoxViewBy = new JComboBox(viewByList);
	private int lastFrogDBID = -1;
	private double Threshold;
	private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

	//
	// Task: IconEdit32_disabled and enabled via working area selection listeners, passing to parentFrame.
	//
	
	
	private JButton btnPrevious = new JButton("Previous Page", new ImageIcon(MainFrogBrowserPanel.class.getResource("IconButtonPrevious32.png")));
	private JButton btnNext = new JButton("Next Page", new ImageIcon(MainFrogBrowserPanel.class.getResource("IconButtonNext32.png")));
	private JCheckBox chkAllImages = new JCheckBox("All Images", true);

	
	public MainFrogBrowserPanel(MainFrame frame) {
		parentFrame = frame;
		try {
			init();
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeException(e);
		}
	}

	void init() throws Exception {
		//maxPageRows = node.getInt("maxPageRows", 10);
		//Threshold = node.getDouble("threshold", 75.0);
		topTenMatches = new TopTenMatches(parentFrame);
		// AHSAN
		//updateCells(currentMatrixType, 5, false, true);
		setLayout(new BorderLayout());
		textRange.setBorder(null);
		textRange.setEditable(false);
		textRange.setMargin(new Insets(3, 3, 3, 3));
		textRange.setSelectionStart(11);
		btnPrevious.setEnabled(false);
		//btnPrevious.addActionListener(new WorkingAreaPanel_butBack_actionAdapter(this));
		//btnNext.addActionListener(new WorkingAreaPanel_butNext_actionAdapter(this));
		//chkAllImages.addActionListener(new WorkingAreaPanel_CheckBoxAllImages_actionAdapter(this));
		labViewBy.setText("View Table:");
		ComboBoxViewBy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//ComboBoxViewBy_actionPerformed(e);
			}
		});
		// panelNavigation.add(labViewBy, null);
		// panelNavigation.add(ComboBoxViewBy, null);
		// AHSAN
		panelNavigation.add(chkAllImages, null);
		// panelNavigation.add(labSort, null);
		// panelNavigation.add(ComboBoxSortBy, null);
		// panelNavigation.add(CheckBoxAscending, null);
		panelNavigation.add(btnPrevious, null);
		panelNavigation.add(btnNext, null);
		panelNavigation.add(textRange, null);
		// center table values
		renderer.setHorizontalAlignment(JLabel.CENTER);
		/*dbTable.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
			           //-- select a row
			           int idx = dbTable.rowAtPoint(e.getPoint());
			           dbTable.getSelectionModel().setSelectionInterval(idx, idx);
			           //---
			           //Get currently selected frog ID.
			           int localID = getSelectedFrog_Id();
				   		if (localID < 0) {
				   			IdentiFrog.LOGGER.writeError("Locally selected ID is null when right click, but one should be selected already!");
				   			return;
				   		}
				   		Frog localFrog = XMLFrogDatabase.searchFrogByID(localID);
			           
			           
			            //codeModel.setSelectedFileName(table.getValueAt(table.getSelectedRow(), 0).toString());
			            JPopupMenu popup = new JPopupMenu();
						JMenuItem popupAddImage, popupEditInfo, popupDeleteFrog, popupSearch;
						popupAddImage = new JMenuItem("Add image to this frog");
						popupAddImage.addActionListener(new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								IdentiFrog.LOGGER.writeMessage("MainFrame: Right click > Add image to this frog on ID: "+idx);
							}
						});
						popupEditInfo = new JMenuItem("Edit frog information");
						popupEditInfo.addActionListener(new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent e) {
								IdentiFrog.LOGGER.writeMessage("Opening Frog Editor via Right Click Menu: "+localFrog.toString());
								FrogEditor editFrogWindow = new FrogEditor(MainFrogBrowserPanel.this.parentFrame, "Edit Frog", localFrog);
								editFrogWindow.pack();
								editFrogWindow.setVisible(true);
							}
						});
						popupDeleteFrog = new JMenuItem("Search for matching frog");
						popupSearch = new JMenuItem("Delete this frog");
						popup.add(popupAddImage);
						popup.add(popupEditInfo);
						popup.add(popupDeleteFrog);
						popup.add(popupSearch);
			            popup.show(e.getComponent(), e.getX(), e.getY());
			       } else{
						tableMouseClicked(e);
			       }
			    }
		});*/
		// //These Items Don't Work
		labSort.setEnabled(false);
		// ComboBoxSortBy.setEnabled(false);
		// CheckBoxAscending.setEnabled(false);
		chkAllImages.setEnabled(false);
		chkAllImages.setSelected(false);
		// //
		ScrollPaneDBPrintout = new JScrollPane();
		this.add(ScrollPaneDBPrintout, BorderLayout.CENTER);
		this.add(panelNavigation, BorderLayout.NORTH);
		this.add(panelButtons, BorderLayout.SOUTH);
		textRange.setColumns(25);
	}
}