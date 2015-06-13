package gov.usgs.identifrog.cellrenderers;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.imgscalr.Scalr;

public class FrogBrowserCellRenderer extends JPanel implements ListCellRenderer {
	private Map<Integer, ImageIcon> idImageMap;
	private JLabel imageLabel;
	private JLabel numSamples = new JLabel("Number of samples");
	private JLabel searchStatus = new JLabel("Search status");
	private TitledBorder border;
	JPanel panel;
	
	public FrogBrowserCellRenderer() {
		super();
		panel = new JPanel(new GridBagLayout());
		
		border = new TitledBorder(new EtchedBorder(), "Frog");
		panel.setBorder(border);
		imageLabel = new JLabel();
		imageLabel.setHorizontalAlignment(JLabel.CENTER);
		imageLabel.setVerticalAlignment(JLabel.CENTER);
		//Dimension maxSize = new Dimension(200,200);
		//panel.setMinimumSize(maxSize);
		//panel.setMaximumSize(maxSize);
		idImageMap = new HashMap<Integer, ImageIcon>();
		
		//configure UI
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.NONE;
		panel.add(imageLabel,c);
		
		c.gridwidth = 1;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 1;
		panel.add(numSamples,c);
		c.gridx = 1;
		panel.add(searchStatus, c);
	}
	
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		//Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		//System.out.println(comp.getClass());
		if (value instanceof Frog) {
			panel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
			panel.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			//border.setTitleColor(isSelected ?  : list.getForeground());
			
			
			Frog frog = (Frog) value;
			loadImage(frog);
			imageLabel.setIcon(idImageMap.get(frog.getID()));
			
			int numberOfSamples = frog.getSiteSamples().size();
			if (numberOfSamples == 1) {
				numSamples.setText(numberOfSamples + " site survey");
			} else {
				numSamples.setText(numberOfSamples + " site surveys");
			}
			if (!frog.isFullySearchable()) {
				searchStatus.setText("Partially searchable");
			} else {
				searchStatus.setText("Fully searchable");
			}
			return panel;
		}
		return this;
	}
	
	/**
	 * Loads the frog image if the ID is not in the imagemap yet.
	 * Does nothing if it already is.
	 * 
	 * WARNING: MAY CAUSE BUG IF EXISTING FROG WITH HIGH NUMBER WAS DELETED AND NEW ONE WAS ADDED.
	 * 
	 * @param frog Frog to load image for.
	 */
	public void loadImage(Frog frog) {
		if (!idImageMap.containsKey(frog.getID())) {
			try {
				Image returnImg = null;
				//get latest frog image
				SiteImage img = frog.getLatestImage();
				BufferedImage src = ImageIO.read(new File(XMLFrogDatabase.getThumbnailFolder()+img.getImageFileName()));
				BufferedImage thumbnail = Scalr.resize(src, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH, 200, 150, Scalr.OP_ANTIALIAS);
				if (!frog.isFullySearchable()){
					ImageFilter filter = new GrayFilter(true, 30);  
					ImageProducer producer = new FilteredImageSource(thumbnail.getSource(), filter);  
					returnImg = Toolkit.getDefaultToolkit().createImage(producer);  
					idImageMap.put(frog.getID(), new ImageIcon(returnImg));
				} else {
					idImageMap.put(frog.getID(), new ImageIcon(thumbnail));
				}
			} catch (IOException e) {
				IdentiFrog.LOGGER.writeExceptionWithMessage("Unable to generate thumbnail for image (in memory).", e);
				idImageMap.put(frog.getID(),new ImageIcon(this.getClass().getResource("/resources/IconError32.png")));
			} catch (NullPointerException e) {
				IdentiFrog.LOGGER.writeExceptionWithMessage("NullPointerException for image (in memory). Likely frog has no images", e);
				idImageMap.put(frog.getID(),new ImageIcon(this.getClass().getResource("/resources/IconError32.png")));
			}
		}
	}
}

/**
 * Sets up the user interface.
 *//*
public void init(){
	JPanel cardPanel = new JPanel(new BorderLayout());
	JLabel image = new JLabel();
	image.setIcon(new ImageIcon(createListThumbnail()));
	image.setHorizontalAlignment(JLabel.CENTER);
	image.setVerticalAlignment(JLabel.CENTER);
	cardPanel.add(image, BorderLayout.CENTER);
	
	
	JPanel statsPanel = new JPanel(new GridBagLayout());
	JLabel numSamples = new JLabel("Number of samples");
	JLabel searchStatus = new JLabel("Search status");
	
	//load frog text
	int numberOfSamples = frog.getSiteSamples().size();
	if (numberOfSamples == 1) {
		numSamples.setText(numberOfSamples + " site survey");
	} else {
		numSamples.setText(numberOfSamples + " site surveys");
	}
	if (frog.isFullySearchable()) {
		searchStatus.setText("Partially searchable");
	} else {
		searchStatus.setText("Fully searchable");
	}
	
	GridBagConstraints c = new GridBagConstraints();
	c.gridx = 0;
	c.gridy = 0;
	c.fill = GridBagConstraints.HORIZONTAL;
	statsPanel.add(numSamples,c);
	c.gridx = 1;
	statsPanel.add(searchStatus, c);
	cardPanel.add(statsPanel, BorderLayout.SOUTH);
	add(cardPanel);
	pack();
}*/

	