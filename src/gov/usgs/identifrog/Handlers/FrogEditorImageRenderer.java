package gov.usgs.identifrog.Handlers;
 
import gov.usgs.identifrog.DataObjects.SiteImage;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
 
/**
 * Custom renderer to display a country's flag alongside its name
 *
 * @author wwww.codejava.net
 */
public class FrogEditorImageRenderer extends JPanel implements ListCellRenderer<SiteImage> {
 
    @Override
    public Component getListCellRendererComponent(JList<? extends SiteImage> list, SiteImage image, int index,
        boolean isSelected, boolean cellHasFocus) {
        
    	setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/" + code + ".png"));
         
        setIcon(imageIcon);
        setText(country.getName());
         
        return this;
    }
     
}