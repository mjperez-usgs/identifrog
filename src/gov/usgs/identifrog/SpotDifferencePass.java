package gov.usgs.identifrog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * <p>
 * Title: SpotDifferencePass.java
 * <p>
 * Description: Manhattan Spot difference in the whole image and overall per quadrant
 * 
 * @author Oksana V. Kelly 2008
 */
public class SpotDifferencePass {
	public boolean queryWithOnespot = false;

	public double getPassManhatDistance(String filename1, String filename2, int passNumber) {
		// the first 5 numbers:
		// Pass 1(1 number)
		// Pass 2(4 numbers)
		final int complete_pass_length = 5;
		int pass_sig_length = 0;
		int start_position = 0;
		ArrayList<Double> sig1 = new ArrayList<Double>();
		ArrayList<Double> sig2 = new ArrayList<Double>();
		switch (passNumber) {
			case 1:
				start_position = 0;
				pass_sig_length = 1;
				break;
			case 2:
				start_position = 1;
				pass_sig_length = 4;
				break;
		}
		int i1, i2;
		int j = 0;
		char str;
		double manhattenDistance;
		try {
			FileReader f1 = new FileReader(filename1);
			BufferedReader bf1 = new BufferedReader(f1);
			StringBuffer string_num = new StringBuffer();
			while ((i1 = bf1.read()) != -1 & j < complete_pass_length) {
				str = (char) i1;
				if (str != ',') {
					string_num.append(str);
				} else {
					String number = new String(string_num);
					sig1.add(Double.parseDouble(number));
					++j;
					string_num = new StringBuffer("");
				}
			}
			f1.close();
		} catch (IOException iox) {
			System.out.println("Problem reading filename1 " + filename1);
		}
		try {
			FileReader f2 = new FileReader(filename2);
			BufferedReader bf2 = new BufferedReader(f2);
			j = 0;
			StringBuffer string_num = new StringBuffer();
			while ((i2 = bf2.read()) != -1 & j < complete_pass_length) {
				str = (char) i2;
				if (str != ',') {
					string_num.append(str);
				} else {
					String number = new String(string_num);
					sig2.add(Double.parseDouble(number));
					++j;
					string_num = new StringBuffer("");
				}
			}
			f2.close();
		} catch (IOException iox) {
			System.out.println("Problem reading filename2 " + filename2);
		}
		Double[] array_sig1 = sig1.toArray(new Double[sig1.size()]);
		Double[] array_sig2 = sig2.toArray(new Double[sig2.size()]);
		Double[] frog1_sig1 = new Double[pass_sig_length];
		Double[] frog2_sig2 = new Double[pass_sig_length];
		System.arraycopy(array_sig1, start_position, frog1_sig1, 0, pass_sig_length);
		System.arraycopy(array_sig2, start_position, frog2_sig2, 0, pass_sig_length);

		if (passNumber == 1 && frog1_sig1[0] == 1) {
			queryWithOnespot = true; // there is only one spot
		}
		manhattenDistance = getManhatDistance(frog1_sig1, frog2_sig2);
		return manhattenDistance;
	}

	private double getManhatDistance(Double[] sig1, Double[] sig2) {
		double manhatDist = 0;
		for (int k = 0; k < sig1.length; ++k) {
			manhatDist = manhatDist + Math.abs(sig1[k].doubleValue() - sig2[k].doubleValue());
		}
		return manhatDist;
	}
}