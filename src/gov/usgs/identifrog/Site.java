package gov.usgs.identifrog;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Site holds metadata about a site, e.g. the time it was opened/saved, the name, the datafile.xml filepath, etc
 * @author mjperez
 *
 */
public class Site implements Serializable, Comparable<Site> {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((datafilePath == null) ? 0 : datafilePath.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Site other = (Site) obj;
		if (datafilePath == null) {
			if (other.datafilePath != null)
				return false;
		} else if (!datafilePath.equals(other.datafilePath))
			return false;
		return true;
	}
	/**
	 * Site Changelog:
	 * v1: sitename, datafilepath, lastmodified.
	 */
	private static final long serialVersionUID = -5146390920073469442L; //never change this
	
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY"); //only init once
	/**
	 * @serial
	 */
	private String siteName;
	/**
	 * @serial
	 */
	private String datafilePath;
	/**
	 * @serial
	 */
	private Date lastModified;
	
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
	public String getDatafilePath() {
		return datafilePath;
	}
	public void setDatafilePath(String datafilePath) {
		this.datafilePath = datafilePath;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	@Override
	public String toString() {
		return "Site [siteName=" + siteName + ", datafilePath=" + datafilePath
				+ ", lastModified=" + dateFormat.format(lastModified) + "]";
	}
	@Override
	public int compareTo(Site otherSite) {
		return -getLastModified().compareTo(otherSite.getLastModified()); //negative flips order so its newest to oldest
	}	
}
