package gov.usgs.identifrog;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * <p>
 * Title: SaveAsDialog.java
 * </p>
 * <p>
 * Description: opens file browser to save a file
 * </p>
 * 
 * @author Oksana Kelly 2010
 *         <p>
 *         This software is released into the public domain.
 *         </p>
 */
@SuppressWarnings("serial")
public class SaveAsDialog extends JDialog {
	private JPanel ReadDialogInteriorPanel = new JPanel();
	private JFileChooser mFileChooser = new JFileChooser();
	private ExtensionFileFilter filter = new ExtensionFileFilter();

	private int Result;
	private String Name;
	private String Title = "Save As...";
	private Frame parentFrame;
	private String fileType = "INP";
	private MarkFileTypeChooser markFileTypeChooser = new MarkFileTypeChooser();

	/**
	 * This constructor takes a string indicating the starting directory along with all other Dialog
	 * parameters
	 */
	public SaveAsDialog(Frame frame, String title, boolean modal, String WorkDir) {
		super(frame, title, modal);
		parentFrame = frame;
		Title = title;

		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Initialization of the dialog UI
	 */
	private void jbInit() throws Exception {

		filter.addExtension(fileType);
		filter.setDescription("MARK Files");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		getContentPane().setLayout(null);
		ReadDialogInteriorPanel.setLayout(null);
		mFileChooser.setMinimumSize(new Dimension(500, 300));
		mFileChooser.setFileFilter(filter);
		mFileChooser.setBounds(new Rectangle(7, 5, 522, 301));
		mFileChooser.setDialogTitle(Title);
		ReadDialogInteriorPanel.setMinimumSize(new Dimension(450, 250));
		ReadDialogInteriorPanel.setPreferredSize(new Dimension(550, 350));
		ReadDialogInteriorPanel.setBounds(new Rectangle(0, 0, 541, 645));
		ReadDialogInteriorPanel.add(mFileChooser, null);
		mFileChooser.setAccessory(markFileTypeChooser);
		getContentPane().add(ReadDialogInteriorPanel, null);
		Result = mFileChooser.showSaveDialog(parentFrame);
		fileType = markFileTypeChooser.getChoice();

		// Determine which button was clicked to close the dialog
		switch (Result) {
			case JFileChooser.APPROVE_OPTION:
				// Approve Save was clicked
				Name = mFileChooser.getSelectedFile().getPath();
				dispose();
				break;
			case JFileChooser.CANCEL_OPTION:
				// Cancel or the close-dialog icon was clicked
				Name = null;
				dispose();
				break;
			case JFileChooser.ERROR_OPTION:
				// The selection process did not complete successfully
				Name = "Error";
				dispose();
				break;
		}
	}

	/**
	 *Returns the path and name of the selected file, "null" if canceled, "Error" if there was an
	 * error
	 */
	@Override
	public String getName() {
		return Name;
	}

	/**
	 *Returns the path and name of the selected file, "null" if canceled, "Error" if there was an
	 * error
	 */
	public String getType() {
		return fileType;
	}

}
