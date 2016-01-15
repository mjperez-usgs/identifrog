package gov.usgs.identifrog.DataObjects;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Describes a single release from the GitHub v3 API.
 * @author mjperez
 *
 */
public class GitHubRelease {
	private String name;
	private String published_at;
	private ArrayList<GitHubAttachment> attachments;
	private String body;
	private boolean isPreRelease;
	private String tag_name;

	public GitHubRelease(JSONObject releaseInfo) {
		this.setName((String) releaseInfo.get("name"));
		this.setPublished_at((String) releaseInfo.get("published_at"));
		this.attachments = new ArrayList<GitHubAttachment>();
		JSONArray assetArray = (JSONArray) releaseInfo.get("assets");
		for (Object obj : assetArray){
			if (obj instanceof JSONObject) {
				GitHubAttachment gha = new GitHubAttachment((JSONObject) obj);
				attachments.add(gha);
			}
		}
		this.setPreRelease((Boolean)releaseInfo.get("prerelease"));
		this.setBody((String) releaseInfo.get("body"));
		this.setTagName((String) releaseInfo.get("tag_name"));
	}
	
	public String getTagName() {
		return tag_name;
	}

	public void setTagName(String tag) {
		this.tag_name = tag;
	}
	
	public boolean isPreRelease() {
		return isPreRelease;
	}

	public void setPreRelease(boolean isPreRelease) {
		this.isPreRelease = isPreRelease;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getName() {
		return name;
	}
	
	public String getFormattedBody(){
		String retStr = body.replace("\r\n", "<br>");
		
		return "<html>"+retStr+"</html>";
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getPublished_at() {
		return published_at;
	}

	public void setPublished_at(String published_at) {
		this.published_at = published_at;
	}

	public ArrayList<GitHubAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(ArrayList<GitHubAttachment> attachments) {
		this.attachments = attachments;
	}
	
	@Override
	public String toString() {
		return (isPreRelease ? "Beta" : "Stable") + ": "+name;
	}

	/**
	 * Describes a GitHub asset from their v3 API.
	 * @author mjperez
	 *
	 */
	public class GitHubAttachment {
		@Override
		public String toString() {
			return "GitHubAttachment [downloadURL=" + downloadURL + ", sizeInBytes=" + sizeInBytes + "]";
		}

		private String downloadURL;
		private long sizeInBytes;
		
		public GitHubAttachment(JSONObject attachmentInfo) {
			this.downloadURL = (String) attachmentInfo.get("browser_download_url");
			this.sizeInBytes = (long) attachmentInfo.get("size");
		}

		public String getDownloadURL() {
			return downloadURL;
		}

		public void setDownloadURL(String downloadURL) {
			this.downloadURL = downloadURL;
		}

		public long getSizeInBytes() {
			return sizeInBytes;
		}

		public void setSizeInBytes(long sizeInBytes) {
			this.sizeInBytes = sizeInBytes;
		}
	}
}
