package gov.usgs.identifrog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * <p>
 * Title: HausdorffDistance.java
 * </p>
 * <p>
 * Description: computes the one-sided Hausdorff distance between query connected components and the
 * centroids of a database image.
 * </p>
 * <p>
 * This software is released into the public domain.
 * </p>
 * 
 * @author Oksana V. Kelly 2008
 */

public class HausdorffDistance {

	public static double getHausdorffDistance(String filename1, String filename2) {

		// signature file contains ...
		ArrayList<String> sig1_string = new ArrayList<String>();
		ArrayList<String> sig2_string = new ArrayList<String>();
		ArrayList<Double> sig2_constellation = new ArrayList<Double>();

		int i1, i2;
		double myHausdorffDistance = 0;
		String comma = ",";
		// centroids start with "Z"
		String stringZ = "Z";
		// connected components start with "A"
		String stringA = "A";

		// connected components
		try {
			FileReader f1 = new FileReader(filename1);
			BufferedReader bf1 = new BufferedReader(f1);
			// IdentiFrog.LOGGER.writeMessage("filename1 " + filename1);

			while ((i1 = bf1.read()) != -1) {
				sig1_string.add(Character.toString((char) i1));
			}
			f1.close();
		} catch (IOException iox) {
			IdentiFrog.LOGGER.writeMessage("Problem reading filename1 hausdorff" + filename1);
		}

		// get constellation coordinates from file2
		try {
			FileReader f2 = new FileReader(filename2);
			BufferedReader bf2 = new BufferedReader(f2);
			// IdentiFrog.LOGGER.writeMessage("Hausdorff filename2 " + filename2);

			while ((i2 = bf2.read()) != -1) {
				sig2_string.add(Character.toString((char) i2));
			}
			f2.close();
		} catch (IOException iox) {
			IdentiFrog.LOGGER.writeMessage("Problem reading filename2" + filename2);
		}

		// constellation
		String[] array_sig2_string = sig2_string.toArray(new String[sig2_string.size()]);
		StringBuffer string_num = new StringBuffer();

		for (int m = 0; m < array_sig2_string.length; ++m) {
			if (array_sig2_string[m].equals(stringZ)) {
				// IdentiFrog.LOGGER.writeMessage("found z " + m + " length " + array_sig2_string.length);
				++m;
				while (m < array_sig2_string.length - 1) {
					++m;
					if (!array_sig2_string[m].equals(comma)) {
						string_num.append(array_sig2_string[m]);
						// IdentiFrog.LOGGER.writeMessage("string_num " + string_num);
					} else {
						String num = new String(string_num);
						sig2_constellation.add(Double.parseDouble(num));
						// IdentiFrog.LOGGER.writeMessage("sig2_constellation.size() " +
						// sig2_constellation.size());
						string_num = new StringBuffer("");
						// IdentiFrog.LOGGER.writeMessage("num z " + num);
					}
				} // end while
			} // end if
		} // end for loop

		// IdentiFrog.LOGGER.writeMessage("total " + sig2_constellation.size());
		Double[] array_sig2_constellation = sig2_constellation.toArray(new Double[sig2_constellation.size()]);

		// components
		String[] array_sig1_string = sig1_string.toArray(new String[sig1_string.size()]);
		StringBuffer string_num1 = new StringBuffer();

		for (int j = 0; j < array_sig1_string.length - 1; ++j) {
			// IdentiFrog.LOGGER.writeMessage("array_sig1_string.length " + array_sig1_string.length + " j " + j
			// + " array_sig1_string[j] " + array_sig1_string[j]);

			if (array_sig1_string[j].equals(stringA)) {
				// IdentiFrog.LOGGER.writeMessage("found a");
				j = j + 2;
				ArrayList<Double> sig1_component = new ArrayList<Double>();

				while (!array_sig1_string[j].equals(stringA) & !array_sig1_string[j].equals(stringZ)) {

					if (!array_sig1_string[j].equals(comma)) {
						string_num1.append(array_sig1_string[j]);
					} else {
						String number = new String(string_num1);
						sig1_component.add(Double.parseDouble(number));
						string_num1 = new StringBuffer("");
						// IdentiFrog.LOGGER.writeMessage("number a " + number);
					}
					++j;
				} // end while

				Double[] array_sig1_component = sig1_component.toArray(new Double[sig1_component.size()]);
				double[] dist = new double[sig1_component.size() / 2];
				// IdentiFrog.LOGGER.writeMessage("array_sig1_component.length1 " +
				// (array_sig1_component.length));
				int k = 0;
				int p = 0;
				while (k < array_sig1_component.length - 1) {
					dist[p] = computeDistance(array_sig1_component[k], array_sig1_component[k + 1], array_sig2_constellation);
					// IdentiFrog.LOGGER.writeMessage("dist[p] " + dist[p] + " p " + p);
					k = k + 2;
					++p;
				}
				// IdentiFrog.LOGGER.writeMessage("dist[dist.length-1] " + dist[dist.length-1]);
				Arrays.sort(dist);
				// IdentiFrog.LOGGER.writeMessage("dist[dist.length-1] " + dist[dist.length-1]);
				myHausdorffDistance = myHausdorffDistance + dist[dist.length - 1];
				j = j - 2; // since we increased j in while loop already
			} // end if

		}

		return myHausdorffDistance;
	}

	private static double computeDistance(Double x, Double y, Double[] constellation) {
		double[] d = new double[constellation.length / 2];
		double minHausdorff = -1;
		int k = 0;
		int i = 0;
		while (k < constellation.length - 1) {
			d[i] = Math.sqrt(Math.pow((x.doubleValue() - constellation[k].doubleValue()), 2) + Math.pow((y.doubleValue() - constellation[k + 1].doubleValue()), 2));
			k = k + 2;
			// IdentiFrog.LOGGER.writeMessage("d[i] " + d[i]);
			++i;

		}

		Arrays.sort(d);
		minHausdorff = d[0];
		return minHausdorff;
	}

} // end oc class sigMatch

