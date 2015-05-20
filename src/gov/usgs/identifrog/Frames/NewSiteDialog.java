package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.IdentiFrog;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

/**
 * <p>
 * Title: NewSiteDialog
 * <p>
 * Description: Has options for placing a new site in a certain folder
 * 
 * @author Michael J. Perez 2015 USGS
 */

@SuppressWarnings("serial")
public class NewSiteDialog extends JDialog implements ActionListener {
	private JButton butOK, butChange;
	private JTextField textSiteName;
	private JLabel labelSaveLocation;
	private ProjectManagerFrame parent;
	
	/**
	 * Creates a new site dialog, where the user chooses where to save their site data.
	 * I'm not sure how 'save as' is supposed to work exactly.
	 * @param parent StartupFrame that spawns this dialog
	 * @author Michael J. Perez
	 */
	public NewSiteDialog(ProjectManagerFrame parent) {
		super(parent);
		this.parent = parent;
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		setTitle("Create new project");
		setLocationRelativeTo(parent);
		setMinimumSize(new Dimension(500,200));
		setResizable(false);
		
		JPanel contentPanel = new JPanel();
		try {
			BufferedImage folderImage =  ImageIO.read(this.getClass().getResource("/resources/IconFolder128.png"));
			JLabel folderLabel = new JLabel(new ImageIcon(folderImage));
			contentPanel.add(folderLabel, BorderLayout.WEST);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			IdentiFrog.LOGGER.writeException(e);
		}
		
		//site folder
		butOK = new JButton("Create project");
		butOK.addActionListener(this);
		butChange = new JButton("Change...");
		butChange.addActionListener(this);
		//default to user dir
		labelSaveLocation = new JLabel(System.getProperty("user.home"));

		JPanel saveLocationPanel = new JPanel();
		saveLocationPanel.setLayout(new BoxLayout(saveLocationPanel, BoxLayout.LINE_AXIS));
		saveLocationPanel.add(labelSaveLocation);
		saveLocationPanel.add(butChange);
		saveLocationPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Site Data Folder"));
		
		//site name
		textSiteName = new JTextField("Project  ");
		JPanel siteNamePanel = new JPanel(new BorderLayout());
		siteNamePanel.add(textSiteName, BorderLayout.SOUTH);
		siteNamePanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Project Name"));
		
		JPanel siteInfoRightPanel = new JPanel(new BorderLayout());
		siteInfoRightPanel.add(saveLocationPanel, BorderLayout.NORTH);
		siteInfoRightPanel.add(siteNamePanel, BorderLayout.CENTER);
		siteInfoRightPanel.add(butOK, BorderLayout.SOUTH);
		siteInfoRightPanel.setMinimumSize(new Dimension(350,200));
		contentPanel.add(siteInfoRightPanel, BorderLayout.EAST);
		
		getRootPane().setDefaultButton(butOK);
		add(contentPanel);
		pack();
		setLocationRelativeTo(parent);
	}

	// overridden so we can exit when window is closed
	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			dispose();
		}
		super.processWindowEvent(e);
	}

	// close the dialog on a button event
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == butOK) {
			parent.createSite(labelSaveLocation.getText(),textSiteName.getText());
			dispose();
		} else 
		if (e.getSource() == butChange) {
			JFileChooser f = new JFileChooser();
	        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); 
	        int result = f.showSaveDialog(null);
	        if (result == JFileChooser.APPROVE_OPTION) {
	        	labelSaveLocation.setText(f.getSelectedFile().toString());
	        } else {
	        	dispose();
	        }
		}
	}
}
