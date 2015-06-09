package gov.usgs.identifrog.DataObjects;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Frog {
	private int ID;
	private String species;
	private String gender;
	private String discriminator;
	/*private Document document;
	private Element element;*/
	
	//DB 2.0
	private ArrayList<SiteSample> siteSamples;

	public ArrayList<SiteSample> getSiteSamples() {
		return siteSamples;
	}

	public void setSiteSamples(ArrayList<SiteSample> siteSamples) {
		this.siteSamples = siteSamples;
	}

	private enum ListItem {
		DORSALVIEW(1), ID(2), GENDER(3), SPECIES(4), DATECAPTURE(5), LOCATIONNAME(6), SURVEYID(7), MASS(8), LENGTH(9), DISCRIMINATOR(10), OBSERVER(11), FORMERID(12), DATEENTRY(13), RECORDER(14);
		private int value;

		private ListItem(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static int getSize() {
			return 14 + 1;
		}
	}

	public Frog() {
	}
	
	/**
	 * Creates a new frog and assigns it a single 
	 * @param ID
	 * @param species
	 * @param gender
	 * @param sample
	 */
	public Frog (int ID, String species, String gender, SiteSample sample) {
		this.ID = ID;
		this.species = species;
		this.gender = gender;
		siteSamples = new ArrayList<SiteSample>();
		siteSamples.add(sample);
	}
	
	public void addSiteSample(SiteSample sample) {
		siteSamples.add(sample);
	}
	
	/**
	 * Creates an XML element representing this frog in the XML database using the DB 2.0 styling (in progress) //TODO
	 */
	public Element createDBElement(Document document) {
		// CREATE FROG ELEMENT
		Element element = document.createElement("frog");
		// SET ID ATTRIBUTE OF FROG
		element.setAttribute("id", Integer.toString(getID()));
		
		// CREATE SPECIES ELEMENT
		Element species = document.createElement("species");
		species.appendChild(document.createTextNode(getSpecies()));
		element.appendChild(species);
		// CREATE GENDER ELEMENT
		Element gender = document.createElement("gender");
		gender.appendChild(document.createTextNode(getGender()));
		element.appendChild(gender);
		
		Element sites = document.createElement("sitesamples");
		for (SiteSample sample : siteSamples) {
			sites.appendChild(sample.createElement(document));
		}
		element.appendChild(sites);
		return element;
	}

	public String toString() {
		String str = "--Frog Object--\n";
		str += "ID: "+ID+"\n";
		str += "Gender: "+gender+"\n";
		str += "Species: "+species+"\n";
		str += "Site Samples: \n";
		for (SiteSample sample : siteSamples) {
			str += sample.toString();
		}
		return str;
	}

	public Object[] toArray() {
		Object fo[] = new Object[ListItem.getSize()];
		fo[ListItem.ID.getValue()] = "FROG" + ID;
		fo[ListItem.SURVEYID.getValue()] = "PLACEHLDR";
		fo[ListItem.GENDER.getValue()] = gender;
		fo[ListItem.SPECIES.getValue()] = species;
		fo[ListItem.MASS.getValue()] = "PLACEHLDR";
		fo[ListItem.LENGTH.getValue()] = "PLACEHLDR";
		fo[ListItem.DATECAPTURE.getValue()] = "PLACEHLDR";
		fo[ListItem.DATEENTRY.getValue()] = "PLACEHLDR";
		fo[ListItem.OBSERVER.getValue()] = "PLACEHLDR";
		fo[ListItem.RECORDER.getValue()] = "PLACEHLDR";
		fo[ListItem.DISCRIMINATOR.getValue()] = discriminator;
		fo[ListItem.LOCATIONNAME.getValue()] = "PLACEHLDR";
		fo[ListItem.DORSALVIEW.getValue()] = XMLFrogDatabase.getThumbPlaceholder();
		return fo;
	}

	public int getID() {
		return ID;
	}

	public String getSpecies() {
		return species;
	}

	public String getDiscriminator() {
		return discriminator;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * Gets an arraylist of all site images this frog has
	 * @return
	 */
	public ArrayList<SiteImage> getAllSiteImages() {
		ArrayList<SiteImage> images = new ArrayList<SiteImage>();
		for (SiteSample sample : siteSamples) {
			images.addAll(sample.getSiteImages());
		}
		return images;
	}

	/**
	 * Returns the first image in the image list of the site sample with the latest capture date
	 * @return first image in the latest capture date's site sample set of images
	 */
	public SiteImage getLatestImage() {
		DateLabelFormatter dlf = new DateLabelFormatter();
		Date latestDate = new Date(0L);
		SiteSample latestSample = null;
		for (SiteSample sample : siteSamples) {
			Date capDate;
			try {
				capDate = (Date) dlf.stringToValue(sample.getDateCapture());
				if (capDate.after(latestDate)){
					latestDate = capDate;
					latestSample = sample;
				}
			} catch (ParseException e) {
				IdentiFrog.LOGGER.writeError("Failed to parse capture date when getting latest frog image for a sample");
				continue;
			}
		}
		if (latestSample == null) {
			return null;
		} else {
			return latestSample.getSiteImages().get(0);
		}
	}

	/**
	 * Returns if the images that belong to this frog all have generated signatures.
	 * @return true if no signatures are missing in the Site Images, false otherwise.
	 */
	public boolean isFullySearchable(){
		for (SiteSample sample : siteSamples) {
			for (SiteImage img : sample.getSiteImages()){
				if (!img.isSignatureGenerated()){
					return false;
				}
			}
		}
		return true;
	}
}
