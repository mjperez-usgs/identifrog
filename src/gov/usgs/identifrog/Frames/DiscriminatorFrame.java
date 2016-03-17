package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.DataObjects.Discriminator;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.cellrenderers.WrappedStringListCellRenderer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DiscriminatorFrame extends JDialog {
	private static final int TOP_ROW_PADDING = 4; //gridbaglayout row's top padding
	private JFrame callingFrame;
	private JDialog callingDialog;
	JButton saveAllButton;
	JList<Discriminator> discriminatorList;
	Discriminator editingDiscriminator;

	public DiscriminatorFrame() {
		setupFrame();
		//setVisible(true);
	}

	public DiscriminatorFrame(JFrame callingFrame) {
		this.callingFrame = callingFrame;
		setupFrame();
		setLocationRelativeTo(callingFrame);
		//setVisible(true);
	}

	public DiscriminatorFrame(JDialog callingDialog) {
		this.callingDialog = callingDialog;
		setupFrame();
		setLocationRelativeTo(callingDialog);
	}

	private void setupFrame() {
		setTitle("Data Discriminators");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImage(new ImageIcon(this.getClass().getResource("/resources/IconFrog.png")).getImage());
		if (callingFrame == null && callingDialog == null) {
			//close if this is opened by main()
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
		} else {
			setModalityType(ModalityType.APPLICATION_MODAL);
		}

		final JTextField discriminatorTextField = new JTextField();
		final DefaultListModel<Discriminator> discriminatorModel = new DefaultListModel<Discriminator>();
		final JButton saveDiscriminator = new JButton("Save Discriminator");
		final JButton deleteDiscriminator = new JButton("Delete Discriminator");
		
		deleteDiscriminator.setEnabled(false);

		ArrayList<Discriminator> discriminators = XMLFrogDatabase.getDiscriminators();
		for (Discriminator Discriminator : discriminators) {
			discriminatorModel.addElement(Discriminator);
		}

		saveAllButton = new JButton("Save Discriminators");
		saveAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				HashSet<Discriminator> discriminators = new HashSet<Discriminator>();
				for (int i = 0; i < discriminatorModel.getSize(); i++) {
					discriminators.add(discriminatorModel.get(i));
				}
				
				ArrayList<Discriminator> uniqueList = new ArrayList<Discriminator>(discriminators);
				Collections.sort(uniqueList);

				XMLFrogDatabase.setDiscriminators(uniqueList);
				XMLFrogDatabase.writeXMLFile();
				dispose();
			}
		});

		//JPanel recorderListPanel = new JPanel(new BorderLayout());
		//JPanel observerListPanel = new JPanel(new BorderLayout());
		TitledBorder discBorder = new TitledBorder(new EtchedBorder(), "Discriminators");
		//Dimension listMinSize = new Dimension(70,350);
		//Dimension listMaxSize = new Dimension(150,10000);

		discriminatorList = new JList<Discriminator>();
		discriminatorList.setCellRenderer(new WrappedStringListCellRenderer(100));
		discriminatorList.setModel(discriminatorModel);
		discriminatorList.setVisibleRowCount(10);
		discriminatorList.setFixedCellWidth(100);
		discriminatorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		discriminatorList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent le) {
				int idx = discriminatorList.getSelectedIndex();
				if (idx != -1) {
					editingDiscriminator = discriminatorModel.get(idx);
					if (editingDiscriminator.isInUse()) {
						editingDiscriminator = null;
						discriminatorTextField.setText("");
						deleteDiscriminator.setEnabled(false);
					} else {
						discriminatorTextField.setText(editingDiscriminator.toString());
						deleteDiscriminator.setEnabled(true);
					}
				} else {
					editingDiscriminator = null;
					discriminatorTextField.setText("");
					deleteDiscriminator.setEnabled(false);

				}
			}
		});

		JScrollPane discScrollPane = new JScrollPane(discriminatorList);
		discScrollPane.setBorder(discBorder);

		JPanel discriminatorPanel = new JPanel(new GridBagLayout());
		discriminatorPanel.setMinimumSize(new Dimension(250, 275));
		GridBagConstraints c = new GridBagConstraints();
		
		Action saveAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (discriminatorTextField.getText().trim().equals("")) {
					return;
				}
				
				if (editingDiscriminator == null) {
					Discriminator Discriminator = new Discriminator(XMLFrogDatabase.getNextAvailableDiscriminatorID(), discriminatorTextField
							.getText().trim());
					discriminatorModel.addElement(Discriminator);
				} else {
					//technically we are editing it cause of pass by reference but java doesn't know the object updated
					editingDiscriminator.setText(discriminatorTextField.getText().trim());
					discriminatorModel.setElementAt(editingDiscriminator, discriminatorList.getSelectedIndex());
					editingDiscriminator = null;
				}
				discriminatorTextField.setText("");
				deleteDiscriminator.setEnabled(false);
			}
		};
		discriminatorTextField.addActionListener(saveAction);
		saveDiscriminator.addActionListener(saveAction);

		deleteDiscriminator.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				discriminatorModel.removeElement(editingDiscriminator);
				deleteDiscriminator.setEnabled(false);
			}
		});
		Insets topPaddingInsets = new Insets(TOP_ROW_PADDING, 0, 0, 0);
		Insets noInsets = new Insets(0, 0, 0, 0);

		//labels row
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0;
		c.weightx = 0;
		c.insets = noInsets;

		JLabel descriptionLabel = new JLabel("Discriminator description");
		discriminatorPanel.add(descriptionLabel, c);

		//textfields row
		c.gridx = 0;
		c.gridy = 1;
		c.insets = noInsets;
		discriminatorPanel.add(discriminatorTextField, c);

		//Save button
		c.gridy = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = topPaddingInsets;
		//c.fill = GridBagConstraints.HORIZONTAL;
		discriminatorPanel.add(saveDiscriminator, c);

		//Save button
		c.gridy = 3;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = topPaddingInsets;
		//c.fill = GridBagConstraints.HORIZONTAL;
		discriminatorPanel.add(deleteDiscriminator, c);
		
		//text
		c.gridy = 4;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		String labelText = String
				.format("<html><div style=\"width:%dpx;\">%s</div><html>",
						120,
						"Discriminators are unique elements about frogs that you can use to filter out search results, such as missing limbs or unusual colors. Disabled items are in use and cannot be modified.");

		JLabel infoLabel = new JLabel(labelText);
		discriminatorPanel.add(infoLabel, c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 5;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		discriminatorPanel.add(discScrollPane, c);

		//save all button
		c.gridx = 0;
		c.gridy = 10;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.weighty = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		discriminatorPanel.add(saveAllButton, c);

		/*
		 * JPanel DiscriminatorsPanel = new JPanel(new GridLayout(1, 3));
		 * DiscriminatorsPanel.add(discriminatorPanel);
		 * DiscriminatorsPanel.add(discScrollPane);
		 */

		discriminatorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		add(discriminatorPanel);
		pack();

		//setResizable(false);
		setMinimumSize(new Dimension(300, 275));
	}
}
