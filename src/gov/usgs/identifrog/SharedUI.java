package gov.usgs.identifrog;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Shared UI contains static methods for performing operations to the UI such as
 * drawing
 * 
 * @author mjperez
 *
 */
public class SharedUI {
	
	/**
	 * Draws a string centered in the rectangle specified.
	 * Does not dispose of the passed-in graphics.
	 * @param g Graphics to use to draw
	 * @param text text to draw
	 * @param rect rectange to use for boundary
	 * @param font font to draw with
	 */
	public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
		// Get the FontMetrics
		FontMetrics metrics = g.getFontMetrics(font);
		// Determine the X coordinate for the text
		int x = (rect.width - metrics.stringWidth(text)) / 2;
		// Determine the Y coordinate for the text
		int y = ((rect.height - metrics.getHeight()) / 2) - metrics.getAscent();
		// Set the font
		g.setFont(font);
		
		((Graphics2D)g).setRenderingHint(
		        RenderingHints.KEY_TEXT_ANTIALIASING,
		        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// Draw the String
		g.drawString(text, x, y);
	}
}
