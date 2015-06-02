package gov.usgs.identifrog.DataObjects;

import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Frog {
	private int ID;
	private String surveyID;
	private String species;
	private String gender;
	private String mass;
	private String length;
	private String dateCapture;
	private String dateEntry;
	private String observer2, recorder2;
	private Personel observer;
	private Personel recorder;
	private String discriminator;
	private String comments;
	private Location location;
	private String pathImage;
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
	 * Creates a frog using the old DB 1.0 style where a frog is a collection of all data rather than biometrics and a set of sitesamples
	 * @param ID
	 * @param formerID
	 * @param surveyID
	 * @param species
	 * @param gender
	 * @param mass
	 * @param length
	 * @param dateCapture
	 * @param dateEntry
	 * @param observer
	 * @param recorder
	 * @param discriminator
	 * @param comments
	 * @param location
	 */
	public Frog(int ID, String surveyID, String species, String gender, String mass, String length, String dateCapture, String dateEntry, Personel observer, Personel recorder,
			String discriminator, String comments, Location location) {
		this.ID = ID;
		this.surveyID = surveyID;
		this.species = species;
		this.gender = gender;
		this.mass = mass;
		this.length = length;
		this.dateCapture = dateCapture;
		this.dateEntry = dateEntry;
		this.observer = observer;
		this.recorder = recorder;
		this.discriminator = discriminator;
		this.comments = comments;
		this.location = location;
	}
	
	public Frog(int ID, String surveyID, String species, String gender, String mass, String length, String dateCapture, String dateEntry, String observer, String recorder,
			String discriminator, String comments, Location location) {
		this.ID = ID;
		this.surveyID = surveyID;
		this.species = species;
		this.gender = gender;
		this.mass = mass;
		this.length = length;
		this.dateCapture = dateCapture;
		this.dateEntry = dateEntry;
		this.observer2 = observer;
		this.recorder2 = recorder;
		this.discriminator = discriminator;
		this.comments = comments;
		this.location = location;
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

	public Frog(int ID, String formerID, String surveyID, String species, String gender, String mass, String length, String dateCapture, String dateEntry, Personel observer, Personel recorder,
			String discriminator, String comments, Location location, String pathImage) {
		this.ID = ID;
		this.surveyID = surveyID;
		this.species = species;
		this.gender = gender;
		this.mass = mass;
		this.length = length;
		this.dateCapture = dateCapture;
		this.dateEntry = dateEntry;
		this.observer = observer;
		this.recorder = recorder;
		this.discriminator = discriminator;
		this.comments = comments;
		this.location = location;
		this.pathImage = pathImage;
	}
	
	/**
	 * This constructor clones the frog passed in as a parameter so one can safely edit the returned object without side effects.
	 * @param frog Frog object to clone
	 */
	public Frog(Frog frog) {
		ID = frog.getID();
		surveyID = frog.getSurveyID();
		species = frog.getSpecies();
		gender = frog.getGender();
		mass = frog.getMass();
		length = frog.getLength();
		dateCapture = frog.getDateCapture();
		dateEntry = frog.getDateEntry();
		observer = frog.getObserver();
		recorder = frog.getRecorder();
		discriminator = frog.getDiscriminator();
		comments = frog.getComments();
		location = frog.getLocation();
		pathImage = frog.getGenericImageName();
	}

	/**
	 * Creates an XML element representing this frog in the XML database.
	 * @return 
	 */
	public Element createElement(Document document) {
		// CREATE FROG ELEMENT
		Element element = document.createElement("frog");
		// SET ID ATTRIBUTE OF FROG
		element.setAttribute("id", Integer.toString(getID()));
		// CREATE SURVEY ID ELEMENT
		Element surveyid = document.createElement("surveyid");
		surveyid.appendChild(document.createTextNode(getSurveyID()));
		element.appendChild(surveyid);
		// CREATE SPECIES ELEMENT
		Element species = document.createElement("species");
		species.appendChild(document.createTextNode(getSpecies()));
		element.appendChild(species);
		// CREATE GENDER ELEMENT
		Element gender = document.createElement("gender");
		gender.appendChild(document.createTextNode(getGender()));
		element.appendChild(gender);
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
		element.appendChild(biometrics);
		// CREATE DATE ELEMENT
		Element date = document.createElement("date");
		// SET CAPTURE ATTRIBUTE OF DATE
		date.setAttribute("capture", getDateCapture());
		// SET ENTRY ATTRIBUTE OF DATE
		date.setAttribute("entry", getDateEntry());
		element.appendChild(date);
		// CREATE PERSONEL OBSERVER ELEMENT
		Personel elementObserver = new Personel(getObserver(), document);
		element.appendChild(elementObserver.getElement());
		// CREATE PERSONEL RECORDER ELEMENT
		Personel elementRecorder = new Personel(getRecorder(), document);
		element.appendChild(elementRecorder.getElement());
		// CREATE DISCRIMINATOR ELEMENT
		Element discriminator = document.createElement("discriminator");
		discriminator.appendChild(document.createTextNode(getDiscriminator()));
		element.appendChild(discriminator);
		// CREATE COMMENTS ELEMENT
		Element comments = document.createElement("comments");
		comments.appendChild(document.createTextNode(getComments()));
		element.appendChild(comments);
		//
		Location elementLocation = new Location(getLocation(), document);
		element.appendChild(elementLocation.getElement());
		//
		element.appendChild(setupImage(document, "image", getGenericImageName()));
		
		return element;
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

	private Element setupImage(Document doc, String type, String path) {
		// CREATE IMAGE ELEMENT
		Element image = doc.createElement("image");
		// SET TYPE ATTRIBUTE OF IMAGE
		image.setAttribute("type", type);
		image.appendChild(doc.createTextNode(path));
		return image;
	}

	public String toString() {
		String buffer = null;
		buffer = "Frog ID: " + ID + "\n";
		buffer = buffer + "Survey ID: " + surveyID + "\n";
		buffer = buffer + "Gender: " + gender + "\n";
		buffer = buffer + "BIOMETICS\n";
		buffer = buffer + "\t" + "Mass: " + mass + "\n";
		buffer = buffer + "\t" + "Length: " + length + "\n";
		buffer = buffer + "Capture Date: " + dateCapture + "\n";
		buffer = buffer + "Entry Date: " + dateEntry + "\n";
		buffer = buffer + observer.printBuffer();
		buffer = buffer + recorder.printBuffer();
		buffer = buffer + "Discriminator: " + discriminator + "\n";
		buffer = buffer + "Comments: " + comments + "\n";
		buffer = buffer + location.printBuffer();
		buffer = buffer + "PATHS" + "\n";
		buffer = buffer + imageBuffer("Image", pathImage);
		return buffer;
	}

	private String imageBuffer(String type, String path) {
		String buffer = null;
		buffer = "\t" + type + ": " + path + "\n";
		return buffer;
	}

	public Object[] toArray() {
		Object fo[] = new Object[ListItem.getSize()];
		fo[ListItem.ID.getValue()] = "FROG" + ID;
		fo[ListItem.SURVEYID.getValue()] = surveyID;
		fo[ListItem.GENDER.getValue()] = gender;
		fo[ListItem.SPECIES.getValue()] = species;
		fo[ListItem.MASS.getValue()] = mass;
		fo[ListItem.LENGTH.getValue()] = length;
		fo[ListItem.DATECAPTURE.getValue()] = dateCapture;
		fo[ListItem.DATEENTRY.getValue()] = dateEntry;
		fo[ListItem.OBSERVER.getValue()] = observer.getName();
		fo[ListItem.RECORDER.getValue()] = recorder.getName();
		fo[ListItem.DISCRIMINATOR.getValue()] = discriminator;
		fo[ListItem.LOCATIONNAME.getValue()] = location.getName();
		fo[ListItem.DORSALVIEW.getValue()] = XMLFrogDatabase.getThumbnailFolder() + pathImage;
		return fo;
	}

	public int getID() {
		return ID;
	}

	public String getSpecies() {
		return species;
	}

	public String getMass() {
		return mass;
	}

	public String getLength() {
		return length;
	}

	public String getDateCapture() {
		return dateCapture;
	}

	public String getDateEntry() {
		return dateEntry;
	}

	public Personel getObserver() {
		return observer;
	}

	public Personel getRecorder() {
		return recorder;
	}

	public String getDiscriminator() {
		return discriminator;
	}

	public String getComments() {
		return comments;
	}

	public Location getLocation() {
		return location;
	}

	public String getGenericImageName() {
		return pathImage;
	}

	public String getPathSignature() {
		return pathImage.substring(0, pathImage.indexOf(".")) + ".dsg";
	}

	public Element getElement(Document document) {
		return createElement(document);
	}

	public void setID(int iD) {
		ID = iD;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public void setMass(String mass) {
		this.mass = mass;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public void setDateCapture(String dateCapture) {
		this.dateCapture = dateCapture;
	}

	public void setDateEntry(String dateEntry) {
		this.dateEntry = dateEntry;
	}

	public void setObserver(Personel observer) {
		this.observer = observer;
	}

	public void setRecorder(Personel recorder) {
		this.recorder = recorder;
	}

	public void setDiscriminator(String discriminator) {
		this.discriminator = discriminator;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setPathImage(String pathImage) {
		this.pathImage = pathImage;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public void setSurveyID(String surveyID) {
		this.surveyID = surveyID;
	}

	public String getSurveyID() {
		return surveyID;
	}

	/**
	 * Gets the string name of whoever entered this frog into DB2.0
	 */
	public String getRecorder2() {
		// TODO Auto-generated method stub
		return recorder2;
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
}
