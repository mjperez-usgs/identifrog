package gov.usgs.identifrog.DataObjects;

import java.util.ArrayList;

/**
 * This class acts as a singleton thread-safe way to access the frog database. It is static so only one Frog DB can be open at a time and is accessed via the IdentiFrog class.
 * @author mjperez
 *
 */
public class FrogDatabase {
	private static ArrayList<Frog> frogs;
	private static String xmlDBFilePath;
	
	/**
	 * Loads the frog database from the specified XML file. 
	 * All values are nullified before loading the database so old ones don't carry over.
	 * @param xmlDBFilePath .xml file to load
	 */
	public static void loadDB(String xmlDBFilePath) {
		FrogDatabase.xmlDBFilePath = xmlDBFilePath;
	}
}
