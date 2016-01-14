package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.IdentiFrog;

import java.awt.BorderLayout;
import java.awt.Dialog;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
/**
 * Displays the "About IdentiFrog" window.
 * @author mjperez
 *
 */
public class AboutDialog extends JDialog {
	JLabel infoLabel;

	public AboutDialog(JFrame callingWindow) {
		setupWindow();
		this.setLocationRelativeTo(callingWindow);
		this.setVisible(true);
	}

	private void setupWindow() {
		this.setTitle("About IdentiFrog");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImages(IdentiFrog.ICONS);

		JPanel aboutPanel = new JPanel(new BorderLayout());
		infoLabel = new JLabel("<html>IdentiFrog<br> Version " + IdentiFrog.HR_VERSION + " Build " + IdentiFrog.INT_VERSION + "- "
				+ IdentiFrog.BUILD_DATE + "<br>Developed by:" + "<br>- Michael Perez (2014, 2015)" + "<br>- Hidayatullah Ahsan (2011)"
				+ "<br>- Oksana V. Kelly (2008)" + "<br>- Steven P. Miller (2005)"
				+ "<br>Source code available at http://github.com/mjperez-usgs/IdentiFrog" + "<br>" + "<br>Uses ini4j: http://ini4j.sourceforge.net"
				+ "<br>Uses json-simple: https://code.google.com/p/json-simple/"
				+ "<br>Uses Apache Commons-io: http://commons.apache.org/proper/commons-io/");
		aboutPanel.add(infoLabel, BorderLayout.NORTH);

		aboutPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(aboutPanel);
		this.pack();
	}
}
