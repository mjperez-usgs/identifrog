package gov.usgs.identifrog;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

/*
 * <p>Title: FillSpot.java </p>
 * <p>Description: Flood Fill Algorithm to fill the inner spot region
 * Fills the selected pixel and all surrounding pixels of the same color with the fill color.
 * @param img image on which operation is applied
 * @param fillColor color to be filled in
 * @param loc location at which to start fill
 * @throws IllegalArgumentException if loc is out of bounds of the image </p>
 *
 * http://www.codecodex.com/wiki/Implementing_the_flood_fill_algorithm
 *
 @author Oksana V. Kelly modified the code written by Claudio Santana and Damian Johnson
 * <p>This software is released into the public domain.</p>
 */

public class FillSpot {

	private WritableRaster raster, result_raster;
	private int white = (255 & 0xff) << 16 | (255 & 0xff) << 8 | 255 & 0xff;
	public BufferedImage newimg;
	int rect_width, rect_height;

	private ArrayList<SpotCoordinate> spotCoor = new ArrayList<SpotCoordinate>();

	/**
	 * Create a buffered image that contains a flood filled image from the point loc with the color fillColor using the image edgeImage as a base for checking outlines
	 * @param edgeImage
	 * @param fillColor
	 * @param loc
	 * @return
	 */
	public BufferedImage floodFill(BufferedImage edgeImage, Color fillColor, Point loc) {
		if (loc.x < 0 || loc.x >= edgeImage.getWidth() || loc.y < 0 || loc.y >= edgeImage.getHeight()) {
			throw new IllegalArgumentException();
		}

		rect_width = edgeImage.getWidth();
		rect_height = edgeImage.getHeight();

		newimg = new BufferedImage(edgeImage.getWidth(), edgeImage.getHeight(), BufferedImage.TYPE_INT_RGB);

		BufferedImage filledImage = edgeImage.getSubimage(0, 0, edgeImage.getWidth(), edgeImage.getHeight());
		raster = edgeImage.getRaster();
		result_raster = filledImage.getRaster();

		int[] fill = new int[] { fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue() };
		int[] old = raster.getPixel(loc.x, loc.y, new int[3]);

		// Checks trivial case where loc is of the fill color
		if (isEqualRgb(fill, old)) {
			return null;
		}
		floodLoop(loc.x, loc.y, fill, old);

		return filledImage;
	}

	// Recursively fills surrounding pixels of the old color
	private void floodLoop(int x, int y, int[] fill, int[] old) {
		Rectangle bounds = raster.getBounds();
		int[] aux = { 255, 255, 255 };

		// finds the left side, filling along the way
		int fillL = x;
		do {
			newimg.setRGB(fillL, y, white);
			if (isEqualRgb(raster.getPixel(fillL, y, aux), old)) {
				newimg.setRGB(fillL, y, white);
			}

			result_raster.setPixel(fillL, y, fill);
			fillL--;

		} while (fillL >= 0 && isEqualRgb(raster.getPixel(fillL, y, aux), old));
		fillL++;

		// find the right right side, filling along the way
		int fillR = x;
		do {
			newimg.setRGB(fillR, y, white);
			if (isEqualRgb(raster.getPixel(fillR, y, aux), old)) {
				newimg.setRGB(fillR, y, white);
			}

			result_raster.setPixel(fillR, y, fill); // filling
			fillR++;

		} while (fillR < bounds.width && isEqualRgb(raster.getPixel(fillR, y, aux), old));
		fillR--;

		// checks if applicable up or down
		for (int i = fillL; i <= fillR; i++) {
			if (y > 0 && isEqualRgb(raster.getPixel(i, y - 1, aux), old)) {
				floodLoop(i, y - 1, fill, old);
			}
			if (y < bounds.height - 1 && isEqualRgb(raster.getPixel(i, y + 1, aux), old)) {
				floodLoop(i, y + 1, fill, old);
			}

		}

	}

	// Returns true if RGB arrays are equivalent, false otherwise
	// Could use Arrays.equals(int[], int[]), but this is probably a little faster...
	private boolean isEqualRgb(int[] pix1, int[] pix2) {
		return pix1[0] == pix2[0] && pix1[1] == pix2[1] && pix1[2] == pix2[2];
	}

	/**
	 * This method has side effects
	 * @param dilationRadius
	 * @param currentfilledSpotNum
	 * @return
	 */
	public BufferedImage getFilledSpot(int dilationRadius, int currentfilledSpotNum) {

		SpotCoordinate spotFiller;
		// 10 means radius = 1.0
		/*
     */
		int dilation_radius = dilationRadius + 10; // since dilation grows the edges, 1 more pixel for edges
		int window = dilation_radius;

		BufferedImage imgExpanded = new BufferedImage(rect_width + window - 1, rect_height + window - 1, BufferedImage.TYPE_INT_RGB);
		BufferedImage dilatedImg = new BufferedImage(rect_width + window - 1, rect_height + window - 1, BufferedImage.TYPE_INT_RGB);
		BufferedImage newdilatedImg = new BufferedImage(rect_width, rect_height, BufferedImage.TYPE_INT_RGB);

		// pad image borders with the closest border pixels before filtering
		// thus border pixels will be extended beyond the image border
		ImageBorder imageBorder = new ImageBorder();
		imgExpanded = imageBorder.expandImageBorder(newimg, window);

		BinMorpher binMorpher = new BinMorpher();
		dilatedImg = binMorpher.dilate(imgExpanded, dilation_radius / 10f);
		dilatedImg = binMorpher.close(dilatedImg, dilationRadius / 10f);

		// from expanded image size back to rect_width, rect_heigh
		int offset = Math.round(window / 2);
		for (int c = offset; c < rect_width + offset; ++c) {
			for (int r = offset; r < rect_height + offset; ++r) {
				int pixel = dilatedImg.getRGB(c, r);
				newdilatedImg.setRGB(c - offset, r - offset, pixel);

				// track pixel coordinates of currently filled spot currentfilledSpotNum
				int red = (pixel & 0xff0000) >> 16;
				if (red > 0) {
					Point pixXY = new Point(c - offset, r - offset);
					spotFiller = new SpotCoordinate(currentfilledSpotNum, pixXY);
					spotCoor.add(spotFiller);
				}
			}
		}

		return newdilatedImg;
	}

	public ArrayList<SpotCoordinate> getFilledSpotCoor() {
		return spotCoor;
	}

} // end of Class

