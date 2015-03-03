package gov.usgs.identifrog;

import java.awt.image.BufferedImage;

/**
 * <p>
 * Title: ImageBorder.java
 * </p>
 * <p>
 * Description: expands the image border before filtering operations.
 * <p>
 * This software is released into the public domain.
 * </p>
 * 
 * @author Oksana V. Kelly 2008
 */

public class ImageBorder {

	public BufferedImage expandImageBorder(BufferedImage img, int wsize) {

		int rect_height = img.getHeight();
		int rect_width = img.getWidth();
		BufferedImage expanded_img = new BufferedImage(rect_width + wsize - 1, rect_height + wsize - 1, img.getType());

		// pad image borders with the closest border pixels before filtering
		// thus border pixels will be extended beyound the image border
		int offset = Math.round(wsize / 2);
		int zero = (0 & 0xff) << 16 | (0 & 0xff) << 8 | 0 & 0xff;

		for (int col = 0; col < rect_width + wsize - 1; ++col) {
			for (int row = 0; row < rect_height + wsize - 1; ++row) {

				if (row >= offset && row < rect_height + offset && col >= offset && col < rect_width + offset) {
					// System.out.println(row + " col " + col);
					int pix = img.getRGB(col - offset, row - offset);
					expanded_img.setRGB(col, row, pix);
				}
				// pad four corner areas with 0s
				if ((row < offset || row >= rect_height + offset) && (col < offset || col >= rect_width + offset)) {
					// System.out.println(row + " col " + col);
					expanded_img.setRGB(col, row, zero);
				}
			}
		}
		// copy border pixels to extended new borders
		for (int row = 0; row < offset; ++row) {
			int otherimagecol = 0;
			for (int col = offset; col < rect_width + offset; ++col) {
				int pix = img.getRGB(otherimagecol, 0);
				expanded_img.setRGB(col, row, pix);
				++otherimagecol;
			}
		}
		// copy border pixels to extended new borders
		for (int row = rect_height + offset; row < rect_height + wsize - 1; ++row) {
			int otherimagecol = 0;
			for (int col = offset; col < rect_width + offset; ++col) {
				int pix = img.getRGB(otherimagecol, rect_height - 1);
				expanded_img.setRGB(col, row, pix);
				++otherimagecol;
			}
		}

		// copy border pixels to extended new borders
		for (int col = 0; col < offset; ++col) {
			int otherimagerow = 0;
			for (int row = offset; row < rect_height + offset; ++row) {
				int pix = img.getRGB(0, otherimagerow);
				expanded_img.setRGB(col, row, pix);
				++otherimagerow;
			}
		}
		// copy border pixels to extended new borders
		for (int col = rect_width + offset; col < rect_width + wsize - 1; ++col) {
			int otherimagerow = 0;
			for (int row = offset; row < rect_height + offset; ++row) {
				int pix = img.getRGB(rect_width - 1, otherimagerow);
				expanded_img.setRGB(col, row, pix);
				++otherimagerow;
			}
		}

		return expanded_img;
	}
}
