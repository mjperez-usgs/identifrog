package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.Angle;
import gov.usgs.identifrog.HammingDistance;
import gov.usgs.identifrog.HausdorffDistance;
import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.SpotDifferencePass;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

	/**
	 * <p>
	 * Title: TopTenMatches.java
	 * <p>
	 * Description: performs Spot-Pattern Recognition Algorithm and displays the top ten ranked closest
	 * matches in GUI
	 * 
	 * @author Oksana V. Kelly 2010
	 */
	public class MatchingFrame extends JFrame {
		// table for top ten ranked matches
		public static final int MROW = 0;
		public static final int FILENAME = 1;
		public static final int MFROG_ID = 2;
		public static final int SCORE = 3;
		public static final int MCAPDATE = 4;
		public static final int MLOCNAME = 5;
		// for recognition algorithm
		public double HammingDistanceThreshold = 0.147;
		public double defaultSpotDifferencePass1 = 2;
		public double defaultSpotDifferencePass2 = 2;
		public int topTen = 10;
		private MainFrame parentFrame;

		public MatchingFrame(MainFrame frame) {
			parentFrame = frame;
		}


		

	
}
