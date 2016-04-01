package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Discriminator;
import gov.usgs.identifrog.DataObjects.Template;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.ui.JCheckBoxList;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class TemplatePickerDialog extends JDialog {
	private JButton loadButton, cancelButton;
	private Template chosenTemplate;

	public TemplatePickerDialog(JDialog dialog) {
		init(dialog);
	}

	private void init(JDialog callingDialog) {
		setTitle("Select a Template");
		setIconImages(IdentiFrog.ICONS);
		setModal(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(250, 250));
		loadTemplateList();
		setLocationRelativeTo(callingDialog);
		setVisible(true);
	}

	private void loadTemplateList() {
		getContentPane().removeAll();

		ArrayList<Template> templates = XMLFrogDatabase.getTemplates();
		JPanel panel = null;
		if (templates.size() <= 0) {
			//no discriminators
			JLabel label = new JLabel("No data templates defined.", SwingConstants.CENTER);
			panel = new JPanel(new BorderLayout());
			panel.add(label, BorderLayout.CENTER);

			JButton openTemplateManagerButton = new JButton("Add a template");
			openTemplateManagerButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					new TemplateFrame(TemplatePickerDialog.this);
					loadTemplateList();
				}
			});
			panel.add(openTemplateManagerButton, BorderLayout.SOUTH);
		} else {
			//templates
			panel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			for (Template template : templates) {
				JRadioButton rb = new JRadioButton(template.getName());
				c.gridy++;
				panel.add(rb, c);
			}

			loadButton = new JButton("Load Template");
			//cancelButton = new JButton("Discard Changes");

			loadButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					chosenTemplate = null; //UPDATE
					dispose();
				}
			});

			c.gridwidth = 1;
			c.weighty = 0;
			c.weightx = 1;
			panel.add(loadButton, c);
		}
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();
	}

	public Template getChosenTemplate() {
		return chosenTemplate;
	}
}
