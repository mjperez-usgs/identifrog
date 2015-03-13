package gov.usgs.identifrog.Handlers;

import java.io.File;
import java.util.ArrayList;

public class FolderHandler {
  private String base;
  private String siteName;
	private static final String drive = System.getProperty("user.home");
	private static final String register = "IdentiFrog Data";
	private static final String filename = "datafile.xml";
	private static final String[] foldernames = { "Images", "Signatures", "Binary", "Dorsal", "Thumbnail" };

	/**
	 * Creates a new "Folder Handler", an object that handles folder creation for storing data related to a single site.
	 * This constructor by default puts the site in the %USERPROFILE%/Identifrog Data folder.
	 */
	public FolderHandler() {
	  base = drive + File.separator + register;
	}

	/**
	 * Creates a new "Folder Handler", an object that handles folder creation for storing data related to a single site.
	 * This constructor uses the base string (site location data) as the data directory. 
	 */
	public FolderHandler(String base) {
	  this.base = base;
	}
	
	public boolean FoldersExist() {
		return new File(base).exists();
	}

	/**
	 * Creates folders required for a site. Uses the base variable as the place to put the data.
	 * @return true if successful, false on any error
	 * @author mjperez
	 */
	public boolean createFolders() {
		boolean exists = false;
		for (String folderName : foldernames) {
			File dataSubfolder = new File(base + File.separator + folderName);
			exists = dataSubfolder.mkdirs();
			if (!exists) {
				return false;
			}
		}
		return true;
	}

	public String getFileName() {
		return filename;
	}

	public String getFileNamePath() {
		return base + File.separator + filename + File.separator;
	}

	// { "Images", "Signatures", "Binary", "Dorsal", "Thumbnail" };

	public String getMainFolder() {
		return base + File.separator;
	}

	public String getImagesFolder() {
		return getMainFolder() + foldernames[0] + File.separator;
	}

	public String getSignaturesFolder() {
		return getMainFolder() + foldernames[1] + File.separator;
	}

	public String getBinaryFolder() {
		return getMainFolder() + foldernames[2] + File.separator;
	}

	public String getDorsalFolder() {
		return getMainFolder() + foldernames[3] + File.separator;
	}

	public String getThumbnailFolder() {
		return getMainFolder() + foldernames[4] + File.separator;
	}

  public String getSiteName() {
    return siteName;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
  }
}