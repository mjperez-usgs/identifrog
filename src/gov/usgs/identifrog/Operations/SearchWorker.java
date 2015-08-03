package gov.usgs.identifrog.Operations;

import gov.usgs.identifrog.Angle;
import gov.usgs.identifrog.HammingDistance;
import gov.usgs.identifrog.HausdorffDistance;
import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.SpotDifferencePass;
import gov.usgs.identifrog.DataObjects.Discriminator;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.FrogMatch;
import gov.usgs.identifrog.DataObjects.ImageMatch;
import gov.usgs.identifrog.DataObjects.SearchPackage;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.DataObjects.SiteSample;
import gov.usgs.identifrog.Frames.MainFrame;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class SearchWorker extends SwingWorker<ArrayList<FrogMatch>, String> {
	//public int topTen = 10;
	public static final double HAMMING_DISTANCE_THRESHOLD = 0.147;
	public static final double DEFAULT_SPOT_DIFFERENCE_PASS1 = 2;
	public static final double DEFAULT_SPOT_DIFFERENCE_PASS2 = 2;
	private SearchPackage sp;
	private MainFrame mf;

	public SearchWorker(SearchPackage sp, MainFrame mf) {
		this.sp = sp;
		this.mf = mf;
	}

	/**
	 * Searches frogs in the database against the specified criteria in the
	 * SearchPackage. Null values will be skipped.
	 */
	private ArrayList<FrogMatch> getMatches(SearchPackage sp) {
		//items are passed from arraylist to arraylist as each one filters down more and more 
		MainFrame.ACTIVE_SEARCH_PACKAGE = sp;
		IdentiFrog.LOGGER.writeMessage("Performing frog search with search package: " + sp);
		ArrayList<ImageMatch> candidates = new ArrayList<ImageMatch>();

		double manhatDistance1 = -1.0;
		double manhatDistance2 = -1.0;
		double myHausdorffDist = -1.0;
		double myAngle = -1.0;
		double myHammingDist = -1.0;

		boolean queryHasOneSpot = false;

		ArrayList<Frog> searchableFrogs = new ArrayList<Frog>(XMLFrogDatabase.getFrogs());
		if (sp.getImage() != null) {
			Frog imageOwner = XMLFrogDatabase.findImageOwnerByHash(sp.getImage().getSourceImageHash()); //frog that owns this image
			searchableFrogs.remove(imageOwner); //don't search the frog that contains the image we are already searching
		}

		//Using stats information (gender, sex, discriminators) we can reduce the number of frogs we should compare against.
		//Using this we will compile a list of frogs, and then iterate through the images of those frogs to search.
		//We start with all frogs.

		//************************* NARROW SCOPE OF SEARCH ***************************
		ArrayList<Frog> frogsToRemove = new ArrayList<Frog>(); // Have to do this to prevent concurrent exception with iterator
		//remove fresh frogs
		for (Frog f : searchableFrogs) {
			if (f.isFreshImport()) {
				frogsToRemove.add(f);
			}
		}

		//Remove frogs from scope
		for (Frog f : frogsToRemove) {
			searchableFrogs.remove(f);
		}

		//Remove surveys if latest only is selected
		for (Frog f : searchableFrogs) {
			if (!sp.useAllSurveys()) {
				//latest only
				IdentiFrog.LOGGER.writeMessage("Searching using latest survey only");
				ArrayList<SiteSample> latestSample = new ArrayList<SiteSample>();
				latestSample.add(f.getLatestSample());
				f.setSiteSamples(latestSample);
			}
		}

		//***********FILTER BY SEX AND SPECIES*****************
		if (sp.getGender() != null) {
			IdentiFrog.LOGGER.writeMessage("Filtering frogs by gender");
			for (Frog f : searchableFrogs) {
				//Filter out frogs that don't have the correct sex
				if (!sp.getGender().equalsIgnoreCase(f.getGender())) {
					frogsToRemove.add(f);
				}
			}
		}

		//Remove frogs from scope
		for (Frog f : frogsToRemove) {
			searchableFrogs.remove(f);
		}

		if (sp.getSpecies() != null) {
			IdentiFrog.LOGGER.writeMessage("Filtering frogs by species");
			for (Frog f : searchableFrogs) {
				//Filter species
				if (!sp.getSpecies().equalsIgnoreCase(f.getSpecies())) {
					frogsToRemove.add(f);
				}
			}
		}

		//Remove frogs from scope
		for (Frog f : frogsToRemove) {
			searchableFrogs.remove(f);
		}

		if (searchableFrogs.size() <= 0) {
			return new ArrayList<FrogMatch>();
		}

		//***********FILTER BY DISCRIMINATORS*****************
		frogsToRemove.clear();
		if (sp.getDiscriminators() != null) {
			IdentiFrog.LOGGER.writeMessage("Filtering frogs by discriminators");

			for (Frog f : searchableFrogs) {
				for (Discriminator d : sp.getDiscriminators()) {
					if (!f.getDiscriminators().contains(d)) {
						//it doesn't have the discriminator we searched for, remove it from the pool
						frogsToRemove.add(f);
						continue;
					}
				}
			}
		}

		//Remove frogs from scope
		for (Frog f : frogsToRemove) {
			searchableFrogs.remove(f);
		}

		if (searchableFrogs.size() <= 0) {
			return new ArrayList<FrogMatch>();
		}

		//************************FILTER ON MASS, LENGTH TOLERANCES******************
		//length
		if (sp.getLength() >= 0 && sp.getLengthTolerance() >= 0) {
			IdentiFrog.LOGGER.writeMessage("Filtering frogs by length");
			for (Frog f : searchableFrogs) {
				for (SiteSample s : f.getSiteSamples()) {
					double length = Double.parseDouble(s.getLength());
					double minVal = sp.getLength() - sp.getLengthTolerance();
					double maxVal = sp.getLength() + sp.getLengthTolerance();
					if (minVal < 0) {
						minVal = 0;
					}
					if (length > maxVal || length < minVal) {
						frogsToRemove.add(f);
						continue;
					}
				}
			}
		}

		//Remove frogs from scope
		for (Frog f : frogsToRemove) {
			searchableFrogs.remove(f);
		}

		if (searchableFrogs.size() <= 0) {
			return new ArrayList<FrogMatch>();
		}

		//mass
		if (sp.getMass() >= 0 && sp.getMassTolerance() >= 0) {
			IdentiFrog.LOGGER.writeMessage("Filtering frogs by mass");
			for (Frog f : searchableFrogs) {
				for (SiteSample s : f.getSiteSamples()) {
					double mass = Double.parseDouble(s.getMass());
					double minVal = sp.getMass() - sp.getMassTolerance();
					double maxVal = sp.getMass() + sp.getMassTolerance();
					if (minVal < 0) {
						minVal = 0;
					}
					if (mass > maxVal || mass < minVal) {
						frogsToRemove.add(f);
						continue;
					}
				}
			}
		}

		//Remove frogs from scope
		for (Frog f : frogsToRemove) {
			searchableFrogs.remove(f);
		}

		if (searchableFrogs.size() <= 0) {
			return new ArrayList<FrogMatch>();
		}

		//*******************************CONSTRUCT FROG MATCHES AND ATTACH IMAGES**********************
		//Attach frog image even if not doing an image search so we can pick one to display in the cell
		ArrayList<FrogMatch> frogMatches = new ArrayList<FrogMatch>();
		for (Frog f : searchableFrogs) {
			FrogMatch fm = new FrogMatch();
			f = new Frog(f);
			fm.setFrog(f); //copyfrog so we don't get side effects
			if (sp.getImage() == null) {
				fm.setSearchOnly(true);
			}

			for (SiteImage img : f.getAllSiteImages()) {
				if (!img.isSignatureGenerated() && sp.getImage() != null) {
					//skip if doing image search and no signature is generated
					continue; //skip
				}
				ImageMatch m = new ImageMatch();
				m.setImage(img);
				candidates.add(m);
				fm.addImage(m);
			}
			if (fm.getImages().size() <= 0) {
				continue; //this frog has no searchable images
			}
			frogMatches.add(fm);
		}

		if (sp.getImage() == null) {
			return frogMatches;
		}

		SiteImage image = sp.getImage();

		//Binary, Signature of image being searched image
		String userSearchDigitalSignature = image.getSignature();
		String userSearchBinaryImage = image.getBinary();

		if (candidates.size() <= 0) {
			return new ArrayList<FrogMatch>();
		}

		//IMAGE SEARCH=====================================================================
		// PATTERN RECOGNITION ALGORITHM: MULTI-STEP REDUCTION
		// STEP 1: DIFFERENCE IN NUMBER OF RELEVANT SPOTS
		ArrayList<ImageMatch> badCandidates = new ArrayList<ImageMatch>(); //items in this list are removed from the candidate list
		for (ImageMatch m : candidates) {
			//		for (int p = 0; p < candidates.size(); ++p) {
			SpotDifferencePass spotDifferencePass1 = new SpotDifferencePass();
			manhatDistance1 = spotDifferencePass1.getPassManhatDistance(userSearchDigitalSignature, m.getSignature(), 1);
			/*
			 * Frog_Info dbFrog_Info = new
			 * Frog_Info(Candidates_forPass1.get(p).dbid,
			 * Candidates_forPass1.get(p).frogid,
			 * Candidates_forPass1.get(p).frogDiscriminators, manhatDistance1,
			 * Candidates_forPass1.get(p).signature,
			 * Candidates_forPass1.get(p).binaryImg);
			 */

			m.setScore(manhatDistance1);
			//Pass1_distances.add(dbFrog_Info);
			// check if query has only one spot
			queryHasOneSpot = spotDifferencePass1.queryWithOnespot; //TODO ASK DAVID WHY THIS IS IN A LOOP
		}
		// STEP 1: RESULTS
		Collections.sort(candidates);

		// keep candidates whose spot difference <= defaultSpotDifferencePass1
		for (ImageMatch im : candidates) {
			if (im.getScore() > DEFAULT_SPOT_DIFFERENCE_PASS1) {
				badCandidates.add(im);
			}
		}
		candidates.removeAll(badCandidates);
		badCandidates.clear();

		/*
		 * while (myind < candidates.size()) { if
		 * (candidates.get(myind).getScore() <= DEFAULT_SPOT_DIFFERENCE_PASS1) {
		 * // keep track of the number of unique frogids go the next step if
		 * (isUnique(candidates.get(myind).frogid, Candidates_forPass2)) {
		 * ++mycount; stopind0 = myind; } // adding each image to the next step
		 * Candidates_forPass2.add(Pass1_distances.get(myind)); } ++myind; } //
		 * end while
		 * 
		 * // if the number of remaining unique candidates < 10 ++stopind0;
		 * while (mycount < topTen & stopind0 < Pass1_distances.size()) { //
		 * check if this image already entered the next step // needed in case
		 * last time two or more images of the same frog entered if
		 * (!isEntered(Pass1_distances.get(stopind0).signature,
		 * Candidates_forPass2)) { // check if it is a unique frog addition to
		 * the next step if (isUnique(Pass1_distances.get(stopind0).frogid,
		 * Candidates_forPass2)) { ++mycount; }
		 * Candidates_forPass2.add(Pass1_distances.get(stopind0)); } ++stopind0;
		 * } // end while
		 */
		/*
		 * else { //All images have a spot difference >
		 * DEFAULT_SPOT_DIFFERENCE_PASS1 int ind0 = 0, count = 0; while (count <
		 * topTen & ind0 < Pass1_distances.size()) { if
		 * (isUnique(Pass1_distances.get(ind0).frogid, Candidates_forPass2)) {
		 * ++count; } Candidates_forPass2.add(Pass1_distances.get(ind0));
		 * ++ind0; } }
		 */
		// STEP 2: DIFFERENCE IN NUMBER OF RELEVANT SPOTS PER QUADRANT
		for (ImageMatch m : candidates) {
			SpotDifferencePass spotDifferencePass2 = new SpotDifferencePass();
			manhatDistance2 = spotDifferencePass2.getPassManhatDistance(userSearchDigitalSignature, m.getSignature(), 2);
			m.setScore(manhatDistance2);
			/*
			 * Frog_Info dbFrog_Info = new
			 * Frog_Info(Candidates_forPass2.get(m).dbid,
			 * Candidates_forPass2.get(m).frogid,
			 * Candidates_forPass2.get(m).frogDiscriminators, manhatDistance2,
			 * Candidates_forPass2.get(m).signature,
			 * Candidates_forPass2.get(m).binaryImg);
			 * 
			 * 
			 * Pass2_distances.add(dbFrog_Info);
			 */
		}
		// STEP 2: RESULTS
		Collections.sort(candidates);

		for (ImageMatch im : candidates) {
			if (im.getScore() > DEFAULT_SPOT_DIFFERENCE_PASS2) {
				badCandidates.add(im);
			}
		}
		candidates.removeAll(badCandidates);
		badCandidates.clear();

		// keep candidates whose spot difference <= defaultSpotDifferencePass2
		/*
		 * if (candidates.get(0).getScore() <= DEFAULT_SPOT_DIFFERENCE_PASS2) {
		 * int myind = 0, mycount = 0, stopind1 = 0; while (myind <
		 * Pass2_distances.size()) { if (Pass2_distances.get(myind).getScore()
		 * <= DEFAULT_SPOT_DIFFERENCE_PASS2) { if
		 * (isUnique(Pass2_distances.get(myind).frogid,
		 * Candidates_forHausdorff)) { ++mycount; stopind1 = myind; }
		 * Candidates_forHausdorff.add(Pass2_distances.get(myind)); } ++myind; }
		 * // end while // if the number of remaining unique candidates < 10 if
		 * (mycount < topTen) { ++stopind1; while (mycount < topTen & stopind1 <
		 * Pass2_distances.size()) { // check if this image already entered the
		 * next step if (!isEntered(Pass2_distances.get(stopind1).signature,
		 * Candidates_forHausdorff)) { // check if it is a unique frog addition
		 * to the next step if (isUnique(Pass2_distances.get(stopind1).frogid,
		 * Candidates_forHausdorff)) { ++mycount; }
		 * Candidates_forHausdorff.add(Pass2_distances.get(stopind1)); }
		 * ++stopind1; } // end while } // end if } else { /* if pass #2 has
		 * spot differences > defaultSpotDifferencePass2 select closest images
		 * of 10 candidates with unique frog_id
		 * 
		 * int ind1 = 0, count = 0; while (count < topTen & ind1 <
		 * Pass2_distances.size()) { if
		 * (isUnique(Pass2_distances.get(ind1).frogid, Candidates_forHausdorff))
		 * { ++count; } Candidates_forHausdorff.add(Pass2_distances.get(ind1));
		 * ++ind1; } }
		 */
		// STEP 3: HAUSDORFF DISTANCE
		for (ImageMatch m : candidates) {
			//HausdorffDistance oneWayHausdorff = new HausdorffDistance();
			myHausdorffDist = HausdorffDistance.getHausdorffDistance(userSearchDigitalSignature, m.getSignature());
			m.setScore(myHausdorffDist);
			//Frog_Info dbFrog_Info = new Frog_Info(fi.dbid, fi.frogid, fi.frogDiscriminators, myHausdorffDist, fi.signature, fi.binaryImg);
			//Hausdorff_distances.add(dbFrog_Info);
		}

		/*
		 * for (int k = 0; k < Candidates_forHausdorff.size(); ++k) {
		 * HausdorffDistance oneWayHausdorff = new HausdorffDistance();
		 * myHausdorffDist =
		 * oneWayHausdorff.getHausdorffDistance(userSearchDigitalSignature,
		 * Candidates_forHausdorff.get(k).signature); Frog_Info dbFrog_Info =
		 * new Frog_Info(Candidates_forHausdorff.get(k).dbid,
		 * Candidates_forHausdorff.get(k).frogid,
		 * Candidates_forHausdorff.get(k).frogDiscriminators, myHausdorffDist,
		 * Candidates_forHausdorff.get(k).signature,
		 * Candidates_forHausdorff.get(k).binaryImg);
		 * Hausdorff_distances.add(dbFrog_Info); }
		 */
		// STEP 3: RESULTS
		Collections.sort(candidates);
		/*
		 * if there are more than topTen unique candidates from the previous
		 * step keep 50% unique candidates with the smallest Hausdorff distance
		 */
		//Removing top 10 limit 2015
		/*
		 * int stop_index = Math.round(candidates.size() / 2); int ind = 0,
		 * count = 0; while (ind < stop_index) { if
		 * (isUnique(Hausdorff_distances.get(ind).frogid, Candidates_forAngle))
		 * { ++count; } Candidates_forAngle.add(Hausdorff_distances.get(ind));
		 * ++ind; } // end while if (count < topTen) { while (count < topTen &
		 * ind < Hausdorff_distances.size()) { if
		 * (isUnique(Hausdorff_distances.get(ind).frogid, Candidates_forAngle))
		 * { ++count; } Candidates_forAngle.add(Hausdorff_distances.get(ind));
		 * ++ind; } }
		 */
		if (!queryHasOneSpot) {
			// STEP 4: ANGLE BETWEEN VECTORS
			for (ImageMatch m : candidates) {
				Angle AnglewithQuery = new Angle();
				myAngle = AnglewithQuery.getAngle(userSearchDigitalSignature, m.getSignature());
				/*
				 * Frog_Info dbFrog_Info = new
				 * Frog_Info(Candidates_forAngle.get(p).dbid,
				 * Candidates_forAngle.get(p).frogid,
				 * Candidates_forAngle.get(p).frogDiscriminators, myAngle,
				 * Candidates_forAngle.get(p).signature,
				 * Candidates_forAngle.get(p).binaryImg);
				 */
				m.setScore(myAngle);
				//Angle_measures.add(dbFrog_Info);
			}
			// STEP 4: RESULTS
			Collections.sort(candidates);
			/*
			 * if there are more than topTen unique candidates from the previous
			 * step keep 50% unique candidates with the smallest Angle
			 * differences
			 */
			/*
			 * int angle_stop_index = Math.round(Angle_measures.size() / 2); int
			 * idx = 0, acount = 0; while (idx < angle_stop_index) { if
			 * (isUnique(Angle_measures.get(idx).frogid, Candidates_forHamming))
			 * { ++acount; } Candidates_forHamming.add(Angle_measures.get(idx));
			 * ++idx; } // end while if (acount < topTen) { while (acount <
			 * topTen & idx < Angle_measures.size()) { if
			 * (isUnique(Angle_measures.get(idx).frogid, Candidates_forHamming))
			 * { ++acount; } Candidates_forHamming.add(Angle_measures.get(idx));
			 * ++idx; } }
			 */
		} // end of else
			// STEP 5: HAMMING DISTANCE
		for (ImageMatch m : candidates) {
			//for (int q = 0; q < Candidates_forHamming.size(); ++q) {
			HammingDistance finalMatch = new HammingDistance();
			myHammingDist = finalMatch.getHammingDistance(userSearchBinaryImage, m.getBinary());
			//Frog_Info dbFrog_Info = new Frog_Info(Candidates_forHamming.get(q).dbid, Candidates_forHamming.get(q).frogid,
			//		Candidates_forHamming.get(q).frogDiscriminators, myHammingDist, Candidates_forHamming.get(q).signature,
			//		Candidates_forHamming.get(q).binaryImg);
			m.setScore(myHammingDist);
			//Hamming_distances.add(dbFrog_Info);
		}
		// STEP 5: RESULTS
		Collections.sort(candidates);
		/* Picking ONLY UNIQUE topTen matches */
		/*
		 * if there are more than topTen unique candidates from the previous
		 * step list topTen unique candidates with the smallest Hamming distance
		 * in increasing order
		 */
		/*
		 * int index = 0; int t = 0; while (index < Hamming_distances.size() & t
		 * < topTen) { if (isUnique(Hamming_distances.get(index).frogid,
		 * Matches)) { Matches.add(Hamming_distances.get(index)); ++t; }
		 * ++index; } /* LIST topTen matches in GUI
		 */
		//int row = 0;
		//rowcount = Matches.size();
		//Object[][] retArray = new Object[rowcount][col];
		// Matches has candidates already in decreasing order
		for (ImageMatch m : candidates) {
			//int myFrogID = Matches.get(i).frogid;
			//String myscore = "" + Matches.get(i).dist;
			if (m.getScore() > HAMMING_DISTANCE_THRESHOLD) {
				m.setOverThreshhold(true);
			}
		}

		//remove frog matches that have no images in the list of candidates
		ArrayList<FrogMatch> badFrogs = new ArrayList<FrogMatch>();
		for (FrogMatch fm : frogMatches) {
			boolean hasOneImage = false;
			for (ImageMatch im : fm.getImages()) {
				if (candidates.contains(im)) {
					hasOneImage = true;
					break;
				}
			}
			if (!hasOneImage) {
				badFrogs.add(fm);
			}
		}
		
		frogMatches.removeAll(badFrogs);
		badFrogs.clear();
		return frogMatches;

		/*
		 * for (int i = 0; i < Matches.size(); ++i) { int myFrodDbId =
		 * Matches.get(i).dbid;
		 * 
		 * String myscore = "" + Matches.get(i).dist; // check if the match is
		 * improbable if (Matches.get(i).dist > HammingDistanceThreshold) {
		 * myscore = myscore + "*"; } Frog f =
		 * XMLFrogDatabase.searchFrogByID(Matches.get(i).frogid);
		 * retArray[row][MROW] = f.getID(); retArray[row][FILENAME] =
		 * XMLFrogDatabase.getThumbnailFolder() + f.getGenericImageName();
		 * retArray[row][MFROG_ID] = f.getID(); retArray[row][SCORE] = myscore;
		 * retArray[row][MCAPDATE] = f.getDateCapture(); retArray[row][MLOCNAME]
		 * = f.getLocation().getName();
		 * 
		 * ++row; } // end for loop // This part is to make sure we don't return
		 * an empty array, if nothing // met threshold we'll return last frog if
		 * (rowcount != 0) { Object[][] tempArray = new Object[rowcount][col];
		 * for (int r = 0; r < rowcount; r++) { for (int c = 0; c < col; c++) {
		 * tempArray[r][c] = retArray[r][c]; } } retArray = new
		 * Object[rowcount][col]; retArray = tempArray; } else { retArray = new
		 * Object[1][col]; retArray[row][MROW] = 0; retArray[row][FILENAME] =
		 * "IconFrog.png"; retArray[row][MFROG_ID] = "-"; retArray[row][SCORE] =
		 * "-"; retArray[row][MCAPDATE] = "-"; retArray[row][MLOCNAME] = "-"; }
		 * return retArray;
		 */

		//return null;
	}

	@Override
	protected ArrayList<FrogMatch> doInBackground() throws Exception {
		return getMatches(sp);
	}

	@Override
	protected void done() {
		ArrayList<FrogMatch> results;
		try {
			results = get();
			mf.setMatches(results);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//Pre 2015:  Note the below is to satisfy Java's idiocy of not thinking something returned in a try
	// is really returned, if it is actually called we're in trouble
	//2015: its called scope yo'
	// Object[][] retArray = new Object[rowcount][col];
	// return retArray;
} // end of getmatching

/*
 * private class Frog_Info { // public int dbid; /* public int dbid; public
 * ArrayList<Discriminator> frogDiscriminators; public int frogid; public double
 * dist; public String signature, binaryImg;
 */

//private Frog frog;
//private double dist;

/*
 * public Frog_Info(int otherfrogid, ArrayList<Discriminator>
 * otherDiscriminators, double dist, String signature, String biImage) { frogid
 * = otherfrogid; this.frogDiscriminators = otherDiscriminators; this.dist =
 * dist; this.signature = signature; binaryImg = biImage; }
 * 
 * public Frog_Info(int db_id, int frog_id, ArrayList<Discriminator>
 * otherDiscriminators, double dist, String signature, String biImage) { dbid =
 * db_id; frogid = frog_id; this.frogDiscriminators = otherDiscriminators;
 * this.dist = dist; this.signature = signature; binaryImg = biImage; }
 * 
 * 
 * public Frog_Info(Frog frog, double dist) { this.frog = frog; this.dist =
 * dist; } }
 * 
 * public class distanceComparator implements Comparator<Frog_Info> {
 * 
 * @Override public int compare(Frog_Info frog1, Frog_Info frog2) { // comparing
 * double values, sort in ascending order if (frog1.dist > frog2.dist) { return
 * 1; } else if (frog1.dist < frog2.dist) { return -1; } else { return 0; } } }
 * 
 * private boolean isUnique(int frogid, ArrayList<Frog_Info> Current_Candidates)
 * { int i = 0; boolean unique = true; while (i < Current_Candidates.size() &&
 * unique) { if (frogid == Current_Candidates.get(i).frog.getID()) { unique =
 * false; } else { ++i; } } return unique; }
 * 
 * private boolean isEntered(String signat, ArrayList<Frog_Info>
 * Current_Candidates) { int i = 0; boolean entered = false; while (i <
 * Current_Candidates.size() && entered == false) { if
 * (signat.equals(Current_Candidates.get(i).signature)) { entered = true; //
 * IdentiFrog.LOGGER.writeMessage("entered " +
 * Current_Candidates.get(i).signature); } else { ++i; } } return entered; }
 */
