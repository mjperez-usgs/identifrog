package gov.usgs.identifrog.cellrenderers;
 
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
 
/**
 * Custom renderer to display a frogimage in the FrogEditor window with an image and a label describing the status
 *
 */
public class FrogEditorImageRenderer extends JPanel implements ListCellRenderer<SiteImage> {
 
	Color selectedForeground, normalForeground;
	JLabel cellImage, cellLabel;
	public FrogEditorImageRenderer() {
		// TODO Auto-generated constructor stub
		cellImage = new JLabel();
		cellLabel = new JLabel("Loading");
    	cellImage.setOpaque(true);
    	//cellLabel.setOpaque(true);
    	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    	cellImage.setAlignmentX(Component.CENTER_ALIGNMENT);
    	cellLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	}
	
    @Override
    public Component getListCellRendererComponent(JList<? extends SiteImage> list, SiteImage image, int index,
        boolean isSelected, boolean cellHasFocus) {
    	if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
    	
    	ImageIcon icon = null;

		if (image.isSignatureGenerated()) {
			//signature images have been processed already
			icon = new ImageIcon(image.getColorThumbnail());
		} else {
    		//grey
    		icon = new ImageIcon(image.getGreyScaleThumbnail());
		}
    	cellImage.setIcon(icon);
    	if (image.isSignatureGenerated()) {
    		cellLabel.setText("Signature Created");
    	} else {
    		cellLabel.setText("Pending Signature");
    	}
    	
    	cellImage.setMaximumSize(new Dimension(100,75));
    	add(cellImage);
    	add(cellLabel);
    	setBorder(new EmptyBorder(4,4,4,4));
        
    	/*
    	if (!isSelected) {
	      setForeground(Color.RED);
	    } else {
	    	setForeground(Color.white);
	    }*/
    	
    	//ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/" + code + ".png"));
         
        //setIcon(imageIcon);
        //setText(country.getName());
        return this;
    }
}