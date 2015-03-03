package gov.usgs.identifrog;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * <p>
 * Title: ChoiceDialog
 * </p>
 * <p>
 * Description: generates A Choice Dialog Box with Yes or No options
 * </p>
 * <p>
 * This software is released into the public domain.
 * </p>
 * 
 * @author: Steven Miller from <b>IdentiFrog</b> <i>2005</i>
 */

// ChoiceDialog is an integer dialog box returning 0 for yes and 1 for no
@SuppressWarnings("serial")
public class ChoiceDialog extends JPanel {
	ChoiceDialog() {
		// this.setAlwaysOnTop(true);//Future version of Java

	}// end constructor

	/**
	 * Takes a String to be displayed as a message returns an integer (0 for yes and 1 for no)
	 */

	public static int choiceMessage(String message) {
		// JOptionPane choicePane = new JOptionPane();
		String m = message;
		int returnValue = JOptionPane.showConfirmDialog(null, m, "Choose", JOptionPane.YES_NO_OPTION);
		// 0 for yes 1 for no
		return returnValue;

	}// end endchoiceMessage
}// end ChoiceDialog
