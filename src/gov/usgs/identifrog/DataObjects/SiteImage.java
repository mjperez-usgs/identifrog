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
 * This class represents a single image of a frog. It includes information about
 * the frog signature as well, such as if one exists (or does not).
 * 
 * @author mjperez
 *
 */
public class SiteImage {
	private String imageFileName, sourceFilePath;
	private BufferedImage colorFileThumbnail;
	private BufferedImage greyScaleThumbnail;
	private boolean signatureGenerated, processed;
	private String sourceImageHash;

	/**
	 * Creates a DB 2.0 Element in XML representing this Site Image
	 * 
	 * @param document
	 *            Document object to use for creating the element
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
	 * Copy constructor
	 * 
	 * @param img
	 *            object to copy
	 */
	public SiteImage(SiteImage img) {
		this.imageFileName = img.getImageFileName();
		this.sourceFilePath = img.getSourceFilePath();
		this.colorFileThumbnail = IdentiFrog.copyImage(img.getColorThumbnail());
		this.greyScaleThumbnail = IdentiFrog.copyImage(img.getGreyScaleThumbnail());
		this.signatureGenerated = img.isSignatureGenerated();
		this.processed = img.isProcessed();
		this.sourceImageHash = img.getSourceImageHash();

		// TODO Auto-generated constructor stub
	}

	public SiteImage() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Generates hash code for original source image. processed should not be
	 * set to true for this method to work correctly.
	 */
	public void generateHash() {
		try {
			sourceImageHash = DigestUtils.md5Hex(FileUtils.readFileToByteArray(new File(sourceFilePath)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			IdentiFrog.LOGGER.writeExceptionWithMessage("Failed to create hash for image!", e);
			sourceImageHash = "ERROR";
		}
	}

	/**
	 * Gets the hash of the original image.
	 * @return
	 */
	public String getSourceImageHash() {
		return sourceImageHash;
	}

	public void setSourceImageHash(String sourceImageHash) {
		this.sourceImageHash = sourceImageHash;
	}

	/**
	 * Gets the database image file name.
	 * Appending this to a XMLDB folder will get you the respective image.
	 * @return
	 */
	public String getImageFileName() {
		return imageFileName;
	}

	public void setImageFileName(String imageName) {
		this.imageFileName = imageName;
	}

	/**
	 * Returns if this image has an associated signature already generated.
	 * 
	 * @return True if image has been generated, false otherwise.
	 */
	public boolean isSignatureGenerated() {
		return signatureGenerated;
	}

	public void setSignatureGenerated(boolean signatureGenerated) {
		this.signatureGenerated = signatureGenerated;
	}

	/**
	 * Gets the full-res file path. Uses source image or one in images depending
	 * on if this image has been moved into the DB.
	 * 
	 * @return path to the full size image
	 */
	public String getSourceFilePath() {
		if (processed == true) {
			return XMLFrogDatabase.getImagesFolder() + imageFileName;
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
	 * Returns if this image has had a thumbnail created in the thumbnail
	 * directory and the full resolution placed in the images/ directory
	 * 
	 * @return true if images are processed into DB, false otherwise
	 */
	public boolean isProcessed() {
		return this.processed;
	}

	public BufferedImage getColorThumbnail() {
		if (colorFileThumbnail == null) {
			createListThumbnail();
		}
		return colorFileThumbnail;
	}

	/**
	 * Sets this image's thumbnail from a buffered image. This is used when a
	 * new frog image is added.
	 * 
	 * @param sourceFileThumbnail
	 */
	public void setColorThumbnail(BufferedImage sourceFileThumbnail) {
		this.colorFileThumbnail = sourceFileThumbnail;
	}

	public Image getGreyScaleThumbnail() {
		return greyScaleThumbnail;
	}

	/**
	 * Loads the greyscale thumbnail into memory if a signature has not yet been
	 * generated
	 */
	public void generateGreyscaleImage() {
		if (isSignatureGenerated()) {
			IdentiFrog.LOGGER.writeError("Image already has signature! should not be greyscale!");
			return;
		}
		ImageFilter filter = new GrayFilter(true, 30);
		ImageProducer producer = new FilteredImageSource(colorFileThumbnail.getSource(), filter);
		greyScaleThumbnail = IdentiFrog.toBufferedImage(Toolkit.getDefaultToolkit().createImage(producer));
	}

	@Override
	public String toString() {
		return "SiteImage [imageFileName=" + imageFileName + ", sourceFilePath=" + sourceFilePath + ", signatureGenerated=" + signatureGenerated
				+ ", processed=" + processed + ", sourceImageHash=" + sourceImageHash + "]";
	}

	/**
	 * Returns the source image as stored in the DB Images/ directory. The
	 * format is as follows: [original base
	 * filename]_[sourcehash].[sourceextension] Only works if processed is set
	 * to false.
	 * 
	 * @return Unique filename, however it returns null if processed is already
	 *         set to true as this method should not be used.
	 */
	public String createUniqueDBFilename() {
		if (processed) {
			IdentiFrog.LOGGER.writeError("Attemped to create unique filename for already processed image!");
			return null;
		}
		return FilenameUtils.getBaseName(sourceFilePath) + "_" + getSourceImageHash() + "." + FilenameUtils.getExtension(sourceFilePath);
	}

	/**
	 * Copies the source image to the images/ folder if it doesn't exist already
	 * and sets the imageFileName. Creates a thumbnail for the thumbnail/
	 * directory. Sets this image status to processed. Does nothing if this is
	 * already done.
	 * 
	 * @param useSignatureThumbnail Setting this to true will use the signature (dorsal) thumbnail, otherwise a thumbnail of the full-res will be generated.
	 */
	public void processImageIntoDB(boolean useSignatureThumbnail) {
		// TODO Auto-generated method stub
		//copy image to Images/ if it doesn't already exist
		IdentiFrog.LOGGER.writeMessage("Preparing to process image into DB: " + this);

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

			if (useSignatureThumbnail) {
				processSignatureThumbnail();
			} else {
				//store thumbnail
				File outputfile = new File(XMLFrogDatabase.getThumbnailFolder() + getImageFileName());
				try {
					ImageIO.write(getColorThumbnail(), "jpg", outputfile);
					IdentiFrog.LOGGER.writeMessage("Copied thumbnail into thumbnail directory.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					IdentiFrog.LOGGER.writeExceptionWithMessage("Failed to save thumbnail to thumbnail/ folder.", e);
				}
			}
			processed = true;
		} else {
			IdentiFrog.LOGGER.writeMessage("Image already processed. Skipping.");
		}
	}

	private void processSignatureThumbnail() {
		IdentiFrog.LOGGER.writeMessage("Preparing to process signature-based thumbnail into DB: " + this);
		if (!signatureGenerated) {
			IdentiFrog.LOGGER.writeError("Cannot update thumbnail, image does not have signature generated");
			return;
		}

		BufferedImage src;
		try {
			src = ImageIO.read(new File(XMLFrogDatabase.getDorsalFolder() + getImageFileName()));
			BufferedImage thumbnail = Scalr.resize(src, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, 200, 150, Scalr.OP_ANTIALIAS);

			//store thumbnail
			File outputfile = new File(XMLFrogDatabase.getThumbnailFolder() + getImageFileName());
			ImageIO.write(thumbnail, "jpg", outputfile);
			IdentiFrog.LOGGER.writeMessage("Copied signature thumbnail into thumbnail directory.");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			IdentiFrog.LOGGER.writeExceptionWithMessage("Failed to write new signature based thumbnail (using dorsal view).", e1);
		}
	}

	/**
	 * Creates a thumbnail for viewing in the left hand side of the FrogEditor
	 * window. Will load greyscale if necessary. Will additionally update the
	 * existing thumbnails.
	 * 
	 * @param image
	 *            SiteImage to create thumbnail for
	 * @return Modified image with loaded thumbnails
	 */
	public void createListThumbnail() {
		IdentiFrog.LOGGER.writeMessage("===Loading image thumbnail " + this);

		BufferedImage src;
		try {
			if (isProcessed()) {
				IdentiFrog.LOGGER.writeMessage("Reading thumbnail file: " + XMLFrogDatabase.getThumbnailFolder() + getImageFileName());
				src = ImageIO.read(new File(XMLFrogDatabase.getThumbnailFolder() + getImageFileName()));
			} else {
				IdentiFrog.LOGGER.writeMessage("Generating thumbnail from full size image: " + getSourceFilePath());
				src = ImageIO.read(new File(getSourceFilePath()));
			}
			BufferedImage thumbnail = Scalr.resize(src, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, 100, 75, Scalr.OP_ANTIALIAS);
			setColorThumbnail(thumbnail);
			if (!isSignatureGenerated()) {
				IdentiFrog.LOGGER.writeMessage("Generating greyscale thumbnail.");
				generateGreyscaleImage();
			} else {
				greyScaleThumbnail = null; //clear references
			}
			IdentiFrog.LOGGER.writeMessage("===Loaded thumbnail===");
		} catch (IOException e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("Unable to generate thumbnail for image (in memory): " + toString(), e);
		}
	}
	
	/**
	 * Returns the original filename that was passed before processing into the DB + .jpg (even though entry may not be jpg...)
	 * @return Original filename
	 */
	public String getOriginalFilename() {
		String filename = getImageFileName();
		String extension = FilenameUtils.getExtension(filename);
		String hash = getSourceImageHash();
		
		String base = FilenameUtils.getBaseName(filename);
		base = base.substring(0, base.length()-hash.length()-1); //-1 for the _
		base = base+"."+extension;
		return base;
	}

	/**
	 * Gets the filepath of this image's signature
	 * @return Signature file path
	 */
	public String getSignature() {
		return XMLFrogDatabase.getSignaturesFolder() + FilenameUtils.getBaseName(getImageFileName())+IdentiFrog.SIGNATURE_EXTENSION;
	}

	/**
	 * Gets the binary image tied to this image
	 * @return Binary image file path
	 */
	public String getBinary() {
		return XMLFrogDatabase.getBinaryFolder() + FilenameUtils.getBaseName(getImageFileName())+IdentiFrog.BINARY_EXTENSION;

	}
}
