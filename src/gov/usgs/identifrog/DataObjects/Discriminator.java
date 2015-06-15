package gov.usgs.identifrog.DataObjects;

import gov.usgs.identifrog.interfaces.InUseFlag;

import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The discriminator class contains a string (the human readable discriminator) and an ID to distinguish different discriminators. 
 * Using these discriminator IDs the users of the software can discriminate against frogs when searching the database for matches,
 * so for example, you could say "Find frogs with only 3 legs" using a "Only has 3 legs" discriminator.
 * 
 * This is stored in the frog but is used by the search methods.
 * 
 * @author mjperez
 *
 */
public class Discriminator implements InUseFlag {

	private String text;
	private boolean inUse;
	private int id;

	/**
	 * Creates a discriminator object with the given ID and the discriminator string.
	 * @param id ID to assign to discriminator
	 * @param discriminator Human readable description
	 */
	public Discriminator(int id, String discriminator) {
		this.id = id;
		this.text = discriminator;
		inUse = new Random().nextBoolean();
	}

	/**
	 * Empty constructor loading from DB
	 */
	public Discriminator() {
		// TODO Auto-generated constructor stub
	}

	public String getText() {
		return text;
	}

	public void setText(String discriminator) {
		this.text = discriminator;
	}

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public boolean isInUse() {
		return inUse;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	/**
	 * Turns this Discriminator object into a DB2.0 XML element
	 * @param doc Document to use for creating the element
	 * @return This object, in XML Element form
	 */
	public Element createElement(Document document) {
		Element element = document.createElement("discriminator");
		element.setAttribute("id", Integer.toString(getID()));
		element.setTextContent(getText());
		return element;
	}
	
	public String toString(){
		return getText();
	}
	
	/**
	 * Used as a debugging toSTring() as toString() is used for the generic list renderer to display text.
	 * @return String reprensentation of this object for debugging
	 */
	public String debugToString() {
		// TODO Auto-generated method stub
		return "Discriminator [text=" + text + ", inUse=" + inUse + ", id=" + id + "]";

	}
}