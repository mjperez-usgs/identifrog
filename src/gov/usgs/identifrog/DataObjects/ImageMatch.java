package gov.usgs.identifrog.DataObjects;


/**
 * Image Match contains a SiteIMage object as well as stats related to how
 * relevant said image is to the current search
 * 
 * @author mjperez
 *
 */
public class ImageMatch implements Comparable<ImageMatch>{
	private SiteImage image;
	private double score = Double.MIN_VALUE;
	private boolean overThreshhold = false;
	private boolean failedPass = false;
	
	public boolean isFailedPass() {
		return failedPass;
	}
	public void setFailedPass(boolean failedPass) {
		this.failedPass = failedPass;
	}
	
	public SiteImage getImage() {
		return image;
	}
	public void setImage(SiteImage image) {
		this.image = image;
	}
	
	/**
	 * Gets the score of this image match.
	 * If this imagematch is in processing this may also mean the current hamming distance, manhat distance, etc.
	 * @return
	 */
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public boolean isOverThreshhold() {
		return overThreshhold;
	}
	public void setOverThreshhold(boolean overThreshhold) {
		this.overThreshhold = overThreshhold;
	}
	
	public int compareTo(ImageMatch other) {
		// score = distance
		if (getScore() > other.getScore()) {
			return 1;
		} else if (getScore() < other.getScore()) {
			return -1;
		} else {
			return 0;
		}
	}
	
	public String getSignature() {
		return image.getSignature();
	}
	public String getBinary() {
		return image.getBinary();
	}
}
