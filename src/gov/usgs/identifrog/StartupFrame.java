package gov.usgs.identifrog;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class StartupFrame extends JFrame {
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
		
		ImageIcon openIcon = new ImageIcon(this.getClass().getResource("/resources/IconSite128.png"));
		openSite = new JButton("Open existing site", openIcon);
		openSite.setVerticalTextPosition(SwingConstants.BOTTOM);
		openSite.setHorizontalTextPosition(SwingConstants.CENTER);
		openSite.setMinimumSize(new Dimension(132,132));

	    
	    //openSite.setIcon(new ImageIcon(img));
		ImageIcon createIcon = new ImageIcon(this.getClass().getResource("/resources/IconBook128.png"));
		createSite = new JButton("Create new site", createIcon);
		createSite.setVerticalTextPosition(SwingConstants.BOTTOM);
		createSite.setHorizontalTextPosition(SwingConstants.CENTER);
		createSite.setMinimumSize(new Dimension(132,132));
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
}
