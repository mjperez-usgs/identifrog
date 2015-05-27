package gov.usgs.identifrog.DataObjects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class represents a single image of a frog. It includes information about the frog signature as well, such as if one exists (or does not).
 * @author mjperez
 *
 */
public class SiteImage {
	private String imageFileName;
	private boolean signatureGenerated;
	private String sourceImageHash;
	
	public String getSourceImageHash() {
		return sourceImageHash;
	}
	public void setSourceImageHash(String sourceImageHash) {
		this.sourceImageHash = sourceImageHash;
	}
	public String getImageFileName() {
		return imageFileName;
	}
	public void setImageFileName(String imageName) {
		this.imageFileName = imageName;
	}
	public boolean isSignatureGenerated() {
		return signatureGenerated;
	}
	public void setSignatureGenerated(boolean signatureGenerated) {
		this.signatureGenerated = signatureGenerated;
	}
	
	/**
	 * Creates a DB 2.0 Element in XML representing this Site Image
	 * @param document Document object to use for creating the element
	 * @return Element with data describing this SiteImage
	 */
	public Element createElement(Document document) {
		Element element = document.createElement("image");

		Element imageNameElement = document.createElement("filename");
		imageNameElement.appendChild(document.createTextNode(getImageFileName()));

		Element signatureElement = document.createElement("signature");
		signatureElement.appendChild(document.createTextNode((isSignatureGenerated()) ? "true" : "false"));
		
		Element imageHashElement = document.createElement("sourcehash");
		imageHashElement.appendChild(document.createTextNode(getSourceImageHash()));
		
		element.appendChild(imageNameElement);
		element.appendChild(signatureElement);
		element.appendChild(imageHashElement);

		return element;
	}
}
