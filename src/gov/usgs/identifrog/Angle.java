package gov.usgs.identifrog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * <p>
 * Title: Angle.java
 * <p>
 * Description: Measures the angle with dot product between two vectors. A vector connects the
 * centroids of the first two relevant spots on the right.
 * 
 * @author Oksana V. Kelly 2008
 */

public class Angle {

	public boolean dbWithOnespot = false;

	public double getAngle(String filename1, String filename2) {

		// signature file contains relevant spot centroids after letter Z
		ArrayList<String> sig1_string = new ArrayList<String>();
		ArrayList<String> sig2_string = new ArrayList<String>();
		ArrayList<Double> sig1_constellation = new ArrayList<Double>();
		ArrayList<Double> sig2_constellation = new ArrayList<Double>();

		int i1, i2;
		double myAngle = 0;
		String comma = ",";
		String stringZ = "Z";

		// file 1
		try {
			FileReader f1 = new FileReader(filename1);
			BufferedReader bf1 = new BufferedReader(f1);

			while ((i1 = bf1.read()) != -1) {
				sig1_string.add(Character.toString((char) i1));
			}
			f1.close();
		} catch (IOException iox) {
			IdentiFrog.LOGGER.writeMessage("Problem reading filename1 hausdorff" + filename1);
		}

		// file 2
		try {
			FileReader f2 = new FileReader(filename2);
			BufferedReader bf2 = new BufferedReader(f2);

			while ((i2 = bf2.read()) != -1) {
				sig2_string.add(Character.toString((char) i2));
			}
			f2.close();
		} catch (IOException iox) {
			IdentiFrog.LOGGER.writeMessage("Problem reading filename2" + filename2);
		}

		// get coordinates from spot constellation 1
		String[] array_sig1_string = sig1_string.toArray(new String[sig1_string.size()]);
		StringBuffer string_num1 = new StringBuffer();

		for (int q = 0; q < array_sig1_string.length; ++q) {
			if (array_sig1_string[q].equals(stringZ)) {
				++q;
				while (q < array_sig1_string.length - 1) {
					++q;
					if (!array_sig1_string[q].equals(comma)) {
						string_num1.append(array_sig1_string[q]);
					} else {
						String num1 = new String(string_num1);
						sig1_constellation.add(Double.parseDouble(num1));
						string_num1 = new StringBuffer("");

					}
				} // end while
			} // end if
		} // end for loop

		// get coordinates from spot constellation 2
		String[] array_sig2_string = sig2_string.toArray(new String[sig2_string.size()]);
		StringBuffer string_num = new StringBuffer();

		for (int m = 0; m < array_sig2_string.length; ++m) {
			if (array_sig2_string[m].equals(stringZ)) {
				++m;
				while (m < array_sig2_string.length - 1) {
					++m;
					if (!array_sig2_string[m].equals(comma)) {
						string_num.append(array_sig2_string[m]);
					} else {
						String num = new String(string_num);
						sig2_constellation.add(Double.parseDouble(num));
						string_num = new StringBuffer("");
					}
				} // end while
			} // end if
		} // end for loop

		Double[] array_sig1_constellation = sig1_constellation.toArray(new Double[sig1_constellation.size()]);
		Double[] array_sig2_constellation = sig2_constellation.toArray(new Double[sig2_constellation.size()]);

		// check if there is only one spot
		// in signature 2
		if (array_sig2_constellation.length / 2 == 1) {
			dbWithOnespot = true; // there is only one spot
			return 0.0; // keep this image as a candidate to the next step
		}

		// vector 1
		double[] X_constellation1 = new double[array_sig1_constellation.length / 2];
		int v = 0, k = 0, p = 0;
		while (k < array_sig1_constellation.length - 1) {
			X_constellation1[v] = array_sig1_constellation[k];
			++v;
			k = k + 2;
		}
		Arrays.sort(X_constellation1);

		double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		while (p < array_sig1_constellation.length - 1) {
			if (array_sig1_constellation[p] == X_constellation1[X_constellation1.length - 1]) {
				x1 = array_sig1_constellation[p];
				y1 = array_sig1_constellation[p + 1];
			}
			if (array_sig1_constellation[p] == X_constellation1[X_constellation1.length - 2]) {
				x2 = array_sig1_constellation[p];
				y2 = array_sig1_constellation[p + 1];
			}
			++p;
		}

		double ir1 = 0, jr1 = 0;
		if (y1 > y2) {
			ir1 = x2 - x1;
			jr1 = -y2 + y1;
		} else {
			ir1 = x1 - x2;
			jr1 = -y1 + y2;
		}

		// vector 2
		double[] X_constellation2 = new double[array_sig2_constellation.length / 2];
		int w = 0, l = 0;
		while (l < array_sig2_constellation.length - 1) {
			X_constellation2[w] = array_sig2_constellation[l];
			++w;
			l = l + 2;
		}
		Arrays.sort(X_constellation2);

		x1 = 0;
		x2 = 0;
		y1 = 0;
		y2 = 0;
		p = 0;
		while (p < array_sig2_constellation.length - 1) {
			if (array_sig2_constellation[p] == X_constellation2[X_constellation2.length - 1]) {
				x1 = array_sig2_constellation[p];
				y1 = array_sig2_constellation[p + 1];
			}
			if (array_sig2_constellation[p] == X_constellation2[X_constellation2.length - 2]) {
				x2 = array_sig2_constellation[p];
				y2 = array_sig2_constellation[p + 1];
			}
			++p;
		}

		double ir2 = 0, jr2 = 0;
		if (y1 > y2) {
			ir2 = x2 - x1;
			jr2 = -y2 + y1;
		} else {
			ir2 = x1 - x2;
			jr2 = -y1 + y2;
		}
		double norm_r1 = Math.sqrt(ir1 * ir1 + jr1 * jr1);
		double norm_r2 = Math.sqrt(ir2 * ir2 + jr2 * jr2);

		double myCos = (ir1 * ir2 + jr1 * jr2) / (norm_r1 * norm_r2);
		// angle between vector 1 and vector2
		myAngle = Math.toDegrees(Math.acos(myCos));

		return myAngle;
	}
}// end of class Angle