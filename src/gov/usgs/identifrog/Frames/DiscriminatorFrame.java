package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.DataObjects.Discriminator;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.cellrenderers.WrappedStringListCellRenderer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class DiscriminatorFrame extends JDialog {
	private static final int TOP_ROW_PADDING = 4; //gridbaglayout row's top padding
	private JFrame callingFrame;
	private JDialog callingDialog;
	JButton okButton;
	JList<Discriminator> discriminatorList;
	public DiscriminatorFrame(){
		setupFrame();
		//setVisible(true);
	}
	
	public DiscriminatorFrame(JFrame callingFrame){
		this.callingFrame = callingFrame;
		setupFrame();
		setLocationRelativeTo(callingFrame);
		//setVisible(true);
	}
	
	public DiscriminatorFrame(JDialog callingDialog){
		this.callingDialog = callingDialog;
		setupFrame();
		setLocationRelativeTo(callingDialog);
		//setVisible(true);
	}

	private void setupFrame() {
		// TODO Auto-generated method stub
		setTitle("Data Discriminators");
		//new ImageIcon(this.getClass().getClassLoader().getResource("/resources/IconFrog.png"));
		setIconImage(new ImageIcon(this.getClass().getResource("/resources/IconFrog.png")).getImage());
		if (callingFrame == null && callingDialog == null) {
			//close if this is opened by main()
			addWindowListener(new WindowAdapter() { 
			    @Override public void windowClosing(WindowEvent e) { 
			      System.exit(0);
			    }
			  });
		} else {
			setModalityType(ModalityType.APPLICATION_MODAL);
		}
		
		DefaultListModel<Discriminator> discriminatorModel = new DefaultListModel<Discriminator>();

		ArrayList<Discriminator> discriminators = XMLFrogDatabase.getDiscriminators();
		for (Discriminator Discriminator : discriminators) {
			discriminatorModel.addElement(Discriminator);
		}
		
		okButton = new JButton("Save Discriminators");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ArrayList<Discriminator> discriminators = new ArrayList<Discriminator>();
				for (int i = 0; i < discriminatorModel.getSize(); i++) {
					discriminators.add(discriminatorModel.get(i));
				}
				
				XMLFrogDatabase.setDiscriminators(discriminators);
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
		
		JScrollPane discScrollPane = new JScrollPane(discriminatorList);
		discScrollPane.setBorder(discBorder);
		
		JPanel DiscriminatorPanel = new JPanel(new GridBagLayout());
		DiscriminatorPanel.setMinimumSize(new Dimension(250,275));
		GridBagConstraints c = new GridBagConstraints();

		JButton saveDiscriminator = new JButton("Save Discriminator");
		JTextField discriminatorTextField = new JTextField();
		
		saveDiscriminator.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				Discriminator Discriminator = new Discriminator(XMLFrogDatabase.getNextAvailableDiscriminatorID(),discriminatorTextField.getText().trim());
				discriminatorModel.addElement(Discriminator);
				discriminatorTextField.setText("");
			}
		});

		Insets topPaddingInsets = new Insets(TOP_ROW_PADDING,0,0,0);
		Insets noInsets = new Insets(0,0,0,0);
		
		//labels row
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.insets = noInsets;
		
		JLabel descriptionLabel = new JLabel("Discriminator Description");
		DiscriminatorPanel.add(descriptionLabel,c);

		//textfields row
		c.gridx = 0;
		c.gridy = 1;
		c.insets = noInsets;
		DiscriminatorPanel.add(discriminatorTextField, c);
		
		//Save button
		c = new GridBagConstraints();
		c.gridy = 4;
		c.insets = topPaddingInsets;
		//c.fill = GridBagConstraints.HORIZONTAL;
		DiscriminatorPanel.add(saveDiscriminator, c);
		
		c.gridy = 6;
		c.weighty = 1;
		c.fill = GridBagConstraints.NONE;
		String labelText = String.format("<html><div style=\"width:%dpx;\">%s</div><html>", 120, "Discriminators are unique elements about frogs that you can use to filter out search results, such as missing limbs or unusual colors. Disabled items are in use and cannot be modified.");

		JLabel infoLabel = new JLabel(labelText);
		DiscriminatorPanel.add(infoLabel, c);
		
		JPanel DiscriminatorsPanel = new JPanel(new GridLayout(1,3));
		DiscriminatorsPanel.add(DiscriminatorPanel);
		DiscriminatorsPanel.add(discScrollPane);
		
		DiscriminatorsPanel.setBorder(new EmptyBorder(5,5,5,5));

		add(DiscriminatorPanel);
		add(discScrollPane, BorderLayout.EAST);
		add(okButton, BorderLayout.SOUTH);
		pack();
		
		//setResizable(false);
		setMinimumSize(new Dimension(300,200));
	}
}
