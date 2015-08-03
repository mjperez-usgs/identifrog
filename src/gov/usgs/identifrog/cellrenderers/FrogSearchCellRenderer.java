package gov.usgs.identifrog.cellrenderers;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.FrogMatch;
import gov.usgs.identifrog.DataObjects.SearchPackage;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.Frames.MainFrame;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.imgscalr.Scalr;

public class FrogSearchCellRenderer extends JPanel implements ListCellRenderer {
	private static final String PLACEHOLDER_TEXT = "PLACEHOLDER_TEXT";
	private Map<Integer, ImageIcon> idImageMap;
	private JLabel imageLabel;
	private JLabel topLeftStatus = new JLabel(PLACEHOLDER_TEXT);
	private JLabel topRightStatus = new JLabel(PLACEHOLDER_TEXT);
	private JLabel leftStatus = new JLabel(PLACEHOLDER_TEXT);
	private JLabel rightStatus = new JLabel(PLACEHOLDER_TEXT);
	private JLabel bottomStatus = new JLabel(PLACEHOLDER_TEXT);
	private TitledBorder border;
	private static Color[] backgrounds = new Color[100];
	
	JPanel panel;
	private EtchedBorder innerborder;

	public FrogSearchCellRenderer() {
		super();
		for (int i = 0; i <= 99 ; i++) {
			float step = (i/99f)*.333333333f;
			//step needs to be a fraction * 360 that is between 1-120. ridiculous
			backgrounds[i] =Color.getHSBColor(step, .5f, 1);
		}
		
		panel = new JPanel(new GridBagLayout());
		innerborder = new EtchedBorder();
		border = new TitledBorder(innerborder, "Frog");
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
		panel.add(topLeftStatus, c);
		c.gridx = 1;
		panel.add(topRightStatus, c);

		//row 2
		c.gridx = 0;
		c.gridy = 2;
		panel.add(leftStatus, c);
		c.gridx = 1;
		panel.add(rightStatus, c);

		//row 3
		c.gridx = 0;
		c.gridwidth = 2;
		c.gridy = 3;
		panel.add(bottomStatus, c);

		//panel.setMaximumSize(new Dimension(100,130));
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		//Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		//System.out.println(comp.getClass());
		if (value instanceof FrogMatch) {
			FrogMatch fm = (FrogMatch) value;
			Frog frog = fm.getFrog();
			border.setTitle("Frog " + frog.getID());
			loadImage(fm);
			imageLabel.setIcon(idImageMap.get(frog.getID()));

			SearchPackage asp = MainFrame.ACTIVE_SEARCH_PACKAGE;
			
			if (asp.getMass() > 0) {
				double latestMass = Double.parseDouble(frog.getLatestSample().getMass());
				double difference = asp.getMass() - latestMass;

				topLeftStatus.setText(latestMass + " grams, \u0394" + difference);
			}
			
			if (asp.getLength() > 0) {
				double latestLength = Double.parseDouble(frog.getLatestSample().getLength());
				double difference = asp.getLength() - latestLength;

				topRightStatus.setText(latestLength + " mm, \u0394" + difference);
			}
			
			if (asp.getImage() != null) {
				//image search
				double topScorePercent = FrogMatch.convertHammingToPercent(fm.getTopScore());
				rightStatus.setText("Average Score: " + IdentiFrog.decimalFormat.format(FrogMatch.convertHammingToPercent(fm.calculateAverageScore()))+"%");
				leftStatus.setText(frog.getAllSiteImages().size() + " images");
				bottomStatus.setText("Top score: " + IdentiFrog.decimalFormat.format(topScorePercent)+"%");
				if (topScorePercent > 0 && topScorePercent <= 100) {
					System.out.println("double: "+topScorePercent+" as int: "+((int) topScorePercent));
					panel.setBackground(backgrounds[(int) topScorePercent]);
				}
			}



			if (isSelected) {
				panel.setForeground(list.getSelectionForeground());
				panel.setBackground(list.getSelectionBackground());
			} else if (asp.getImage()==null) {
				panel.setBackground(Color.white);
			}

			//hide extra items
			if (topLeftStatus.getText().equals(PLACEHOLDER_TEXT)) {
				topLeftStatus.setVisible(false);
			}

			if (topRightStatus.getText().equals(PLACEHOLDER_TEXT)) {
				topRightStatus.setVisible(false);
			}

			if (leftStatus.getText().equals(PLACEHOLDER_TEXT)) {
				leftStatus.setVisible(false);
			}
			if (rightStatus.getText().equals(PLACEHOLDER_TEXT)) {
				rightStatus.setVisible(false);
			}
			if (bottomStatus.getText().equals(PLACEHOLDER_TEXT)) {
				bottomStatus.setText("Last captured on "+frog.getLatestSample().getDateCapture());
			}

			return panel;
		}
		return this;
	}

	/**
	 * Loads the top frog image if the ID is not in the imagemap yet. Does
	 * nothing if it already is.
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
