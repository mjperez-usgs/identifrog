package gov.usgs.identifrog.DataObjects;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Defines a spot in part of a binary image of a frog. Part of a constellation.
 * 
 * @author mjperez
 *
 */
public class Spot {
	private ArrayList<Point> filledArea;
	private int spotNum;
	private Point2D.Double centroid;
	private double percentCoverage;

	/**
	 * Gets list of filled area as Point(x,y)
	 * 
	 * @return
	 */
	public ArrayList<Point> getFilledArea() {
		return filledArea;
	}

	/**
	 * Sets the list of filled area via a list of Point(x,y)
	 * 
	 * @param filledArea
	 */
	public void setFilledArea(ArrayList<Point> filledArea) {
		this.filledArea = filledArea;
	}

	/**
	 * Gets this spot number.
	 * 
	 * @return
	 */
	public int getSpotNum() {
		return spotNum;
	}

	/**
	 * Sets this spot's number.
	 * 
	 * @param spotNum
	 */
	public void setSpotNum(int spotNum) {
		this.spotNum = spotNum;
	}

	/**
	 * Get's the amount of coverage this spot has between 0 and 1 (inclusive)
	 * 
	 * @return
	 */
	public double getPercentCoverage() {
		return percentCoverage;
	}
	
	public boolean isFullCoverage() {
		return percentCoverage >= 1;
	}

	/**
	 * Sets the percent coverage of the object
	 * 
	 * @param percentCoverage
	 */
	public void setPercentCoverage(double percentCoverage) {
		assert percentCoverage > 0 && percentCoverage <= 1.0;
		this.percentCoverage = percentCoverage;
	}

	/**
	 * Gets the centroid of this spot by averaging the X and Y values of each
	 * pixel, and returns it in a Point2D with values stored as doubles (15.40,
	 * 16.9, etc).
	 * 
	 * @return Newly calculated centroid if this method was not previously
	 *         accessed, cached one if centroid was previously accessed.
	 */
	public Point2D.Double getCentroid() {
		if (centroid == null) {
			//find centroid
			//can probably be more efficient
			double xsum = 0, ysum = 0;
			for (Point p : filledArea) {
				xsum += p.getX();
				ysum += p.getY();
			}

			centroid = new Point2D.Double(xsum / filledArea.size(), ysum / filledArea.size()); //point does not accept double as a constructor
		}
		return centroid;
	}
}
