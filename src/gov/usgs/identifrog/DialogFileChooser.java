package gov.usgs.identifrog;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * <p>
 * Title: DialogImageFileChooser.java
 * </p>
 * <p>
 * Description: opens file browser to select an image, dialog to choose an existing file or a
 * convenient way to write to another directory
 * </p>
 * 
 * @author Steven P. Miller from <b>IdentiFrog</b> <i>2005</i>
 *         <p>
 *         This software is released into the public domain.
 *         </p>
 */

@SuppressWarnings("serial")
public class DialogFileChooser extends JDialog {
	ExtensionFileFilter filter;
	private JPanel ReadDialogInteriorPanel = new JPanel();
	private JFileChooser jFileChooser = new JFileChooser();
	private int Result;
	private String workingDirectory, Name;
	private String Title = "Open";
	private Frame parentFrame;
	private String fileType = "jpg";
	private ImageTypeChooser imageTypeChooser = new ImageTypeChooser();
	private JDialog parentDialog;

	/**
	 * Frame constructor, specifying a title, the starting directory, and the filter to use on files.
	 */
	public DialogFileChooser(Frame frame, String title, String WorkDir,ExtensionFileFilter f) {
		super(frame, title, true); //true is for modal
		parentFrame = frame;
		Title = title;
		filter = f;
		setWorkingDirectory(WorkDir);
		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Dialog parent constructor, specifying a title, the starting directory, and the filter to use on files.
	 */
	public DialogFileChooser(JDialog dialog, String title, String WorkDir, ExtensionFileFilter f) {
		super(dialog, title, true); //ture is for modal
		parentDialog = dialog;
		Title = title;
		filter = f;
		setWorkingDirectory(WorkDir);
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
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		getContentPane().setLayout(null);
		ReadDialogInteriorPanel.setLayout(null);
		jFileChooser.setMinimumSize(new Dimension(500, 300));
		jFileChooser.setFileFilter(filter);
		jFileChooser.setBounds(new Rectangle(7, 5, 522, 301));
		jFileChooser.setDialogTitle(Title);
		ReadDialogInteriorPanel.setMinimumSize(new Dimension(450, 250));
		ReadDialogInteriorPanel.setPreferredSize(new Dimension(550, 350));
		ReadDialogInteriorPanel.setBounds(new Rectangle(0, 0, 541, 645));
		ReadDialogInteriorPanel.add(jFileChooser, null);
		jFileChooser.setAccessory(imageTypeChooser);
		getContentPane().add(ReadDialogInteriorPanel, null);
		Result = jFileChooser.showOpenDialog((parentFrame == null) ? parentDialog : parentFrame);
		fileType = imageTypeChooser.getChoice();

		// Determine which button was clicked to close the dialog
		switch (Result) {
			case JFileChooser.APPROVE_OPTION:
				// Approve (Open or Save) was clicked
				Name = jFileChooser.getSelectedFile().getPath();
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
	public String getName() {
		return Name;
	}

	/**
	 *Returns the file type the user selected, eg PNG, JPEG
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * Sets initial working directory of FileChooser Dialog
	 */
	public void setWorkingDirectory(String S) {
		workingDirectory = S;
		File currentFile = new File(workingDirectory);
		jFileChooser.setCurrentDirectory(currentFile);
	}
	
	public static ExtensionFileFilter getImageFilter(){
		String[] mime = ImageIO.getWriterFormatNames();
		ExtensionFileFilter f = new ExtensionFileFilter();
		f.setDescription("Image files");
		for (int i = 0; i < mime.length; i++) {
			f.addExtension(mime[i]);
		}
		return f;
	}
	

}// end DialogFileChooser Class

