package gov.usgs.identifrog;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import gov.usgs.identifrog.DataObjects.Frog;

/**
 * <p>
 * Title: MarkExport.java
 * <p>
 * Description: Exports the frog data to MARK compatible input file.
 * 
 * @author Hidayatullah Ahsan 2011
 * @author Oksana V. Kelly 2010
 */

public class MarkExport {
	// //////// METHODS //////////
	public static void saveToMark(ArrayList<Frog> frogs, String path) {
		ArrayList<String> surveyList = getUniqueSurveyList(frogs);
		ArrayList<Integer> frogList = getUniqueFrogList(frogs);

		String delim = ",";
		String stop = "\n";
		String buffer = "";

		for (int i = 0; i < surveyList.size(); i++) {
			buffer = buffer + delim + surveyList.get(i);
		}
		buffer = buffer + stop;
		for (int i = 0; i < frogList.size(); i++) {
			buffer = buffer + "FROG" + frogList.get(i);
			for (int j = 0; j < surveyList.size(); j++) {
				buffer = buffer + delim + isFrogInSurvey(frogs, frogList.get(i), surveyList.get(j));
			}
			buffer = buffer + "\n";
		}
		IdentiFrog.LOGGER.writeMessage(buffer);

		String filename;
		String ext = path.substring(path.length() - 3, path.length());
		if (ext.equalsIgnoreCase("inp")) {
			filename = path;
		} else {
			filename = path + ".INP";
		}
		BufferedWriter wr = null;
		try {
			wr = new BufferedWriter(new FileWriter(filename));
			wr.write(buffer);
		} catch (FileNotFoundException e) {
			IdentiFrog.LOGGER.writeException(e);
		} catch (IOException e) {
			IdentiFrog.LOGGER.writeException(e);
		} finally {
			try {
				if (wr != null) {
					wr.flush();
					wr.close();
				}
			} catch (IOException e) {
				IdentiFrog.LOGGER.writeException(e);
			}
		}
	}

	private static ArrayList<String> getSurveyList(ArrayList<Frog> frogs) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < frogs.size(); i++) {
			list.add(frogs.get(i).getSurveyID());
		}
		return list;
	}

	/**
	 * Gets a list of all Frog IDs
	 * @param frogs array of frogs to create a list from
	 * @return list of frog IDs
	 */
	private static ArrayList<Integer> getFrogList(ArrayList<Frog> frogs) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < frogs.size(); i++) {
			list.add(frogs.get(i).getID()); //TODO
		}
		return list;
	}

	private static ArrayList<String> getUniqueSurveyList(ArrayList<Frog> frogs) {
		ArrayList<String> list = getSurveyList(frogs);
		return makeUniqueList(list);
	}

	private static ArrayList<Integer> getUniqueFrogList(ArrayList<Frog> frogs) {
		ArrayList<Integer> list = getFrogList(frogs);
		return makeUniqueIntegerList(list);
	}

	private static ArrayList<String> makeUniqueList(ArrayList<String> list) {
		HashSet<String> hs = new HashSet<String>();
		hs.addAll(list);
		list.clear();
		list.addAll(hs);
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Creates a unique integer list. This method exists because of limits on the Java language syntax.
	 * @param list List to check for uniqueness
	 * @return Unique list that is sorted low to high
	 */
	private static ArrayList<Integer> makeUniqueIntegerList(ArrayList<Integer> list) {
		HashSet<Integer> hs = new HashSet<Integer>();
		hs.addAll(list);
		list.clear();
		list.addAll(hs);
		Collections.sort(list);
		return list;
	}

	private static ArrayList<Frog> getInstancesOfFrog(ArrayList<Frog> frogs, int frogID) {
		ArrayList<Frog> list = new ArrayList<Frog>();
		for (int i = 0; i < frogs.size(); i++) {
			if (frogs.get(i).getID() == frogID) {
				list.add(frogs.get(i));
			}
		}
		return list;
	}

	private static char isFrogInSurvey(ArrayList<Frog> frogs, int frogID, String surveyID) {
		ArrayList<Frog> afrog = getInstancesOfFrog(frogs, frogID);
		for (int i = 0; i < afrog.size(); i++) {
			if (afrog.get(i).getSurveyID().equals(surveyID)) {
				return '1';
			}
		}
		return '0';
	}
}