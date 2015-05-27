package gov.usgs.identifrog.DataObjects;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The sitesample class defines a data object representing a "data collection"
 * at a point in time. For example, this represents collecting frog data for a
 * particular day. All frogs will contain at least one SiteSample simply because
 * the data was collected at a site at least once. Frogs with multiple site
 * samples have data collected from different points in time.
 * 
 * SiteSamples contain information relevant to that point in time. As such,
 * things such as gender and species are not considered in a site sample as
 * these do not change over time.
 * 
 * @author mjperez
 *
 */
public class SiteSample {
	private String surveyID;
	private String mass;
	private String length;
	private String dateCapture;
	private String dateEntry;
	private Personel observer;
	private Personel recorder;
	private String discriminator;
	private String comments;
	private Location location;
	private String pathImage;
	private ArrayList<SiteImage> siteImages;

	/**
	 * Creates an element <SiteSample> for attaching to an XML document.
	 * 
	 * @param document Document to use when creating this element
	 */
	public Element createElement(Document document) {
		// CREATE FROG ELEMENT
		Element element = document.createElement("sitesample");
		// SET ID ATTRIBUTE OF FROG
		// element.setAttribute("id", getID().toString());
		// SET FORMER ID ATTRIBUTE OF FROG
		// element.setAttribute("formerid", getFormerID());
		// CREATE SURVEY ID ELEMENT
		Element surveyid = document.createElement("surveyid");
		surveyid.appendChild(document.createTextNode(getSurveyID()));

		// CREATE SPECIES ELEMENT
		// Element species = document.createElement("species");
		// species.appendChild(document.createTextNode(getSpecies()));
		// element.appendChild(species);
		// CREATE GENDER ELEMENT
		// Element gender = document.createElement("gender");
		// gender.appendChild(document.createTextNode(getGender()));
		// element.appendChild(gender);
		// CREATE BIOMETRICS ELEMENT
		Element biometrics = document.createElement("biometrics");
		// SET MASS ATTRIBUTE OF BIOMETRICS
		if (getMass() != null || getMass().length() > 0) {
			biometrics.setAttribute("mass", getMass().toString());
		}
		// SET LENGTH ATTRIBUTE OF BIOMETRICS
		if (getLength() != null || getLength().length() > 0) {
			biometrics.setAttribute("length", getLength().toString());
		}

		// CREATE DATE ELEMENT
		Element date = document.createElement("date");
		// SET CAPTURE ATTRIBUTE OF DATE
		date.setAttribute("capture", getDateCapture());
		// SET ENTRY ATTRIBUTE OF DATE
		date.setAttribute("entry", getDateEntry());

		// CREATE PERSONEL OBSERVER ELEMENT
		Personel elementObserver = new Personel(getObserver(), document);

		// CREATE PERSONEL RECORDER ELEMENT
		Personel elementRecorder = new Personel(getRecorder(), document);

		// CREATE DISCRIMINATOR ELEMENT
		Element discriminator = document.createElement("discriminator");
		discriminator.appendChild(document.createTextNode(getDiscriminator()));

		// CREATE COMMENTS ELEMENT
		Element comments = document.createElement("comments");
		comments.appendChild(document.createTextNode(getComments()));

		//
		Location elementLocation = new Location(getLocation(), document);

		//
		// element.appendChild(setupImage(document, "image",
		// getGenericImageName()));

		element.appendChild(surveyid);
		element.appendChild(comments);
		element.appendChild(elementRecorder.getElement());
		element.appendChild(discriminator);
		element.appendChild(elementLocation.getElement());
		element.appendChild(biometrics);
		element.appendChild(date);
		element.appendChild(elementObserver.getElement());

		Element images = document.createElement("images");

		for (SiteImage image : siteImages) {
			images.appendChild(image.createElement(document));
		}
		element.appendChild(images);

		return element;
	}

	public ArrayList<SiteImage> getSiteImages() {
		return siteImages;
	}

	public void setSiteImages(ArrayList<SiteImage> siteImages) {
		this.siteImages = siteImages;
	}

	public String getSurveyID() {
		return surveyID;
	}

	public void setSurveyID(String surveyID) {
		this.surveyID = surveyID;
	}

	public String getMass() {
		return mass;
	}

	public void setMass(String mass) {
		this.mass = mass;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getDateCapture() {
		return dateCapture;
	}

	public void setDateCapture(String dateCapture) {
		this.dateCapture = dateCapture;
	}

	public String getDateEntry() {
		return dateEntry;
	}

	public void setDateEntry(String dateEntry) {
		this.dateEntry = dateEntry;
	}

	public Personel getObserver() {
		return observer;
	}

	public void setObserver(Personel observer) {
		this.observer = observer;
	}

	public Personel getRecorder() {
		return recorder;
	}

	public void setRecorder(Personel recorder) {
		this.recorder = recorder;
	}

	public String getDiscriminator() {
		return discriminator;
	}

	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getPathImage() {
		return pathImage;
	}

	public void setPathImage(String pathImage) {
		this.pathImage = pathImage;
	}
}