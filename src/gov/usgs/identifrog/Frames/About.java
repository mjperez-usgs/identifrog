package gov.usgs.identifrog.Frames;

import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class About extends JDialog {

	private JPanel jContentPane = null;

	/**
	 * @param owner
	 */
	public About(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(100, 100);
		setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridLayout gridLayout = new GridLayout(5, 1);

			jContentPane = new JPanel();
			jContentPane.setLayout(gridLayout);
		}
		return jContentPane;
	}
}