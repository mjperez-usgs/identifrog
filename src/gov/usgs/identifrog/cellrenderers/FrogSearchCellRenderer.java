package gov.usgs.identifrog.cellrenderers;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.FrogMatch;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.awt.Component;
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

public class FrogSearchCellRenderer extends JPanel implements ListCellRenderer {
	private Map<Integer, ImageIcon> idImageMap;
	private JLabel imageLabel;
	private JLabel topScore = new JLabel("Top Score");
	private JLabel averageScore = new JLabel("Average Score");
	private JLabel score2 = new JLabel("Score 2");
	private JLabel score3 = new JLabel("Score 3");
	private JLabel score4 = new JLabel("Score 4");
	private JLabel score5 = new JLabel("Score 5");
	private TitledBorder border;
	JPanel panel;

	public FrogSearchCellRenderer() {
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
		c.gridy = 0;
		c.weightx = .75;
		c.weighty = 0;
		c.gridheight = 4;
		c.fill = GridBagConstraints.NONE;
		panel.add(imageLabel, c);

		//scores
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = .25;
		c.weighty = 1;
		panel.add(score2, c);
		c.gridy = 1;
		panel.add(score3, c);
		c.gridy = 2;
		panel.add(score4, c);
		c.gridy = 3;
		panel.add(score5, c);

		//Top Score
		c.gridx = 0;
		c.gridy = 4;
		c.weighty = 0;
		c.gridwidth = 2;
		panel.add(topScore, c);
		c.gridy = 5;
		panel.add(averageScore, c);
		panel.setMaximumSize(new Dimension(100,130));
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		//Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		//System.out.println(comp.getClass());
		if (value instanceof FrogMatch) {
			panel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
			panel.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			//border.setTitleColor(isSelected ?  : list.getForeground());

			FrogMatch match = (FrogMatch) value;
			border.setTitle("Frog " + match.getFrog().getID());
			loadImage(match);
			imageLabel.setIcon(idImageMap.get(match.getFrog().getID()));
			
			topScore.setText("Top Score: "+match.getTopScore());
			averageScore.setText("Average Score: "+match.calculateAverageScore());
			return panel;
		}
		return this;
	}

	/**
	 * Loads the top frog image if the ID is not in the imagemap yet. Does nothing
	 * if it already is.
	 * 
	 * 
	 * @param match
	 *            Frog to load image for.
	 */
	public void loadImage(FrogMatch match) {
		if (!idImageMap.containsKey(match.getFrog().getID())) {
			try {
				SiteImage img = match.getTopImage().getImage();
				BufferedImage src = ImageIO.read(new File(XMLFrogDatabase.getThumbnailFolder() + img.getImageFileName()));
				BufferedImage thumbnail = Scalr.resize(src, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH, 200, 150, Scalr.OP_ANTIALIAS);
				idImageMap.put(match.getFrog().getID(), new ImageIcon(thumbnail));
			} catch (IOException e) {
				IdentiFrog.LOGGER.writeExceptionWithMessage("Unable to generate thumbnail for image (in memory).", e);
				idImageMap.put(match.getFrog().getID(), new ImageIcon(this.getClass().getResource("/resources/IconError32.png")));
			} catch (NullPointerException e) {
				IdentiFrog.LOGGER.writeExceptionWithMessage("NullPointerException for image (in memory). Likely frog has no images", e);
				idImageMap.put(match.getFrog().getID(), new ImageIcon(this.getClass().getResource("/resources/IconError32.png")));
			}
		}
	}
}

