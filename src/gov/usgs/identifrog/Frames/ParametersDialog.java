package gov.usgs.identifrog.Frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.WorkingAreaPanel;

/**
 * <p>
 * Title: ParametersDialog.java
 * <p>
 * Description: Sets variable parameters
 * 
 * @author Oksana V. Kelly 2008
 * @author Steven P. Miller for IdentiFrog Team 2005
 */

@SuppressWarnings("serial")
public class ParametersDialog extends JDialog {
	MainFrame parentFrame;
	WorkingAreaPanel workingAreaPanel;
	JPanel PanelContainer = new JPanel();
	BorderLayout borderLayout1 = new BorderLayout();
	JPanel PanelTabs = new JPanel();
	JPanel PanelButtons = new JPanel();
	JPanel dbPanel = new JPanel();
	GridLayout gridLayout1 = new GridLayout();
	JTabbedPane TabbedPaneParams = new JTabbedPane();
	JLabel labMaxPages = new JLabel();
	Object[] ComboBoxNumRowsList = { " 10 ", " 20 ", " 30 ", " 40 ", " 50 ", " 60 ", " 70 ", " 80 ", " 90 ", " 100 " };
	JComboBox ComboBoxNumRows = new JComboBox(ComboBoxNumRowsList);

	JCheckBox CheckBoxSexDiscr = new JCheckBox();
	JCheckBox CheckBoxSnoutSpotDiscr = new JCheckBox();
	JCheckBox CheckBoxQueryImg = new JCheckBox();

	JLabel labSearchCriteria = new JLabel();
	JTextField textThreshold = new JTextField();
	JLabel labPercent = new JLabel();
	JButton butClose = new JButton();

	public ParametersDialog(MainFrame frame, String title, boolean modal) {
		super(frame, title, modal);
		parentFrame = frame;
		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		PanelContainer.setLayout(borderLayout1);
		PanelTabs.setLayout(gridLayout1);
		labMaxPages.setText("Number of Rows per Page:");
		labMaxPages.setBounds(new Rectangle(20, 26, 168, 15));
		ComboBoxNumRows.addActionListener(new ParametersDialog_ComboBoxNumRows_actionAdapter(this));
		PanelContainer.setPreferredSize(new Dimension(240, 160));

		dbPanel.setLayout(null);
		ComboBoxNumRows.setBounds(new Rectangle(153, 24, 50, 21)); // 41 instead of 47
		IdentiFrog.LOGGER.writeMessage(" parentFrame.getWorkingAreaPanel().getMaxPageRows() " + parentFrame.getWorkingAreaPanel().getMaxPageRows());
		// ComboBoxNumRows.setSelectedIndex((int)(parentFrame.getWorkingAreaPanel().getMaxPageRows()/10));
		ComboBoxNumRows.setSelectedIndex(0);
		butClose.setBounds(new Rectangle(70, 80, 94, 35));
		butClose.setIcon(new ImageIcon(MainFrame.class.getResource("IconSave32.png")));
		butClose.setText("Save");
		butClose.addActionListener(new ParametersDialog_butClose_actionAdapter(this));
		getContentPane().add(PanelContainer, BorderLayout.CENTER);
		PanelContainer.add(PanelTabs, BorderLayout.CENTER);
		PanelContainer.add(PanelButtons, BorderLayout.SOUTH);
		PanelTabs.add(TabbedPaneParams, null);
		TabbedPaneParams.addTab("Rows Per Page", dbPanel);

		dbPanel.add(labMaxPages, null);
		dbPanel.add(ComboBoxNumRows, null);
		dbPanel.add(butClose, null);
	}

	void ComboBoxNumRows_actionPerformed(ActionEvent e) {
		parentFrame.setDBRows((ComboBoxNumRows.getSelectedIndex() + 1) * 10);
	}

	void butClose_actionPerformed(ActionEvent e) {
		dispose();
	}
}

class ParametersDialog_ComboBoxNumRows_actionAdapter implements java.awt.event.ActionListener {
	ParametersDialog adaptee;

	ParametersDialog_ComboBoxNumRows_actionAdapter(ParametersDialog adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.ComboBoxNumRows_actionPerformed(e);
	}
}

class ParametersDialog_butClose_actionAdapter implements java.awt.event.ActionListener {
	ParametersDialog adaptee;

	ParametersDialog_butClose_actionAdapter(ParametersDialog adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butClose_actionPerformed(e);
	}
}
