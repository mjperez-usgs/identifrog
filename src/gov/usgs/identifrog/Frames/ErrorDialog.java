package gov.usgs.identifrog.Frames;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * <p>
 * Title: ChoiceDialog.java
 * <p>
 * Description: Error dialog box with error message
 * 
 * @author Hidayatullah Ahsan (Code Cleanup)
 * @author Steven P. Miller (<b>IdentiFrog Team</b>) 2005
 */
@SuppressWarnings("serial")
public class ErrorDialog extends JPanel {
	public ErrorDialog() {
		JOptionPane.showMessageDialog(null, "Something went wrong.", "ERROR", JOptionPane.ERROR_MESSAGE);
	}

	public ErrorDialog(String message) {
		JOptionPane.showMessageDialog(null, message, "ERROR", JOptionPane.ERROR_MESSAGE);
	}
}