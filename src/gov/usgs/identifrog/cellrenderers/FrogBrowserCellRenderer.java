package gov.usgs.identifrog.cellrenderers;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.awt.Color;
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
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.imgscalr.Scalr;

public class FrogBrowserCellRenderer extends JPanel implements ListCellRenderer {
	public static Map<Integer, ImageIcon> idImageMap;
	private JLabel imageLabel;
	private JLabel numSamples = new JLabel("Number of samples");
	private JLabel searchStatus = new JLabel("Search status");
	private JLabel numImages = new JLabel("X images");
	private JLabel numDiscriminators = new JLabel("Num Discriminators");
	private JLabel lastCapture = new JLabel("Last capture date");
	private TitledBorder border;
	private static Color full = UIManager.getDefaults().getColor("List.background");
	private static Color partial = new Color(211,211,211);
	private static Color none = new Color(147,147,147);
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
		panel.add(imageLabel, c);

		//row 1
		c.gridwidth = 1;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 1;
		panel.add(numSamples, c);
		c.gridx = 1;
		panel.add(searchStatus, c);

		//row 2
		c.gridx = 0;
		c.gridy = 2;
		panel.add(numImages, c);
		c.gridx = 1;
		panel.add(numDiscriminators, c);

		//row 3
		c.gridx = 0;
		c.gridwidth = 2;
		c.gridy = 3;
		panel.add(lastCapture, c);

		//panel.setMaximumSize(new Dimension(100,130));
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		//Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		//System.out.println(comp.getClass());
		if (value instanceof Frog) {
			Frog frog = (Frog) value;
			border.setTitle("Frog " + frog.getID());
			loadImage(frog);
			imageLabel.setIcon(idImageMap.get(frog.getID()));

			int numberOfSamples = frog.getSiteSamples().size();
			if (numberOfSamples == 1) {
				numSamples.setText(numberOfSamples + " site survey");
			} else {
				numSamples.setText(numberOfSamples + " site surveys");
			}

			int numberOfDiscriminators = frog.getDiscriminators().size();
			if (numberOfDiscriminators == 0) {
				numDiscriminators.setText("No discriminators");
			} else if (numberOfDiscriminators > 1) {
				numDiscriminators.setText(numberOfDiscriminators + " discriminators");
			} else {
				numDiscriminators.setText(numberOfDiscriminators + " discriminator");
			}

			int numberOfImages = frog.getAllSiteImages().size();
			if (numberOfImages == 1) {
				numImages.setText(numberOfImages + " image");
			} else {
				numImages.setText(numberOfImages + " images");
			}

			if (frog.isFreshImport()) {
				lastCapture.setText("Pending information entry");
			} else {
				String latestCapture = frog.getLatestSample().getDateCapture();
				lastCapture.setText("Last captured on " + latestCapture);
			}
			if (frog.isFreshImport()) {
				searchStatus.setText("Not searchable");
				panel.setBackground(none);
			} else if (!frog.isFullySearchable()) {
				searchStatus.setText("Partially searchable");
				panel.setBackground(partial);
			} else {
				searchStatus.setText("Fully searchable");
				panel.setBackground(full);
			}
			if (isSelected) {
				panel.setForeground(list.getSelectionForeground());
				panel.setBackground(list.getSelectionBackground());				
			}

			return panel;
		}
		return this;
	}

	/**
	 * Loads the frog image if the ID is not in the imagemap yet. Does nothing
	 * if it already is.
	 * 
	 * WARNING: MAY CAUSE BUG IF EXISTING FROG WITH HIGH NUMBER WAS DELETED AND
	 * NEW ONE WAS ADDED.
	 * 
	 * @param frog
	 *            Frog to load image for.
	 */
	public void loadImage(Frog frog) {
		if (!idImageMap.containsKey(frog.getID())) {
			try {
				Image returnImg = null;
				//get latest frog image
				SiteImage img = frog.getLatestImage();
				BufferedImage src = ImageIO.read(new File(XMLFrogDatabase.getThumbnailFolder() + img.getImageFileName()));
				BufferedImage thumbnail = Scalr.resize(src, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH, 200, 150, Scalr.OP_ANTIALIAS);
				if (!frog.isFullySearchable()) {
					ImageFilter filter = new GrayFilter(true, 30);
					ImageProducer producer = new FilteredImageSource(thumbnail.getSource(), filter);
					returnImg = Toolkit.getDefaultToolkit().createImage(producer);
					idImageMap.put(frog.getID(), new ImageIcon(returnImg));
				} else {
					idImageMap.put(frog.getID(), new ImageIcon(thumbnail));
				}
			} catch (IOException e) {
				IdentiFrog.LOGGER.writeExceptionWithMessage("Unable to generate thumbnail for image (in memory).", e);
				idImageMap.put(frog.getID(), new ImageIcon(this.getClass().getResource("/resources/IconError32.png")));
			} catch (NullPointerException e) {
				IdentiFrog.LOGGER.writeExceptionWithMessage("NullPointerException for image (in memory). Likely frog has no images: ", e);
				idImageMap.put(frog.getID(), new ImageIcon(this.getClass().getResource("/resources/IconError32.png")));
			}
		}
	}
}
