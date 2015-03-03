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
		ArrayList<String> frogList = getUniqueFrogList(frogs);

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
		System.out.println(buffer);

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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (wr != null) {
					wr.flush();
					wr.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
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

	private static ArrayList<String> getFrogList(ArrayList<Frog> frogs) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < frogs.size(); i++) {
			list.add(frogs.get(i).getID());
		}
		return list;
	}

	private static ArrayList<String> getUniqueSurveyList(ArrayList<Frog> frogs) {
		ArrayList<String> list = getSurveyList(frogs);
		list = makeUniqueList(list);
		return list;
	}

	private static ArrayList<String> getUniqueFrogList(ArrayList<Frog> frogs) {
		ArrayList<String> list = getFrogList(frogs);
		list = makeUniqueList(list);
		return list;
	}

	private static ArrayList<String> makeUniqueList(ArrayList<String> list) {
		HashSet<String> hs = new HashSet<String>();
		hs.addAll(list);
		list.clear();
		list.addAll(hs);
		Collections.sort(list);
		return list;
	}

	private static ArrayList<Frog> getInstancesOfFrog(ArrayList<Frog> frogs, String frogID) {
		ArrayList<Frog> list = new ArrayList<Frog>();
		for (int i = 0; i < frogs.size(); i++) {
			if (frogs.get(i).getID().equals(frogID)) {
				list.add(frogs.get(i));
			}
		}
		return list;
	}

	private static char isFrogInSurvey(ArrayList<Frog> frogs, String frogID, String surveyID) {
		ArrayList<Frog> afrog = getInstancesOfFrog(frogs, frogID);
		for (int i = 0; i < afrog.size(); i++) {
			if (afrog.get(i).getSurveyID().equals(surveyID)) {
				return '1';
			}
		}
		return '0';
	}
}