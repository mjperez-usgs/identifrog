package gov.usgs.identifrog.DataObjects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Location implements Comparable<Location> {
	public static String COORDINATE_LATLONG = "Lat/Long";
	public static String COORDINATE_UTM = "UTM";
	public static int EMPTY_ZONE = 0;
	
	private String name;
	private String description;
	private String coordinateType;
	private String longitude;
	private String latitude;
	private String datum;
	private int zone;

	/**
	 * Empty constructor
	 */
	public Location() {
	}
	
	
	/**
	 * Constructor for creating a Lat/Long value. Sets zone to EMPTY_ZONE
	 * @param name
	 * @param description
	 * @param coordinateType
	 * @param longitude
	 * @param latitude
	 * @param datum
	 */
	public Location(String name, String description, String coordinateType, String longitude, String latitude, String datum) {
		this.name = name;
		this.description = description;
		this.coordinateType = coordinateType;
		this.longitude = longitude;
		this.latitude = latitude;
		this.datum = datum;
		zone = EMPTY_ZONE;
	}

	/**
	 * Constructor for creating a UTM location, includes a zone.
	 * @param name
	 * @param description
	 * @param coordinateType
	 * @param longitude
	 * @param latitude
	 * @param datum
	 * @param zone
	 */
	public Location(String name, String description, String coordinateType, String longitude, String latitude, String datum, int zone) {
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
				this.zone = EMPTY_ZONE;
			}
		}
	}

	/**
	 * Copy constructor
	 * @param location object to be copied
	 */
	public Location(Location location) {
		if (location == null) {
			//likely was imported and has no existing location
			coordinateType = COORDINATE_LATLONG;
			return;
		}
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
				elementZone.appendChild(document.createTextNode(Integer.toString(getZone())));
				elementCoordinate.appendChild(elementZone);
			}
		}
		element.appendChild(elementCoordinate);
		return element;
	}
	
	public String toString(){
		return name;
	}

	public String toDebugString() {
		String str = "\t" + "Name: " + name + "\n";
		str += "\t" + "Description: " + description + "\n";
		str += "\t" + "Type: " + coordinateType + "\n";
		str += "\t" + "Longitude: " + longitude + "\n";
		str += "\t" + "Latitude: " + latitude + "\n";
		str += "\t" + "Datum: " + datum + "\n";
		str += "\t" + "Zone: " + zone + "\n";
		return str;
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

	public int getZone() {
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

	public void setZone(int zone) {
		this.zone = zone;
	}

	@Override
	public int compareTo(Location otherLoc) {
		return name.toLowerCase().compareTo(otherLoc.getName().toLowerCase());
	}
}
