package gov.usgs.identifrog;

import java.util.ArrayList;

/**
 * <p>
 * Title: PassVector.java
 * <p>
 * Description: computes number of relevant spots per image quadrant
 * 
 * @author Oksana V. Kelly 2008
 */
public class PassVector {
	public int[] makePassVector(ArrayList<BinaryRegion> Centroids, int imgW, int imgH) {
		// Standardized Rectangle
		final int half_rect_width = Math.round(imgW / 2);
		final int half_rect_height = Math.round(imgH / 2);
		int[] passvector = new int[5];
		/* ------------------------------- Pass 0 --------------------------------- */
		/* --------- total number of relevant spots in the standard image --------- */
		passvector[0] = Centroids.size();
		/* ------------------------------- Pass 1 --------------------------------- */
		/* ----- total number of relevant spots in each of the four quadrants ----- */
		for (int i = 0; i < Centroids.size(); ++i) {
			if (Centroids.get(i).xc <= half_rect_width) {
				if (Centroids.get(i).yc <= half_rect_height) {
					passvector[1] = passvector[1] + 1;
				} else {
					passvector[2] = passvector[2] + 1;
				}
			} else {
				if (Centroids.get(i).yc <= half_rect_height) {
					passvector[3] = passvector[3] + 1;
				} else {
					passvector[4] = passvector[4] + 1;
				}
			}
		}
		return passvector;
	}
}