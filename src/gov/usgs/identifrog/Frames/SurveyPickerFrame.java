package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.SiteSample;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

public class SurveyPickerFrame extends JDialog {
	JDialog callingDialog;
	HashMap<JRadioButton, SiteSample> radioButtonMap = new HashMap<JRadioButton, SiteSample>(); //crazy, I know...
	private JButton loadButton, cancelButton, deleteButton, addButton;
	private ButtonGroup group = new ButtonGroup();
	protected SiteSample loadSurvey;

	public SurveyPickerFrame(JDialog callingDialog, Frog frog, SiteSample activeSurvey) {
		this.callingDialog = callingDialog;
		init(frog, activeSurvey);
	}

	private void init(Frog frog, SiteSample activeSurvey) {
		setTitle("Survey Switcher");
		setIconImages(IdentiFrog.ICONS);
		setModal(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		reloadSurveyList(frog, activeSurvey);
		setLocationRelativeTo(callingDialog);
	}

	private void reloadSurveyList(Frog frog, SiteSample activeSurvey) {
		getContentPane().removeAll();
		radioButtonMap.clear();
		ArrayList<SiteSample> samples = frog.getSiteSamples();
		JPanel surveysPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 2;
		c.weighty = 0;
		c.weightx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		for (SiteSample s : samples) {
			System.out.println("GRIDY: " + c.gridy);
			JRadioButton button = new JRadioButton((s.getDateCapture() != null ? s.getDateCapture() : "No Capture Date") + ": " + s.getSurveyID());
			group.add(button);
			if (s == activeSurvey) {
				button.setSelected(true);
			}
			radioButtonMap.put(button, s);
			surveysPanel.add(button, c);
			c.gridy++;
		}

		loadButton = new JButton("Switch to Survey");
		cancelButton = new JButton("Cancel");
		deleteButton = new JButton("Delete Survey");
		addButton = new JButton("New Survey");

		loadButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				group.getSelection();
				for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
					AbstractButton button = buttons.nextElement();
					if (button.isSelected()) {
						loadSurvey = radioButtonMap.get(button);
					}
				}
				dispose();
			}
		});

		addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SiteSample sample = new SiteSample();
				sample.setSurveyID("New Survey");
				frog.addSiteSample(sample);
				loadSurvey = sample;
				dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//do nothing. do not set chosenDiscrims to anything.
				loadSurvey = null; //don't change anything
				dispose();
			}
		});

		GridBagConstraints bottomConstraints = new GridBagConstraints();
		bottomConstraints.gridy = c.gridy;

		JPanel bottomPanel = new JPanel(new GridBagLayout());
		bottomConstraints.gridy++;
		bottomConstraints.gridwidth = 1;
		bottomConstraints.weightx = 1;
		bottomConstraints.fill = GridBagConstraints.BOTH;
		bottomPanel.add(deleteButton, bottomConstraints);
		bottomConstraints.gridx = 1;
		bottomPanel.add(addButton, bottomConstraints);

		bottomConstraints.gridy++;
		bottomConstraints.gridwidth = 2;
		bottomConstraints.gridx = 0;
		bottomPanel.add(loadButton, bottomConstraints);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.add(new JLabel("Surveys for frog "+frog.getID(),JLabel.CENTER),BorderLayout.NORTH);
		panel.add(new JScrollPane(surveysPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
				BorderLayout.CENTER);
		panel.add(bottomPanel, BorderLayout.SOUTH);
		add(panel);
		pack();
		setMinimumSize(new Dimension(250, 250));
	}

	public SiteSample getSurveyToLoad() {
		return loadSurvey;
	}
}
