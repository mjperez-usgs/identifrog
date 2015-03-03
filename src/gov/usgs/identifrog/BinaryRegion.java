/**
 * This sample code is made available as part of the book "Digital Image
 * Processing - An Algorithmic Introduction using Java" by Wilhelm Burger
 * and Mark J. Burge, Copyright (C) <i>2005</i>-2008 Springer-Verlag Berlin,
 * Heidelberg, New York.
 * Note that this code comes with absolutely no warranty of any kind.
 * See http://www.imagingbook.com for details and licensing conditions.
 * This software is released under the terms of the GNU Lesser General Public License (LGPL).
 * Date: 2007/11/10
 */

package gov.usgs.identifrog;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

/*
 * This class is used to incrementally compute and maintain
 * the statistics of a binary region.
 */

public class BinaryRegion {
	int label;
	int numberOfPixels = 0;
	double xc = Double.NaN;
	double yc = Double.NaN;
	int left = Integer.MAX_VALUE;
	int right = -1;
	int top = Integer.MAX_VALUE;
	int bottom = -1;

	// auxiliary variables
	int x_sum = 0;
	int y_sum = 0;
	int x2_sum = 0;
	int y2_sum = 0;

	// ------- public methods --------------------------

	public BinaryRegion(int id) {
		label = id;
	}

	public int getSize() {
		return numberOfPixels;
	}

	public Rectangle getBoundingBox() {
		if (left == Integer.MAX_VALUE) {
			return null;
		} else {
			return new Rectangle(left, top, right - left + 1, bottom - top + 1);
		}
	}

	public Point2D.Double getCenter() {
		if (Double.isNaN(xc)) {
			return null;
		} else {
			return new Point2D.Double(xc, yc);
		}
	}

	public double getOrientation() { // TO BE DONE
		if (numberOfPixels < 1) {
			throw new Error("uninitialized region (no orientation)");
		}
		return 0;
	}

	public double getEccentricityValue() { // TO BE DONE
		if (numberOfPixels < 1) {
			throw new Error("uninitialized region (no eccentricity)");
		}
		return 0;
	}

	public Point2D.Double getEccentricityVector() { // TO BE DONE
		if (numberOfPixels < 1) {
			throw new Error("uninitialized region (no eccentricity)");
		}
		return null;
	}

	public void addPixel(int x, int y) {
		numberOfPixels = numberOfPixels + 1;
		x_sum = x_sum + x;
		y_sum = y_sum + y;
		x2_sum = x2_sum + x * x;
		y2_sum = y2_sum + y * y;
		if (x < left) {
			left = x;
		}
		if (y < top) {
			top = y;
		}
		if (x > right) {
			right = x;
		}
		if (y > bottom) {
			bottom = y;
		}
	}

	public void update() {
		if (numberOfPixels > 0) {
			double x = (double) x_sum / numberOfPixels;
			double y = (double) y_sum / numberOfPixels;
			xc = trunc(x, 2);
			yc = trunc(y, 2);

		}
	}

	// --------- local methods -------------------

	String trunc(double d) {
		long k = Math.round(d * 100);
		return String.valueOf(k / 100.0);
	}

	double trunc(double d, int precision) {
		double m = Math.pow(10, precision);
		long k = Math.round(d * m);
		double v = k / m;
		return v;
	}

}
