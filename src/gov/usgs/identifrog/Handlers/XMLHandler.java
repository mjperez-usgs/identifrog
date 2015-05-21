package gov.usgs.identifrog.Handlers;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.Location;
import gov.usgs.identifrog.DataObjects.Personel;

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

		for (int i = 0; i < nList.getLength(); i++) {
			frog = new Frog();
			frog.setID(nList.item(i).getAttributes().getNamedItem("id").getNodeValue());
			frog.setFormerID(nList.item(i).getAttributes().getNamedItem("formerid").getNodeValue());

			Element eElem = (Element) nList.item(i);
			frog.setSpecies(eElem.getElementsByTagName("species").item(0).getChildNodes().item(0).getNodeValue());
			frog.setGender(eElem.getElementsByTagName("gender").item(0).getChildNodes().item(0).getNodeValue());
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
			frog.setDiscriminator(eElem.getElementsByTagName("discriminator").item(0).getChildNodes().item(0).getNodeValue());
			// String comments =
			// eElem.getElementsByTagName("comments").item(0).getChildNodes().item(0).getNodeValue();
			NodeList comments = eElem.getElementsByTagName("comments").item(0).getChildNodes();
			if (comments.getLength() < 1) {
				frog.setComments("");
			} else {
				frog.setComments(comments.item(0).getNodeValue());
			}
			frog.setSurveyID(eElem.getElementsByTagName("surveyid").item(0).getChildNodes().item(0).getNodeValue());

			Personel observer = new Personel("observer");
			Personel recorder = new Personel("recorder");
			for (int j = 0; j < eElem.getElementsByTagName("personel").getLength(); j++) {
				NodeList nn = eElem.getElementsByTagName("personel");
				if (nn.item(j).getAttributes().getNamedItem("type").getNodeValue().equals("observer")) {
					observer.setFirstName(((Element) nn.item(j)).getElementsByTagName("firstname").item(0).getChildNodes().item(0).getNodeValue());
					observer.setLastName(((Element) nn.item(j)).getElementsByTagName("lastname").item(0).getChildNodes().item(0).getNodeValue());
				}
				if (nn.item(j).getAttributes().getNamedItem("type").getNodeValue().equals("recorder")) {
					recorder.setFirstName(((Element) nn.item(j)).getElementsByTagName("firstname").item(0).getChildNodes().item(0).getNodeValue());
					recorder.setLastName(((Element) nn.item(j)).getElementsByTagName("lastname").item(0).getChildNodes().item(0).getNodeValue());
				}
			}
			frog.setObserver(observer);
			frog.setRecorder(recorder);

			Location location = new Location();
			NodeList nn = eElem.getElementsByTagName("location");
			location.setName(((Element) nn.item(0)).getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue());
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
						location.setLongitude(((Element) nn.item(0)).getElementsByTagName("longitude").item(0).getChildNodes().item(0).getNodeValue());
						location.setLatitude(((Element) nn.item(0)).getElementsByTagName("latitude").item(0).getChildNodes().item(0).getNodeValue());
						location.setDatum(((Element) nn.item(0)).getElementsByTagName("datum").item(0).getChildNodes().item(0).getNodeValue());
					}
					if (location.getCoordinateType().equals("UTM")) {
						location.setLongitude(((Element) nn.item(0)).getElementsByTagName("easting").item(0).getChildNodes().item(0).getNodeValue());
						location.setLatitude(((Element) nn.item(0)).getElementsByTagName("northing").item(0).getChildNodes().item(0).getNodeValue());
						location.setDatum(((Element) nn.item(0)).getElementsByTagName("datum").item(0).getChildNodes().item(0).getNodeValue());
						location.setZone(((Element) nn.item(0)).getElementsByTagName("zone").item(0).getChildNodes().item(0).getNodeValue());
					}
				}
			}

			frog.setLocation(location);

			String temp;
			for (int j = 0; j < eElem.getElementsByTagName("image").getLength(); j++) {
				temp = eElem.getElementsByTagName("image").item(j).getAttributes().getNamedItem("type").getNodeValue();
				if (temp.equals("image")) {
					frog.setPathImage(eElem.getElementsByTagName("image").item(j).getChildNodes().item(0).getNodeValue());
				}
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