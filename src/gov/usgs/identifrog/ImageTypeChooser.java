package gov.usgs.identifrog;

/**
 * <p>Title: ImageTypeChooser.java </p>
 * <p>Description: panel for the save image as dialog</p>
 * <p>This software is released into the public domain.</p>
 * @author Steven P. Miller from <b>IdentiFrog</b> <i>2005</i>
 */

import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

/* ImagePreview.java is a 1.4 example used by FileChooserDemo2.java. */

@SuppressWarnings("serial")
public class ImageTypeChooser extends JComponent {
	private static final String jpg = "JPEG";
	// private static final String png = "PNG";
	JRadioButton radioButtonJpg = new JRadioButton();
	JRadioButton radioButtonPng = new JRadioButton();
	ButtonGroup buttonGroup = new ButtonGroup();
	GridLayout gridLayout2 = new GridLayout(5, 1);
	String choice = jpg;
	JLabel labFileType = new JLabel();
	FlowLayout FlowLayout1 = new FlowLayout();

	public ImageTypeChooser() {
		try {
			jbInit();
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeException(e);
		}
	}

	private void jbInit() throws Exception {
		setLayout(FlowLayout1);
		labFileType.setHorizontalAlignment(SwingConstants.CENTER);
		labFileType.setHorizontalTextPosition(SwingConstants.CENTER);
		labFileType.setText("Choose File Type...");
	}

	public String getChoice() {
		return choice;
	}

	public void setChoice(String choice) {
		this.choice = choice;
	}

}
