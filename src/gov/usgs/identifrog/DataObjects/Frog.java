package gov.usgs.identifrog.DataObjects;

import gov.usgs.identifrog.IdentiFrog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Frog {
	private int ID;
	private String species;
	private String gender;
	private ArrayList<Discriminator> discriminators;
	private ArrayList<SiteSample> siteSamples;

	public Frog() {
		siteSamples = new ArrayList<SiteSample>();
		discriminators = new ArrayList<Discriminator>();
	}

	/**
	 * Creates a new frog and assigns it a single sitesample
	 * 
	 * @param ID
	 * @param species
	 * @param gender
	 * @param sample
	 */
	public Frog(int ID, String species, String gender, SiteSample sample) {
		this.ID = ID;
		this.species = species;
		this.gender = gender;
		siteSamples = new ArrayList<SiteSample>();
		siteSamples.add(sample);
		discriminators = new ArrayList<Discriminator>();
	}

	/**
	 * This copy constructor creates a duplicate of the frog so changing values
	 * does not have side effects if this frog's data is discarded before
	 * commiting to the database. It recursively copies data structures.
	 * 
	 * @param frog
	 *            Frog to duplicate
	 */
	public Frog(Frog frog) {
		//Strings are immutable.
		this.species = frog.getSpecies();
		this.gender = frog.getGender();
		this.ID = frog.getID();
		siteSamples = new ArrayList<SiteSample>();
		discriminators = new ArrayList<Discriminator>();
		for (SiteSample s : frog.getSiteSamples()) {
			siteSamples.add(new SiteSample(s));
		}
		for (Discriminator d : frog.getDiscriminators()) {
			discriminators.add(new Discriminator(d));
		}
	}

	public void addSiteSample(SiteSample sample) {
		siteSamples.add(sample);
	}

	/**
	 * Creates an XML element representing this frog in the XML database using
	 * the DB 2.0 styling (in progress) //TODO
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

		// CREATE DISCRIMINATOR ELEMENT
		Element discriminatorsElem = document.createElement("localdiscriminators");
		for (Discriminator discriminator : discriminators) {
			Element discrimElem = document.createElement("discriminator");
			discrimElem.appendChild(document.createTextNode(Integer.toString(discriminator.getID())));
			discriminatorsElem.appendChild(discrimElem);
		}

		element.appendChild(discriminatorsElem);

		Element sites = document.createElement("sitesamples");
		for (SiteSample sample : siteSamples) {
			sites.appendChild(sample.createElement(document));
		}
		element.appendChild(sites);
		return element;
	}

	public String toString() {
		String str = "--Frog Object--\n";
		str += "ID: " + ID + "\n";
		str += "Gender: " + gender + "\n";
		str += "Species: " + species + "\n";
		str += "Discriminators:\n";
		for (Discriminator disc : discriminators) {
			str += "--";
			str += disc.toString();
			str += "\n";
		}
		str += "Site Samples: \n";
		for (SiteSample sample : siteSamples) {
			str += sample.toString();
		}
		return str;
	}

	public int getID() {
		return ID;
	}

	public String getSpecies() {
		return species;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * Gets an arraylist of all site images this frog has
	 * 
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
	 * Returns the first image in the image list of the site sample with the
	 * latest capture date
	 * 
	 * @return first image in the latest capture date's site sample set of
	 *         images
	 */
	public SiteImage getLatestImage() {
		SiteSample sample = getLatestSample();
		if (sample != null) {
			return sample.getSiteImages().get(0);
		} else {
			return null;
		}
	}

	/**
	 * Returns if the images that belong to this frog all have generated
	 * signatures.
	 * 
	 * @return true if no signatures are missing in the Site Images, false
	 *         otherwise.
	 */
	public boolean isFullySearchable() {
		for (SiteSample sample : siteSamples) {
			for (SiteImage img : sample.getSiteImages()) {
				if (!img.isSignatureGenerated()) {
					return false;
				}
			}
		}
		return true;
	}

	public void addDiscriminator(Discriminator discriminator) {
		discriminators.add(discriminator);
	}

	public ArrayList<Discriminator> getDiscriminators() {
		return discriminators;
	}

	public void setDiscriminators(ArrayList<Discriminator> discriminators) {
		this.discriminators = discriminators;
	}

	public ArrayList<SiteSample> getSiteSamples() {
		return siteSamples;
	}

	public void setSiteSamples(ArrayList<SiteSample> siteSamples) {
		this.siteSamples = siteSamples;
	}

	/**
	 * Returns the latest sitesample based on capture date
	 * 
	 * @return
	 */
	public SiteSample getLatestSample() {
		DateLabelFormatter dlf = new DateLabelFormatter();
		Date latestDate = new Date(0L);
		SiteSample latestSample = null;
		for (SiteSample sample : siteSamples) {
			Date capDate;
			try {
				capDate = (Date) dlf.stringToValue(sample.getDateCapture());
				if (capDate.after(latestDate)) {
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
			return latestSample;
		}
	}

	/**
	 * Gets the position in the list of sitesamples of the latest one based on capture date
	 * @return
	 */
	public int getLatestSampleIndex() {
		DateLabelFormatter dlf = new DateLabelFormatter();
		Date latestDate = new Date(0L);
		SiteSample latestSample = null;
		for (SiteSample sample : siteSamples) {
			Date capDate;
			try {
				capDate = (Date) dlf.stringToValue(sample.getDateCapture());
				if (capDate.after(latestDate)) {
					latestDate = capDate;
					latestSample = sample;
				}
			} catch (ParseException e) {
				IdentiFrog.LOGGER.writeError("Failed to parse capture date when getting latest frog image for a sample");
				continue;
			}
		}
		if (latestSample == null) {
			return 0;
		} else {
			return siteSamples.indexOf(latestSample);
		}
	}
}
