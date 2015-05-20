package gov.usgs.identifrog;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * <p>
 * Title: HammingDistance.java
 * </p>
 * <p>
 * Description: returns min Hamming distance of nine Ham.distances computed when query low
 * resolution image is shifted by one pixel over the database image in nine different directions.
 * The Ham.dist is computed in the overlap, normalized by the overlap
 * </p>
 * <p>
 * This software is released into the public domain.
 * </p>
 * 
 * @author Oksana V. Kelly 2008
 */
public class HammingDistance {

	private static final int threshold = 192; // pixel value 3/4 of 256
	private static final int directionshifts = 9;

	// private static final int shiftamount = 1;

	public double getHammingDistance(String filename1, String filename2) {

		BufferedImage binary_image1 = null;
		BufferedImage binary_image2 = null;

		double[] hamdistances = new double[directionshifts];
		Raster db, query;

		try {
			binary_image1 = ImageIO.read(new File(filename1));
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeException(e);
		}

		try {
			binary_image2 = ImageIO.read(new File(filename2));
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeException(e);
		}

		int img_width = binary_image1.getWidth();
		int img_height = binary_image1.getHeight();

		query = binary_image1.getRaster();
		db = binary_image2.getRaster();

		// no shift
		int[] queryNoshift = query.getSamples(0, 0, img_width, img_height, 1, (int[]) null); // 5th
																								// parameter
																								// to
																								// get
																								// green=1
		int[] dbNoshift = db.getSamples(0, 0, img_width, img_height, 1, (int[]) null);
		hamdistances[0] = computeHamDist(queryNoshift, dbNoshift);

		// shift North
		int[] queryNorth = query.getSamples(0, 1, img_width, img_height - 1, 1, (int[]) null);
		int[] dbNorth = db.getSamples(0, 0, img_width, img_height - 1, 1, (int[]) null);
		hamdistances[1] = computeHamDist(queryNorth, dbNorth);

		// shift South
		int[] querySouth = query.getSamples(0, 0, img_width, img_height - 1, 1, (int[]) null);
		int[] dbSouth = db.getSamples(0, 1, img_width, img_height - 1, 1, (int[]) null);
		hamdistances[2] = computeHamDist(querySouth, dbSouth);

		// shift East
		int[] queryEast = query.getSamples(0, 0, img_width - 1, img_height, 1, (int[]) null);
		int[] dbEast = db.getSamples(1, 0, img_width - 1, img_height, 1, (int[]) null);
		hamdistances[3] = computeHamDist(queryEast, dbEast);

		// shift West
		int[] queryWest = query.getSamples(1, 0, img_width - 1, img_height, 1, (int[]) null);
		int[] dbWest = db.getSamples(0, 0, img_width - 1, img_height, 1, (int[]) null);
		hamdistances[4] = computeHamDist(queryWest, dbWest);

		// shift North East
		int[] queryNE = query.getSamples(0, 1, img_width - 1, img_height - 1, 1, (int[]) null);
		int[] dbNE = db.getSamples(1, 0, img_width - 1, img_height - 1, 1, (int[]) null);
		hamdistances[5] = computeHamDist(queryNE, dbNE);

		// shift North West
		int[] queryNW = query.getSamples(1, 1, img_width - 1, img_height - 1, 1, (int[]) null);
		int[] dbNW = db.getSamples(0, 0, img_width - 1, img_height - 1, 1, (int[]) null);
		hamdistances[6] = computeHamDist(queryNW, dbNW);

		// shift South East
		int[] querySE = query.getSamples(0, 0, img_width - 1, img_height - 1, 1, (int[]) null);
		int[] dbSE = db.getSamples(1, 1, img_width - 1, img_height - 1, 1, (int[]) null);
		hamdistances[7] = computeHamDist(querySE, dbSE);

		// shift South West
		int[] querySW = query.getSamples(1, 0, img_width - 1, img_height - 1, 1, (int[]) null);
		int[] dbSW = db.getSamples(0, 1, img_width - 1, img_height - 1, 1, (int[]) null);
		hamdistances[8] = computeHamDist(querySW, dbSW);

		// IdentiFrog.LOGGER.writeMessage("0=" + hamdistances[0] + " 1=" + hamdistances[1] + " 2=" +
		// hamdistances[2] + " 3=" + hamdistances[3] + " 4=" + hamdistances[4] + " 5=" +
		// hamdistances[5] + " 6=" + hamdistances[6] + " 7=" + hamdistances[7] + " 8=" +
		// hamdistances[8]);
		Arrays.sort(hamdistances);
		// IdentiFrog.LOGGER.writeMessage("0=" + hamdistances[0] + " 1=" + hamdistances[1] + " 2=" +
		// hamdistances[2] + " 3=" + hamdistances[3] + " 4=" + hamdistances[4] + " 5=" +
		// hamdistances[5] + " 6=" + hamdistances[6] + " 7=" + hamdistances[7] + " 8=" +
		// hamdistances[8]);
		return hamdistances[0];
	}

	private double computeHamDist(int[] qpix, int[] dbpix) {
		double hamdist = 0;

		// IdentiFrog.LOGGER.writeMessage("dbpix.length = " + dbpix.length + " qpix.length = " + qpix.length);
		for (int i = 0; i < qpix.length; ++i) {
			int Qpix = qpix[i];
			int Dpix = dbpix[i];

			if (Qpix > threshold) {
				Qpix = 1;
			} else {
				Qpix = 0;
			}
			if (Dpix > threshold) {
				Dpix = 1;
			} else {
				Dpix = 0;
			}
			if (Qpix != Dpix) {
				++hamdist;
			}

			/*
			 * if ((qpix[i] < threshold & dbpix[i] > threshold) || (qpix[i] > threshold & dbpix[i] <
			 * threshold)) ++hamdist;
			 */
		}

		hamdist = hamdist / qpix.length;
		return roundDecimal(hamdist); // normalize by overlap
	}

	private double roundDecimal(double d) {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}

}
