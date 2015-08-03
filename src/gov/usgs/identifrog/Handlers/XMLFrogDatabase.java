package gov.usgs.identifrog.Handlers;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Discriminator;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.Location;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.DataObjects.SiteSample;
import gov.usgs.identifrog.DataObjects.User;
import gov.usgs.identifrog.Frames.MainFrame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
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

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
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
	private static boolean USERS_LOADED = false;
	private static boolean FULLY_LOADED = false;
	private static File dbfile;
	private static ArrayList<Frog> frogs = new ArrayList<Frog>();
	private static ArrayList<User> recorders, observers;
	private static ArrayList<Discriminator> discriminators;
	private static int highestSessionDiscriminatorID = 0; //used when adding items to the discriminator list but not yet commited
	private static SwingWorker<Boolean, Integer> thread;

	/*
	 * public XMLFrogDatabase() { XMLFrogDatabase.frogs = new ArrayList<Frog>();
	 * }
	 * 
	 * public XMLFrogDatabase(File file) { XMLFrogDatabase.file = file;
	 * XMLFrogDatabase.frogs = new ArrayList<Frog>(); }
	 * 
	 * public XMLFrogDatabase(String filename) { XMLFrogDatabase.file = new
	 * File(filename); XMLFrogDatabase.frogs = new ArrayList<Frog>(); }
	 * 
	 * public XMLFrogDatabase(File file, ArrayList<Frog> frogs) {
	 * XMLFrogDatabase.file = file; XMLFrogDatabase.frogs = frogs; }
	 */

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
		Element frogsElem = doc.createElement("frogs");
		Element usersElem = doc.createElement("users");
		Element recorderElement = doc.createElement("recorders");
		Element observersElement = doc.createElement("observers");
		Element discrimintorsElement = doc.createElement("discriminators");
		root.appendChild(frogsElem);
		usersElem.appendChild(recorderElement);
		usersElem.appendChild(observersElement);
		root.appendChild(usersElem);
		root.appendChild(discrimintorsElement);
		doc.appendChild(root);

		// WRITE XML FILE
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute("indent-number", 2);
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		} catch (TransformerConfigurationException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		}

		StringWriter buffer = new StringWriter();
		DOMSource source = new DOMSource(doc);
		try {
			transformer.transform(source, new StreamResult(buffer));
			dbfile.delete(); //remove old
			FileUtils.writeStringToFile(dbfile, buffer.toString());
			IdentiFrog.LOGGER.writeMessage("Database commited to disk.");
		} catch (TransformerException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		} catch (IOException e) {
			IdentiFrog.LOGGER.writeException(e);
			return false;
		}

		return true;
	}

	/**
	 * Writes this Frog DB to disk. Additionally updates the status of
	 * Discriminators in-use flags (since we are updating DB)
	 * 
	 */
	public static void writeXMLFile() {
		thread = new XMLFrogDatabase.CommitWorker(IdentiFrog.activeMainFrame);
		thread.execute();
	}

	/**
	 * Reads the database XML file and loads frog data into this Frog DB object.
	 * This method is built for DB 2.0. Frogs can then be accessed via the
	 * getFrogs() method.
	 * 
	 */
	public static void loadXMLFile() {
		IdentiFrog.LOGGER.writeMessage("Loading XML DB: " + dbfile.toString() + " Size: " + dbfile.length() + " bytes");
		USERS_LOADED = false;
		frogs = new ArrayList<Frog>();
		recorders = new ArrayList<User>();
		observers = new ArrayList<User>();
		discriminators = new ArrayList<Discriminator>();
		try {
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
			//LOAD USERS FIRST
			IdentiFrog.LOGGER.writeMessage("Parsing XML for Users...");

			Element usersElement = (Element) doc.getElementsByTagName("users").item(0);
			NodeList rList = ((Element) usersElement.getElementsByTagName("recorders").item(0)).getElementsByTagName("user");
			NodeList oList = ((Element) usersElement.getElementsByTagName("observers").item(0)).getElementsByTagName("user"); //XML IS FUN! this drills down users > observers > user objects
			for (int i = 0; i < rList.getLength(); i++) {
				Element userElem = (Element) rList.item(i);
				User user = new User();
				user.setID(Integer.parseInt(userElem.getAttribute("id")));
				user.setFirstName(userElem.getElementsByTagName("firstname").item(0).getTextContent());
				user.setLastName(userElem.getElementsByTagName("lastname").item(0).getTextContent());
				recorders.add(user);
				IdentiFrog.LOGGER.writeMessage("Loaded recorder: " + user.getName());
			}

			for (int i = 0; i < oList.getLength(); i++) {
				Element userElem = (Element) oList.item(i);
				User user = new User();
				user.setID(Integer.parseInt(userElem.getAttribute("id")));
				user.setFirstName(userElem.getElementsByTagName("firstname").item(0).getTextContent());
				user.setLastName(userElem.getElementsByTagName("lastname").item(0).getTextContent());
				observers.add(user);
				IdentiFrog.LOGGER.writeMessage("Loaded observer: " + user.getName());

			}

			USERS_LOADED = true;

			//LOAD DISCRIMINATORS
			IdentiFrog.LOGGER.writeMessage("Parsing XML for Discriminators...");

			Element discriminatorsElement = (Element) doc.getElementsByTagName("discriminators").item(0);
			NodeList dList = discriminatorsElement.getElementsByTagName("discriminator");
			for (int i = 0; i < dList.getLength(); i++) {
				Element discriminatorElem = (Element) dList.item(i);
				Discriminator discriminator = new Discriminator();
				discriminator.setID(Integer.parseInt(discriminatorElem.getAttribute("id")));
				discriminator.setText(discriminatorElem.getTextContent());
				discriminators.add(discriminator);
				IdentiFrog.LOGGER.writeMessage("Loaded discriminator: " + discriminator.debugToString());
			}

			//Load frogs (depends on users)
			IdentiFrog.LOGGER.writeMessage("Parsing XML for Frogs...");

			Frog frog = null;
			NodeList nList = doc.getElementsByTagName("frog");
			for (int i = 0; i < nList.getLength(); i++) {
				// DB2.0 code
				frog = new Frog();
				Element frogElement = (Element) nList.item(i);
				NamedNodeMap frogAttributes = nList.item(i).getAttributes();

				// Load frog object data
				IdentiFrog.LOGGER.writeMessage("--Loading frog data--");
				frog.setID(Integer.parseInt(frogAttributes.getNamedItem("id").getTextContent()));
				String importStatus = frogAttributes.getNamedItem("freshimport").getTextContent();
				if (importStatus.equals("true")) {
					IdentiFrog.LOGGER.writeMessage("Short-circuit loading fresh imported frog with Frog ID " + frog.getID());
					frog.setFreshImport(true);
				} else {
					frog.setGender(frogElement.getElementsByTagName("gender").item(0).getTextContent());
					frog.setSpecies(frogElement.getElementsByTagName("species").item(0).getTextContent());

					//load discriminators
					NodeList discList = ((Element) frogElement.getElementsByTagName("localdiscriminators").item(0))
							.getElementsByTagName("discriminator");
					IdentiFrog.LOGGER.writeMessage("Parsing XML for Discriminators associated with Frog ID " + frog.getID());
					for (int d = 0; d < discList.getLength(); d++) {
						Element discElement = (Element) discList.item(d);
						int discID = Integer.parseInt(discElement.getTextContent());
						Discriminator discriminator = getDiscrmininatorByID(discID);
						discriminator.setInUse(true);
						frog.addDiscriminator(discriminator);
					}
					IdentiFrog.LOGGER.writeMessage("Associated " + discList.getLength() + " discriminators with Frog ID " + frog.getID());
				}

				// load sitesamples
				Element siteSamples = (Element) frogElement.getElementsByTagName("sitesamples").item(0);
				NodeList sList = siteSamples.getElementsByTagName("sitesample");
				for (int s = 0; s < sList.getLength(); s++) {
					// load collection data
					IdentiFrog.LOGGER.writeMessage("Parsing XML for SiteSample #" + s + " on frog with ID " + frog.getID());
					Element sampleElement = (Element) sList.item(s);
					SiteSample sample = new SiteSample();
					sample.setFrogID(frog.getID());
					// Date
					IdentiFrog.LOGGER.writeMessage("Loading -date- for SiteSample #" + s + " on frog with ID " + frog.getID());
					NamedNodeMap dateAttributes = sampleElement.getElementsByTagName("date").item(0).getAttributes();
					sample.setDateEntry(dateAttributes.getNamedItem("entry").getTextContent());

					if (importStatus.equals("false")) { //has this item
						sample.setDateCapture(dateAttributes.getNamedItem("capture").getTextContent());
						// Biometrics
						IdentiFrog.LOGGER.writeMessage("Loading -biometrics- for SiteSample #" + s + " on frog with ID " + frog.getID());
						NamedNodeMap bm = sampleElement.getElementsByTagName("biometrics").item(0).getAttributes();
						if (bm.getNamedItem("mass") != null) {
							sample.setMass(bm.getNamedItem("mass").getNodeValue());
						}
						if (bm.getNamedItem("length") != null) {
							sample.setLength(bm.getNamedItem("length").getNodeValue());
						}

						// Comments
						IdentiFrog.LOGGER.writeMessage("Loading -comments- for SiteSample #" + s + " on frog with ID " + frog.getID());
						sample.setComments(sampleElement.getElementsByTagName("comments").item(0).getTextContent());
					}
					// Images
					IdentiFrog.LOGGER.writeMessage("Loading -images- for SiteSample #" + s + " on frog with ID " + frog.getID());
					Element imagesElement = (Element) sampleElement.getElementsByTagName("images").item(0);
					NodeList imagesList = imagesElement.getElementsByTagName("image");
					ArrayList<SiteImage> siteImages = new ArrayList<SiteImage>();
					for (int n = 0; n < imagesList.getLength(); n++) {
						IdentiFrog.LOGGER.writeMessage("Loading -image #" + n + "- for SiteSample #" + s + " on frog with ID " + frog.getID());
						Element imageElement = (Element) imagesList.item(n);
						SiteImage image = new SiteImage();
						image.setImageFileName(imageElement.getElementsByTagName("filename").item(0).getTextContent());
						image.setSignatureGenerated(imageElement.getElementsByTagName("signature").item(0).getTextContent().equals("true"));
						image.setSourceImageHash(imageElement.getElementsByTagName("sourcehash").item(0).getTextContent());
						image.setProcessed(true);
						siteImages.add(image);
					}
					sample.setSiteImages(siteImages);

					if (importStatus.equals("false")) { //has this item
						// Location
						IdentiFrog.LOGGER.writeMessage("Loading -location- for SiteSample #" + s + " on frog with ID " + frog.getID());
						Location location = new Location();
						Element locationElement = (Element) frogElement.getElementsByTagName("location").item(0);
						// Location - name
						location.setName(locationElement.getElementsByTagName("name").item(0).getTextContent());
						// Location - description
						location.setDescription(locationElement.getElementsByTagName("description").item(0).getTextContent());
						// Location - coordinate
						NodeList coordinate = locationElement.getElementsByTagName("coordinate");
						if (coordinate.getLength() < 1) {
							// no coordinate was set
							IdentiFrog.LOGGER.writeMessage("No coordinate data in -location- for SiteSample #" + s + " on frog with ID "
									+ frog.getID());
							location.setCoordinateType(null);
						} else {
							Element coordinateElement = (Element) coordinate.item(0);
							//Node ct = coordinateElement.getAttributes().("type");
							if (coordinateElement != null && coordinateElement.getAttributes().getNamedItem("type") != null) {
								location.setCoordinateType(coordinate.item(0).getAttributes().getNamedItem("type").getTextContent());
								// Element coordinateElement = ((Element)
								// nn.item(0)).getElementsByTagName("coordinate");
								if (location.getCoordinateType().equals("Lat/Long")) {
									IdentiFrog.LOGGER.writeMessage("Loading LatLong -location- for SiteSample #" + s + " on frog with ID "
											+ frog.getID());
									location.setLongitude(coordinateElement.getElementsByTagName("longitude").item(0).getTextContent());
									location.setLatitude(coordinateElement.getElementsByTagName("latitude").item(0).getTextContent());
									location.setDatum(coordinateElement.getElementsByTagName("datum").item(0).getTextContent());
								} else if (location.getCoordinateType().equals("UTM")) {
									IdentiFrog.LOGGER
											.writeMessage("Loading UTM -location- for SiteSample #" + s + " on frog with ID " + frog.getID());
									location.setLongitude(coordinateElement.getElementsByTagName("easting").item(0).getTextContent());
									location.setLatitude(coordinateElement.getElementsByTagName("northing").item(0).getTextContent());
									location.setDatum(coordinateElement.getElementsByTagName("datum").item(0).getTextContent());
									location.setZone(Integer.parseInt(coordinateElement.getElementsByTagName("zone").item(0).getTextContent()));
								} else {
									IdentiFrog.LOGGER.writeError("Error: Unknown coordinate type (" + location.getCoordinateType()
											+ ")for -location- in SiteSample #" + s + " on frog with ID " + frog.getID()
											+ ", skipping coordinate data.");
								}
							} else {
								IdentiFrog.LOGGER
										.writeError("Frog has sample in DB that has a location without a TYPE attribute on the LOCATION node.");
							}
						}
						sample.setLocation(location);

						// Personel
						IdentiFrog.LOGGER.writeMessage("Loading -userids(s)- for SiteSample #" + s + " on frog with ID " + frog.getID());
						//User observer = new User();
						//User recorder = new User();
						Element observerElem = (Element) sampleElement.getElementsByTagName("observer").item(0);
						sample.setObserver(XMLFrogDatabase.getObserverByID(Integer.parseInt(observerElem.getTextContent())));

						Element recorderElem = (Element) sampleElement.getElementsByTagName("recorder").item(0);
						sample.setRecorder(XMLFrogDatabase.getRecorderByID(Integer.parseInt(recorderElem.getTextContent())));

						// SurveyID
						IdentiFrog.LOGGER.writeMessage("Loading -surveyid- for SiteSample #" + s + " on frog with ID " + frog.getID());
						sample.setSurveyID(sampleElement.getElementsByTagName("surveyid").item(0).getTextContent());
					}
					frog.addSiteSample(sample);
				}

				frogs.add(frog);
			}
			IdentiFrog.LOGGER.writeMessage("Loaded XML DB, loaded data for " + frogs.size() + " frogs.");
			XMLFrogDatabase.FULLY_LOADED = true;
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("Exception occured while loading the XML DB file.", e);
			JOptionPane.showMessageDialog(null,
					"An error occured while attempting to load the XML database.\nCheck the session log once you close IdentiFrog.",
					"Error loading DB", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Searches the database for the descriminator with the specified ID.
	 * Returns null if one is not found.
	 * 
	 * @param discID
	 *            Discriminator ID to find
	 * @return Discriminator in DB, null otherwise
	 */
	public static Discriminator getDiscrmininatorByID(int discID) {
		for (Discriminator d : discriminators) {
			if (d.getID() == discID) {
				return d;
			}
		}
		return null;
	}

	/**
	 * Returns the Recorder User object associated with the given ID
	 * 
	 * @param id
	 *            User ID to lookup
	 * @return Recorder from the DB if it's in the DB, null otherwise
	 */
	public static User getRecorderByID(int id) {
		if (!USERS_LOADED) {
			IdentiFrog.LOGGER.writeError("Attempting to get recorder from DB when users haven't been loaded!");
			return new User();
		}
		for (User user : recorders) {
			if (user.getID() == id) {
				return user;
			}
		}
		return null;
	}

	/**
	 * Returns the Observer User object associated with the given ID
	 * 
	 * @param id
	 *            User ID to lookup
	 * @return Observer from the DB if it's in the DB, null otherwise
	 */
	public static User getObserverByID(int id) {
		if (!USERS_LOADED) {
			IdentiFrog.LOGGER.writeError("Attempting to get observer from DB when users haven't been loaded!");
			return new User();
		}
		for (User user : observers) {
			if (user.getID() == id) {
				return user;
			}
		}
		return null;
	}

	public static File getDBFile() {
		return dbfile;
	}

	/**
	 * Sets the datafile this database is using for loading and storing values.
	 * 
	 * @param file
	 *            datafile.xml (or backing database file)
	 */
	public static void setFile(File file) {
		if (!file.getAbsolutePath().toLowerCase().endsWith(IdentiFrog.DB_FILENAME.toLowerCase())) {
			IdentiFrog.LOGGER.writeError("Setting database file to something not named database.xml!");
		}

		XMLFrogDatabase.dbfile = file;
		IdentiFrog.LOGGER.writeMessage("XML Database file set to " + file.toString());

		PROJECT_FOLDER = file.getAbsolutePath();
		if (PROJECT_FOLDER.endsWith(IdentiFrog.DB_FILENAME)) {
			PROJECT_FOLDER = PROJECT_FOLDER.substring(0, PROJECT_FOLDER.length() - IdentiFrog.DB_FILENAME.length() - 1);
		}
	}

	/**
	 * Returns the list of active observer users as reflected from the current
	 * database
	 * 
	 * @return list of unique observers. Empty if DB is not loaded.
	 */
	public static ArrayList<User> getObservers() {
		//ArrayList<User> observers = new ArrayList<User>();
		if (XMLFrogDatabase.USERS_LOADED) {
			Collections.sort(observers);
			return observers;
		} else {
			return new ArrayList<User>();
		}
	}

	/**
	 * Returns a unique, alphabetized (by first name) list of recorders.
	 * 
	 * @return list of unique recorders. Empty if DB is not loaded.
	 */
	public static ArrayList<User> getRecorders() {
		//ArrayList<User> observers = new ArrayList<User>();
		if (XMLFrogDatabase.USERS_LOADED) {
			Collections.sort(recorders);
			return recorders;
		} else {
			return new ArrayList<User>();
		}
	}

	/**
	 * Gets the list of frogs in this database.
	 * 
	 * @return Frogs in this database, or an empty arraylist if the database is
	 *         not yet loaded. Prints an error if the DB was not loaded.
	 */
	public static ArrayList<Frog> getFrogs() {
		if (!FULLY_LOADED) {
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
	public static Frog getFrogByID(int ID) {
		for (Frog frog : frogs) {
			if (frog.getID() == ID) {
				return frog;
			}
		}
		return null;
	}

	/**
	 * Removes a frog from the database. Does not commit to disk or delete
	 * images/signatures.
	 * 
	 * @param ID
	 *            Frog ID to remove
	 */
	public static void removeFrog(int ID) {
		frogs.remove(getFrogByID(ID));
	}

	/*
	 * public void replaceFrog(String ID, Frog frog) { int index = -1; for (int
	 * i = 0; i < frogs.size(); i++) { if
	 * (frogs.get(i).getFormerID().equals(ID)) { index = i; } } if (index == -1)
	 * { frogs.set(index, frog); } }
	 */

	/**
	 * Gets the next open frog ID after the current highest frog ID number
	 * 
	 * @return next free ID for a frog to use
	 */
	public static int getNextAvailableFrogID() {
		if (!FULLY_LOADED) {
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

	/**
	 * Creates a copy of the current database and it's folders and places the
	 * copy at the newSitePath. The folder passed is the project folder. (Inside
	 * of it is a datafile.xml file) The XML database is then set to this new
	 * datafile.xml so be sure to call loadXMLFile() on it directly afterwards.
	 * 
	 * @param newSitePath
	 *            Folder to copy contents of DB to
	 * @throws IOException
	 */
	public static void createCopy(File newSitePath) throws IOException {
		copyFolder(new File(getMainFolder()), newSitePath);
		setFile(new File(newSitePath + File.separator + IdentiFrog.DB_FILENAME));
	}

	/**
	 * Copies a project folder to another location
	 * 
	 * @param src
	 *            source folder
	 * @param dest
	 *            destionation folder
	 * @throws IOException
	 *             if an error occurs
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
	 * Scans the DB for an existing source hash based on the image to check if
	 * an image is being entered twice.
	 * 
	 * @param image
	 *            Source, full resolution image
	 * @return true if exists in DB already, false otherwise
	 */
	public static boolean checkForExistingHashInDB(File image) {
		//TODO
		return false;
	}

	//=================Migrated from FolderHandler
	private static String PROJECT_FOLDER;
	private static String SITE_NAME;
	//private static final String filename = "datafile.xml";
	private static final String IMAGES = "Images", SIGNATURES = "Signatures", BINARY = "Binary", DORSAL = "Dorsal";
	public static final String THUMB = "Thumbnail";
	private static final String[] PROJECT_FOLDERS = { IMAGES, SIGNATURES, BINARY, DORSAL, THUMB };

	/**
	 * Creates a new "Folder Handler", an object that handles folder creation
	 * for storing data related to a single site. This constructor by default
	 * puts the site in the %USERPROFILE%/Identifrog Data folder.
	 */
	/*
	 * public FolderHandler() { base = drive + File.separator + register; }
	 */

	/**
	 * Creates a new "Folder Handler", an object that handles folder creation
	 * for storing data related to a single site. This constructor uses the base
	 * string (site location data) as the data directory.
	 */
	/*
	 * public FolderHandler(String base) { if
	 * (base.endsWith(IdentiFrog.DB_FILENAME)) { this.base =
	 * base.substring(0,base.length()-IdentiFrog.DB_FILENAME.length()-1); } else
	 * { this.base = base; } }
	 */

	public static boolean siteFoldersExist() {
		return new File(XMLFrogDatabase.PROJECT_FOLDER).exists();
	}

	/**
	 * Creates folders required for a site. Uses the 'base' variable as the
	 * place to put the data.
	 * 
	 * @return true if successful, false on any error
	 * @author mjperez
	 */
	public static boolean createFolders() {
		boolean exists = false;
		for (String folderName : PROJECT_FOLDERS) {
			File dataSubfolder = new File(PROJECT_FOLDER + File.separator + folderName);
			IdentiFrog.LOGGER.writeMessage("Creating data folder: " + dataSubfolder.toString());
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
	 * Returns the datafile.xml path by adding the folder, the path separator,
	 * and the database filename.
	 * 
	 * @return Project Folder + (separator) + DB_NAME e.g.
	 *         C:\project\datafile.xml
	 */
	public static String getFileNamePath() {
		return PROJECT_FOLDER + File.separator + IdentiFrog.DB_FILENAME;
	}

	// { "Images", "Signatures", "Binary", "Dorsal", "Thumbnail" };

	/**
	 * Gets the project directory, with a \ on the end.
	 * 
	 * @return Project directory
	 */
	public static String getMainFolder() {
		return PROJECT_FOLDER + File.separator;
	}

	/**
	 * Gets the images/ folders of the project, with a \ on the end.
	 * 
	 * @return Images directory
	 */
	public static String getImagesFolder() {
		return getMainFolder() + IMAGES + File.separator;
	}

	/**
	 * Gets the signatures/ folders of the project, with a \ on the end.
	 * 
	 * @return Signatures directory
	 */
	public static String getSignaturesFolder() {
		return getMainFolder() + SIGNATURES + File.separator;
	}

	/**
	 * Gets the binary/ folders of the project, with a \ on the end.
	 * 
	 * @return Binary directory
	 */
	public static String getBinaryFolder() {
		return getMainFolder() + BINARY + File.separator;
	}

	/**
	 * Gets the dorsal/ folders of the project, with a \ on the end.
	 * 
	 * @return Dorsal directory
	 */
	public static String getDorsalFolder() {
		return getMainFolder() + DORSAL + File.separator;
	}

	/**
	 * Gets the thumbnail/ folders of the project, with a \ on the end.
	 * 
	 * @return Thumbnail directory
	 */
	public static String getThumbnailFolder() {
		return getMainFolder() + THUMB + File.separator;
	}

	public String getSiteName() {
		return XMLFrogDatabase.SITE_NAME;
	}

	public static void setSiteName(String siteName) {
		XMLFrogDatabase.SITE_NAME = siteName;
	}

	/**
	 * Adds a frog to the database. Does not check if it already exists, so use
	 * caution when using this method. This does not commit to disk so you must
	 * do so using writeXMLFile().
	 * 
	 * @param newFrog
	 *            Frog to add to the DB.
	 */
	public static void addFrog(Frog newFrog) {
		frogs.add(newFrog);
	}

	/**
	 * Returns the highest in use ID + 1 of all observer users.
	 * 
	 * @return Highest ID + 1 of all observers
	 */
	public static int getNextAvailableObserverID() {
		int highestID = 0;
		for (User user : observers) {
			if (user.getID() > highestID) {
				highestID = user.getID();
			}
		}
		return highestID + 1;
	}

	/**
	 * Returns the highest in use ID + 1 of all recorder users.
	 * 
	 * @return Highest ID + 1 of all recorders
	 */
	public static int getNextAvailableRecorderID() {
		int highestID = 0;
		for (User user : recorders) {
			if (user.getID() > highestID) {
				highestID = user.getID();
			}
		}
		return highestID + 1;
	}

	/**
	 * Sets the list of recorders to the passed in value. Does not commit to
	 * disk.
	 * 
	 * @param recorders
	 *            New recorders for DB.
	 */
	public static void setRecorders(ArrayList<User> recorders) {
		XMLFrogDatabase.recorders = recorders;
	}

	/**
	 * Returns a list of unique locations, sorted by name.
	 * 
	 * @return list of locations frogs have been captured
	 */
	public static ArrayList<Location> uniqueLocations() {
		HashSet<Location> locations = new HashSet<Location>();
		for (Frog frog : frogs) {
			for (SiteSample sample : frog.getSiteSamples()) {
				locations.add(sample.getLocation());
			}
		}
		ArrayList<Location> sortedLocations = new ArrayList<Location>(locations);
		Collections.sort(sortedLocations);
		return sortedLocations;
	}

	/**
	 * Sets the list of observers to the passed in value. Does not commit to
	 * disk.
	 * 
	 * @param observers
	 *            New observers for DB.
	 */
	public static void setObservers(ArrayList<User> observers) {
		XMLFrogDatabase.observers = observers;
	}

	public static String getThumbPlaceholder() {
		return "PLCHOLDR";
	}

	public static int getNextAvailableDiscriminatorID() {
		int highestID = highestSessionDiscriminatorID++; //increment so we don't hand this ID out again
		for (Discriminator discriminator : discriminators) {
			if (discriminator.getID() > highestID) {
				highestID = discriminator.getID();
			}
		}
		if (highestSessionDiscriminatorID <= highestID + 1) {
			highestSessionDiscriminatorID = highestID + 1;
		}
		return highestID + 1;
	}

	/**
	 * Gets the list of all available discriminators for use
	 * 
	 * @return
	 */
	public static ArrayList<Discriminator> getDiscriminators() {
		return discriminators;
	}

	/**
	 * Sets the list of all discriminators that can be used
	 * 
	 * @param discriminators
	 */
	public static void setDiscriminators(ArrayList<Discriminator> discriminators) {
		XMLFrogDatabase.discriminators = discriminators;
	}

	/**
	 * Sets the frog with the specified ID to the passed in frog
	 * 
	 * @param id
	 *            ID of frog to replace
	 * @param editedFrog
	 *            new frog to replace old one with
	 */
	public static void updateFrog(int id, Frog newFrog) {
		int index = -1;
		for (int i = 0; i <= frogs.size(); i++) {
			if (frogs.get(i).getID() == id) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			IdentiFrog.LOGGER.writeError("Unable to update frog with ID " + id + ", its not in the DB");
			return;
		}
		frogs.set(index, newFrog);
		IdentiFrog.LOGGER.writeMessage("Updated frog with ID " + id + " in the DB.");
	}

	/**
	 * Searches all frog images in the database for one with the matching hash.
	 * 
	 * @param sourceImageHash
	 *            Hash to search for
	 * @return siteimage with same hash
	 */
	public static SiteImage searchImageByHash(String sourceImageHash) {
		for (Frog f : frogs) {
			for (SiteImage img : f.getAllSiteImages()) {
				if (img.getSourceImageHash().equals(sourceImageHash)) {
					return img;
				}
			}
		}
		return null;
	}

	/**
	 * Searches all frog images in the database for one with the matching hash.
	 * 
	 * @param sourceImageHash
	 *            Hash to search for
	 * @return Frog frog that has this image
	 */
	public static Frog findImageOwnerByHash(String sourceImageHash) {
		for (Frog f : frogs) {
			for (SiteImage img : f.getAllSiteImages()) {
				if (img.getSourceImageHash().equals(sourceImageHash)) {
					return f;
				}
			}
		}
		return null;
	}

	/**
	 * Gets a sorted (by location name) list of all locations of samples in the
	 * DB
	 * 
	 * @return list of unique sorted locations
	 */
	public static ArrayList<Location> getAllLocations() {
		HashSet<Location> locs = new HashSet<Location>();
		for (Frog f : frogs) {
			for (SiteSample s : f.getSiteSamples()) {
				if (s.getLocation() != null) {
					locs.add(s.getLocation());
				}
			}
		}
		ArrayList<Location> sortedList = new ArrayList<Location>(locs);
		Collections.sort(sortedList);
		return sortedList;
	}

	/**
	 * Gets a sorted (by species name) list of all species of frogs in the DB
	 * 
	 * @return list of unique sorted species strings
	 */
	public static ArrayList<String> getSpecies() {
		HashSet<String> species = new HashSet<String>();
		for (Frog f : frogs) {
			if (f.getSpecies() != null) {
				species.add(f.getSpecies());
			}
		}
		ArrayList<String> sortedList = new ArrayList<String>(species);
		Collections.sort(sortedList);
		return sortedList;
	}

	/**
	 * Checks if all frogs in the DB have signatures generated
	 * 
	 * @return true if all frogs in DB are searchable. Still true even if no
	 *         frogs exist. False otherwise
	 */
	public static boolean isFullyImageSearchable() {
		for (Frog f : frogs) {
			if (!f.isFullySearchable()) {
				return false;
			}
		}
		return true;
	}

	static class CommitWorker extends SwingWorker<Boolean, Integer> {

		private MainFrame attachedFrame;
		private int numToProcess = 0;
		private int numProcessed = 0;

		public CommitWorker(MainFrame attachedFrame) {
			this.attachedFrame = attachedFrame;
			numToProcess += discriminators.size();
			numToProcess += frogs.size();
			numToProcess += recorders.size();
			numToProcess += observers.size();
		}
		
		@Override
		protected Boolean doInBackground() throws Exception {
			IdentiFrog.LOGGER.writeMessage("Synchronizing memory-database to disk...");
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
			Element frogsElement = doc.createElement("frogs");
			root.appendChild(frogsElement);
			doc.appendChild(root);

			IdentiFrog.LOGGER.writeMessage("Writing frogs to DB");

			for (Discriminator d : discriminators) {
				d.setInUse(false); //reset inuse flag for setting when parsing frogs attached discriminators (will set to true again)
			}

			for (Frog frog : frogs) {
				frogsElement.appendChild(frog.createDBElement(doc));
				numProcessed++;
				publish(numProcessed);
			}

			Element users = doc.createElement("users");
			IdentiFrog.LOGGER.writeMessage("Writing users to DB");
			Element recordersElement = doc.createElement("recorders");
			Element observersElement = doc.createElement("observers");

			for (User user : recorders) {
				recordersElement.appendChild(user.createElement(doc));
				numProcessed++;
				publish(numProcessed);
			}
			for (User user : observers) {
				observersElement.appendChild(user.createElement(doc));
				numProcessed++;
				publish(numProcessed);
			}

			users.appendChild(recordersElement);
			users.appendChild(observersElement);

			root.appendChild(users);

			//Discriminators
			IdentiFrog.LOGGER.writeMessage("Writing discriminators to DB");
			Element discrimsElement = doc.createElement("discriminators");

			for (Discriminator discriminator : discriminators) {
				discrimsElement.appendChild(discriminator.createElement(doc));
				numProcessed++;
				publish(numProcessed);
			}
			root.appendChild(discrimsElement);

			/*
			 * System.out.println("Dumping frogs tostrg"); for (Frog frog :
			 * frogs) { System.out.println(frog); }
			 */

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
			StreamResult result = new StreamResult(dbfile);
			try {
				IdentiFrog.LOGGER.writeMessage("Saving DB to disk");
				transformer.transform(source, result);
				IdentiFrog.LOGGER.writeMessage("Saved DB to disk");
			} catch (TransformerException e) {
				IdentiFrog.LOGGER.writeException(e);
				return false;
			}
			return true;
		}

		@Override
		protected void process(List<Integer> chunks) {
			// Messages received from the doInBackground() (when invoking the publish() method)
			int latestUpdate = chunks.get(chunks.size() - 1);
			if (attachedFrame != null) {
				attachedFrame.getStatusBar().setMessage("Saving database... " + calcPercent(latestUpdate) + "%");
			} else {
				System.out.println("AF IS NULL");
			}
		}

		private int calcPercent(int latestUpdate) {
			return (int) (((double) numProcessed / numToProcess) * 100);
		}
	}

}
