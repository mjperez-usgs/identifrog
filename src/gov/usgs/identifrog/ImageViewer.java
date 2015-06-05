package gov.usgs.identifrog;

import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.Frames.ErrorDialog;
import gov.usgs.identifrog.Frames.MainFrame;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * <p>
 * Title: ImageViewer.java
 * <p>
 * Description: The image viewer lets the user view the larger version of the thumbnail. In the
 * <b>view</b> mode it lets the user see additional images of the frog based on frog id, and in the
 * <b>search</b> mode it lets the user join the frogs together (based on the search performed by the
 * user).
 * 
 * @author Hidayatullah Ahsan 2011
 * @author Oksana V. Kelly 2008
 * @author Steven P. Miller for the IdentiFrog Team 2005
 */

@SuppressWarnings("serial")
public class ImageViewer extends JDialog {
	// //////// ATTRIBUTES //////////
	private File imageFile;
	private int frogID;

	protected JButton btnShowAllImages = new JButton("Show All Images");
	protected JButton btnMatch = new JButton("Join", new ImageIcon(ImageViewer.class.getResource("joinFrogMed.gif")));
	private JLabel imageLabel = new JLabel();
	private JPanel pnlImage = new JPanel();
	private JPanel pnlButton = new JPanel();
	private BorderLayout borderLayout = new BorderLayout();

	private MainFrame parentFrame;
	private MatchingDialog matchingDialog;
	
	private boolean view = true;
	private boolean searched = false;
	private boolean displayAllImages = false;
	
	private boolean shouldShow;

	// //////// CONSTRUCTORS //////////
	/**
	 * Overloaded Constructor
	 * 
	 * @param localFrogID
	 * @param frame
	 * @param title
	 * @param modal
	 * @param imageFile
	 * @param view
	 * @param displayAllImages
	 */
	public ImageViewer(int localFrogID, MainFrame frame, String title, boolean modal, File imageFile, boolean view, boolean displayAllImages) {
		super(frame, title, modal);
		this.shouldShow = true;
		this.frogID = localFrogID;
		parentFrame = frame;
		this.view = view;
		this.imageFile = imageFile;
		this.displayAllImages = displayAllImages;

		try {
			init();
			pack();
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeMessage("ImageViewer.ImageViewer(String,MainFrame,String,boolean,File,boolean,boolean) Exception");
			IdentiFrog.LOGGER.writeException(e);
		}
	}

	/**
	 * Overloaded Constructor
	 * 
	 * @param matchingDialog
	 * @param frame
	 * @param title
	 * @param modal
	 * @param imageFile
	 * @param view
	 */
	public ImageViewer(MatchingDialog matchingDialog, MainFrame frame, String title, boolean modal, File imageFile, boolean view) {
		super(frame, title, modal);

		this.matchingDialog = matchingDialog;
		parentFrame = frame;
		this.view = view;
		this.imageFile = imageFile;

		try {
			init();
			pack();
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeMessage("ImageViewer.ImageViewer(MatchingDialog,MainFrame,String,boolean,File,boolean) Exception");
			IdentiFrog.LOGGER.writeException(e);
		}
	}

	// //////// METHODS //////////
	/**
	 * Component Initialization
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		imageLabel.setIcon(new ImageIcon(imageFile.getAbsolutePath()));

		pnlImage.setMinimumSize(new Dimension(600, 256));
		pnlImage.setPreferredSize(new Dimension(600, 256));
		btnMatch.setToolTipText("Join this frog with a selected frog");

		getContentPane().setLayout(borderLayout);

		addWindowListener(new ImageViewer_this_windowAdapter(this));
		btnShowAllImages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnShowAllImages_actionPerformed(e);
			}
		});
		btnMatch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnMatch_actionPerformed(e);
			}
		});

		pnlButton.add(btnShowAllImages);
		pnlButton.add(btnMatch);
		pnlImage.add(imageLabel);
		getContentPane().add(pnlImage, BorderLayout.CENTER);
		getContentPane().add(pnlButton, BorderLayout.SOUTH);

		ViewOrMatch(view);
		setResizable(false);
	}

	protected void ViewOrMatch(boolean view) {
		if (view) {
			btnMatch.setVisible(false);
		} else {
			btnMatch.setVisible(true);
			parentFrame.getWorkingAreaPanel().showMatches();
			searched = true;
		}
		if (displayAllImages) {
			btnShowAllImages.setVisible(true);
		} else {
			btnShowAllImages.setVisible(false);
		}

	}

	/**
	 * Close the window if in <b>search</b> mode.
	 */
	void close() {
		if (matchingDialog != null) {
			matchingDialog.setOpen(false);
			parentFrame.getWorkingAreaPanel().doneMatches();
		}
		dispose();
	}

	// //////// ACTIONS //////////
	void btnShowAllImages_actionPerformed(ActionEvent e) {
		boolean additImg = false;
		int searchFrogID = parentFrame.getWorkingAreaPanel().getSelectedFrog_Id();
		Frog frog = XMLFrogDatabase.searchFrogByID(searchFrogID);
		searchFrogID = frog.getID();
		int searchFrogFormerID = frog.getID();
		ArrayList<Frog> localFrogs = parentFrame.getFrogData().getFrogs();
		for (int i = 0; i < localFrogs.size(); i++) {
			frog = localFrogs.get(i);
			if (frog.getID() == searchFrogID) {
				parentFrame.OpenImageViewer(frog.getID(), "Frog ID: " + frog.getID() + " (" + frog.getGenericImageName() + ")", new File(XMLFrogDatabase.getDorsalFolder() + frog.getGenericImageName()), false);
				additImg = true;
			}
		}
		if (!additImg) {
			JOptionPane.showMessageDialog(null, "There are no additional images for this individual.");
		}
	}

	void btnMatch_actionPerformed(ActionEvent e) {
		if (!searched) {
			new ErrorDialog("You must first search for matches.");
			return;
		}
		int joinFrogId = parentFrame.getWorkingAreaPanel().getSelectedFrog_Id(true);
		if (joinFrogId == -1) {
			new ErrorDialog("You must select a row first.");
			return;
		}
		parentFrame.setChangesMade(true);
		// XXX
		int frogIDList = new Integer(joinFrogId);
		System.err.println("Running debug code (10) in ImageViewer (match action performed) ");
		int frogIDImage = new Integer(10);
		
		IdentiFrog.LOGGER.writeMessage("DEBUG: Frog ID in the List " + frogIDList);
		IdentiFrog.LOGGER.writeMessage("DEBUG: Frog ID in the Image " + frogIDImage);
		
		// parentFrame.getFrogData().searchFrog(parentFrame.getMatchForg().getFormerID()).setID(new Integer(frogIDTemp).toString());
		
		if (frogIDList < frogIDImage) {
			XMLFrogDatabase.searchFrogByID(frogIDImage).setID(frogIDList);
		} else {
			XMLFrogDatabase.searchFrogByID(frogIDList).setID(frogIDList);
		}
		
		if (parentFrame.getChangesMade()) {
			XMLFrogDatabase.writeXMLFile();
		}
		close();
	}

	void this_windowClosing(WindowEvent e) {
		if (!view) {
			close();
		}
	}

	// //////// GETTERS //////////
	public int getFrogID() {
		return frogID;
	}

	// //////// SETTERS //////////
	public void setFrogID(int frogID) {
		this.frogID = frogID;
	}

	/**
	 * Indicates if this image viewer should show itself. If not, this object will be discarded. 
	 * This method exists because the image viewer is coupled with the match finder and will eventually be separated.
	 * @return if this image viewer should show
	 */
	public boolean shouldShow() {
		return true;
	}
}

class ImageViewer_this_windowAdapter extends java.awt.event.WindowAdapter {
	ImageViewer adaptee;

	ImageViewer_this_windowAdapter(ImageViewer adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void windowClosing(WindowEvent e) {
		adaptee.this_windowClosing(e);
	}
}