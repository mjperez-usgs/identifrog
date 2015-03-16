package gov.usgs.identifrog;

import gov.usgs.identifrog.Frames.ErrorDialog;
import gov.usgs.identifrog.Frames.NewSiteDialog;
import gov.usgs.identifrog.Handlers.FolderHandler;
import gov.usgs.identifrog.Handlers.XMLHandler;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class StartupFrame extends JFrame implements ActionListener {
	private Image icon = Toolkit.getDefaultToolkit().getImage("IconFrog.png");
	private JButton openSite, createSite;
	
	public StartupFrame(){
		setupFrame();
		//setVisible(true);
	}

	private void setupFrame() {
		// TODO Auto-generated method stub
		setMinimumSize(new Dimension(600,300));
		setTitle("IdentiFrog");
		setIconImage(icon);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
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
		
		
		add(buttonPanel);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == createSite) {
			NewSiteDialog nsd = new NewSiteDialog(this);
			nsd.setVisible(true);
		} else
		if (e.getSource() == openSite) {
			JFileChooser f = new JFileChooser();
			ExtensionFileFilter xmlFilter = new ExtensionFileFilter();
			xmlFilter.addExactFile(IdentiFrog.DB_FILENAME);
			xmlFilter.setDescription("IdentiFrog Site Files ("+IdentiFrog.DB_FILENAME+")");
	        f.setFileFilter(xmlFilter);
	        int result = f.showOpenDialog(this);
	        if (result == JFileChooser.APPROVE_OPTION) {
	        	if (f.getSelectedFile().getName().equals(IdentiFrog.DB_FILENAME)) {
	        		//valid
	        		loadSite(f.getCurrentDirectory().getAbsolutePath());
	        		dispose();
	        	} else {
	        		new ErrorDialog("The selected XML file is not an IdentiFrog site file.");
	        	}
	        } else {
	        	dispose();
	        }
		}
	}
	
	public void createSite(String location, String siteName){
		FolderHandler fh = new FolderHandler(location+File.separator+siteName);
		XMLHandler file = new XMLHandler(fh.getFileNamePath());
		if (!fh.FoldersExist()) {
			fh.createFolders();
			file.CreateXMLFile();
		}
		File f = new File(fh.getFileNamePath());
		if (f.exists() && f.length() == 0) {
			file.CreateXMLFile();
		}
		// create an instance of the MainFrame
		MainFrame frame = new MainFrame(fh);
		// validate frames that have preset sizes
		// pack frames that have useful preferred size info, e.g. from their layout
		
		/*if (packFrame) {
			frame.pack();
		} else {
			frame.validate();
		}*/
		// center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		frame.setLocation(0, 0);
		// close Splash Screen
		//splash.dispose();
		dispose();
		frame.setVisible(true);
	}
	
	public void loadSite(String dataFilePath){
		FolderHandler fh = new FolderHandler(dataFilePath);
		// create an instance of the MainFrame
		MainFrame frame = new MainFrame(fh);
		// center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		frame.setLocation(0, 0);
		dispose();
		frame.setVisible(true);
	}
}
