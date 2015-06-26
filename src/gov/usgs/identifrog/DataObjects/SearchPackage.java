package gov.usgs.identifrog.DataObjects;

import java.util.ArrayList;

/**
 * Describes search criteria when passing to the SearchWorker thread
 * @author mjperez
 *
 */
public class SearchPackage {
	private boolean useAllSurveys = true;
	private ArrayList<Discriminator> discriminators;
	private double mass, massTolerance;
	private double length, lengthTolerance;
	private String gender, species;
	private SiteImage image;
	
	/**
	 * Creates a search package with only default criteria
	 */
	public SearchPackage(){
		
	}

	public boolean isUseAllSurveys() {
		return useAllSurveys;
	}

	public void setUseAllSurveys(boolean useAllSurveys) {
		this.useAllSurveys = useAllSurveys;
	}

	public ArrayList<Discriminator> getDiscriminators() {
		return discriminators;
	}

	public void setDiscriminators(ArrayList<Discriminator> discriminators) {
		this.discriminators = discriminators;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public double getMassTolerance() {
		return massTolerance;
	}

	public void setMassTolerance(double massTolerance) {
		this.massTolerance = massTolerance;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getLengthTolerance() {
		return lengthTolerance;
	}

	public void setLengthTolerance(double lengthTolerance) {
		this.lengthTolerance = lengthTolerance;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public SiteImage getImage() {
		return image;
	}

	public void setImage(SiteImage image) {
		this.image = image;
	}
	
	
}
