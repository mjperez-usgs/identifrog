package gov.usgs.identifrog.DataObjects;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class User implements Comparable<User>{
	private String firstName;
	private String lastName;
	private int ID;
	private boolean inUse;

	public User() {

	}

	public User(int ID, String firstName, String lastName) {
		this.ID = ID;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public Element createElement(Document document) {
		// CREATE PERSONEL ELEMENT
		Element element = document.createElement("user");
		// SET ID ATTRIBUTE OF PERSONEL
		element.setAttribute("id", Integer.toString(getID()));
		// SET FIRSTNAME OF THE PERSONEL
		Element elementFirstname = document.createElement("firstname");
		elementFirstname.appendChild(document.createTextNode(getFirstName()));
		element.appendChild(elementFirstname);
		// SET LASTNAME OF THE PERSONEL
		Element elementLastname = document.createElement("lastname");
		elementLastname.appendChild(document.createTextNode(getLastName()));
		element.appendChild(elementLastname);
		return element;
	}

	public String printBuffer() {
		String buffer = null;
		buffer = "User DB ID: " + getID() + "\n";
		buffer = buffer + "\t" + "First Name: " + firstName + "\n";
		buffer = buffer + "\t" + "Last Name: " + lastName + "\n";
		return buffer;
	}

	/*
	 * public Object[] toArray() { Object fo[] = new Object[ListItem.getSize()];
	 * fo[ListItem.FIRSTNAME.getValue()] = firstName;
	 * fo[ListItem.LASTNAME.getValue()] = lastName; return fo; }
	 */

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getName() {
		return firstName + " " + lastName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}
	
	public boolean isInUse() {
		return inUse;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	@Override
	public int compareTo(User otherUser) {
		return firstName.compareToIgnoreCase(otherUser.firstName);
	}
}
