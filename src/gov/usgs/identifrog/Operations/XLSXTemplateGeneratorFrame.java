package gov.usgs.identifrog.Operations;

import gov.usgs.identifrog.MainFrame;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class XLSXTemplateGeneratorFrame extends JDialog {
	public XLSXTemplateGeneratorFrame(MainFrame parent) {
		setupUI();
		
		setTitle("Batch Template Generator");
		setVisible(true);
	}

	private void setupUI() {
		JPanel panel = new JPanel();
		JLabel info = new JLabel("<html><div style=\"width:115px;\">The batch generator will preprocess a folder of images and create a template .xlsx file that can be filled out and then batch processed to import frogs.</div></html>");
		panel.add(info);
		add(panel);
	}
}
