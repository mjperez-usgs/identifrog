package gov.usgs.identifrog.DataObjects;

import gov.usgs.identifrog.IdentiFrog;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;

import javax.swing.GrayFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a single image of a frog. It includes information about the frog signature as well, such as if one exists (or does not).
 * @author mjperez
 *
 */
public class SiteImage {
	private String imageFileName, sourceFilePath;
	private BufferedImage sourceFileThumbnail;
	Image greyScaleThumbnail;
	private boolean signatureGenerated, processed;
	private String sourceImageHash;
	
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
	 * Gets the full-res file path. Only works if the processed variable is set to true.
	 * @return path to the full size image
	 */
	public String getSourceFilePath() {
		return sourceFilePath;
	}

	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	public boolean isProcessed() {
		return this.processed;
	}

	public BufferedImage getSourceFileThumbnail() {
		return sourceFileThumbnail;
	}

	/**
	 * Sets this image's thumbnail from a buffered image. This is used when a new frog image is added.
	 * @param sourceFileThumbnail
	 */
	public void setSourceFileThumbnail(BufferedImage sourceFileThumbnail) {
		this.sourceFileThumbnail = sourceFileThumbnail;
	}

	public Image getGreyScaleThumbnail() {
		return greyScaleThumbnail;
	}
	
	public void generateGreyscaleImage(){
		if (isSignatureGenerated()) {
			IdentiFrog.LOGGER.writeError("Image already has signature! should not be greyscale!");
			return;
		}
		ImageFilter filter = new GrayFilter(true, 30);  
		ImageProducer producer = new FilteredImageSource(sourceFileThumbnail.getSource(), filter);  
		greyScaleThumbnail = Toolkit.getDefaultToolkit().createImage(producer);  
	}

	@Override
	public String toString() {
		return "SiteImage [imageFileName=" + imageFileName
				+ ", sourceFilePath=" + sourceFilePath
				+ ", signatureGenerated=" + signatureGenerated + ", processed="
				+ processed + ", sourceImageHash=" + sourceImageHash + "]";
	}
	
	
}
