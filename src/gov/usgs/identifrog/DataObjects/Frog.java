package gov.usgs.identifrog.DataObjects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import gov.usgs.identifrog.Handlers.FolderHandler;

public class Frog {
	private String ID;
	private String formerID;
	private String surveyID;
	private String species;
	private String gender;
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
	private Document document;

	private Element element;

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

	public Frog(String ID, String formerID, String surveyID, String species, String gender, String mass, String length, String dateCapture, String dateEntry, Personel observer, Personel recorder,
			String discriminator, String comments, Location location) {
		this.ID = ID;
		this.formerID = formerID;
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

	public Frog(String ID, String formerID, String surveyID, String species, String gender, String mass, String length, String dateCapture, String dateEntry, Personel observer, Personel recorder,
			String discriminator, String comments, Location location, String pathImage) {
		this.ID = ID;
		this.formerID = formerID;
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

	public Frog(String ID, String formerID, String surveyID, String species, String gender, String mass, String length, String dateCapture, String dateEntry, Personel observer, Personel recorder,
			String discriminator, String comments, Location location, String pathImage, Document document) {
		this.ID = ID;
		this.formerID = formerID;
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
		this.document = document;
	}

	public Frog(Frog frog) {
		ID = frog.getID();
		formerID = frog.getFormerID();
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

	public Frog(Frog frog, Document document) {
		ID = frog.getID();
		formerID = frog.getFormerID();
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
		this.document = document;
	}

	public void createElement() {
		// CREATE FROG ELEMENT
		element = document.createElement("frog");
		// SET ID ATTRIBUTE OF FROG
		element.setAttribute("id", getID().toString());
		// SET FORMER ID ATTRIBUTE OF FROG
		element.setAttribute("formerid", getFormerID());
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
		buffer = buffer + "Former ID: " + formerID + "\n";
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

	public Object[] toArray(FolderHandler fh) {
		Object fo[] = new Object[ListItem.getSize()];
		fo[ListItem.ID.getValue()] = "FROG" + ID;
		fo[ListItem.FORMERID.getValue()] = formerID;
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
		fo[ListItem.DORSALVIEW.getValue()] = fh.getThumbnailFolder() + pathImage;
		return fo;
	}

	public String getID() {
		return ID;
	}

	public String getFormerID() {
		return formerID;
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

	public Element getElement() {
		createElement();
		return element;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public void setFormerID(String formerID) {
		this.formerID = formerID;
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
}
