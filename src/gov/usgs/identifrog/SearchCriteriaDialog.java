package gov.usgs.identifrog;

import gov.usgs.identifrog.Frames.ErrorDialog;
import gov.usgs.identifrog.Frames.MainFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * <p>
 * Title: SearchCriteriaDialog.java
 * <p>
 * Description: Sets search Criteria
 * 
 * @author Oksana V. Kelly 2009
 */

@SuppressWarnings("serial")
public class SearchCriteriaDialog extends JDialog {
	MainFrame parentFrame;
	WorkingAreaPanel workingAreaPanel;
	JPanel PanelContainer = new JPanel();
	BorderLayout borderLayout1 = new BorderLayout();
	JPanel PanelTabs = new JPanel();
	JPanel PanelButtons = new JPanel();
	JPanel dbPanel = new JPanel();
	GridLayout gridLayout1 = new GridLayout();
	JTabbedPane TabbedPaneParams = new JTabbedPane();

	JCheckBox CheckBoxSexDiscr = new JCheckBox("Gender", false);
	JCheckBox CheckBoxAdditDiscr = new JCheckBox("Additional Discriminator", false);
	JCheckBox CheckBoxQueryImg = new JCheckBox("Include Images of Query Individual in Search", false);

	JLabel lblSearchCriteria = new JLabel("Define Search Criteria:");
	JTextField textThreshold = new JTextField();
	JButton btnClose = new JButton("Save", new ImageIcon(SearchCriteriaDialog.class.getResource("IconSave32.png")));

	public SearchCriteriaDialog(MainFrame frame, String title, boolean modal) {
		super(frame, title, modal);
		parentFrame = frame;
		try {
			init();
			pack();
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeMessage("SearchCriteriaDialog.SearchCriteriaDialog() Exception");
			IdentiFrog.LOGGER.writeException(e);
		}
	}

	private void init() throws Exception {
		PanelContainer.setLayout(borderLayout1);
		PanelTabs.setLayout(gridLayout1);

		CheckBoxSexDiscr.addActionListener(new SearchCriteriaDialog_SexDiscr_CheckBox_actionAdapter(this));
		CheckBoxAdditDiscr.addActionListener(new SearchCriteriaDialog_SnoutSpotDiscr_CheckBox_actionAdapter(this));
		CheckBoxQueryImg.addActionListener(new SearchCriteriaDialog_SnoutSpotDiscr_CheckBox_actionAdapter(this));

		PanelContainer.setPreferredSize(new Dimension(330, 260));

		lblSearchCriteria.setBounds(new Rectangle(20, 26, 150, 15));
		CheckBoxSexDiscr.setBounds(new Rectangle(35, 56, 70, 15));
		CheckBoxAdditDiscr.setBounds(new Rectangle(35, 86, 150, 15));
		CheckBoxQueryImg.setBounds(new Rectangle(35, 116, 250, 15));

		dbPanel.setLayout(null);

		btnClose.setBounds(new Rectangle(120, 160, 94, 35));
		btnClose.addActionListener(new SearchCriteriaDialog_butClose_actionAdapter(this));

		getContentPane().add(PanelContainer, BorderLayout.CENTER);
		PanelContainer.add(PanelTabs, BorderLayout.CENTER);
		PanelContainer.add(PanelButtons, BorderLayout.SOUTH);
		PanelTabs.add(TabbedPaneParams, null);
		TabbedPaneParams.addTab("Search Criteria", dbPanel);

		dbPanel.add(lblSearchCriteria, null);
		dbPanel.add(CheckBoxSexDiscr, null);
		dbPanel.add(CheckBoxAdditDiscr, null);
		dbPanel.add(CheckBoxQueryImg, null);
		dbPanel.add(btnClose, null);
	}

	public void setSearchCriteria() {
		parentFrame.getWorkingAreaPanel().setSearchBySex(CheckBoxSexDiscr.isSelected());
		parentFrame.getWorkingAreaPanel().setSearchBySnoutSpot(CheckBoxAdditDiscr.isSelected());
		parentFrame.getWorkingAreaPanel().setIncludeQueryImages(CheckBoxQueryImg.isSelected());
	}

	void SexDiscr_CheckBox_actionPerformed(ActionEvent e) {
		parentFrame.getWorkingAreaPanel().setSearchBySex(CheckBoxSexDiscr.isSelected());
	}

	void SnoutSpotDiscr_CheckBox_actionPerformed(ActionEvent e) {
		parentFrame.getWorkingAreaPanel().setSearchBySnoutSpot(CheckBoxAdditDiscr.isSelected());
	}

	void QueryImg_CheckBox_actionPerformed(ActionEvent e) {
		parentFrame.getWorkingAreaPanel().setIncludeQueryImages(CheckBoxQueryImg.isSelected());
	}

	void textThreshold_focusLost(FocusEvent e) {
		double Num = parentFrame.getWorkingAreaPanel().getThreshold();
		try {
			Num = Double.parseDouble(textThreshold.getText().trim());
			if (Num > 100) {
				Num = 100;
			}
			if (Num < 0) {
				Num = 0;
			}
		} catch (Exception ex) {
			new ErrorDialog("Invalid Threshold");
		}
		textThreshold.setText("" + Num);
		parentFrame.getWorkingAreaPanel().setThreshold(Num);
	}

	void butClose_actionPerformed(ActionEvent e) {
		// set Search Criteria in WorkingAreaPanel
		setSearchCriteria();

		dispose();
	}
}

class SearchCriteriaDialog_SexDiscr_CheckBox_actionAdapter implements java.awt.event.ActionListener {
	SearchCriteriaDialog adaptee;

	SearchCriteriaDialog_SexDiscr_CheckBox_actionAdapter(SearchCriteriaDialog adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.SexDiscr_CheckBox_actionPerformed(e);
	}
}

class SearchCriteriaDialog_SnoutSpotDiscr_CheckBox_actionAdapter implements java.awt.event.ActionListener {
	SearchCriteriaDialog adaptee;

	SearchCriteriaDialog_SnoutSpotDiscr_CheckBox_actionAdapter(SearchCriteriaDialog adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.SnoutSpotDiscr_CheckBox_actionPerformed(e);
	}
}

class SearchCriteriaDialog_QueryImg_CheckBox_actionAdapter implements java.awt.event.ActionListener {
	SearchCriteriaDialog adaptee;

	SearchCriteriaDialog_QueryImg_CheckBox_actionAdapter(SearchCriteriaDialog adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.QueryImg_CheckBox_actionPerformed(e);
	}
}

class SearchCriteriaDialog_textThreshold_focusAdapter extends java.awt.event.FocusAdapter {
	SearchCriteriaDialog adaptee;

	SearchCriteriaDialog_textThreshold_focusAdapter(SearchCriteriaDialog adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void focusLost(FocusEvent e) {
		adaptee.textThreshold_focusLost(e);
	}
}

class SearchCriteriaDialog_butClose_actionAdapter implements java.awt.event.ActionListener {
	SearchCriteriaDialog adaptee;

	SearchCriteriaDialog_butClose_actionAdapter(SearchCriteriaDialog adaptee) {
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e) {
		adaptee.butClose_actionPerformed(e);
	}
}
