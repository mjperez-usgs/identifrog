package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Discriminator;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.SiteSample;

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
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 2;
		c.weighty = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		for (SiteSample s : samples) {
			JRadioButton button = new JRadioButton(s.getDateCapture() + ": " + s.getSurveyID());
			group.add(button);
			if (s == activeSurvey) {
				button.setSelected(true);
			}
			radioButtonMap.put(button, s);
			panel.add(button, c);
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

		c.gridy++;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		panel.add(deleteButton, c);
		c.gridx = 1;
		panel.add(addButton, c);

		c.gridy++;
		c.gridwidth = 2;
		c.gridx = 0;
		panel.add(loadButton, c);

		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();
		setMinimumSize(new Dimension(250, 250));
	}

	public SiteSample getSurveyToLoad() {
		return loadSurvey;
	}
}
