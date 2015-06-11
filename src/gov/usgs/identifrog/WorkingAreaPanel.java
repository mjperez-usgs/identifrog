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
public class WorkingAreaPanel extends JPanel {
	//private ArrayList<Frog> frogs = new ArrayList<Frog>();
	private static final int PREFERRED_WIDTH = 180;
	private static final int PREFERED_HEIGHT = 80;
	private static final int STANDARD_WIDTH = 75;
	private static final int STANDARD_HEIGHT = 16;
	private static final int FROG = 0;
	private static final int LOCATION = 1;
	private static final int MATCHING = 2;
	private static final int OBSERVER = 3;
	private static final int RECORDER = 4;
	private int currentMatrixType = FROG;
	private BorderLayout borderLayout1 = new BorderLayout();
	private Preferences root = Preferences.userRoot();
	private final Preferences node = root.node("edu/isu/aadis/defaults");
	private JScrollPane ScrollPaneDBPrintout;
	private JPanel panelNavigation = new JPanel();
	private JPanel panelButtons = new JPanel();
	private JTable dbTable = new JTable();
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
	
	
	private JButton btnPrevious = new JButton("Previous Page", new ImageIcon(WorkingAreaPanel.class.getResource("IconButtonPrevious32.png")));
	private JButton btnNext = new JButton("Next Page", new ImageIcon(WorkingAreaPanel.class.getResource("IconButtonNext32.png")));
	private JCheckBox chkAllImages = new JCheckBox("All Images", true);

	
	public WorkingAreaPanel(MainFrame frame) {
		parentFrame = frame;
		try {
			init();
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeException(e);
		}
	}

	void init() throws Exception {
		maxPageRows = node.getInt("maxPageRows", 10);
		Threshold = node.getDouble("threshold", 75.0);
		topTenMatches = new TopTenMatches(parentFrame);
		// AHSAN
		updateCells(currentMatrixType, 5, false, true);
		setLayout(borderLayout1);
		textRange.setBorder(null);
		textRange.setEditable(false);
		textRange.setMargin(new Insets(3, 3, 3, 3));
		textRange.setSelectionStart(11);
		btnPrevious.setEnabled(false);
		btnPrevious.addActionListener(new WorkingAreaPanel_butBack_actionAdapter(this));
		btnNext.addActionListener(new WorkingAreaPanel_butNext_actionAdapter(this));
		chkAllImages.addActionListener(new WorkingAreaPanel_CheckBoxAllImages_actionAdapter(this));
		labViewBy.setText("View Table:");
		ComboBoxViewBy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ComboBoxViewBy_actionPerformed(e);
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
		dbTable.addMouseListener(new java.awt.event.MouseAdapter() {
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
								FrogEditor editFrogWindow = new FrogEditor(WorkingAreaPanel.this.parentFrame, "Edit Frog", localFrog);
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
		});
		//this listens for changes to the selection, so when a row is clicked/selected
		dbTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
		    @Override
		    public void valueChanged(ListSelectionEvent event) {
		        if (dbTable.getSelectedRow() > -1) {
		            //enable buttons
		        	WorkingAreaPanel.this.parentFrame.setButtonState(true);
		        } else {
		        	//disable buttons
		        	WorkingAreaPanel.this.parentFrame.setButtonState(false);
		        }
		    }
		});
		// //These Items Don't Work
		labSort.setEnabled(false);
		// ComboBoxSortBy.setEnabled(false);
		// CheckBoxAscending.setEnabled(false);
		chkAllImages.setEnabled(false);
		chkAllImages.setSelected(false);
		// //
		ScrollPaneDBPrintout = new JScrollPane(dbTable);
		this.add(ScrollPaneDBPrintout, BorderLayout.CENTER);
		this.add(panelNavigation, BorderLayout.NORTH);
		this.add(panelButtons, BorderLayout.SOUTH);
		textRange.setColumns(25);
	}

	/**
	 * Updates cell data in the table. If this method returns false, the table view should not be updated.
	 * @return boolean indicating if the table should be updated or not.
	 */
	public boolean updateCells() {
		return updateCells(currentMatrixType);
	}

	public boolean updateCells(int matrixType) {
		return updateCells(matrixType, lastType, wasAscending, wasAllImagesDisplay);
	}

	public boolean updateCells(int sortType, boolean ascending, boolean allImages) {
		return updateCells(currentMatrixType, sortType, ascending, allImages);
	}

	public boolean updateCells(int matrixType, int sortType, boolean ascending, boolean allImagesDisp) {
		System.gc();
		Object[][] badCells = emptyFrogCells;
		try {
			if (matrixType == FROG) {
				currentMatrixType = FROG;
				//frogsData = parentFrame.getFrogData();
				// replace with frogsArray(fh)
				cells = XMLFrogDatabase.getFrogsArray(allImagesDisp);
				parentFrame.setButtonsOn(true);
				badCells = emptyFrogCells;
			/*} else if (matrixType == OBSERVER) {
				currentMatrixType = OBSERVER;
				cells = frogsData.personelArray("observer");
				badCells = emptyObserverCells;
				parentFrame.setButtonsOn(false);
			} else if (matrixType == LOCATION) {
				currentMatrixType = LOCATION;
				cells = frogsData.locationArray();
				badCells = emptyFrogCells;
				parentFrame.setButtonsOn(false);
			} else if (matrixType == RECORDER) {
				currentMatrixType = RECORDER;
				cells = frogsData.personelArray("recorder");
				badCells = emptyEntryPersonCells;
				parentFrame.setButtonsOn(false);*/
			} else if (matrixType == MATCHING) {
				currentMatrixType = MATCHING;
				// JOptionPane
				// .showMessageDialog(null, "Your Search Criteria: \n\n" + "Sex = " +
				// discriminatorSex + "\nSnout Spot = " + additDiscriminator +
				// "\nInclude Query Images = " + includeQueryImg);
				// cells = topTenMatches.getMatches(lastFrogDBID, sortType, ascending,
				// includeQueryImg, discriminatorSex, additDiscriminator);
				Frog myFrog = XMLFrogDatabase.searchFrogByID(this.getSelectedFrog_Id());
				// if (allImagesDisp) {
				//cells = topTenMatches.getMatches(frogsData, myFrog, sortType, ascending, includeQueryImg, discriminatorSex, additDiscriminator);
				if (cells == null) {
					//no matches
					return false;
				}
				// } else {
				//   cells = topTenMatches.getMatches(frogsData.getUniqueFrogs(), myFrog, sortType, ascending, includeQueryImg, discriminatorSex, additDiscriminator);
				// }
				badCells = emptyMatchingCells;
			} else {
				currentMatrixType = FROG;
			// replace with frogsArray(fh)
				cells = XMLFrogDatabase.getFrogsArray(allImagesDisp);
				parentFrame.setButtonsOn(true);
				badCells = emptyFrogCells;
			}
			lastType = sortType;
			wasAscending = ascending;
			wasAllImagesDisplay = allImagesDisp;
			File tnFile;
			refreshRows();
		} catch (Exception e) {
			if (matrixType == MATCHING) {
				// / new ErrorDialog("No matches within "+Threshold+"%.");
				new ErrorDialog("ERROR: A matching error.");
			}
			cells = badCells;
			try {
				setTable();
			} catch (Exception exc) {
				exc.printStackTrace();
				new ErrorDialog("ERROR: Table Error 001");
			}
		}
		try {
			setTable();
		} catch (Exception ex) {
			if (matrixType == MATCHING) {
				new ErrorDialog("No matches within " + Threshold + "%.");
			}
			cells = badCells;
			try {
				setTable();
			} catch (Exception exc) {
				exc.printStackTrace();
				new ErrorDialog("ERROR: Table Error 002");
				return false;
			}
		}
		return true;
	}

	private Object[][] getEmptyCells() {
		if (currentMatrixType == FROG) {
			return emptyFrogCells;
		} else if (currentMatrixType == LOCATION) {
			return emptyLocationCells;
		} else if (currentMatrixType == OBSERVER) {
			return emptyObserverCells;
		} else if (currentMatrixType == RECORDER) {
			return emptyEntryPersonCells;
		} else if (currentMatrixType == MATCHING) {
			return emptyMatchingCells;
		} else {
			currentMatrixType = FROG;
			return emptyFrogCells;
		}
	}

	private void setTable() throws Exception {
		try {
			setTable(currentMatrixType);
		} catch (Exception ex) {
			throw ex;
		}
	}

	private void setTable(int matrixType) throws Exception {
		if (cells.length <= maxPageRows) {
			isMaxPages = true;
			btnNext.setEnabled(false);
		}
		if (matrixType == FROG) {
			currentMatrixType = FROG;
			TableColumn Frogcolumn;
			try {
				// XXX
				dbTable.setModel(new localTableModel(FROG));
				/*
				 * set width for column "Row" and "Sex", "Mass", "Length" Frogcolumn =
				 * dbTable.getColumnModel().getColumn(0);
				 * Frogcolumn.setPreferredWidth(STANDARD_WIDTH-30); Frogcolumn =
				 * dbTable.getColumnModel().getColumn(3);
				 * Frogcolumn.setPreferredWidth(STANDARD_WIDTH-30); Frogcolumn =
				 * dbTable.getColumnModel().getColumn(7);
				 * Frogcolumn.setPreferredWidth(STANDARD_WIDTH-20); Frogcolumn =
				 * dbTable.getColumnModel().getColumn(8);
				 * Frogcolumn.setPreferredWidth(STANDARD_WIDTH-20);
				 */
			} catch (Exception exc) {
				// new ErrorDialog("Table error 3");
				throw exc;
			}
			// set width for column "Row" and "Sex", "Mass", "Length"
			Frogcolumn = dbTable.getColumnModel().getColumn(1);
			Frogcolumn.setPreferredWidth(STANDARD_WIDTH - 50);
			Frogcolumn = dbTable.getColumnModel().getColumn(3);
			Frogcolumn.setPreferredWidth(STANDARD_WIDTH - 40);
			Frogcolumn = dbTable.getColumnModel().getColumn(7);
			Frogcolumn.setPreferredWidth(STANDARD_WIDTH - 30);
			Frogcolumn = dbTable.getColumnModel().getColumn(8);
			Frogcolumn.setPreferredWidth(STANDARD_WIDTH - 30);
			Frogcolumn = dbTable.getColumnModel().getColumn(9);
			Frogcolumn.setPreferredWidth(STANDARD_WIDTH - 30);
			Frogcolumn = dbTable.getColumnModel().getColumn(10);
			Frogcolumn.setPreferredWidth(STANDARD_WIDTH + 15);
			Frogcolumn = dbTable.getColumnModel().getColumn(12);
			Frogcolumn.setMaxWidth(0);
			
			
			

		} else if (matrixType == LOCATION) {
			currentMatrixType = LOCATION;
			try {
				dbTable.setModel(new localTableModel(LOCATION));
			} catch (Exception exc) {
				// new ErrorDialog("Table Error CaptureLocation");
				throw exc;
			}
		} else if (matrixType == OBSERVER) {
			currentMatrixType = OBSERVER;
			try {
				dbTable.setModel(new localTableModel(OBSERVER));
			} catch (Exception exc) {
				// new ErrorDialog("Table error Observer");
				throw exc;
			}
		} else if (matrixType == RECORDER) {
			currentMatrixType = RECORDER;
			try {
				dbTable.setModel(new localTableModel(RECORDER));
			} catch (Exception exc) {
				// new ErrorDialog("Table error RECORDER");
				throw exc;
			}
		} else if (matrixType == MATCHING) {
			currentMatrixType = MATCHING;
			try {
				dbTable.setModel(new localTableModel(MATCHING));
			} catch (Exception exc) {
				// new ErrorDialog("Table error 5");
				throw exc;
			}
		} else {
			currentMatrixType = FROG;
			try {
				TableColumn Frogcolumn;
				dbTable.setModel(new localTableModel(FROG));
				// set width for column "Row" and "Sex", "Mass", "Length"
				Frogcolumn = dbTable.getColumnModel().getColumn(3);
				Frogcolumn.setPreferredWidth(STANDARD_WIDTH - 40);
				Frogcolumn = dbTable.getColumnModel().getColumn(7);
				Frogcolumn.setPreferredWidth(STANDARD_WIDTH - 30);
				Frogcolumn = dbTable.getColumnModel().getColumn(8);
				Frogcolumn.setPreferredWidth(STANDARD_WIDTH - 30);
				Frogcolumn = dbTable.getColumnModel().getColumn(9);
				Frogcolumn.setPreferredWidth(STANDARD_WIDTH - 30);
				Frogcolumn = dbTable.getColumnModel().getColumn(10);
				Frogcolumn.setPreferredWidth(STANDARD_WIDTH + 15);
				Frogcolumn = dbTable.getColumnModel().getColumn(12);
				Frogcolumn.setMaxWidth(0);
			} catch (Exception exc) {
				new ErrorDialog("Table error 6");
				throw exc;
			}
		}
		System.gc();
		dbTable.setDragEnabled(false);
		dbTable.getTableHeader().setReorderingAllowed(false);
		TableColumn column;
		if (showThumbnails) {
			column = dbTable.getColumnModel().getColumn(1); // image in column 1
			column.setPreferredWidth(PREFERRED_WIDTH);
			dbTable.setRowHeight(PREFERED_HEIGHT);
		} else {
			column = dbTable.getColumnModel().getColumn(1);
			column.setPreferredWidth(STANDARD_WIDTH);
			dbTable.setRowHeight(STANDARD_HEIGHT);
		}
		// center data in each column
		if (matrixType == FROG || matrixType == MATCHING) {
			column = dbTable.getColumnModel().getColumn(0);
			column.setCellRenderer(renderer);
			for (int i = 2; i < columns; ++i) {
				column = dbTable.getColumnModel().getColumn(i);
				column.setCellRenderer(renderer);
			}
		} else {
			for (int i = 0; i < columns; ++i) {
				column = dbTable.getColumnModel().getColumn(i);
				column.setCellRenderer(renderer);
			}
		}
		// Remove the first visible column without removing the underlying data
		dbTable.removeColumn(dbTable.getColumnModel().getColumn(0));
		
		
		//dbTable.setComponentPopupMenu(derp);
		
		// repaint to show table cell changes
		refreshTable();
	}

	class localTableModel extends AbstractTableModel {
		String[] ColumnNames;

		public localTableModel(int matrixType) {
			if (matrixType == FROG) {
				ColumnNames = new String[] { "Row", "Dorsal View", "Frog ID", "Gender", "Species", "Capture Date", "Location Name", "Survey ID", "Mass", "Length", "Addit. Discriminator", "Observer",
						"ID", "Entry Date", "Entry Person" };
			}
			if (matrixType == OBSERVER) {
				ColumnNames = new String[] { "Row", "Last Name", "First Name" };
			}
			if (matrixType == RECORDER) {
				ColumnNames = new String[] { "Row", "Last Name", "First Name" };
			}
			if (matrixType == LOCATION) {
				ColumnNames = new String[] { "Row", "Location Name", "Description", "Latitude", "Longitude", "Coordinate Type", "Datum", "Zone" };
			}
			if (matrixType == MATCHING) {
				ColumnNames = new String[] { "Row", "Frog", "Frog ID", "Score", "CaptureDate", "LocationName" };
			}
			columns = ColumnNames.length;
			rowOffset();
		}

		public int getRowCount() {
			return pageCells.length;
		} // getRowCount()

		public int getColumnCount() {
			return pageCells[0].length;
		} // getColumnCount()

		public String getColumnName(int c) {
			if (ColumnNames == null) {
				ColumnNames = new String[ColumnNames.length];
				// ColumnNames = ColumnNames;
			}
			return ColumnNames[c];
		} // getColumnName(int c)

		@SuppressWarnings("unchecked")
		/**
		 * This method tells Swing what to use to render the JTable cell. 
		 * e.g. if it should draw a string or an icon/image.
		 * Removing this method breaks empty frog cells and dorsal views
		 */
		public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
		}/*
			Class cls;
			try {
				cls = pageCells[0][c].getClass();
				return cls;
			} catch (Exception e) {
				try {
					cls = Class.forName("java.lang.String");
					return cls;
				} catch (Exception ex) {
					ex.printStackTrace();
					return null;
				}
			}
		}*/

		public boolean isCellEditable(int r, int c) {
			return false;
		} // isCellEditable(int r,int c)

		public Object getValueAt(int r, int c) {
			return pageCells[r][c];
		}

		public void setValueAt(int obj, int r, int c) {
			pageCells[r][c] = obj;
		}
	} // end table model class

	public void rowOffset() {
		int offset = (page - 1) * maxPageRows;
		int rows = maxPageRows;
		if (offset + maxPageRows >= cells.length) {
			rows = cells.length - offset;
			isMaxPages = true;
		} else {
			isMaxPages = false;
		}
		textRange.setText(" Rows: " + (offset + 1) + " - " + (offset + rows) + " of " + cells.length);
		int col = columns, startCol = 0;
		pageCells = new Object[rows][col];
		for (int r = 0; r < rows; r++) {
			for (int c = startCol; c < col; c++) {
				// IdentiFrog.LOGGER.writeMessage("rowOffset  col loop " + c + " r " + r +
				// " cells.length " + cells.length);
				if (c == 1 && (currentMatrixType == 0 || currentMatrixType == 2)) { // image
					// in
					// column
					// 1
					if (!showThumbnails || cells[r + offset][c] == null) {
						pageCells[r][c] = new ImageIcon(this.getClass().getClassLoader().getResource("/resources/IconFrog.png"));
					} else {
						pageCells[r][c] = new ImageIcon(cells[r + offset][c].toString());
					}
				} else {
					pageCells[r][c] = cells[r + offset][c];
				}
			}
		}
	}

	public void butBack_actionPerformed(ActionEvent e) {
		page--;
		if (page == 1) {
			btnPrevious.setEnabled(false);
		}
		btnNext.setEnabled(true);
		// rowOffset();
		try {
			setTable();
		} catch (Exception ex) {
			cells = getEmptyCells();
			try {
				setTable();
			} catch (Exception exc) {
				new ErrorDialog("Table error 7");
				exc.printStackTrace();
			}
		}
		// this.refreshTable();
	}

	public void butNext_actionPerformed(ActionEvent e) {
		page++;
		rowOffset();
		btnPrevious.setEnabled(true);
		if (isMaxPages) {
			btnNext.setEnabled(false);
		}
		try {
			setTable();
		} catch (Exception ex) {
			cells = getEmptyCells();
			try {
				setTable();
			} catch (Exception exc) {
				exc.printStackTrace();
				new ErrorDialog("Table error 8");
			}
		}
	}

	public void setShowThumbnails(boolean showThumbnails) {
		this.showThumbnails = showThumbnails;
		// rowOffset();
		try {
			setTable();
		} catch (Exception ex) {
			cells = getEmptyCells();
			try {
				setTable();
			} catch (Exception exc) {
				exc.printStackTrace();
				new ErrorDialog("Table error 9");
			}
		}
	}

	public void refreshRows() {
		setMaxPageRows(maxPageRows);
	}

	public void setMaxPageRows(int maxPageRows) {
		this.maxPageRows = maxPageRows;
		page = 1;
		btnPrevious.setEnabled(false);
		if (cells.length > maxPageRows) {
			isMaxPages = false;
			btnNext.setEnabled(true);
		}
		try {
			setTable();
		} catch (Exception ex) {
			cells = getEmptyCells();
			try {
				setTable();
			} catch (Exception exc) {
				exc.printStackTrace();
				new ErrorDialog("Table error 10");
			}
		}
	}

	public int getSelectedFrog_Id() {
		int myFrog_ID;
		int row = dbTable.getSelectedRow();
		int col = 12; // col = 12 is where former Frog_ID in GUI
		try {
			myFrog_ID = (Integer) pageCells[row][col];
			return myFrog_ID;
		} catch (Exception ex) {
			new ErrorDialog("You must select a row first.");
			return -1;
		}
	}

	public int getSelectedFrog_Id(boolean search) {
		int row = dbTable.getSelectedRow();
		int col = 2; // col = 12 is where former Frog_ID in GUI
		try {
			return (Integer) pageCells[row][col];
		} catch (Exception ex) {
			new ErrorDialog("You must select a row first.");
			return -1;
		}
	}

	@SuppressWarnings("unused")
	public Integer[] getSelectedRowDbId() {
		int[] row = dbTable.getSelectedRows();
		Integer[] dbid = new Integer[row.length];
		int col = 0;
		Integer[] ar = new Integer[row.length];
		for (int i = 0; i < row.length; i++) {
			ar[i] = row[i];
		}
		return ar;
		/*
		 * try { // access invisible column Row dbTable.getColumnModel().getColumn(0); // while
		 * (dbTable.getColumnName(col) != "Row") { // col++; // } int i = 0; for (int r = 0; r <
		 * row.length; r++) { dbid[r] = (Integer) pageCells[row[i]][col]; i++; } return dbid; }
		 * catch (Exception ex) { ex.printStackTrace(); new ErrorDialog("Error: Cannot read row.");
		 * return new Integer[] { new Integer(-1) }; }
		 */
	}

	public int getSelectedRowVisitID() {
		int row = dbTable.getSelectedRow();
		int col = 9; // 9 is visit_ID column
		try {
			// while (dbTable.getColumnName(col) != "VisitID") {
			// col++;
			// }
			Integer visid = (Integer) pageCells[row][col];
			return visid.intValue();
		} catch (Exception ex) {
			new ErrorDialog("You must select a row first.");
			return -1;
		}
	}

	public int getLastType() {
		return lastType;
	}

	public boolean isWasAscending() {
		return wasAscending;
	}

	public boolean isWasAllImages() {
		return wasAllImagesDisplay;
	}

	public void CheckBoxAllImages_actionPerformed(ActionEvent e) {
		updateCells(currentMatrixType, lastType, false, chkAllImages.isSelected()); // Ascending=false
		// for
		// now
		refreshRows();
	}

	private void refreshTable() {
		if (firstChange) {
			parentFrame.setSize(new Dimension(parentFrame.getWidth() + 1, parentFrame.getHeight()));
			firstChange = false;
		} else {
			parentFrame.setSize(new Dimension(parentFrame.getWidth() - 1, parentFrame.getHeight()));
			firstChange = true;
		}
	}

	/**
	 * When the table view in AADIS GUI is double clicked it opens a dialog box with the frog image.
	 * 
	 * @param e
	 *            the mouse event
	 */
	void tableMouseClicked(MouseEvent e) {
		// get frogs data
		if (e.getClickCount() == 2 && currentMatrixType == FROG) {
			// double click was detected
			IdentiFrog.LOGGER.writeMessage("WorkingAreaPanel.tableMouseClicked(e)");
			int localFrogID = getSelectedFrog_Id();
			if (localFrogID == BLANK_FROG_ID) {
				return;
			}
			IdentiFrog.LOGGER.writeMessage("\tSearch Frog ID = " + localFrogID);
			
			
			//Frog localFrog = XMLFrogDatabase.searchFrogByID(localFrogID);
			//String localImagename = XMLFrogDatabase.getDorsalFolder() + localFrog.getGenericImageName();
			//parentFrame.OpenImageViewer(localFrogID, "Frog ID: " + localFrog.getID() + " (" + localFrog.getGenericImageName() + ")", new File(localImagename), true);
		}
		
		if (e.isPopupTrigger() && currentMatrixType == FROG) {
			IdentiFrog.LOGGER.writeMessage("popup");
		}
	}

	void jButton1_actionPerformed(ActionEvent e) {
		refreshTable();
	}

	public JTable getDbTable() {
		return dbTable;
	}

	public File getImageFileFromSelectedRow() {
		int row = dbTable.getSelectedRow();
		int actualRow = (page - 1) * maxPageRows + row;
		String filename = cells[actualRow][1].toString(); // image in column 1
		return new File(filename);
	}

	void ComboBoxViewBy_actionPerformed(ActionEvent e) {
		if (ComboBoxViewBy.getSelectedItem().equals("Frog")) {
			showThumbnails = true;
			currentMatrixType = FROG;
			parentFrame.getButEdit().setEnabled(true);
			parentFrame.getMenuItemEdit().setEnabled(true);
			chkAllImages.setEnabled(true);
		}
		if (ComboBoxViewBy.getSelectedItem().equals("Observer")) {
			currentMatrixType = OBSERVER;
			showThumbnails = false;
			parentFrame.getButEdit().setEnabled(true);
			parentFrame.getMenuItemEdit().setEnabled(true);
			chkAllImages.setEnabled(false);
		}
		if (ComboBoxViewBy.getSelectedItem().equals("Capture Location")) {
			showThumbnails = false;
			currentMatrixType = LOCATION;
			parentFrame.getButEdit().setEnabled(true);
			parentFrame.getMenuItemEdit().setEnabled(true);
			chkAllImages.setEnabled(false);
		}
		if (ComboBoxViewBy.getSelectedItem().equals("Recorder")) {
			showThumbnails = false;
			currentMatrixType = RECORDER;
			parentFrame.getButEdit().setEnabled(true);
			parentFrame.getMenuItemEdit().setEnabled(true);
			chkAllImages.setEnabled(false);
		}
		try {
			if (updateCells()){
				refreshRows();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void showMatches() {
		// ComboBoxViewBy.setEnabled(false);
		currentMatrixType = MATCHING;
		try {
			if (updateCells())
			{
				refreshRows();
			}
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeMessage("WorkingAreaPanel.showMatches() Exception");
			IdentiFrog.LOGGER.writeException(e);
		}
	}

	public void doneMatches() {
		// ComboBoxViewBy.setEnabled(true);
		currentMatrixType = FROG;
		ComboBoxViewBy.setSelectedIndex(FROG);
	}

	// set Search Criteria
	public void setSearchBySex(boolean sexdiscr) {
		discriminatorSex = sexdiscr;
	}

	public void setSearchBySnoutSpot(boolean adddiscr) {
		additDiscriminator = adddiscr;
	}

	public void setIncludeQueryImages(boolean includeORNot) {
		includeQueryImg = includeORNot;
	}

	public double getThreshold() {
		return Threshold;
	}

	public void setThreshold(double Threshold) {
		this.Threshold = Threshold;
	}

	public void setLastFrogID(int lastFrogdbid) {
		lastFrogDBID = lastFrogdbid;
	}

	public int getLastFrogDbId() {
		return lastFrogDBID;
	}

	public int getMaxPageRows() {
		return maxPageRows;
	}
} // end WorkingAreaPanel Class

class WorkingAreaPanel_butBack_actionAdapter implements java.awt.event.ActionListener {
	WorkingAreaPanel adaptee;

	WorkingAreaPanel_butBack_actionAdapter(WorkingAreaPanel adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butBack_actionPerformed(e);
	}
}

class WorkingAreaPanel_butNext_actionAdapter implements java.awt.event.ActionListener {
	WorkingAreaPanel adaptee;

	WorkingAreaPanel_butNext_actionAdapter(WorkingAreaPanel adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butNext_actionPerformed(e);
	}
}

class WorkingAreaPanel_CheckBoxAllImages_actionAdapter implements java.awt.event.ActionListener {
	WorkingAreaPanel adaptee;

	WorkingAreaPanel_CheckBoxAllImages_actionAdapter(WorkingAreaPanel adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.CheckBoxAllImages_actionPerformed(e);
	}
}
/*
 * class WorkingAreaPanel_CheckBoxAscending_actionAdapter implements java.awt.event.ActionListener {
 * WorkingAreaPanel adaptee; WorkingAreaPanel_CheckBoxAscending_actionAdapter(WorkingAreaPanel
 * adaptee) { this.adaptee = adaptee; } public void actionPerformed(ActionEvent e) {
 * adaptee.CheckBoxAscending_actionPerformed(e); } } /*class
 * WorkingAreaPanel_ComboBoxSortBy_actionAdapter implements java.awt.event.ActionListener {
 * WorkingAreaPanel adaptee; WorkingAreaPanel_ComboBoxSortBy_actionAdapter(WorkingAreaPanel adaptee)
 * { this.adaptee = adaptee; } public void actionPerformed(ActionEvent e) {
 * adaptee.ComboBoxSortBy_actionPerformed(e); } }
 */
