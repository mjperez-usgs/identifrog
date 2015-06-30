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
public class Discriminator implements InUseFlag, Comparable<Discriminator> {

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
		inUse = false;
	}

	/**
	 * Copy constructor
	 * @param d Object to copy
	 */
	public Discriminator(Discriminator d) {
		//Strings are immutable
		this.text = d.getText();
		this.inUse = d.isInUse();
		this.id = d.getID();
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (inUse ? 1231 : 1237);
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Discriminator other = (Discriminator) obj;
		if (inUse != other.inUse)
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	@Override
	public int compareTo(Discriminator otherDiscriminator) {
		return toString().compareTo(otherDiscriminator.toString());
	}
}
