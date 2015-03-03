package gov.usgs.identifrog;

import java.io.File;
import java.util.ArrayList;
import javax.swing.filechooser.FileFilter;

/**
 * <p>
 * Title: ExtensionFileFilter.java
 * <p>
 * Description: this filters the file view based on file extensions
 * 
 * @author Steven P. Miller <b>IdentiFrog Team</b> <i>2005</i>
 */

public class ExtensionFileFilter extends FileFilter {
	private String description = "";
	@SuppressWarnings("unchecked")
	private ArrayList extensions = new ArrayList();

	/**
	 * Adds an extension to the list of filters
	 * 
	 * @param extension
	 *            String file extension
	 */
	@SuppressWarnings("unchecked")
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
		for (int i = 0; i < extensions.size(); i++) {
			if (name.endsWith((String) extensions.get(i))) {
				return true;
			}
		}
		return false;
	}
}