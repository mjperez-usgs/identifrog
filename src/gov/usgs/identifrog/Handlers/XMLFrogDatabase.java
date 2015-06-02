package gov.usgs.identifrog.Handlers;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.Location;
import gov.usgs.identifrog.DataObjects.Personel;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.DataObjects.SiteSample;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

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

/**
 * Stores the XML Frog DB in memory and can commit to disk. This class is thread
 * safe using a singleton pattern.
 * 
 * @author mjperez
 *
 */
public class XMLFrogDatabase {
	private static boolean LOADED = false;
	private static File dbfile;
	private static ArrayList<Frog> frogs = new ArrayList<Frog>();

	/*public XMLFrogDatabase() {
		XMLFrogDatabase.frogs = new ArrayList<Frog>();
	}

	public XMLFrogDatabase(File file) {
		XMLFrogDatabase.file = file;
		XMLFrogDatabase.frogs = new ArrayList<Frog>();
	}

	public XMLFrogDatabase(String filename) {
		XMLFrogDatabase.file = new File(filename);
		XMLFrogDatabase.frogs = new ArrayList<Frog>();
	}

	public XMLFrogDatabase(File file, ArrayList<Frog> frogs) {
		XMLFrogDatabase.file = file;
		XMLFrogDatabase.frogs = frogs;
	}*/
	
	public XMLFrogDatabase() throws Exception {
		throw new Exception("This class is not meant to be initialized, access it statically");
	}

	/**
	 * Creates a new, blank Frog DB.
	 * 
	 * @return true if successful, false otherwise.
	 */
	public static boolean createXMLFile() {
		IdentiFrog.LOGGER.writeMessage("Creating XML DB for the first time");
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
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
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		transformerFactory.setAttribute("indent-number", 2);
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			// transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
			// "1");
		} catch (TransformerConfigurationException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		}
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(dbfile);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		}
		return true;
	}

	/**
	 * Writes this Frog DB to disk.
	 * 
	 * @return true if successful, false otherwise.
	 */
	public static boolean WriteXMLFile() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
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
			root.appendChild(frog.createDBElement(doc));
		}

		// WRITE XML FILE
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		}
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(dbfile);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		}
		return true;
	}

	/**
	 * Reads the database XML file and loads frog data into this Frog DB object.
	 * This method is built for DB 2.0.
	 * Frogs can then be accessed via the getFrogs() method.
	 * 
	 */
	public static void loadXMLFile() {
		IdentiFrog.LOGGER.writeMessage("Loading XML DB: "+dbfile.toString() +" Size: "+dbfile.length()+" bytes");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			IdentiFrog.LOGGER.writeException(e);
		}
		try {
			doc = docBuilder.parse(dbfile);
		} catch (SAXException e) {
			IdentiFrog.LOGGER.writeException(e);
		} catch (IOException e) {
			IdentiFrog.LOGGER.writeException(e);
		}
		doc.getDocumentElement().normalize();
		Frog frog = null;
		NodeList nList = doc.getElementsByTagName("frog");

		/*
		 * for (int i = 0; i < nList.getLength(); i++) {
		 * 
		 * DB 1.0 code frog = new Frog();
		 * frog.setID(nList.item(i).getAttributes(
		 * ).getNamedItem("id").getNodeValue());
		 * frog.setFormerID(nList.item(i).getAttributes
		 * ().getNamedItem("formerid").getNodeValue());
		 * 
		 * Element eElem = (Element) nList.item(i);
		 * frog.setSpecies(eElem.getElementsByTagName
		 * ("species").item(0).getTextContent());
		 * frog.setGender(eElem.getElementsByTagName
		 * ("gender").item(0).getTextContent());
		 * frog.setDateCapture(eElem.getElementsByTagName
		 * ("date").item(0).getAttributes
		 * ().getNamedItem("capture").getNodeValue());
		 * frog.setDateEntry(eElem.getElementsByTagName
		 * ("date").item(0).getAttributes
		 * ().getNamedItem("entry").getNodeValue()); NodeList bioNode =
		 * eElem.getElementsByTagName("biometrics"); if
		 * (bioNode.item(0).hasAttributes()) { NamedNodeMap bm =
		 * bioNode.item(0).getAttributes(); if (bm.getNamedItem("mass") != null)
		 * { frog.setMass(bm.getNamedItem("mass").getNodeValue()); } if
		 * (bm.getNamedItem("length") != null) {
		 * frog.setLength(bm.getNamedItem("length").getNodeValue()); } }
		 * frog.setDiscriminator
		 * (eElem.getElementsByTagName("discriminator").item
		 * (0).getTextContent()); // String comments = //
		 * eElem.getElementsByTagName("comments").item(0).getTextContent();
		 * NodeList comments =
		 * eElem.getElementsByTagName("comments").item(0).getChildNodes(); if
		 * (comments.getLength() < 1) { frog.setComments(""); } else {
		 * frog.setComments(comments.item(0).getNodeValue()); }
		 * frog.setSurveyID(
		 * eElem.getElementsByTagName("surveyid").item(0).getTextContent());
		 * 
		 * Personel observer = new Personel("observer"); Personel recorder = new
		 * Personel("recorder"); for (int j = 0; j <
		 * eElem.getElementsByTagName("personel").getLength(); j++) { NodeList
		 * nn = eElem.getElementsByTagName("personel"); if
		 * (nn.item(j).getAttributes
		 * ().getNamedItem("type").getNodeValue().equals("observer")) {
		 * observer.setFirstName(((Element)
		 * nn.item(j)).getElementsByTagName("firstname"
		 * ).item(0).getTextContent()); observer.setLastName(((Element)
		 * nn.item(j
		 * )).getElementsByTagName("lastname").item(0).getTextContent()); } if
		 * (nn
		 * .item(j).getAttributes().getNamedItem("type").getNodeValue().equals
		 * ("recorder")) { recorder.setFirstName(((Element)
		 * nn.item(j)).getElementsByTagName
		 * ("firstname").item(0).getTextContent());
		 * recorder.setLastName(((Element)
		 * nn.item(j)).getElementsByTagName("lastname"
		 * ).item(0).getTextContent()); } } frog.setObserver(observer);
		 * frog.setRecorder(recorder);
		 * 
		 * Location location = new Location(); NodeList nn =
		 * eElem.getElementsByTagName("location"); location.setName(((Element)
		 * nn.item(0)).getElementsByTagName("name").item(0).getTextContent());
		 * NodeList description = ((Element)
		 * nn.item(0)).getElementsByTagName("description"); if
		 * (description.getLength() < 1) { location.setDescription(""); } else {
		 * location.setDescription(description.item(0).getNodeValue()); }
		 * 
		 * // location.setCoordinateType(((Element) //
		 * nn.item(0)).getElementsByTagName
		 * ("coordinate").item(0).getAttributes()
		 * .getNamedItem("type").getNodeValue()); NodeList coordinate =
		 * ((Element) nn.item(0)).getElementsByTagName("coordinate"); if
		 * (coordinate.getLength() < 1) { location.setCoordinateType(""); } else
		 * { Node ct = coordinate.item(0).getAttributes().getNamedItem("type");
		 * if (ct != null && ct.hasAttributes()) {
		 * location.setCoordinateType(coordinate
		 * .item(0).getAttributes().getNamedItem("type").getNodeValue()); nn =
		 * ((Element) nn.item(0)).getElementsByTagName("coordinate"); if
		 * (!location.getCoordinateType().equals("UTM")) {
		 * location.setLongitude(((Element)
		 * nn.item(0)).getElementsByTagName("longitude"
		 * ).item(0).getTextContent()); location.setLatitude(((Element)
		 * nn.item(0
		 * )).getElementsByTagName("latitude").item(0).getTextContent());
		 * location.setDatum(((Element)
		 * nn.item(0)).getElementsByTagName("datum").item(0).getTextContent());
		 * } if (location.getCoordinateType().equals("UTM")) {
		 * location.setLongitude(((Element)
		 * nn.item(0)).getElementsByTagName("easting"
		 * ).item(0).getTextContent()); location.setLatitude(((Element)
		 * nn.item(0
		 * )).getElementsByTagName("northing").item(0).getTextContent());
		 * location.setDatum(((Element)
		 * nn.item(0)).getElementsByTagName("datum").item(0).getTextContent());
		 * location.setZone(((Element)
		 * nn.item(0)).getElementsByTagName("zone").item(0).getTextContent()); }
		 * } }
		 * 
		 * frog.setLocation(location);
		 * 
		 * String temp; for (int j = 0; j <
		 * eElem.getElementsByTagName("image").getLength(); j++) { temp =
		 * eElem.getElementsByTagName
		 * ("image").item(j).getAttributes().getNamedItem
		 * ("type").getNodeValue(); if (temp.equals("image")) {
		 * frog.setPathImage
		 * (eElem.getElementsByTagName("image").item(j).getTextContent()); } }
		 * 
		 * frogs.add(frog); }
		 */
		for (int i = 0; i < nList.getLength(); i++) {
			// DB2.0 code
			frog = new Frog();
			Element frogElement = (Element) nList.item(i);
			NamedNodeMap frogAttributes = nList.item(i).getAttributes();

			// Load frog object data
			frog.setID(Integer.parseInt(frogAttributes.getNamedItem("id")
					.getTextContent()));
			frog.setGender(frogElement.getElementsByTagName("gender").item(0)
					.getTextContent());
			frog.setSpecies(frogElement.getElementsByTagName("species").item(0)
					.getTextContent());

			// load sitesamples
			Element siteSamples = (Element) frogElement.getElementsByTagName(
					"sitesamples").item(0);
			NodeList sList = siteSamples.getElementsByTagName("sitesample");
			for (int s = 0; i < sList.getLength(); s++) {
				// load collection data
				IdentiFrog.LOGGER.writeMessage("Parsing XML for SiteSample #"
						+ s + " on frog with ID " + frog.getID());
				Element sampleElement = (Element) sList.item(s);
				SiteSample sample = new SiteSample();
				sample.setFrogID(frog.getID());
				// Date
				IdentiFrog.LOGGER
						.writeMessage("Loading -date- for SiteSample #" + s
								+ " on frog with ID " + frog.getID());
				NamedNodeMap dateAttributes = sampleElement
						.getElementsByTagName("date").item(0).getAttributes();
				sample.setDateCapture(dateAttributes.getNamedItem("capture")
						.getTextContent());
				sample.setDateEntry(dateAttributes.getNamedItem("entry")
						.getTextContent());

				// Biometrics
				IdentiFrog.LOGGER
						.writeMessage("Loading -biometrics- for SiteSample #"
								+ s + " on frog with ID " + frog.getID());
				NamedNodeMap bm = sampleElement
						.getElementsByTagName("biometrics").item(0)
						.getAttributes();
				if (bm.getNamedItem("mass") != null) {
					sample.setMass(bm.getNamedItem("mass").getNodeValue());
				}
				if (bm.getNamedItem("length") != null) {
					sample.setLength(bm.getNamedItem("length").getNodeValue());
				}

				// Comments
				IdentiFrog.LOGGER
						.writeMessage("Loading -comments- for SiteSample #" + s
								+ " on frog with ID " + frog.getID());
				sample.setComments(sampleElement
						.getElementsByTagName("comments").item(0)
						.getTextContent());

				// Discriminator
				IdentiFrog.LOGGER
						.writeMessage("Loading -discriminator- for SiteSample #"
								+ s + " on frog with ID " + frog.getID());
				sample.setDiscriminator(sampleElement
						.getElementsByTagName("discriminator").item(0)
						.getTextContent());

				// Images
				IdentiFrog.LOGGER
						.writeMessage("Loading -images- for SiteSample #" + s
								+ " on frog with ID " + frog.getID());
				Element imagesElement = (Element) sampleElement
						.getElementsByTagName("images").item(0);
				NodeList imagesList = imagesElement
						.getElementsByTagName("image");
				ArrayList<SiteImage> siteImages = new ArrayList<SiteImage>();
				for (int n = 0; n < imagesList.getLength(); n++) {
					IdentiFrog.LOGGER.writeMessage("Loading -image #" + n
							+ "- for SiteSample #" + s + " on frog with ID "
							+ frog.getID());
					Element imageElement = (Element) imagesList.item(n);
					SiteImage image = new SiteImage();
					image.setImageFileName(imageElement
							.getElementsByTagName("filename").item(0)
							.getTextContent());
					image.setSignatureGenerated(imageElement
							.getElementsByTagName("signature").item(0)
							.getTextContent().equals("true"));
					image.setSourceImageHash(imageElement
							.getElementsByTagName("srchash").item(0)
							.getTextContent());
					siteImages.add(image);
				}
				sample.setSiteImages(siteImages);

				// Location
				IdentiFrog.LOGGER
						.writeMessage("Loading -location- for SiteSample #" + s
								+ " on frog with ID " + frog.getID());
				Location location = new Location();
				Element locationElement = (Element) frogElement
						.getElementsByTagName("location").item(0);
				// Location - name
				location.setName(locationElement.getElementsByTagName("name")
						.item(0).getTextContent());
				// Location - description
				location.setDescription(locationElement
						.getElementsByTagName("description").item(0)
						.getNodeValue());
				// Location - coordinate
				NodeList coordinate = locationElement
						.getElementsByTagName("coordinate");
				if (coordinate.getLength() < 1) {
					// no coordinate was set
					IdentiFrog.LOGGER
							.writeMessage("No coordinate data in -location- for SiteSample #"
									+ s + " on frog with ID " + frog.getID());
					location.setCoordinateType(null);
				} else {
					Element coordinateElement = (Element) coordinate.item(0);
					Node ct = coordinateElement.getAttributes().getNamedItem(
							"type");
					if (ct != null && ct.hasAttributes()) {
						location.setCoordinateType(coordinate.item(0)
								.getAttributes().getNamedItem("type")
								.getTextContent());
						// Element coordinateElement = ((Element)
						// nn.item(0)).getElementsByTagName("coordinate");
						if (location.getCoordinateType().equals("LatLong")) {
							IdentiFrog.LOGGER
									.writeMessage("Loading LatLong -location- for SiteSample #"
											+ s
											+ " on frog with ID "
											+ frog.getID());
							location.setLongitude(coordinateElement
									.getElementsByTagName("longitude").item(0)
									.getTextContent());
							location.setLatitude(coordinateElement
									.getElementsByTagName("latitude").item(0)
									.getTextContent());
							location.setDatum(coordinateElement
									.getElementsByTagName("datum").item(0)
									.getTextContent());
						} else if (location.getCoordinateType().equals("UTM")) {
							IdentiFrog.LOGGER
									.writeMessage("Loading UTM -location- for SiteSample #"
											+ s
											+ " on frog with ID "
											+ frog.getID());
							location.setLongitude(coordinateElement
									.getElementsByTagName("easting").item(0)
									.getTextContent());
							location.setLatitude(coordinateElement
									.getElementsByTagName("northing").item(0)
									.getTextContent());
							location.setDatum(coordinateElement
									.getElementsByTagName("datum").item(0)
									.getTextContent());
							location.setZone(coordinateElement
									.getElementsByTagName("zone").item(0)
									.getTextContent());
						} else {
							IdentiFrog.LOGGER
									.writeError("Error: Unknown coordinate type for -location- in SiteSample #"
											+ s
											+ " on frog with ID "
											+ frog.getID()
											+ ", skipping coordinate data.");
						}
					}
				}
				sample.setLocation(location);

				// Personel
				IdentiFrog.LOGGER
						.writeMessage("Loading -personel(s)- for SiteSample #"
								+ s + " on frog with ID " + frog.getID());
				Personel observer = new Personel("observer");
				Personel recorder = new Personel("recorder");
				NodeList personelList = sampleElement
						.getElementsByTagName("personel");
				for (int j = 0; j < personelList.getLength(); j++) {
					Element personelElement = (Element) personelList.item(j);
					NamedNodeMap personelAttributes = personelList.item(j)
							.getAttributes();
					if (personelAttributes.getNamedItem("type")
							.getTextContent().equals("observer")) {
						observer.setFirstName(personelElement
								.getElementsByTagName("firstname").item(0)
								.getTextContent());
						observer.setLastName(personelElement
								.getElementsByTagName("lastname").item(0)
								.getTextContent());
					} else if (personelList.item(j).getAttributes()
							.getNamedItem("type").getTextContent()
							.equals("recorder")) {
						recorder.setFirstName(personelElement
								.getElementsByTagName("firstname").item(0)
								.getTextContent());
						recorder.setLastName(personelElement
								.getElementsByTagName("lastname").item(0)
								.getTextContent());
					}
				}
				sample.setObserver(observer);
				sample.setRecorder(recorder);

				// SurveyID
				IdentiFrog.LOGGER
						.writeMessage("Loading -surveyid- for SiteSample #" + s
								+ " on frog with ID " + frog.getID());
				sample.setSurveyID(sampleElement
						.getElementsByTagName("surveyid").item(0)
						.getTextContent());
			}

			frogs.add(frog);
		}
		IdentiFrog.LOGGER.writeMessage("Loaded XML DB, loaded data for "+frogs.size()+" frogs.");
		XMLFrogDatabase.LOADED = true;
	}

	public static File getFile() {
		return dbfile;
	}

	/**
	 * Sets the datafile this database is using for loading and storing values.
	 * @param file datafile.xml (or backing database file) 
	 */
	public static void setFile(File file) {
		if (!file.getAbsolutePath().toLowerCase().endsWith(IdentiFrog.DB_FILENAME.toLowerCase())) {
			IdentiFrog.LOGGER.writeError("SETFILE IN XML DB DID NOT USE A DATAFILE.XML FILE");
		}
		
		XMLFrogDatabase.dbfile = file;
		IdentiFrog.LOGGER.writeMessage("XML Database file set to "+file.toString());

		PROJECT_FOLDER = file.getAbsolutePath();
		if (PROJECT_FOLDER.endsWith(IdentiFrog.DB_FILENAME)) {
			PROJECT_FOLDER = PROJECT_FOLDER.substring(0,PROJECT_FOLDER.length()-IdentiFrog.DB_FILENAME.length()-1);
		}
	}

	/**
	 * Returns a unique, alphabetized (by first name) list of observers.
	 * 
	 * @return list of unique observers. Empty if DB is not loaded.
	 */
	public static ArrayList<String> getObservers() {
		HashSet<String> observers = new HashSet<String>();
		if (XMLFrogDatabase.LOADED) {
			for (Frog frog : frogs) {
				for (SiteSample sample : frog.getSiteSamples()) {
					observers.add(sample.getObserver2());
				}
			}
		}
		ArrayList<String> uniqueObservers = new ArrayList<String>(observers);
		Collections.sort(uniqueObservers);
		return uniqueObservers;
	}

	/**
	 * Returns a unique, alphabetized (by first name) list of recorders.
	 * 
	 * @return list of unique recorders. Empty if DB is not loaded.
	 */
	public static ArrayList<String> getRecorders() {
		HashSet<String> recorders = new HashSet<String>();
		if (XMLFrogDatabase.LOADED) {
			for (Frog frog : frogs) {
				for (SiteSample sample : frog.getSiteSamples()) {
					recorders.add(sample.getRecorder2());
				}
			}
		}
		ArrayList<String> uniqueRecorders = new ArrayList<String>(recorders);
		Collections.sort(uniqueRecorders);
		return uniqueRecorders;
	}

	/**
	 * Gets the list of frogs in this database.
	 * 
	 * @return Frogs in this database, or an empty arraylist if the database is
	 *         not yet loaded. Prints an error if the DB was not loaded.
	 */
	public static ArrayList<Frog> getFrogs() {
		if (!LOADED) {
			IdentiFrog.LOGGER.writeError("Accessing getFrogs() before the DB was loaded!");
			return new ArrayList<Frog>();
		}
		return frogs;
	}

	/**
	 * Finds a frog by its ID.
	 * 
	 * @param ID
	 *            ID of the frog in the DB.
	 * @return Frog object containing info about frog with the ID.
	 */
	public static Frog searchFrogByID(int ID) {
		for (Frog frog : frogs) {
			if (frog.getID() == ID) {
				return frog;
			}
		}
		return null;
	}

	public static void removeFrog(int ID) {
		frogs.remove(searchFrogByID(ID));
		/*
		 * int index = searchFrogByID(ID); if (index == -1) { return; }
		 * frogs.remove(index);
		 */
	}

	/*
	 * public void replaceFrog(String ID, Frog frog) { int index = -1; for (int
	 * i = 0; i < frogs.size(); i++) { if
	 * (frogs.get(i).getFormerID().equals(ID)) { index = i; } } if (index == -1)
	 * { frogs.set(index, frog); } }
	 */

	/**
	 * Gets the next open frog ID after the current highest frog ID number
	 * @return next free ID for a frog to use
	 */
	public static int getNextAvailableID() {
		if (!LOADED) {
			IdentiFrog.LOGGER.writeError("Attempting to get next available ID before DB has been loaded!");
		}
		int nextAvailable = 0;
		for (Frog frog : frogs) {
			if (frog.getID() > nextAvailable) {
				nextAvailable = frog.getID();
			}
		}
		return nextAvailable + 1;
	}

	public static void createCopy(File newSitePath) throws IOException {
		copyFolder(new File(getMainFolder()), newSitePath);
		setFile(new File(newSitePath+File.separator+IdentiFrog.DB_FILENAME));
	}
	
	/**
	 * Copies a project folder to another location
	 * @param src source folder
	 * @param dest destionation folder
	 * @throws IOException if an error occurs
	 */
	private static void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
				// IdentiFrog.LOGGER.writeMessage("Directory copied from " + src
				// + "  to " +
				// dest);
			}
			// list all the directory contents
			String files[] = src.list();
			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}
		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[2048];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
			// IdentiFrog.LOGGER.writeMessage("File copied from " + src + " to "
			// + dest);
		}
	}
	
	/**
	 * Scans the DB for an existing source hash based on the image to check if an image is being entered twice.
	 * @param image Source, full resolution image
	 * @return true if exists in DB already, false otherwise
	 */
	public static boolean checkForExistingHashInDB(File image) {
		//TODO
		return false;
	}
	
	
	
	//=================Migrated from FolderHandler
	  	private static String PROJECT_FOLDER;
	  	private static String SITE_NAME;
		private static final String DEFAULT_PROJECT_FOLDER = System.getProperty("user.home");
		private static final String DEFAULT_PROJECT_NAME = "IdentiFrog Data";
		//private static final String filename = "datafile.xml";
		private static final String IMAGES = "Images", SIGNATURES = "Signatures", BINARY = "Binary", DORSAL = "Dorsal";
		public static final String THUMB = "Thumbnail";
		private static final String PENDING="Pending";
		private static final String[] PROJECT_FOLDERS = { IMAGES, SIGNATURES, BINARY, DORSAL, THUMB, PENDING };

		/**
		 * Creates a new "Folder Handler", an object that handles folder creation for storing data related to a single site.
		 * This constructor by default puts the site in the %USERPROFILE%/Identifrog Data folder.
		 */
		/*public FolderHandler() {
		  base = drive + File.separator + register;
		}*/

		/**
		 * Creates a new "Folder Handler", an object that handles folder creation for storing data related to a single site.
		 * This constructor uses the base string (site location data) as the data directory. 
		 */
		/*public FolderHandler(String base) {
			if (base.endsWith(IdentiFrog.DB_FILENAME)) {
				this.base = base.substring(0,base.length()-IdentiFrog.DB_FILENAME.length()-1);
			} else {
				this.base = base;
			}
		}*/
		
		public static boolean siteFoldersExist() {
			return new File(XMLFrogDatabase.PROJECT_FOLDER).exists();
		}

		/**
		 * Creates folders required for a site. Uses the 'base' variable as the place to put the data.
		 * @return true if successful, false on any error
		 * @author mjperez
		 */
		public static boolean createFolders() {
			boolean exists = false;
			for (String folderName : PROJECT_FOLDERS) {
				File dataSubfolder = new File(PROJECT_FOLDER + File.separator + folderName);
				IdentiFrog.LOGGER.writeMessage("Creating data folder: "+dataSubfolder.toString());
				exists = dataSubfolder.mkdirs();
				if (!exists) {
					return false;
				}
			}
			return true;
		}

		public String getFileName() {
			return IdentiFrog.DB_FILENAME;
		}

		/**
		 * Returns the datafile.xml path by adding the folder, the path separator, and the database filename.
		 * @return Project Folder + (separator) + DB_NAME e.g. C:\project\datafile.xml
		 */
		public static String getFileNamePath() {
			return PROJECT_FOLDER + File.separator + IdentiFrog.DB_FILENAME;
		}

		// { "Images", "Signatures", "Binary", "Dorsal", "Thumbnail" };

		public static String getMainFolder() {
			return PROJECT_FOLDER + File.separator;
		}

		public static String getImagesFolder() {
			return getMainFolder() + IMAGES + File.separator;
		}

		public static String getSignaturesFolder() {
			return getMainFolder() + SIGNATURES + File.separator;
		}

		public static String getBinaryFolder() {
			return getMainFolder() + BINARY + File.separator;
		}

		public static String getDorsalFolder() {
			return getMainFolder() + DORSAL + File.separator;
		}

		public static String getThumbnailFolder() {
			return getMainFolder() + THUMB + File.separator;
		}
		
		/**
		 * Gets folder path that contains full resolution images pending a signature
		 * @return
		 */
		public static String getPendingFolder() {
			return getMainFolder() + PENDING + File.separator;
		}

	  public String getSiteName() {
	    return XMLFrogDatabase.SITE_NAME;
	  }

	  public static void setSiteName(String siteName) {
	    XMLFrogDatabase.SITE_NAME = siteName;
	  }
}