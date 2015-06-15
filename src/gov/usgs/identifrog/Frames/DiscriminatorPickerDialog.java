package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.DataObjects.Discriminator;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.ui.JCheckBoxList;

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
import javax.swing.JPanel;

public class DiscriminatorPickerDialog extends JDialog {
	JDialog callingDialog;
	JCheckBoxList discriminatorList;
	DefaultListModel<JCheckBox> model = new DefaultListModel<JCheckBox>();
	HashMap<JCheckBox, Discriminator> checkMap = new HashMap<JCheckBox,Discriminator>(); //crazy, I know...
	private ArrayList<Discriminator> chosenDiscriminators;
	private JButton saveButton, cancelButton;
	
	public DiscriminatorPickerDialog(JDialog callingDialog, ArrayList<Discriminator> alreadySelectedDiscrims){
		this.callingDialog = callingDialog;
		init(alreadySelectedDiscrims);
	}

	private void init(ArrayList<Discriminator> alreadySelectedDiscrims) {
		setTitle("Select discriminators");
		setIconImage(getToolkit().getImage(getClass().getResource("/resources/IconFrog.png")));
		setModal(true);
		
		ArrayList<Discriminator> dL = XMLFrogDatabase.getDiscriminators();
		discriminatorList = new JCheckBoxList(model);
		for(Discriminator disc : dL){
			JCheckBox cb = new JCheckBox(disc.getText());
			checkMap.put(cb, disc);
			if (alreadySelectedDiscrims!= null && alreadySelectedDiscrims.contains(disc)) {
				cb.setSelected(true);
			}
			model.addElement(cb);
		}
		
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
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		c.weightx = 1;
		c.gridwidth = 5;
		panel.add(discriminatorList,c);
		
		c.gridwidth = 1;
		c.weighty = 0;
		c.weightx = 1;
		c.gridx = 1;
		panel.add(cancelButton, c);
		c.gridx = 3;
		panel.add(saveButton, c);
		
		add(panel);
		pack();
		setMinimumSize(new Dimension(200,200));
		setLocationRelativeTo(callingDialog);
	}

	public ArrayList<Discriminator> getChosenDiscriminators() {
		return chosenDiscriminators;
	}
}
