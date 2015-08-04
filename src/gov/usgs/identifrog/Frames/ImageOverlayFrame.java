package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.DataObjects.SiteImage;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This frame allows you to drag one dorsal view over another with the top one
 * being semi transparent. It does not use a layout to allow the user absolute
 * dragging position
 * 
 * @author mjperez
 *
 */
public class ImageOverlayFrame extends JFrame {

	private SiteImage bottom;
	private SiteImage top;
	private BufferedImage loadedTop, loadedBottom;
	private ImageIcon currentTop, currentBottom;
	int overlayx, overlayy;
	private JLabel dorsalLabel;
	float opacity = 0.5f;
	long flickerRate = 100;
	Timer flickerTimer;
	float previousOpacity = opacity;
	protected boolean timerIsRunning = false;
	private boolean drawTop = true;

	public ImageOverlayFrame(JFrame callingFrame, SiteImage bottom, SiteImage top) {
		this.bottom = bottom;
		this.top = top;
		init();
		setLocationRelativeTo(callingFrame);
		setVisible(true);
	}

	private void init() {
		setMinimumSize(new Dimension(400, 300));
		setTitle("Dorsal Comparison");
		//new ImageIcon(this.getClass().getClassLoader().getResource("/resources/IconFrog.png"));
		setIconImage(new ImageIcon(this.getClass().getResource("/resources/IconFrog.png")).getImage());

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		dorsalLabel = new JLabel("Images failed to load");
		JButton up = new JButton("↑");
		JButton left = new JButton("←");
		JButton right = new JButton("→");
		JButton down = new JButton("↓");

		up.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				overlayy -= 3;
				updateOverlay();
			}
		});

		down.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				overlayy += 3;
				updateOverlay();
			}
		});

		left.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				overlayx -= 3;
				updateOverlay();
			}
		});

		right.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				overlayx += 3;
				updateOverlay();
			}
		});

		JButton opacityUp = new JButton("More Opacity");
		JButton flicker = new JButton("Flicker Images");
		JButton opacityDown = new JButton("Less Opacity");
		opacityUp.setToolTipText("Opacity is the amount of solid color of an image. It is the opposite of transparency.");
		flicker.setToolTipText("Causes dorsals to alternate at full opacity quickly");
		opacityDown.setToolTipText("Opacity is the amount of transparency of an image. It is the opposite of transparency.");

		opacityUp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				opacity += .05f;
				if (opacity > 1) {
					opacity = 1;
					opacityUp.setEnabled(false);
				}
				opacityDown.setEnabled(true);
				updateOverlay();
			}
		});

		opacityDown.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				opacity -= .05f;
				if (opacity < 0) {
					opacity = 0;
					opacityDown.setEnabled(false);
				}
				opacityUp.setEnabled(true);
				updateOverlay();
			}
		});

		flicker.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (flickerTimer == null) {
					flickerTimer = new Timer();
				}
				if (timerIsRunning) {
					flickerTimer.cancel();
					flickerTimer.purge();
					flickerTimer = null;
					timerIsRunning = !timerIsRunning;
					flicker.setText("Flicker Images");
					opacity = previousOpacity;
					updateOverlay();
					return;
				}
				previousOpacity = opacity;
				flicker.setText("Stop flickering");
				timerIsRunning = !timerIsRunning;
				flickerTimer.scheduleAtFixedRate(new FlickerTask(), 0, flickerRate);
			}
		});
		updateOverlay();

		c.gridwidth = 11;
		panel.add(dorsalLabel, c);

		c.gridy = 1;
		c.gridwidth = 1;
		c.gridx = 1;

		panel.add(up, c);
		c.gridy = 2;
		c.gridx = 0;
		panel.add(left, c);
		c.gridx = 2;
		panel.add(right, c);
		c.gridy = 3;
		c.gridx = 1;
		panel.add(down, c);

		//opacity buttons
		c.anchor = GridBagConstraints.EAST;
		c.gridy = 1;
		c.gridx = 10;
		panel.add(opacityUp, c);

		c.gridx = 10;
		c.gridy = 2;
		panel.add(flicker, c);
		c.gridy = 3;
		c.gridx = 10;
		panel.add(opacityDown, c);

		add(panel);
		pack();
	}

	private void toggleTopImage() {
		drawTop = !drawTop;
	}

	private void updateOverlay() {
		ImageIcon icon = generateOverlay();
		if (icon != null) {
			dorsalLabel.setText(null);
			dorsalLabel.setIcon(icon);
		}
		System.gc(); //we can waste a lot of memory if this doesn't run for a while
	}

	private ImageIcon generateOverlay() {
		try {
			if (loadedTop == null) {
				loadedTop = ImageIO.read(new File(this.bottom.getDorsalImage()));
			}
			if (loadedBottom == null) {
				loadedBottom = ImageIO.read(new File(this.top.getDorsalImage()));
			}

			int w = Math.max(loadedBottom.getWidth(), loadedTop.getWidth());
			int h = Math.max(loadedBottom.getHeight(), loadedTop.getHeight());
			BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

			Graphics2D g = combined.createGraphics();
			g.drawImage(loadedBottom, 0, 0, null);
			if (drawTop ) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				g.drawImage(loadedTop, overlayx, overlayy, null);
			}
			return new ImageIcon(combined);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	class FlickerTask extends TimerTask {
		boolean topLayerOn = false;

		public void run() {
			opacity = 1;
			toggleTopImage();
			updateOverlay();
		}
	}
}
