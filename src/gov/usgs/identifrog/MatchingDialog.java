package gov.usgs.identifrog;

import gov.usgs.identifrog.Handlers.FolderHandler;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JPanel;

/**
 * <p>
 * Title: MatchingDialog.java
 * <p>
 * Description: matching Dialog 2008
 * 
 * @author Oksana V. Kelly 2008
 * @author Steven P. Miller for IdentiFrog Team 2005
 */

public class MatchingDialog {
	JPanel panelContent = new JPanel();
	BorderLayout borderLayout1 = new BorderLayout();
	private MainFrame parentFrame;
	private File imageFile;
	private int frogDBID;
	private boolean open;
	protected static ChoiceDialog choiceDialog = new ChoiceDialog();
	ImageViewer imageViewer;
	private FolderHandler fh;

	public MatchingDialog(MainFrame frame, File ImageFile, int FrogDbId, FolderHandler fh) {
		parentFrame = frame;
		imageFile = ImageFile;
		this.fh = fh;
		setFrogDBID(FrogDbId);

		try {
			init();
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeMessage("MatchingDialog.MatchingDialog() Exception");
			IdentiFrog.LOGGER.writeException(e);
		}
	}

	/**
	 * Component Initialization
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		OpenImageViewer("Search for Match for this Query Frog", imageFile);
	}

	protected void OpenImageViewer(String title, File imageFile) {
		if (imageFile.exists()) {
			imageViewer = new ImageViewer(fh,MatchingDialog.this, parentFrame, title, false, imageFile, false);
			open = true;
			if (imageViewer.shouldShow()) {
				imageViewer.setVisible(true);
			}
		} else {
			if (ChoiceDialog.choiceMessage("Cannot find Digital Image File for " + title + "\nWould you like to create one now?") == 0) {
				// parentFrame.digSigAction();
				parentFrame.setButtonsOn(true);
				open = false;
			}// if
			else {
				parentFrame.setButtonsOn(true);
			}
		}// else
	}

	protected boolean isOpen() {
		return open;
	}

	protected void setOpen(boolean open) {
		this.open = open;
		parentFrame.setButtonsOn(!open);
	}

	public void setFrogDBID(int frogDBID) {
		this.frogDBID = frogDBID;
	}

	public int getFrogDBID() {
		return frogDBID;
	}
}
