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
public class SpotFiller {
	public int filledSpotNum;
	public Point pixCoor;

	public SpotFiller(int spotNum, Point filledpixcoor) {
		filledSpotNum = spotNum;
		pixCoor = filledpixcoor;
	}
}