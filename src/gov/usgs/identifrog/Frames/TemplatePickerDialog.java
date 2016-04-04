package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.SiteSample;
import gov.usgs.identifrog.DataObjects.Template;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

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
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Controls the interface for selecting a template to use (to quickly fill
 * fields)
 * 
 * @author mjperez
 *
 */
public class TemplatePickerDialog extends JDialog {
	private JButton loadButton, cancelButton;
	private Template chosenTemplate;
	private ButtonGroup group = new ButtonGroup();
	private HashMap<JRadioButton, Template> radioButtonMap = new HashMap<JRadioButton, Template>();

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

	/**
	 * Loads the list of templates and updates the UI accordingly
	 */
	private void loadTemplateList() {
		getContentPane().removeAll();

		ArrayList<Template> templates = XMLFrogDatabase.getTemplates();
		JPanel panel = null;
		JButton openTemplateManagerButton = new JButton();
		openTemplateManagerButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new TemplateFrame(TemplatePickerDialog.this);
				loadTemplateList();
			}
		});
		if (templates.size() <= 0) {
			//no discriminators
			JLabel label = new JLabel("No data templates defined.", SwingConstants.CENTER);
			panel = new JPanel(new BorderLayout());
			panel.add(label, BorderLayout.CENTER);
			openTemplateManagerButton.setText("Add a template");

			panel.add(openTemplateManagerButton, BorderLayout.SOUTH);
		} else {
			//templates
			panel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			for (Template template : templates) {
				JRadioButton rb = new JRadioButton(template.getName());
				c.gridy++;
				group.add(rb);
				radioButtonMap.put(rb,template);
				panel.add(rb, c);
			}

			loadButton = new JButton("Load Template");
			//cancelButton = new JButton("Discard Changes");

			loadButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
						AbstractButton button = buttons.nextElement();
						if (button.isSelected()) {
							chosenTemplate = radioButtonMap.get(button);
							assert chosenTemplate != null;
							break;
						}
					}
					dispose();
				}
			});

			c.gridwidth = 1;
			c.weighty = 0;
			c.weightx = 1;

			JPanel totalPanel = new JPanel(new BorderLayout());
			openTemplateManagerButton.setText("Template Manager");

			totalPanel.add(openTemplateManagerButton, BorderLayout.NORTH);
			totalPanel.add(panel, BorderLayout.CENTER);
			totalPanel.add(loadButton, BorderLayout.SOUTH);
			panel = totalPanel;
		}
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel);
		pack();
	}

	/**
	 * Returns the chosen template, or null if the window was canceled.
	 * 
	 * @return
	 */
	public Template getChosenTemplate() {
		return chosenTemplate;
	}
}
