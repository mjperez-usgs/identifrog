package gov.usgs.identifrog;

import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.Frames.MainFrame;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.activation.DataHandler;
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
public class TopTenMatches {
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

	public TopTenMatches(MainFrame frame) {
		IdentiFrog.LOGGER.writeError("TopTenMatches is currently not implemented!");
		parentFrame = frame;
	}

	// XXX
	public Object[][] getMatches(DataHandler frogData, Frog frog, int attribute, boolean ascending, boolean imgInclude, boolean isSex, boolean isAdditDiscr) {
		boolean discriminateBySex = isSex;
		boolean discriminateByAdditDiscr = isAdditDiscr;
		boolean includeQueryImages = imgInclude;

		int rowcount = 0;
		int col = 6;
		int otherdbid;

		String digsigtomatch, binaryImgtomatch, queryspecies, querygender, queryAdditDiscr;
		int queryfrogid, otherfrogid;
		String otherdigsig, otherbinaryImg, otherspecies, othergender, otherAdditDiscr;

		ArrayList<Frog_Info> Candidates_afterDiscriminateSex = new ArrayList<Frog_Info>();
		ArrayList<Frog_Info> Candidates_forPass1 = new ArrayList<Frog_Info>();
		ArrayList<Frog_Info> Candidates_forPass2 = new ArrayList<Frog_Info>();
		ArrayList<Frog_Info> Pass1_distances = new ArrayList<Frog_Info>();
		ArrayList<Frog_Info> Pass2_distances = new ArrayList<Frog_Info>();
		ArrayList<Frog_Info> Angle_measures = new ArrayList<Frog_Info>();
		ArrayList<Frog_Info> Hausdorff_distances = new ArrayList<Frog_Info>();
		ArrayList<Frog_Info> Hamming_distances = new ArrayList<Frog_Info>();
		ArrayList<Frog_Info> Candidates_forAngle = new ArrayList<Frog_Info>();
		ArrayList<Frog_Info> Candidates_forHausdorff = new ArrayList<Frog_Info>();
		ArrayList<Frog_Info> Candidates_forHamming = new ArrayList<Frog_Info>();
		ArrayList<Frog_Info> Matches = new ArrayList<Frog_Info>();

		double manhatDistance1 = -1.0;
		double manhatDistance2 = -1.0;
		double myHausdorffDist = -1.0;
		double myAngle = -1.0;
		double myHammingDist = -1.0;

		boolean queryHasOneSpot = false;
		digsigtomatch = XMLFrogDatabase.getSignaturesFolder() + frog.getPathSignature();
		binaryImgtomatch = XMLFrogDatabase.getBinaryFolder() + frog.getGenericImageName();
		queryfrogid = frog.getID();
		querygender = frog.getGender();
		queryspecies = frog.getSpecies();
		queryAdditDiscr = frog.getDiscriminator();
		parentFrame.setMatchForg(frog);
		ArrayList<Frog> searchableFrogs = new ArrayList<Frog>();
		if (includeQueryImages) {
			/*for (int i = 0; i < frogData.getFrogs().size(); i++) {
				if (!frogData.getFrogs().get(i).getFormerID().equals(queryfrogid)) {
					searchableFrogs.add(frogData.getFrogs().get(i));
				}
			}*/
			System.err.println("Running commented out code in TopTenMatches includeQueryImages()");
		} else {
			for (int i = 0; i < frogData.getFrogs().size(); i++) {
				if (frogData.getFrogs().get(i).getID() != queryfrogid) {
					searchableFrogs.add(frogData.getFrogs().get(i));
				}
			}
			IdentiFrog.LOGGER.writeMessage("SIZE " + searchableFrogs.size());
		}
		/************ CHECK SEARCH DESCRIMINATORS (SEX or SNOUT SPOT) ************/
		// remember about row count will be decreasing after passes!
		/************************* DESCRIMINATE BY SEX ***************************/
		for (int i = 0; i < searchableFrogs.size(); i++) {
			otherdigsig = XMLFrogDatabase.getSignaturesFolder() + searchableFrogs.get(i).getPathSignature();
			otherbinaryImg = XMLFrogDatabase.getBinaryFolder() + searchableFrogs.get(i).getGenericImageName();
			otherfrogid = searchableFrogs.get(i).getID();
			//String lame = searchableFrogs.get(i).getFormerID();
			// lame = lame.substring(3, lame.length());
			//otherdbid = new Integer(lame).intValue();
			// otherdbid = (new Integer(lame)).intValue();
			othergender = searchableFrogs.get(i).getGender();
			otherAdditDiscr = searchableFrogs.get(i).getDiscriminator();
			otherspecies = searchableFrogs.get(i).getSpecies();

			if (discriminateBySex && querygender.equalsIgnoreCase(othergender) && queryspecies.equalsIgnoreCase(otherspecies)) {
				Frog_Info dbFrog_Info = new Frog_Info(/*otherdbid,*/ otherfrogid, otherAdditDiscr, 0, otherdigsig, otherbinaryImg);
				// zero is meaningless here
				Candidates_afterDiscriminateSex.add(dbFrog_Info);
				++rowcount;
			}
			if (!discriminateBySex) {
				Frog_Info dbFrog_Info = new Frog_Info(/*otherdbid,*/ otherfrogid, otherAdditDiscr, 0, otherdigsig, otherbinaryImg);
				Candidates_afterDiscriminateSex.add(dbFrog_Info);
				++rowcount;
			}
		}
		/* Check if there are no candidates based on Sex */
		if (Candidates_afterDiscriminateSex.size() == 0) {
			JOptionPane.showMessageDialog(null, "There are no matches according to Search Criteria.");
			Object[][] retArray = new Object[rowcount][col];
			return null;
			//return retArray;
		}
		/*********************** DESCRIMINATE BY Additional Discriminator *************************/
		if (discriminateByAdditDiscr) {
			for (int s = 0; s < Candidates_afterDiscriminateSex.size(); ++s) {
				String otherFrogDiscr = Candidates_afterDiscriminateSex.get(s).additDiscr;
				if (queryAdditDiscr.equals(otherFrogDiscr)) {
					Frog_Info dbFrog_Info = new Frog_Info(Candidates_afterDiscriminateSex.get(s).dbid, Candidates_afterDiscriminateSex.get(s).frogid,
							Candidates_afterDiscriminateSex.get(s).additDiscr, 0, Candidates_afterDiscriminateSex.get(s).signature, Candidates_afterDiscriminateSex.get(s).binaryImg);
					Candidates_forPass1.add(dbFrog_Info);
				}
			} // end for loop
		} else { // no discrimination by Additional Discriminator
			Candidates_forPass1 = Candidates_afterDiscriminateSex;
		}

		/* Check if there are no candidates based on Sex and/or Additional Discriminator */
		if (Candidates_forPass1.size() == 0) {
			JOptionPane.showMessageDialog(null, "There are no matches according to Search Criteria.");
			Object[][] retArray = new Object[rowcount][col];
			return retArray;
		}
		// PATTERN RECOGNITION ALGORITHM: MULTI-STEP REDUCTION
		// STEP 1: DIFFERENCE IN NUMBER OF RELEVANT SPOTS
		for (int p = 0; p < Candidates_forPass1.size(); ++p) {
			SpotDifferencePass spotDifferencePass1 = new SpotDifferencePass();
			manhatDistance1 = spotDifferencePass1.getPassManhatDistance(digsigtomatch, Candidates_forPass1.get(p).signature, 1);
			Frog_Info dbFrog_Info = new Frog_Info(Candidates_forPass1.get(p).dbid, Candidates_forPass1.get(p).frogid, Candidates_forPass1.get(p).additDiscr, manhatDistance1, Candidates_forPass1
					.get(p).signature, Candidates_forPass1.get(p).binaryImg);
			Pass1_distances.add(dbFrog_Info);
			// check if query has only one spot
			queryHasOneSpot = spotDifferencePass1.queryWithOnespot;
		}
		// STEP 1: RESULTS
		Collections.sort(Pass1_distances, new distanceComparator());
		// keep candidates whose spot difference <= defaultSpotDifferencePass1
		if (Pass1_distances.get(0).dist <= defaultSpotDifferencePass1) {
			int myind = 0, mycount = 0, stopind0 = 0;
			while (myind < Pass1_distances.size()) {
				if (Pass1_distances.get(myind).dist <= defaultSpotDifferencePass1) {
					// keep track of the number of unique frogids go the next step
					if (isUnique(Pass1_distances.get(myind).frogid, Candidates_forPass2)) {
						++mycount;
						stopind0 = myind;
					}
					// adding each image to the next step
					Candidates_forPass2.add(Pass1_distances.get(myind));
				}
				++myind;
			} // end while
			// if the number of remaining unique candidates < 10
			if (mycount < topTen) {
				++stopind0;
				while (mycount < topTen & stopind0 < Pass1_distances.size()) {
					// check if this image already entered the next step
					// needed in case last time two or more images of the same frog entered
					if (!isEntered(Pass1_distances.get(stopind0).signature, Candidates_forPass2)) {
						// check if it is a unique frog addition to the next step
						if (isUnique(Pass1_distances.get(stopind0).frogid, Candidates_forPass2)) {
							++mycount;
						}
						Candidates_forPass2.add(Pass1_distances.get(stopind0));
					}
					++stopind0;
				} // end while
			} // end if
		} else {
			/*
			 * if pass #1 has spot differences > defaultSpotDifferencePass1 select closest images of
			 * 10 candidates with unique frog_id
			 */
			int ind0 = 0, count = 0;
			while (count < topTen & ind0 < Pass1_distances.size()) {
				if (isUnique(Pass1_distances.get(ind0).frogid, Candidates_forPass2)) {
					++count;
				}
				Candidates_forPass2.add(Pass1_distances.get(ind0));
				++ind0;
			}
		}
		// STEP 2: DIFFERENCE IN NUMBER OF RELEVANT SPOTS PER QUADRANT
		for (int m = 0; m < Candidates_forPass2.size(); ++m) {
			SpotDifferencePass spotDifferencePass2 = new SpotDifferencePass();
			manhatDistance2 = spotDifferencePass2.getPassManhatDistance(digsigtomatch, Candidates_forPass2.get(m).signature, 2);
			Frog_Info dbFrog_Info = new Frog_Info(Candidates_forPass2.get(m).dbid, Candidates_forPass2.get(m).frogid, Candidates_forPass2.get(m).additDiscr, manhatDistance2, Candidates_forPass2
					.get(m).signature, Candidates_forPass2.get(m).binaryImg);
			Pass2_distances.add(dbFrog_Info);
		}
		// STEP 2: RESULTS
		Collections.sort(Pass2_distances, new distanceComparator());
		// keep candidates whose spot difference <= defaultSpotDifferencePass2
		if (Pass2_distances.get(0).dist <= defaultSpotDifferencePass2) {
			int myind = 0, mycount = 0, stopind1 = 0;
			while (myind < Pass2_distances.size()) {
				if (Pass2_distances.get(myind).dist <= defaultSpotDifferencePass2) {
					if (isUnique(Pass2_distances.get(myind).frogid, Candidates_forHausdorff)) {
						++mycount;
						stopind1 = myind;
					}
					Candidates_forHausdorff.add(Pass2_distances.get(myind));
				}
				++myind;
			} // end while
			// if the number of remaining unique candidates < 10
			if (mycount < topTen) {
				++stopind1;
				while (mycount < topTen & stopind1 < Pass2_distances.size()) {
					// check if this image already entered the next step
					if (!isEntered(Pass2_distances.get(stopind1).signature, Candidates_forHausdorff)) {
						// check if it is a unique frog addition to the next step
						if (isUnique(Pass2_distances.get(stopind1).frogid, Candidates_forHausdorff)) {
							++mycount;
						}
						Candidates_forHausdorff.add(Pass2_distances.get(stopind1));
					}
					++stopind1;
				} // end while
			} // end if
		} else {
			/*
			 * if pass #2 has spot differences > defaultSpotDifferencePass2 select closest images of
			 * 10 candidates with unique frog_id
			 */
			int ind1 = 0, count = 0;
			while (count < topTen & ind1 < Pass2_distances.size()) {
				if (isUnique(Pass2_distances.get(ind1).frogid, Candidates_forHausdorff)) {
					++count;
				}
				Candidates_forHausdorff.add(Pass2_distances.get(ind1));
				++ind1;
			}
		}
		// STEP 3: HAUSDORFF DISTANCE
		for (int k = 0; k < Candidates_forHausdorff.size(); ++k) {
			HausdorffDistance oneWayHausdorff = new HausdorffDistance();
			myHausdorffDist = oneWayHausdorff.getHausdorffDistance(digsigtomatch, Candidates_forHausdorff.get(k).signature);
			Frog_Info dbFrog_Info = new Frog_Info(Candidates_forHausdorff.get(k).dbid, Candidates_forHausdorff.get(k).frogid, Candidates_forHausdorff.get(k).additDiscr, myHausdorffDist,
					Candidates_forHausdorff.get(k).signature, Candidates_forHausdorff.get(k).binaryImg);
			Hausdorff_distances.add(dbFrog_Info);
		}
		// STEP 3: RESULTS
		Collections.sort(Hausdorff_distances, new distanceComparator());
		/*
		 * if there are more than topTen unique candidates from the previous step keep 50% unique
		 * candidates with the smallest Hausdorff distance
		 */
		int stop_index = Math.round(Hausdorff_distances.size() / 2);
		int ind = 0, count = 0;
		while (ind < stop_index) {
			if (isUnique(Hausdorff_distances.get(ind).frogid, Candidates_forAngle)) {
				++count;
			}
			Candidates_forAngle.add(Hausdorff_distances.get(ind));
			++ind;
		} // end while
		if (count < topTen) {
			while (count < topTen & ind < Hausdorff_distances.size()) {
				if (isUnique(Hausdorff_distances.get(ind).frogid, Candidates_forAngle)) {
					++count;
				}
				Candidates_forAngle.add(Hausdorff_distances.get(ind));
				++ind;
			}
		}
		if (queryHasOneSpot) {
			Candidates_forHamming = Candidates_forAngle;
		} else {
			// STEP 4: ANGLE BETWEEN VECTORS
			for (int p = 0; p < Candidates_forAngle.size(); ++p) {
				Angle AnglewithQuery = new Angle();
				myAngle = AnglewithQuery.getAngle(digsigtomatch, Candidates_forAngle.get(p).signature);
				Frog_Info dbFrog_Info = new Frog_Info(Candidates_forAngle.get(p).dbid, Candidates_forAngle.get(p).frogid, Candidates_forAngle.get(p).additDiscr, myAngle,
						Candidates_forAngle.get(p).signature, Candidates_forAngle.get(p).binaryImg);
				Angle_measures.add(dbFrog_Info);
			}
			// STEP 4: RESULTS
			Collections.sort(Angle_measures, new distanceComparator());
			/*
			 * if there are more than topTen unique candidates from the previous step keep 50%
			 * unique candidates with the smallest Angle differences
			 */
			int angle_stop_index = Math.round(Angle_measures.size() / 2);
			int idx = 0, acount = 0;
			while (idx < angle_stop_index) {
				if (isUnique(Angle_measures.get(idx).frogid, Candidates_forHamming)) {
					++acount;
				}
				Candidates_forHamming.add(Angle_measures.get(idx));
				++idx;
			} // end while
			if (acount < topTen) {
				while (acount < topTen & idx < Angle_measures.size()) {
					if (isUnique(Angle_measures.get(idx).frogid, Candidates_forHamming)) {
						++acount;
					}
					Candidates_forHamming.add(Angle_measures.get(idx));
					++idx;
				}
			}
		} // end of else
		// STEP 5: HAMMING DISTANCE
		for (int q = 0; q < Candidates_forHamming.size(); ++q) {
			HammingDistance finalMatch = new HammingDistance();
			myHammingDist = finalMatch.getHammingDistance(binaryImgtomatch, Candidates_forHamming.get(q).binaryImg);
			Frog_Info dbFrog_Info = new Frog_Info(Candidates_forHamming.get(q).dbid, Candidates_forHamming.get(q).frogid, Candidates_forHamming.get(q).additDiscr, myHammingDist, Candidates_forHamming
					.get(q).signature, Candidates_forHamming.get(q).binaryImg);
			Hamming_distances.add(dbFrog_Info);
		}
		// STEP 5: RESULTS
		Collections.sort(Hamming_distances, new distanceComparator());
		/* Picking ONLY UNIQUE topTen matches */
		/*
		 * if there are more than topTen unique candidates from the previous step list topTen unique
		 * candidates with the smallest Hamming distance in increasing order
		 */
		int index = 0;
		int t = 0;
		while (index < Hamming_distances.size() & t < topTen) {
			if (isUnique(Hamming_distances.get(index).frogid, Matches)) {
				Matches.add(Hamming_distances.get(index));
				++t;
			}
			++index;
		}
		/* LIST topTen matches in GUI */
		int row = 0;
		rowcount = Matches.size();
		Object[][] retArray = new Object[rowcount][col];
		// Matches has candidates already in decreasing order
		for (int i = 0; i < Matches.size(); i++) {
			int myFrogID = Matches.get(i).frogid;
			String myscore = "" + Matches.get(i).dist;
			if (Matches.get(i).dist > HammingDistanceThreshold) {
				myscore = myscore + "*";
			}

		}

		for (int i = 0; i < Matches.size(); ++i) {
			int myFrodDbId = Matches.get(i).dbid;

			String myscore = "" + Matches.get(i).dist;
			// check if the match is improbable
			if (Matches.get(i).dist > HammingDistanceThreshold) {
				myscore = myscore + "*";
			}
			Frog f = XMLFrogDatabase.searchFrogByID(Matches.get(i).frogid);
			retArray[row][MROW] = f.getID();
			retArray[row][FILENAME] = XMLFrogDatabase.getThumbnailFolder() + f.getGenericImageName();
			retArray[row][MFROG_ID] = f.getID();
			retArray[row][SCORE] = myscore;
			retArray[row][MCAPDATE] = f.getDateCapture();
			retArray[row][MLOCNAME] = f.getLocation().getName();

			++row;
		} // end for loop
		// This part is to make sure we don't return an empty array, if nothing
		// met threshold we'll return last frog
		if (rowcount != 0) {
			Object[][] tempArray = new Object[rowcount][col];
			for (int r = 0; r < rowcount; r++) {
				for (int c = 0; c < col; c++) {
					tempArray[r][c] = retArray[r][c];
				}
			}
			retArray = new Object[rowcount][col];
			retArray = tempArray;
		} else {
			retArray = new Object[1][col];
			retArray[row][MROW] = 0;
			retArray[row][FILENAME] = "IconFrog.png";
			retArray[row][MFROG_ID] = "-";
			retArray[row][SCORE] = "-";
			retArray[row][MCAPDATE] = "-";
			retArray[row][MLOCNAME] = "-";
		}
		return retArray;

		// Note the below is to satisfy Java's idiocy of not thinking something returned in a try
		// is really returned, if it is actually called we're in trouble
		// Object[][] retArray = new Object[rowcount][col];
		// return retArray;
	} // end of getmatching

	private class Frog_Info {
		// public int dbid;
		public int dbid;
		public String additDiscr;
		public int frogid;
		public double dist;
		public String signature, binaryImg;

		public Frog_Info( int otherfrogid, String addDiscr, double dist, String signature, String biImage) {
			frogid = otherfrogid;
			additDiscr = addDiscr;
			this.dist = dist;
			this.signature = signature;
			binaryImg = biImage;
		}

	
		public Frog_Info(int db_id, int frog_id, String addDiscr, double dist, String signature, String biImage) {
			dbid = db_id;
			frogid = frog_id;
			additDiscr = addDiscr;
			this.dist = dist;
			this.signature = signature;
			binaryImg = biImage;
		}
	}

	public class distanceComparator implements Comparator<Frog_Info> {
		@Override
		public int compare(Frog_Info frog1, Frog_Info frog2) {
			// comparing double values, sort in ascending order
			if (frog1.dist > frog2.dist) {
				return 1;
			} else if (frog1.dist < frog2.dist) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	private boolean isUnique(int frogid, ArrayList<Frog_Info> Current_Candidates) {
		int i = 0;
		boolean unique = true;
		while (i < Current_Candidates.size() && unique) {
			if (frogid == Current_Candidates.get(i).frogid) {
				unique = false;
			} else {
				++i;
			}
		}
		return unique;
	}

	private boolean isEntered(String signat, ArrayList<Frog_Info> Current_Candidates) {
		int i = 0;
		boolean entered = false;
		while (i < Current_Candidates.size() && entered == false) {
			if (signat.equals(Current_Candidates.get(i).signature)) {
				entered = true;
				// IdentiFrog.LOGGER.writeMessage("entered " + Current_Candidates.get(i).signature);
			} else {
				++i;
			}
		}
		return entered;
	}
}
