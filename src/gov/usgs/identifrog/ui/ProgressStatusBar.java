package gov.usgs.identifrog.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Creates a status bar panel that has a progress bar
 * @author mjperez
 *
 */
public class ProgressStatusBar extends JPanel {
	//private JLabel status = new JLabel();
	private JLabel status = new JLabel();
	private JLabel icon = new JLabel();
	private JProgressBar progressBar = new JProgressBar();
	private EmptyIcon emptyIcon = new EmptyIcon(16,16);
	
    /** Creates a new instance of StatusBar, with the left status message replaced with a progress bar. */
    public ProgressStatusBar() {
        super();
        JPanel panel = new JPanel(new GridBagLayout());
        MatteBorder border = BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("InternalFrame.borderShadow"));
        setBorder(border);
        setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(2,2,0,2));
        setMaximumSize(new Dimension(10000,15));
        icon.setAlignmentX(LEFT_ALIGNMENT);
        icon.setBorder(new EmptyBorder(0,0,0,2));
        status.setAlignmentX(RIGHT_ALIGNMENT);
        progressBar.setMinimumSize(new Dimension(150,10));
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.weighty = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        panel.add(icon,c);
        c.gridx = 1;
        panel.add(progressBar,c);
        c.gridx = 2;
        //c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(Box.createHorizontalStrut(10),c);
        
        c.weightx = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 4;
        status.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panel.add(status,c);
        add(panel);
        clear();
    }

    public void setMessage(String message) {
        status.setText(message);        
    }    
    
    public void setIcon(ImageIcon icon) {
    	this.icon.setIcon(icon);
    }
    
    public void clear() {
    	status.setText("");
    	icon.setIcon(emptyIcon);
    }
    
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JLabel getStatusLabel() {
		return status;
	}
}