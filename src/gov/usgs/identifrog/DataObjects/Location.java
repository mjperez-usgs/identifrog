package gov.usgs.identifrog.DataObjects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Location {
	private String name;
	private String description;
	private String coordinateType;
	private String longitude;
	private String latitude;
	private String datum;
	private String zone;
	private Document document;
	private Element element;

	private enum ListItem {
		NAME(1), DESCRIPTION(2), LATITUDE(3), LONGITUDE(4), TYPE(5), DATUM(6), ZONE(7);
		private int value;

		private ListItem(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static int getSize() {
			return 7 + 1;
		}
	}

	public Location() {
	}

	public Location(String name, String description, String coordinateType, String longitude, String latitude, String datum) {
		this.name = name;
		this.description = description;
		this.coordinateType = coordinateType;
		this.longitude = longitude;
		this.latitude = latitude;
		this.datum = datum;
		zone = null;
	}

	public Location(String name, String description, String coordinateType, String longitude, String latitude, String datum, String zone) {
		this.name = name;
		this.description = description;
		this.coordinateType = coordinateType;
		if (coordinateType != null) {
			this.longitude = longitude;
			this.latitude = latitude;
			this.datum = datum;
			if (coordinateType != null && coordinateType.equals("UTM")) {
				this.zone = zone;
			} else {
				this.zone = null;
			}
		}
	}

	public Location(String name, String description, String coordinateType, String longitude, String latitude, String datum, String zone, Document document) {
		this.name = name;
		this.description = description;
		this.coordinateType = coordinateType;
		if (coordinateType != null) {
			this.longitude = longitude;
			this.latitude = latitude;
			this.datum = datum;
			if (coordinateType.equals("UTM")) {
				this.zone = zone;
			} else {
				this.zone = null;
			}
		}
		this.document = document;
	}

	public Location(Location location, Document document) {
		name = location.getName();
		description = location.getDescription();
		coordinateType = location.getCoordinateType();
		if (coordinateType != null) {
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			datum = location.getDatum();
			if (coordinateType.equals("UTM")) {
				zone = null;
			}
		}
		this.document = document;
	}

	private void createElement() {
		// CREATE LOCATION ELEMENT
		element = document.createElement("location");
		// CREATE NAME ELEMENT
		Element elementName = document.createElement("name");
		elementName.appendChild(document.createTextNode(getName()));
		element.appendChild(elementName);
		// CREATE DESCRIPTION ELEMENT
		Element elementDescription = document.createElement("description");
		if (getDescription() != null && getDescription().length() > 0) {
			elementDescription.appendChild(document.createTextNode(getDescription()));
		}
		element.appendChild(elementDescription);
		// CREATE COORDINATE ELEMENT
		Element elementCoordinate = document.createElement("coordinate");
		if (getCoordinateType() != null && getCoordinateType().length() > 0) {
			elementCoordinate.setAttribute("type", getCoordinateType());
			// CREATE X COORDINATE ELEMENT
			if (!getCoordinateType().equals("UTM")) {
				Element elementX = document.createElement("latitude");
				elementX.appendChild(document.createTextNode(getLatitude()));
				elementCoordinate.appendChild(elementX);
				// CREATE Y COORDINATE ELEMENT
				Element elementY = document.createElement("longitude");
				elementY.appendChild(document.createTextNode(getLongitude()));
				elementCoordinate.appendChild(elementY);
				// CREATE DATUM COORDINATE ELEMENT
				Element elementDatum = document.createElement("datum");
				elementDatum.appendChild(document.createTextNode(getDatum()));
				elementCoordinate.appendChild(elementDatum);
			}
			// CREATE ZONE COORDINATE ELEMENT
			if (coordinateType.equals("UTM")) {
				Element elementX = document.createElement("northing");
				elementX.appendChild(document.createTextNode(getLatitude()));
				elementCoordinate.appendChild(elementX);
				// CREATE Y COORDINATE ELEMENT
				Element elementY = document.createElement("easting");
				elementY.appendChild(document.createTextNode(getLongitude()));
				elementCoordinate.appendChild(elementY);
				// CREATE DATUM COORDINATE ELEMENT
				Element elementDatum = document.createElement("datum");
				elementDatum.appendChild(document.createTextNode(getDatum()));
				elementCoordinate.appendChild(elementDatum);
				Element elementZone = document.createElement("zone");
				elementZone.appendChild(document.createTextNode(getZone()));
				elementCoordinate.appendChild(elementZone);
			}
		}
		element.appendChild(elementCoordinate);
	}

	public String printBuffer() {
		String buffer = null;
		buffer = "LOCATION" + "\n";
		buffer = buffer + "\t" + "Name: " + name + "\n";
		buffer = buffer + "\t" + "Description: " + description + "\n";
		buffer = buffer + "\t" + "Type: " + coordinateType + "\n";
		buffer = buffer + "\t" + "Longitude: " + longitude + "\n";
		buffer = buffer + "\t" + "Latitude: " + latitude + "\n";
		buffer = buffer + "\t" + "Datum: " + datum + "\n";
		if (coordinateType != null && coordinateType.equals("UTM")) {
			buffer = buffer + "\t" + "Zone: " + zone + "\n";
		}
		return buffer;
	}

	public Object[] toArray() {
		Object fo[] = new Object[ListItem.getSize()];
		fo[ListItem.NAME.getValue()] = name;
		fo[ListItem.DESCRIPTION.getValue()] = description;
		fo[ListItem.LATITUDE.getValue()] = latitude;
		fo[ListItem.LONGITUDE.getValue()] = longitude;
		fo[ListItem.TYPE.getValue()] = coordinateType;
		fo[ListItem.DATUM.getValue()] = datum;
		fo[ListItem.ZONE.getValue()] = zone;
		return fo;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getCoordinateType() {
		return coordinateType;
	}

	public String getLongitude() {
		return longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getDatum() {
		return datum;
	}

	public String getZone() {
		return zone;
	}

	public Element getElement() {
		createElement();
		return element;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCoordinateType(String coordinateType) {
		this.coordinateType = coordinateType;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public void setDatum(String datum) {
		this.datum = datum;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}
}
