package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.DataObjects.User;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.cellrenderers.UserListCellRenderer;

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

public class UsersFrame extends JDialog implements ActionListener {
	private static final int TOP_ROW_PADDING = 4; //gridbaglayout row's top padding
	private JFrame callingFrame;
	private JDialog callingDialog;
	JButton okButton;
	JList<User> recorderList, observerList;
	public UsersFrame(){
		setupFrame();
		//setVisible(true);
	}
	
	public UsersFrame(JFrame callingFrame){
		this.callingFrame = callingFrame;
		setupFrame();
		//setVisible(true);
	}
	
	public UsersFrame(JDialog callingDialog){
		this.callingDialog = callingDialog;
		setupFrame();
		//setVisible(true);
	}

	private void setupFrame() {
		// TODO Auto-generated method stub
		setTitle("Data Users");
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
		
		final DefaultListModel<User> observerModel = new DefaultListModel<User>();
		final DefaultListModel<User> recorderModel = new DefaultListModel<User>();

		ArrayList<User> observers = XMLFrogDatabase.getObservers();
		ArrayList<User> recorders = XMLFrogDatabase.getRecorders();
		for (User user : observers) {
			observerModel.addElement(user);
		}
		for (User user : recorders) {
			recorderModel.addElement(user);
		}
		
		recorderList = new JList<User>();
		observerList = new JList<User>();
		
		recorderList.setCellRenderer(new UserListCellRenderer());
		observerList.setCellRenderer(new UserListCellRenderer());

		observerList.setModel(observerModel);
		recorderList.setModel(recorderModel);
		
		observerList.setVisibleRowCount(10);
		observerList.setFixedCellHeight(15);
		observerList.setFixedCellWidth(100);
		recorderList.setVisibleRowCount(10);
		recorderList.setFixedCellHeight(15);
		recorderList.setFixedCellWidth(100);
		
		okButton = new JButton("Save Users");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ArrayList<User> recorders = new ArrayList<User>(), observers = new ArrayList<User>();
				for (int i = 0; i < recorderModel.getSize(); i++) {
					recorders.add(recorderModel.get(i));
				}
				for (int i = 0; i < observerModel.getSize(); i++) {
					observers.add(observerModel.get(i));
				}
				
				XMLFrogDatabase.setRecorders(recorders);
				XMLFrogDatabase.setObservers(observers);
				XMLFrogDatabase.writeXMLFile();
				dispose();
			}
		});
		
		
		//JPanel recorderListPanel = new JPanel(new BorderLayout());
		//JPanel observerListPanel = new JPanel(new BorderLayout());
		TitledBorder recBorder = new TitledBorder(new EtchedBorder(), "Recorders");
		TitledBorder obsBorder = new TitledBorder(new EtchedBorder(), "Observers");
		//Dimension listMinSize = new Dimension(70,350);
		//Dimension listMaxSize = new Dimension(150,10000);


		JScrollPane recScrollPane = new JScrollPane(recorderList);
		recScrollPane.setBorder(recBorder);

		////recScrollPane.setMinimumSize(listMinSize);
		//recScrollPane.setMinimumSize(listMaxSize);
		
		JScrollPane obsScrollPane = new JScrollPane(observerList);
		obsScrollPane.setBorder(obsBorder);
		//obsScrollPane.setMinimumSize(listMinSize);
		//obsScrollPane.setMinimumSize(listMaxSize);
		
		JPanel userPanel = new JPanel(new GridBagLayout());
		userPanel.setMinimumSize(new Dimension(250,275));
		GridBagConstraints c = new GridBagConstraints();

		JButton saveUser = new JButton("Save User");
		final JCheckBox checkIsObserver = new JCheckBox("Observer");
		final JCheckBox checkIsRecorder = new JCheckBox("Recorder");
		final JTextField fName = new JTextField();
		final JTextField lName = new JTextField();
		
		saveUser.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (checkIsObserver.isSelected()) {
					User user = new User();
					user.setFirstName(fName.getText().trim());
					user.setLastName(lName.getText().trim());
					user.setID(XMLFrogDatabase.getNextAvailableObserverID());
					observerModel.addElement(user);
				}
				if (checkIsRecorder.isSelected()) {
					User user = new User();
					user.setFirstName(fName.getText().trim());
					user.setLastName(lName.getText().trim());
					user.setID(XMLFrogDatabase.getNextAvailableRecorderID());
					recorderModel.addElement(user);
				}
				fName.setText("");
				lName.setText("");
			}
		});

		Insets topPaddingInsets = new Insets(TOP_ROW_PADDING,0,0,0);
		Insets leftPaddingInsets = new Insets(0,5,0,0);
		Insets noInsets = new Insets(0,0,0,0);
		
		//labels row
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.insets = noInsets;
		
		JLabel fLabel = new JLabel("First name"), lLabel = new JLabel("Last name");
		userPanel.add(fLabel,c);
		c.gridx = 1;
		c.insets = leftPaddingInsets;
		userPanel.add(lLabel,c);

		//textfields row
		c.gridx = 0;
		c.gridy = 1;
		c.insets = noInsets;
		userPanel.add(fName, c);
		c.gridx = 1;
		c.insets = leftPaddingInsets;
		userPanel.add(lName, c);
				
		//checkboxes
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.insets = topPaddingInsets;
		c.weightx = 1;
		userPanel.add(checkIsObserver,c);
		c.gridx = 1;
		userPanel.add(checkIsRecorder,c);
		
		//Save button
		c = new GridBagConstraints();
		c.gridy = 4;
		c.insets = topPaddingInsets;
		c.gridwidth = 2;
		//c.fill = GridBagConstraints.HORIZONTAL;
		userPanel.add(saveUser, c);
		
		JPanel usersPanel = new JPanel(new GridLayout(1,3));
		usersPanel.add(obsScrollPane);
		usersPanel.add(userPanel);
		usersPanel.add(recScrollPane);
		
		userPanel.setBorder(new EmptyBorder(5,5,5,5));

		add(obsScrollPane, BorderLayout.WEST);
		add(userPanel);
		add(recScrollPane, BorderLayout.EAST);
		add(okButton, BorderLayout.SOUTH);
		pack();
		
		//setResizable(false);
		setMinimumSize(new Dimension(400,300));
		setMaximumSize(new Dimension(500,400));
	}

	

	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
}
