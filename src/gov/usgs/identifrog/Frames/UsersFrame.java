package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.DataObjects.User;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.cellrenderers.UserListCellRenderer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
		setMinimumSize(new Dimension(400,300));
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
		
		DefaultListModel<User> observerModel = new DefaultListModel<User>();
		DefaultListModel<User> recorderModel = new DefaultListModel<User>();

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
		
		okButton = new JButton("Save Users");
		
		
		JPanel recorderListPanel = new JPanel(new BorderLayout());
		JPanel observerListPanel = new JPanel(new BorderLayout());
		TitledBorder recBorder = new TitledBorder(new EtchedBorder(), "Recorders");
		TitledBorder obsBorder = new TitledBorder(new EtchedBorder(), "Observers");
		Dimension listMinSize = new Dimension(150,100);
		
		
		recorderListPanel.setBorder(recBorder);
		recorderListPanel.setMinimumSize(listMinSize);
		recorderListPanel.add(recorderList, BorderLayout.CENTER);
		
		observerListPanel.setBorder(obsBorder);
		observerListPanel.setMinimumSize(listMinSize);
		observerListPanel.add(observerList, BorderLayout.CENTER);
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5,5,5,5));
		contentPanel.add(observerListPanel,BorderLayout.WEST);
		contentPanel.add(recorderListPanel,BorderLayout.EAST);
		contentPanel.add(okButton,BorderLayout.SOUTH);
		
		
		JPanel userPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JButton saveUser = new JButton("Save User");
		JCheckBox isObserver = new JCheckBox("Observer"), isRecorder = new JCheckBox("Recorder");
		JTextField fName = new JTextField();
		JTextField lName = new JTextField();
		
		//textfields
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.7;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(TOP_ROW_PADDING,0,0,0);  //top padding

		userPanel.add(fName, c);
		c.gridy = 1;
		userPanel.add(lName, c);
		
		//labels
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.3;
		c.insets = new Insets(TOP_ROW_PADDING,0,0,0);  //top padding

		JLabel fLabel = new JLabel("First name"), lLabel = new JLabel("Last name");
		userPanel.add(fLabel,c);
		c.gridy = 1;
		userPanel.add(lLabel,c);
		
		//checkboxes
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.insets = new Insets(TOP_ROW_PADDING,0,0,0);  //top padding
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		userPanel.add(isObserver,c);
		c.gridx = 1;
		userPanel.add(isRecorder,c);
		
		//Save button
		c = new GridBagConstraints();
		c.gridy = 4;
		c.insets = new Insets(TOP_ROW_PADDING,0,0,0);  //top padding
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		userPanel.add(saveUser, c);
		
		contentPanel.add(userPanel,BorderLayout.CENTER);
		
		
		//add(observerList);
		//add(recorderList);
		add(contentPanel);
		/*
		ImageIcon openIcon = new ImageIcon(this.getClass().getResource("/resources/IconSite128.png"));
		openSite = new JButton("Open existing site", openIcon);
		openSite.setVerticalTextPosition(SwingConstants.BOTTOM);
		openSite.setHorizontalTextPosition(SwingConstants.CENTER);
		openSite.setMinimumSize(new Dimension(132,132));
		openSite.addActionListener(this);
	    
	    //openSite.setIcon(new ImageIcon(img));
		ImageIcon createIcon = new ImageIcon(this.getClass().getResource("/resources/IconBook128.png"));
		createSite = new JButton("Create new site", createIcon);
		createSite.setVerticalTextPosition(SwingConstants.BOTTOM);
		createSite.setHorizontalTextPosition(SwingConstants.CENTER);
		createSite.setMinimumSize(new Dimension(132,132));
		createSite.addActionListener(this);
		//img = Toolkit.getDefaultToolkit().getImage("IconBook128.png");
	    //createSite.setIcon(new ImageIcon(img));
		
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(openSite);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(createSite);
		buttonPanel.add(Box.createHorizontalGlue());
		
		
		
		JPanel verticalPanel = new JPanel();
		verticalPanel.add(Box.createVerticalGlue());
		verticalPanel.setLayout(new BoxLayout(verticalPanel,BoxLayout.PAGE_AXIS));
		verticalPanel.add(buttonPanel);
		verticalPanel.add(Box.createVerticalGlue());
		
		//Recent sites panel
		JPanel recentSitesPanel = new JPanel();
		Border border = BorderFactory.createEtchedBorder();
		TitledBorder title = BorderFactory.createTitledBorder(border,"Recently opened sites");
		title.setTitleJustification(TitledBorder.CENTER);
		recentSitesPanel.setBorder(title);
		recentSitesPanel.setLayout(new BoxLayout(recentSitesPanel,BoxLayout.LINE_AXIS));
		recentSitesPanel.add(Box.createHorizontalGlue());
		
		//start population
		getRecentSites();
		for (Site site : recentSites) {
			JButton recentSite = createRecentSiteButton(site);
			recentSitesPanel.add(recentSite);
			recentSitesPanel.add(Box.createHorizontalGlue());
		}
		
		if (recentSites.size() <= 0) {
			JLabel noSites = new JLabel ("No recently opened sites");
			noSites.setEnabled(false);
			recentSitesPanel.add(noSites);
		}
		
		//end population
		recentSitesPanel.add(Box.createHorizontalGlue());
		verticalPanel.add(recentSitesPanel);
		
		//version
		JPanel versionPanel = new JPanel();
		versionPanel.setLayout(new BoxLayout(versionPanel,BoxLayout.LINE_AXIS));
		versionPanel.add(Box.createHorizontalGlue());
		JLabel versionInfo = new JLabel("IdentiFrog "+IdentiFrog.HR_VERSION);
		versionInfo.setEnabled(false);
		versionPanel.add(versionInfo);
		versionPanel.add(Box.createHorizontalGlue());
		
		verticalPanel.add(versionPanel);
		add(verticalPanel);
		*/
		pack();
	}

	

	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
}
