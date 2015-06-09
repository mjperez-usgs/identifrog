package gov.usgs.identifrog.Operations;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.awt.BorderLayout;
import java.awt.Dimension;
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

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.imgscalr.Scalr;

public class UITest1 extends JFrame {

	Frog frog;
	
	public UITest1(Frog frog) {
		this.frog = frog;
		init();
		setMinimumSize(new Dimension(200,200));
		setMaximumSize(new Dimension(200,200));
		setResizable(false);
	}
	
	/**
	 * Sets up the user interface.
	 */
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
	}
	
	public Image createListThumbnail() {
		try {
			Image returnImg = null;
			//get latest frog image
			SiteImage img = frog.getLatestImage();
			BufferedImage src = ImageIO.read(new File(XMLFrogDatabase.getImagesFolder()+img.getImageFileName()));
			BufferedImage thumbnail = Scalr.resize(src, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH, 200, 150, Scalr.OP_ANTIALIAS);
			if (!frog.isFullySearchable()){
				ImageFilter filter = new GrayFilter(true, 30);  
				ImageProducer producer = new FilteredImageSource(thumbnail.getSource(), filter);  
				returnImg = Toolkit.getDefaultToolkit().createImage(producer);  
			}
			return returnImg;
		} catch (IOException e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("Unable to generate thumbnail for image (in memory).", e);
		}
		return null;
	}
	
}
