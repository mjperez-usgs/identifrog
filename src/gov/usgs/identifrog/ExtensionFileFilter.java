package gov.usgs.identifrog;

import java.io.File;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

/**
 * <p>
 * Title: ExtensionFileFilter.java
 * <p>
 * Description: this filters the file view based on file extensions
 * Use when using filechosers.
 * 
 * @author Steven P. Miller <b>IdentiFrog Team</b> <i>2005</i>
 */

public class ExtensionFileFilter extends FileFilter {
	private String description = "";
	private ArrayList<String> extensions = new ArrayList<String>();
	private ArrayList<String> exactfiles = new ArrayList<String>();

	/**
	 * Adds an extension to the list of filters. The . is optional, e.g. .xml and xml will both work.
	 * 
	 * @param extension
	 *            String file extension
	 */
	public void addExtension(String extension) {
		if (!extension.startsWith(".")) {
			extension = "." + extension;
		}
		extensions.add(extension.toLowerCase());
	}

	/**
	 * Description of the file filters
	 * 
	 * @param aDescription
	 *            String description
	 */
	public void setDescription(String aDescription) {
		description = aDescription;
	}

	/**
	 * @return String returns the description
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Checks file against the filter
	 * 
	 * @param f
	 *            File file to be checked
	 * @return boolean true if filtered, false if not
	 */
	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		String name = f.getName().toLowerCase();
		for (String ext : extensions) {
			if (name.endsWith(ext)) {
				return true;
			}
		}
		for (String fname : exactfiles) {
			if (name.toLowerCase().equals(fname.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public void addExactFile(String filename) {
		exactfiles.add(filename);
	}
}