package gov.usgs.identifrog.DataObjects;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.ThumbnailCreator;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
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
	
	/**
	 * Generates hash code for original source image. processed should not be set to true for this method to work correctly.
	 */
	public void generateHash(){
		try {
			sourceImageHash = DigestUtils.md5Hex(FileUtils.readFileToByteArray(new File(sourceFilePath)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			IdentiFrog.LOGGER.writeExceptionWithMessage("Failed to create hash for image!", e);
			sourceImageHash = "ERROR";
		}
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
	 * Gets the full-res file path. Uses source image or one in images depending on if this image has been moved into the DB.
	 * @return path to the full size image
	 */
	public String getSourceFilePath() {
		if (processed == true) {
			return XMLFrogDatabase.getImagesFolder()+imageFileName;
		} else {
			return sourceFilePath;
		}
	}

	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	/**
	 * Returns if this image has had a thumbnail created in the thumbnail directory and the full resolution placed in the images/ directory
	 * @return
	 */
	public boolean isProcessed() {
		return this.processed;
	}

	public BufferedImage getColorThumbnail() {
		return sourceFileThumbnail;
	}

	/**
	 * Sets this image's thumbnail from a buffered image. This is used when a new frog image is added.
	 * @param sourceFileThumbnail
	 */
	public void setColorThumbnail(BufferedImage sourceFileThumbnail) {
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
	
	/**
	 * Returns the source image as stored in the DB Images/ directory. The format is as follows:
	 * [original base filename]_[sourcehash].[sourceextension]
	 * Only works if processed is set to false.
	 * @return Unique filename, however it returns null if processed is already set to true as this method should not be used.
	 */
	public String createUniqueDBFilename(){
		if (processed) {
			IdentiFrog.LOGGER.writeError("Attemped to create unique filename for already processed image!");
			return null;
		}
		return FilenameUtils.getBaseName(sourceFilePath)+"_"+getSourceImageHash()+"."+FilenameUtils.getExtension(sourceFilePath);
	}

	/**
	 * Copies the source image to the images/ folder if it doesn't exist already and sets the imageFileName.
	 * Creates a thumbnail for the thumbnail/ directory.
	 * Sets this image status to processed.
	 * Does nothing if this is already done.
	 */
	public void processImageIntoDB() {
		// TODO Auto-generated method stub
		//copy image to Images/ if it doesn't already exist
		IdentiFrog.LOGGER.writeMessage("Preparing to process image into DB: "+this);

		if (!processed) {
			generateHash();
			//store full res
			String imgFolder = XMLFrogDatabase.getImagesFolder();
			setImageFileName(createUniqueDBFilename());
			File uniqueFile = new File(imgFolder + getImageFileName());
			if (!uniqueFile.exists()) {
				try {
					FileUtils.copyFile(new File(getSourceFilePath()), uniqueFile);
					IdentiFrog.LOGGER.writeMessage("Copied full resolution image into DB.");
				} catch (IOException e) {
					IdentiFrog.LOGGER.writeExceptionWithMessage("Failed to copy new image to images/: " + uniqueFile.toString(), e);
				}
			}
			//store thumbnail
			File outputfile = new File(XMLFrogDatabase.getThumbnailFolder()+getImageFileName());
			try {
				ImageIO.write(getColorThumbnail(), "jpg", outputfile);
				IdentiFrog.LOGGER.writeMessage("Copied thumbnail into thumbnail directory.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				IdentiFrog.LOGGER.writeExceptionWithMessage("Failed to save thumbnail to thumbnail/ folder.", e);
			}
			processed = true;
		} else {
			IdentiFrog.LOGGER.writeMessage("Image already processed. Skipping.");
		}
	}
	
	/**
	 * Creates a thumbnail for viewing in the left hand side of the FrogEditor
	 * window. Will load greyscale if necessary. Will additionally update the existing thumbnails.
	 * 
	 * @param image
	 *            SiteImage to create thumbnail for
	 * @return Modified image with loaded thumbnails
	 */
	public void createListThumbnail() {
		IdentiFrog.LOGGER.writeMessage("Generating list thumbnail for " + this);
		BufferedImage src;
		try {
			if (isProcessed()) {
				IdentiFrog.LOGGER.writeMessage("Reading thumbnail file: "+XMLFrogDatabase.getThumbnailFolder() + getImageFileName());
				src = ImageIO.read(new File(XMLFrogDatabase.getThumbnailFolder() + getImageFileName()));
			} else {
				IdentiFrog.LOGGER.writeMessage("Generating thumbnail from full size image: "+getSourceFilePath());
				src = ImageIO.read(new File(getSourceFilePath()));
			}
			BufferedImage thumbnail = Scalr.resize(src, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH, 100, 75, Scalr.OP_ANTIALIAS);
			setColorThumbnail(thumbnail);
			if (!isSignatureGenerated()) {
				IdentiFrog.LOGGER.writeMessage("Generating greyscale thumbnail.");
				generateGreyscaleImage();
			} else {
				greyScaleThumbnail = null; //clear references
			}
		} catch (IOException e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("Unable to generate thumbnail for image (in memory): " + toString(), e);
		}
	}
}
