package gov.usgs.identifrog;

import java.awt.Point;

/**
 * <p>
 * Title: SpotFiller.java
 * <p>
 * Description: tracks coordinates of filled spots
 * 
 * @author Oksana V. Kelly 2008
 */
public class SpotCoordinate {
	public int filledSpotNum;
	public Point pixCoor;

	public SpotCoordinate(int spotNum, Point filledpixcoor) {
		filledSpotNum = spotNum;
		pixCoor = filledpixcoor;
	}
}