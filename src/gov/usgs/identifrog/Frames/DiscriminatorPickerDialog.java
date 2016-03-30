package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Discriminator;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.ui.JCheckBoxList;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class DiscriminatorPickerDialog extends JDialog {
	JDialog callingDialog;
	JCheckBoxList discriminatorList;
	DefaultListModel<JCheckBox> model = new DefaultListModel<JCheckBox>();
	HashMap<JCheckBox, Discriminator> checkMap = new HashMap<JCheckBox, Discriminator>(); //crazy, I know...
	private ArrayList<Discriminator> chosenDiscriminators;
	private JButton saveButton, cancelButton;

	public DiscriminatorPickerDialog(JDialog callingDialog, ArrayList<Discriminator> alreadySelectedDiscrims) {
		this.callingDialog = callingDialog;
		init(alreadySelectedDiscrims);
	}

	private void init(ArrayList<Discriminator> alreadySelectedDiscrims) {
		setTitle("Select discriminators");
		setIconImages(IdentiFrog.ICONS);
		setModal(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		reloadDiscriminatorsList(alreadySelectedDiscrims);
		setLocationRelativeTo(callingDialog);
	}

	private void reloadDiscriminatorsList(ArrayList<Discriminator> alreadySelectedDiscrims) {
		getContentPane().removeAll();
		ArrayList<Discriminator> dL = XMLFrogDatabase.getDiscriminators();
		JPanel panel = null;
		if (dL.size() <= 0) {
			//no discriminators
			JLabel label = new JLabel("No project discriminators defined.", SwingConstants.CENTER);
			panel = new JPanel(new BorderLayout());
			panel.add(label,BorderLayout.CENTER);
			
			JButton manageDiscrimsButton = new JButton("Add Project Discriminators");
			manageDiscrimsButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					new DiscriminatorFrame(DiscriminatorPickerDialog.this).setVisible(true);
					//reload discriminators list
					reloadDiscriminatorsList(alreadySelectedDiscrims); //if bugs arise due to recusion here, may consider checking out a better implementation of this.
				}
			});
			panel.add(manageDiscrimsButton,BorderLayout.SOUTH);
		} else {
			//discriminators
			discriminatorList = new JCheckBoxList(model);
			for (Discriminator disc : dL) {
				JCheckBox cb = new JCheckBox(disc.getText());
				checkMap.put(cb, disc);
				if (alreadySelectedDiscrims != null && alreadySelectedDiscrims.contains(disc)) {
					cb.setSelected(true);
				}
				model.addElement(cb);
			}
			panel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			saveButton = new JButton("Save Changes");
			cancelButton = new JButton("Discard Changes");

			saveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					chosenDiscriminators = new ArrayList<Discriminator>();
					//update chosen discrims
					for (Map.Entry<JCheckBox, Discriminator> entry : checkMap.entrySet()) {
						JCheckBox key = entry.getKey();
						Discriminator value = entry.getValue();
						if (key.isSelected()) {
							chosenDiscriminators.add(value);
						}
					}
					dispose();
				}
			});

			cancelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					//do nothing. do not set chosenDiscrims to anything.
					dispose();
				}
			});

			c.fill = GridBagConstraints.BOTH;
			c.weighty = 1;
			c.weightx = 1;
			c.gridwidth = 5;
			panel.add(discriminatorList, c);

			c.gridwidth = 1;
			c.weighty = 0;
			c.weightx = 1;
			c.gridx = 1;
			panel.add(cancelButton, c);
			c.gridx = 3;
			panel.add(saveButton, c);
		}
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();
		setMinimumSize(new Dimension(250, 250));
	}

	public ArrayList<Discriminator> getChosenDiscriminators() {
		return chosenDiscriminators;
	}
}
