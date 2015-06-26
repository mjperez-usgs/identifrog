package gov.usgs.identifrog.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class StatusBar extends JPanel {
	private JLabel status = new JLabel();
	private JLabel rightStatus = new JLabel();
	private JLabel icon = new JLabel();
	private EmptyIcon emptyIcon = new EmptyIcon(16,16);

    /** Creates a new instance of StatusBar */
    public StatusBar() {
        super();
        setLayout(new GridBagLayout());
        setMaximumSize(new Dimension(10000,15));
        setBorder(new EmptyBorder(3,3,3,3));
        icon.setAlignmentX(LEFT_ALIGNMENT);
        icon.setBorder(new EmptyBorder(0,0,0,2));
        status.setAlignmentX(LEFT_ALIGNMENT);
        rightStatus.setAlignmentX(RIGHT_ALIGNMENT);
        
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.SOUTHWEST;
        add(icon,c);
        c.gridx = 1;
        add(status,c);
        //add(Box.createHorizontalGlue());
        
        GridBagConstraints sepCon = new GridBagConstraints();
        sepCon.gridheight = GridBagConstraints.REMAINDER;
        sepCon.fill = GridBagConstraints.VERTICAL;
        sepCon.anchor = GridBagConstraints.EAST;
        sepCon.weightx = 1;
        sepCon.gridx = 2;
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        add(sep,sepCon);
        
        c.weightx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 3;
        add(Box.createHorizontalStrut(6),c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 4;
        add(rightStatus,c);
        clear();
    }

    /**
     * Sets a message on the status bar.
     * Removes any icon if present.
     * @param message Message to display
     */
    public void setMessage(String message) {
    	this.icon.setIcon(emptyIcon);
        this.status.setText(message);        
    }    
    
    /**
     * Sets both the icon and the message
     */
    public void setMessageWithIcon(String message, ImageIcon icon) {
    	this.icon.setIcon(icon);
        this.status.setText(message);        
    }
    
    /**
     * Sets the icon on the status bar.
     * Removes any message if present.
     * @param icon Icon to display
     */
    public void setIcon(ImageIcon icon) {
    	this.status.setText("");
    	this.icon.setIcon(icon);
    }
    
    public void setRightStatus(String message){ 
    	rightStatus.setText(message);
    }
    
    public void clear() {
    	status.setText("");
    	rightStatus.setText("");
    	icon.setIcon(emptyIcon);
    }
}