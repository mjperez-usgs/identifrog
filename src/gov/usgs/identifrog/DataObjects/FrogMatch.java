package gov.usgs.identifrog.DataObjects;

import java.awt.Component;
import java.util.ArrayList;

/**
 * Frog Match contains a frog object (that it represents) as well as a list of
 * ImageMatch object that hold scores based on the search.
 * 
 * @author mjperez
 *
 */
public class FrogMatch implements Comparable<FrogMatch> {
	private Frog frog;
	private boolean searchOnly = false; 
	private ArrayList<ImageMatch> images;

	public FrogMatch() {
		images = new ArrayList<ImageMatch>();
	}
	

	public boolean isSearchOnly() {
		return searchOnly;
	}


	/**
	 * Sets this frogmatch object to indicate it does not include an image search
	 * @param searchOnly
	 */
	public void setSearchOnly(boolean searchOnly) {
		this.searchOnly = searchOnly;
	}


	/**
	 * Associates a ImageMatch (image and score) object with this FrogMatch
	 * (frog) object
	 * 
	 * @param img
	 *            Image to associate with this frogmatch
	 */
	public void addImage(ImageMatch img) {
		// TODO Auto-generated method stub
		images.add(img);
	}

	/**
	 * Calculates the average score of all images owned by this frog
	 * 
	 * @return
	 */
	public double calculateAverageScore() {
		if (images.size() <= 0) {
			return 0;
		}

		double totalScore = 0;
		for (ImageMatch img : images) {
			totalScore += img.getScore();
		}
		return totalScore / images.size();
	}

	public ImageMatch getTopImage() {
		ImageMatch topImage = null;
		for (ImageMatch img : images) {
			if (topImage == null) {
				//first iteration can't compare
				topImage = img;
				continue;
			}
			if (img.getScore() > topImage.getScore()) {
				topImage = img;
			}
		}
		return topImage;
	}

	/**
	 * Gets the top image's score
	 * 
	 * @return top score
	 */
	public double getTopScore() {
		double highestScore = Double.MIN_VALUE;
		for (ImageMatch img : images) {
			if (img.getScore() > highestScore) {
				highestScore = img.getScore();
			}
		}
		return highestScore;
	}

	public void setFrog(Frog f) {
		// TODO Auto-generated method stub
		this.frog = f;
	}

	public Frog getFrog() {
		return frog;
	}

	public ArrayList<ImageMatch> getImages() {
		return images;
	}

	@Override
	public int compareTo(FrogMatch o) {
		if (getTopScore() > o.getTopScore()) {
			return 1;
		}

		if (getTopScore() < o.getTopScore()) {
			return -1;
		}

		return 0;
	}

}
