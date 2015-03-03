package gov.usgs.identifrog.DataObjects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Personel {
	private String type;
	private String firstName;
	private String lastName;
	private Document document;
	private Element element;

	private enum ListItem {
		TYPE(0), FIRSTNAME(2), LASTNAME(1);
		private int value;

		private ListItem(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static int getSize() {
			return 2 + 1;
		}
	}

	public Personel() {
	}

	public Personel(String type) {
		this.type = type;
	}

	public Personel(String type, String firstName, String lastName) {
		this.type = type;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public Personel(String type, String firstName, String lastName, Document document) {
		this.type = type;
		this.firstName = firstName;
		this.lastName = lastName;
		this.document = document;
	}

	public Personel(Personel personel, Document document) {
		type = personel.getType();
		firstName = personel.getFirstName();
		lastName = personel.getLastName();
		this.document = document;
	}

	private void createElement() {
		// CREATE PERSONEL ELEMENT
		element = document.createElement("personel");
		// SET TYPE ATTRIBUTE OF PERSONEL
		element.setAttribute("type", getType());
		// SET FIRSTNAME OF THE PERSONEL
		Element elementFirstname = document.createElement("firstname");
		elementFirstname.appendChild(document.createTextNode(getFirstName()));
		element.appendChild(elementFirstname);
		// SET LASTNAME OF THE PERSONEL
		Element elementLastname = document.createElement("lastname");
		elementLastname.appendChild(document.createTextNode(getLastName()));
		element.appendChild(elementLastname);
	}

	public String printBuffer() {
		String buffer = null;
		buffer = "Personel Type: " + type.toUpperCase() + "\n";
		buffer = buffer + "\t" + "First Name: " + firstName + "\n";
		buffer = buffer + "\t" + "Last Name: " + lastName + "\n";
		return buffer;
	}

	public Object[] toArray() {
		Object fo[] = new Object[ListItem.getSize()];
		fo[ListItem.FIRSTNAME.getValue()] = firstName;
		fo[ListItem.LASTNAME.getValue()] = lastName;
		return fo;
	}

	public String getType() {
		return type;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getName() {
		return firstName + " " + lastName;
	}

	public Element getElement() {
		createElement();
		return element;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
