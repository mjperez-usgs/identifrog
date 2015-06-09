package gov.usgs.identifrog;

/**
 * <p>Title: MarkFileTypeChooser.java </p>
 * <p>Description: displays ".INP" files, "save as" dialog</p>
 * <p>This software is released into the public domain.</p>
 * @author Oksana Kelly 2010
 */

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class MarkFileTypeChooser extends JComponent {

	private static final String inp = "INP";
	JRadioButton radioButtonINP = new JRadioButton();
	ButtonGroup buttonGroup = new ButtonGroup();
	GridLayout gridLayout2 = new GridLayout(5, 1);
	String choice = inp;
	JLabel labFileType = new JLabel();
	FlowLayout FlowLayout1 = new FlowLayout();

	public MarkFileTypeChooser() {
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
