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

	public FolderHandler() {
	  base = drive + File.separator + register;
	}

	public FolderHandler(String base) {
	  this.base = base;
	}
	
	public boolean FoldersExist() {
		if (new File(base).exists()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean CreateFolders() {
		boolean exists = false;
		ArrayList<File> folders = new ArrayList<File>();
		for (int i = 0; i < foldernames.length; i++) {
			folders.add(new File(base + File.separator + foldernames[i]));
		}
		if (new File(base).mkdir()) {
			for (int i = 0; i < foldernames.length; i++) {
				exists = folders.get(i).mkdir();
				if (!exists) {
					return false;
				}
			}
		} else {
			return false;
		}
		folders.get(0).delete();
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