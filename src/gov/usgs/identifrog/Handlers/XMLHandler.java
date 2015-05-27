package gov.usgs.identifrog.Handlers;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.Location;
import gov.usgs.identifrog.DataObjects.Personel;
import gov.usgs.identifrog.DataObjects.SiteSample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.omg.CORBA.NamedValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLHandler {
	private File file;
	ArrayList<Frog> frogs;

	public XMLHandler() {
		frogs = new ArrayList<Frog>();
	}

	public XMLHandler(File file) {
		this.file = file;
		frogs = new ArrayList<Frog>();
	}

	public XMLHandler(String filename) {
		file = new File(filename);
		frogs = new ArrayList<Frog>();
	}

	public XMLHandler(File file, ArrayList<Frog> frogs) {
		this.file = file;
		this.frogs = frogs;
	}

	public boolean CreateXMLFile() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		}
		// CREATE ROOT ELEMENT
		Document doc = docBuilder.newDocument();
		Element root = doc.createElement("frogdatabase");
		doc.appendChild(root);

		// WRITE XML FILE
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute("indent-number",  2);
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
		} catch (TransformerConfigurationException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		}
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		}
		return true;
	}

	public boolean WriteXMLFile() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		}
		// CREATE ROOT ELEMENT
		Document doc = docBuilder.newDocument();
		Element root = doc.createElement("frogdatabase");
		doc.appendChild(root);
		for (Frog frog : frogs) {
			root.appendChild(frog.createElement(doc));
		}

		// WRITE XML FILE
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		}
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(file);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		}
		return true;
	}

	/**
	 * Reads the database XML file and loads frog data into an arraylist.
	 * This method is built for DB 2.0.
	 * @return Arraylist of Frog objects containing data from the XML database
	 * 
	 */
	public ArrayList<Frog> ReadXMLFile() {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			IdentiFrog.LOGGER.writeException(e);
		}
		try {
			doc = docBuilder.parse(file);
		} catch (SAXException e) {
			IdentiFrog.LOGGER.writeException(e);
		} catch (IOException e) {
			IdentiFrog.LOGGER.writeException(e);
		}
		doc.getDocumentElement().normalize();
		Frog frog = null;
		NodeList nList = doc.getElementsByTagName("frog");

		/*for (int i = 0; i < nList.getLength(); i++) {
			
			 * DB 1.0 code
			 * frog = new Frog();
			frog.setID(nList.item(i).getAttributes().getNamedItem("id").getNodeValue());
			frog.setFormerID(nList.item(i).getAttributes().getNamedItem("formerid").getNodeValue());

			Element eElem = (Element) nList.item(i);
			frog.setSpecies(eElem.getElementsByTagName("species").item(0).getTextContent());
			frog.setGender(eElem.getElementsByTagName("gender").item(0).getTextContent());
			frog.setDateCapture(eElem.getElementsByTagName("date").item(0).getAttributes().getNamedItem("capture").getNodeValue());
			frog.setDateEntry(eElem.getElementsByTagName("date").item(0).getAttributes().getNamedItem("entry").getNodeValue());
			NodeList bioNode = eElem.getElementsByTagName("biometrics");
			if (bioNode.item(0).hasAttributes()) {
				NamedNodeMap bm = bioNode.item(0).getAttributes();
				if (bm.getNamedItem("mass") != null) {
					frog.setMass(bm.getNamedItem("mass").getNodeValue());
				}
				if (bm.getNamedItem("length") != null) {
					frog.setLength(bm.getNamedItem("length").getNodeValue());
				}
			}
			frog.setDiscriminator(eElem.getElementsByTagName("discriminator").item(0).getTextContent());
			// String comments =
			// eElem.getElementsByTagName("comments").item(0).getTextContent();
			NodeList comments = eElem.getElementsByTagName("comments").item(0).getChildNodes();
			if (comments.getLength() < 1) {
				frog.setComments("");
			} else {
				frog.setComments(comments.item(0).getNodeValue());
			}
			frog.setSurveyID(eElem.getElementsByTagName("surveyid").item(0).getTextContent());

			Personel observer = new Personel("observer");
			Personel recorder = new Personel("recorder");
			for (int j = 0; j < eElem.getElementsByTagName("personel").getLength(); j++) {
				NodeList nn = eElem.getElementsByTagName("personel");
				if (nn.item(j).getAttributes().getNamedItem("type").getNodeValue().equals("observer")) {
					observer.setFirstName(((Element) nn.item(j)).getElementsByTagName("firstname").item(0).getTextContent());
					observer.setLastName(((Element) nn.item(j)).getElementsByTagName("lastname").item(0).getTextContent());
				}
				if (nn.item(j).getAttributes().getNamedItem("type").getNodeValue().equals("recorder")) {
					recorder.setFirstName(((Element) nn.item(j)).getElementsByTagName("firstname").item(0).getTextContent());
					recorder.setLastName(((Element) nn.item(j)).getElementsByTagName("lastname").item(0).getTextContent());
				}
			}
			frog.setObserver(observer);
			frog.setRecorder(recorder);

			Location location = new Location();
			NodeList nn = eElem.getElementsByTagName("location");
			location.setName(((Element) nn.item(0)).getElementsByTagName("name").item(0).getTextContent());
			NodeList description = ((Element) nn.item(0)).getElementsByTagName("description");
			if (description.getLength() < 1) {
				location.setDescription("");
			} else {
				location.setDescription(description.item(0).getNodeValue());
			}

			// location.setCoordinateType(((Element)
			// nn.item(0)).getElementsByTagName("coordinate").item(0).getAttributes().getNamedItem("type").getNodeValue());
			NodeList coordinate = ((Element) nn.item(0)).getElementsByTagName("coordinate");
			if (coordinate.getLength() < 1) {
				location.setCoordinateType("");
			} else {
				Node ct = coordinate.item(0).getAttributes().getNamedItem("type");
				if (ct != null && ct.hasAttributes()) {
					location.setCoordinateType(coordinate.item(0).getAttributes().getNamedItem("type").getNodeValue());
					nn = ((Element) nn.item(0)).getElementsByTagName("coordinate");
					if (!location.getCoordinateType().equals("UTM")) {
						location.setLongitude(((Element) nn.item(0)).getElementsByTagName("longitude").item(0).getTextContent());
						location.setLatitude(((Element) nn.item(0)).getElementsByTagName("latitude").item(0).getTextContent());
						location.setDatum(((Element) nn.item(0)).getElementsByTagName("datum").item(0).getTextContent());
					}
					if (location.getCoordinateType().equals("UTM")) {
						location.setLongitude(((Element) nn.item(0)).getElementsByTagName("easting").item(0).getTextContent());
						location.setLatitude(((Element) nn.item(0)).getElementsByTagName("northing").item(0).getTextContent());
						location.setDatum(((Element) nn.item(0)).getElementsByTagName("datum").item(0).getTextContent());
						location.setZone(((Element) nn.item(0)).getElementsByTagName("zone").item(0).getTextContent());
					}
				}
			}

			frog.setLocation(location);

			String temp;
			for (int j = 0; j < eElem.getElementsByTagName("image").getLength(); j++) {
				temp = eElem.getElementsByTagName("image").item(j).getAttributes().getNamedItem("type").getNodeValue();
				if (temp.equals("image")) {
					frog.setPathImage(eElem.getElementsByTagName("image").item(j).getTextContent());
				}
			}
			
			frogs.add(frog);
		}*/
		for (int i = 0; i < nList.getLength(); i++) {
			//DB2.0 code
			frog = new Frog();
			Element frogElement = (Element) nList.item(i);
			NamedNodeMap frogAttributes = nList.item(i).getAttributes();
			
			//Load frog object data
			frog.setID(frogAttributes.getNamedItem("id").getNodeValue());
			frog.setGender(frogElement.getElementsByTagName("gender").item(0).getTextContent());
			frog.setSpecies(frogElement.getElementsByTagName("species").item(0).getTextContent());

			//load sitesamples
			Element siteSamples = (Element) frogElement.getElementsByTagName("sitesamples").item(0);
			NodeList sList = siteSamples.getElementsByTagName("sitesample");
			for (int s = 0; i < sList.getLength(); s++) {
				//load collection data
				IdentiFrog.LOGGER.writeMessage("Parsing XML for SiteSample #"+s+" on frog with ID "+frog.getID());
				Element sampleElement = (Element) sList.item(s);
				SiteSample sample = new SiteSample();
				
				//Date
				IdentiFrog.LOGGER.writeMessage("Loading -date- for SiteSample #"+s+" on frog with ID "+frog.getID());
				NamedNodeMap dateAttributes = sampleElement.getElementsByTagName("date").item(0).getAttributes();
				sample.setDateCapture(dateAttributes.getNamedItem("capture").getTextContent());
				sample.setDateEntry(dateAttributes.getNamedItem("entry").getTextContent());
				
				//Biometrics
				IdentiFrog.LOGGER.writeMessage("Loading -biometrics- for SiteSample #"+s+" on frog with ID "+frog.getID());
				NamedNodeMap bm = sampleElement.getElementsByTagName("biometrics").item(0).getAttributes();
				if (bm.getNamedItem("mass") != null) {
					sample.setMass(bm.getNamedItem("mass").getNodeValue());
				}
				if (bm.getNamedItem("length") != null) {
					sample.setLength(bm.getNamedItem("length").getNodeValue());
				}
				
				//Comments
				IdentiFrog.LOGGER.writeMessage("Loading -comments- for SiteSample #"+s+" on frog with ID "+frog.getID());
				sample.setComments(sampleElement.getElementsByTagName("comments").item(0).getTextContent());
				
				//Discriminator
				IdentiFrog.LOGGER.writeMessage("Loading -discriminator- for SiteSample #"+s+" on frog with ID "+frog.getID());
				sample.setDiscriminator(sampleElement.getElementsByTagName("discriminator").item(0).getTextContent());
				
				//Images
				String temp;
				for (int j = 0; j < frogElement.getElementsByTagName("image").getLength(); j++) {
					temp = frogElement.getElementsByTagName("image").item(j).getAttributes().getNamedItem("type").getNodeValue();
					if (temp.equals("image")) {
						frog.setPathImage(frogElement.getElementsByTagName("image").item(j).getTextContent());
					}
				}
				
				//Location
				IdentiFrog.LOGGER.writeMessage("Loading -location- for SiteSample #"+s+" on frog with ID "+frog.getID());
				Location location = new Location();
				Element locationElement = (Element) frogElement.getElementsByTagName("location").item(0);
				//Location - name
				location.setName(locationElement.getElementsByTagName("name").item(0).getTextContent());
				//Location - description
				location.setDescription(locationElement.getElementsByTagName("description").item(0).getNodeValue());
				//Location - coordinate
				NodeList coordinate = locationElement.getElementsByTagName("coordinate");
				if (coordinate.getLength() < 1) {
					//no coordinate was set
					IdentiFrog.LOGGER.writeMessage("No coordinate data in -location- for SiteSample #"+s+" on frog with ID "+frog.getID());
					location.setCoordinateType(null);
				} else {
					Element coordinateElement = (Element) coordinate.item(0);
					Node ct = coordinateElement.getAttributes().getNamedItem("type");
					if (ct != null && ct.hasAttributes()) {
						location.setCoordinateType(coordinate.item(0).getAttributes().getNamedItem("type").getTextContent());
						//Element coordinateElement = ((Element) nn.item(0)).getElementsByTagName("coordinate");
						if (location.getCoordinateType().equals("LatLong")) {
							IdentiFrog.LOGGER.writeMessage("Loading LatLong -location- for SiteSample #"+s+" on frog with ID "+frog.getID());
							location.setLongitude(coordinateElement.getElementsByTagName("longitude").item(0).getTextContent());
							location.setLatitude(coordinateElement.getElementsByTagName("latitude").item(0).getTextContent());
							location.setDatum(coordinateElement.getElementsByTagName("datum").item(0).getTextContent());
						} else if (location.getCoordinateType().equals("UTM")) {
							IdentiFrog.LOGGER.writeMessage("Loading UTM -location- for SiteSample #"+s+" on frog with ID "+frog.getID());
							location.setLongitude(coordinateElement.getElementsByTagName("easting").item(0).getTextContent());
							location.setLatitude(coordinateElement.getElementsByTagName("northing").item(0).getTextContent());
							location.setDatum(coordinateElement.getElementsByTagName("datum").item(0).getTextContent());
							location.setZone(coordinateElement.getElementsByTagName("zone").item(0).getTextContent());
						} else {
							IdentiFrog.LOGGER.writeError("Error: Unknown coordinate type for -location- in SiteSample #"+s+" on frog with ID "+frog.getID()+", skipping coordinate data.");
						}
					}
				}
				sample.setLocation(location);
				
				//Personel
				IdentiFrog.LOGGER.writeMessage("Loading -personel(s)- for SiteSample #"+s+" on frog with ID "+frog.getID());
				Personel observer = new Personel("observer");
				Personel recorder = new Personel("recorder");
				NodeList personelList = sampleElement.getElementsByTagName("personel");
				for (int j = 0; j < personelList.getLength(); j++) {
					Element personelElement = (Element) personelList.item(j);
					NamedNodeMap personelAttributes = personelList.item(j).getAttributes();
					if (personelAttributes.getNamedItem("type").getTextContent().equals("observer")) {
						observer.setFirstName(personelElement.getElementsByTagName("firstname").item(0).getTextContent());
						observer.setLastName(personelElement.getElementsByTagName("lastname").item(0).getTextContent());
					} 
					else if (personelList.item(j).getAttributes().getNamedItem("type").getTextContent().equals("recorder")) {
						recorder.setFirstName(personelElement.getElementsByTagName("firstname").item(0).getTextContent());
						recorder.setLastName(personelElement.getElementsByTagName("lastname").item(0).getTextContent());
					}
				}
				sample.setObserver(observer);
				sample.setRecorder(recorder);
				
				
				//SurveyID
				IdentiFrog.LOGGER.writeMessage("Loading -surveyid- for SiteSample #"+s+" on frog with ID "+frog.getID());
				sample.setSurveyID(sampleElement.getElementsByTagName("surveyid").item(0).getTextContent());
			}
			
			frogs.add(frog);
		}
		return frogs;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}