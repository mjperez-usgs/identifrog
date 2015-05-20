package gov.usgs.identifrog;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.net.URL;

/**
 * <p>
 * Title: SplashScreen.java
 * <p>
 * Description: The splash screen lets the user know tht the program is starting.
 * 
 * @author Oksana V. Kelly 2008
 * @author Steven P. Miller for IdentiFrog Team 2005
 */
@SuppressWarnings("serial")
public final class SplashScreen extends Frame {
	private final String fImageId;
	private MediaTracker fMediaTracker;
	private Image fImage;

	public SplashScreen(String aImageId) {
		if (aImageId == null || aImageId.trim().length() == 0) {
			throw new IllegalArgumentException("Image Id does not have content.");
		}
		fImageId = aImageId;
	}

	public void splash() {
		initImageAndTracker();
		setSize(fImage.getWidth(null), fImage.getHeight(null));
		center();
		fMediaTracker.addImage(fImage, 0);
		try {
			fMediaTracker.waitForID(0);
		} catch (InterruptedException ie) {
			IdentiFrog.LOGGER.writeException(ie);
		}
		@SuppressWarnings("unused")
		SplashWindow splashWindow = new SplashWindow(this, fImage);
	}

	private void initImageAndTracker() {
		fMediaTracker = new MediaTracker(this);
		URL imageURL = SplashScreen.class.getResource(fImageId);
		fImage = Toolkit.getDefaultToolkit().getImage(imageURL);
	}

	/**
	 * Centers the frame on the screen. This centering service is more or less in {@link UiUtil};
	 * this duplication is justified only because the use of {@link UiUtil} would entail more class
	 * loading, which is not desirable for a splash screen.
	 */
	private void center() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle frame = getBounds();
		setLocation((screen.width - frame.width) / 2, (screen.height - frame.height) / 2);
	}

	private class SplashWindow extends Window {
		private Image fImage;

		SplashWindow(Frame aParent, Image aImage) {
			super(aParent);
			fImage = aImage;
			setSize(fImage.getWidth(null), fImage.getHeight(null));
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			Rectangle window = getBounds();
			setLocation((screen.width - window.width) / 2, (screen.height - window.height) / 2);
			setVisible(true);
		}

		@Override
		public void paint(Graphics graphics) {
			if (fImage != null) {
				graphics.drawImage(fImage, 0, 0, this);
			}
		}
	}
}