package gov.usgs.identifrog.DataObjects;

import gov.usgs.identifrog.IdentiFrog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Location implements Comparable<Location> {
	private String name;
	private String description;
	private String coordinateType;
	private String longitude;
	private String latitude;
	private String datum;
	private String zone;

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

	/**
	 * Copy constructor
	 * @param location object to be copied
	 */
	public Location(Location location) {
		name = location.getName();
		description = location.getDescription();
		coordinateType = location.getCoordinateType();
		if (coordinateType != null) {
			longitude = location.getLongitude();
			latitude = location.getLatitude();
			datum = location.getDatum();
			if (coordinateType.equals("UTM")) {
				zone = location.getZone();
			}
		}
	}

	/**
	 * Creates an XML representation of this object for storing in the DB.
	 * Creates the location element and populates and returns the entire object that can be directly attached to a parent
	 * @param document Document to use for construction of the node
	 * @return Element that can be attached to a parent
	 */
	public Element createElement(Document document) {
		// CREATE LOCATION ELEMENT
		Element element = document.createElement("location");
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
		return element;
	}

	public String toString() {
		String str = "\t" + "Name: " + name + "\n";
		str += "\t" + "Description: " + description + "\n";
		str += "\t" + "Type: " + coordinateType + "\n";
		str += "\t" + "Longitude: " + longitude + "\n";
		str += "\t" + "Latitude: " + latitude + "\n";
		str += "\t" + "Datum: " + datum + "\n";
		str += "\t" + "Zone: " + zone + "\n";
		return str;
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

	/**
	 * In an (X,Y) plane, this returns the horizontal point (in standard US coordinate system, known as X), aka EAST/WEST<br>
	 * For UTM it returns Y. For LatLong it returns X.<br>
	 * <br>
	 * Latitude: Y<br>
	 * Longitude: X<br>
	 * <br>
	 * Easting: X<br>
	 * Northing: Y<br>
	 * <br>
	 * On a standard north facing map, latitude is represented by horizontal lines, which are drawn an equal distance from the equator that are equally spaced up and down (North and South) the Y axis. <br>
	 * It's easy to think that since they are horizontal lines, they would be on the x axis, but they are not, as the lines trace an equal distance from the equator line at the same X point.
	 * 
	 * @return horizontal point value
	 */
	public String getLongitude() {
		return longitude;
	}
	/**
	 * In an (X,Y) plane, this returns the vertical point (in standard US coordinate system, known as Y), aka NORTH/SOUTH<br>
	 * For UTM it returns X. For LatLong it returns Y.<br>
	 * <br>
	 * Latitude: Y<br>
	 * Longitude: X<br>
	 * <br>
	 * Easting: X<br>
	 * Northing: Y<br>
	 * <br>
	 * On a standard north facing map, longitude is represented by vertical lines, which are drawn an equal distance from the Prime Meridian that are equally spaced left and right (East and West) the Y axis.<br> 
	 * It's easy to think that since they are vertical lines, they would be on the y axis, but they are not, as the lines trace an equal distance from the Prime Meridian line at the same Y point.
	 * 
	 * @return vertical point value
	 */
	public String getLatitude() {
		return latitude;
	}

	public String getDatum() {
		return datum;
	}

	public String getZone() {
		return zone;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the coordinate type. Valid values are UTM, LatLong, and null.
	 * @param coordinateType
	 */
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

	@Override
	public int compareTo(Location otherLoc) {
		return name.toLowerCase().compareTo(otherLoc.getName().toLowerCase());
	}
}
