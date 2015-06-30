package gov.usgs.identifrog.DataObjects;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
	private int frogID; //used only for looking up what this belongs to without keeping a reference
	private String surveyID;
	private String mass;
	private String length;
	private String dateCapture;
	private String dateEntry;
	private User observer;
	private User recorder;
	private String comments;
	private Location location;
	private ArrayList<SiteImage> siteImages;

	/**
	 * Creates an element <SiteSample> for attaching to an XML document.
	 * 
	 * @param document
	 *            Document to use when creating this element
	 */
	public Element createElement(Document document) {

		// CREATE FROG ELEMENT
		Element element = document.createElement("sitesample");

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
		if (getMass() != null && getMass().length() > 0) {
			biometrics.setAttribute("mass", getMass().toString());
		}
		// SET LENGTH ATTRIBUTE OF BIOMETRICS
		if (getLength() != null && getLength().length() > 0) {
			biometrics.setAttribute("length", getLength().toString());
		}

		// CREATE DATE ELEMENT
		Element date = document.createElement("date");
		// SET CAPTURE ATTRIBUTE OF DATE
		date.setAttribute("capture", getDateCapture());
		// SET ENTRY ATTRIBUTE OF DATE
		date.setAttribute("entry", getDateEntry());

		// CREATE USER LINKS (to user DB)
		Element recorderElem = document.createElement("recorder");
		Element observerElem = document.createElement("observer");
		recorderElem.setTextContent(Integer.toString(recorder.getID()));
		observerElem.setTextContent(Integer.toString(observer.getID()));

		// CREATE COMMENTS ELEMENT
		Element comments = document.createElement("comments");
		comments.appendChild(document.createTextNode(getComments()));
		//
		Element locationElem = getLocation().createElement(document);
		//
		// element.appendChild(setupImage(document, "image",
		// getGenericImageName()));

		element.appendChild(surveyid);
		element.appendChild(comments);
		element.appendChild(recorderElem);
		element.appendChild(locationElem);
		element.appendChild(biometrics);
		element.appendChild(date);
		element.appendChild(observerElem);

		Element images = document.createElement("images");

		for (SiteImage image : siteImages) {
			images.appendChild(image.createElement(document));
		}
		element.appendChild(images);

		return element;
	}

	/**
	 * Creates a new, blank SiteSample with no data.
	 */
	public SiteSample() {
		siteImages = new ArrayList<SiteImage>();
	}

	/**
	 * Copy constructor
	 * 
	 * @param s
	 *            object to copy
	 */
	public SiteSample(SiteSample s) {
		siteImages = new ArrayList<SiteImage>();
		for (SiteImage img : s.getSiteImages()) {
			siteImages.add(new SiteImage(img));
		}
		this.frogID = s.getFrogID();
		this.dateCapture = s.getDateCapture();
		this.dateEntry = s.getDateEntry();
		this.surveyID = s.getSurveyID();
		this.mass = s.getMass();
		this.length = s.getLength();
		this.comments = s.getComments();
		this.observer = s.getObserver(); //not duplicated cause this is a reference to an object we don't modify
		this.recorder = s.getRecorder(); //not duplicated cause this is a reference to an object we don't modify
		this.location = new Location(s.getLocation());
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

	public User getObserver() {
		return observer;
	}

	public void setObserver(User observer) {
		this.observer = observer;
	}

	public User getRecorder() {
		return recorder;
	}

	public void setRecorder(User recorder) {
		this.recorder = recorder;
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

	public int getFrogID() {
		return frogID;
	}

	public void setFrogID(int i) {
		this.frogID = i;
	}

	public String toString() {
		String str = "----SiteSample--\n";
		str += "--Entry Date: " + dateEntry + "\n";
		str += "--Capture Date: " + dateCapture + "\n";
		str += "--Survey ID: " + surveyID + "\n";
		if (recorder != null) {
			str += "--Recorder: (" + recorder.getID() + ")" + recorder.getName() + "\n";
		} else {
			str += "--Recorder: null\n";
		}
		if (observer != null) {
			str += "--Observer: (" + observer.getID() + ")" + observer.getName() + "\n";
		} else {
			str += "--Observer: null\n";
		}
		str += "--Mass: " + mass + "\n";
		str += "--Length: " + mass + "\n";
		str += "--Comments: " + comments + "\n";
		str += "--Location: \n";
		str += location;
		str += "--SiteImages:";

		for (SiteImage image : siteImages) {
			str += "\n";
			str += image.toString();
		}
		return str;
	}

	/**
	 * Adds a site image to this samples list of site images.
	 * 
	 * @param img
	 *            Image to add to this sample
	 */
	public void addSiteImage(SiteImage img) {
		siteImages.add(img);
	}

	/**
	 * Creates an element for attaching to a frog that has been imported but not-yet edited
	 * @param document Document to create XML with
	 * @return "Fresh" XML version of this object
	 */
	public Element createFreshElement(Document document) {
		Element element = document.createElement("sitesample");


		// CREATE DATE ELEMENT
		Element date = document.createElement("date");
		// SET ENTRY ATTRIBUTE OF DATE
		date.setAttribute("entry", getDateEntry());
		
		Element images = document.createElement("images");
		for (SiteImage image : siteImages) {
			images.appendChild(image.createElement(document));
		}
		element.appendChild(date);
		element.appendChild(images);

		return element;
	}

}