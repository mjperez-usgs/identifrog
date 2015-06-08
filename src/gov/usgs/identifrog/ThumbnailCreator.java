package gov.usgs.identifrog;

import gov.usgs.identifrog.Handlers.XMLFrogDatabase;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * <p>
 * Title: ThumbnailCreator.java
 * <p>
 * Description: Takes an image file and reduces it in size by a given dimension, keeping aspects
 * intact.
 * 
 * @author Oksana V. Kelly 2008
 * @author Steven P. Miller for IdentiFrog Team 2005
 */
public class ThumbnailCreator {
/*	private BufferedImage image;
	private Rectangle dimensions;
	private File thumbnailFolder;

	*//**
	 * Default Constructor
	 *//*
	public ThumbnailCreator() {
		thumbnailFolder = new File(XMLFrogDatabase.getThumbnailFolder());
	}

	*//**
	 * Overloaded Constructor: Creates a thumbnail folder (if one doesn't already exist)
	 * 
	 * @param thumbnailFolder
	 *//*
	public ThumbnailCreator(File thumbnailFolder) {
		if (!thumbnailFolder.isDirectory()) {
			thumbnailFolder.mkdir();
		}
		this.thumbnailFolder = thumbnailFolder;
	}

	*//**
	 * @param file
	 * @throws IOException
	 *//*
	private void importFile(File file) throws IOException {
		Image tempImage = ImageIO.read(file);
		BufferedImage newImage = new BufferedImage(tempImage.getWidth(null), tempImage.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);// TYPE_INT_RGB
		Graphics2D gc = newImage.createGraphics();
		gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gc.drawImage(tempImage, 0, 0, null);
		image = newImage;
		gc.dispose();
	}

	*//**
	 * createThumbnail() Creates a thumbnail of the input image
	 * 
	 * @param imageFile
	 * @param thumbMaxWidth
	 * @param thumbMaxHeight
	 * @param outputFile
	 * @param forceOverwrite
	 * @return
	 *//*
	public File createThumbnail(File imageFile, int thumbMaxWidth, int thumbMaxHeight, File outputFile, boolean forceOverwrite) {
		try {
			importFile(imageFile);
			return createThumbnail(image, thumbMaxWidth, thumbMaxHeight, outputFile, forceOverwrite);
		} catch (IOException e) {
			IdentiFrog.LOGGER.writeMessage("ThumbnailCreator.createThumbnail() IOException");
			IdentiFrog.LOGGER.writeException(e);
			return null;
		}
	}

	*//**
	 * @param imageFile
	 * @param thumbMaxWidth
	 * @param thumbMaxHeight
	 * @param forceOverwrite
	 * @return
	 *//*
	public File createThumbnail(File imageFile, int thumbMaxWidth, int thumbMaxHeight, boolean forceOverwrite) {
		try {
			importFile(imageFile);
			File outFile;
			if (thumbnailFolder != null) {
				if (imageFile.getParent() != null) {
					outFile = new File(thumbnailFolder.getAbsolutePath() + File.separator + "tn_" + imageFile.getName());
				} else {
					outFile = new File("tn_" + imageFile.getName());
				}
			} else {// thumbDir is null
				if (imageFile.getParent() != null) {
					outFile = new File(imageFile.getParent() + File.separator + "Temp" + File.separator + "tn_" + imageFile.getName());
				} else {
					outFile = new File("tn_" + imageFile.getName());
				}
			}
			return createThumbnail(image, thumbMaxWidth, thumbMaxHeight, outFile, forceOverwrite);
		} catch (IOException ex) {
			return null;
		}
	}

	public File createThumbnail(BufferedImage image, int thumbMaxWidth, int thumbMaxHeight, File outFile, boolean forceOverwrite) {
		dimensions = new Rectangle();
		dimensions.setBounds(0, 0, thumbMaxWidth, thumbMaxHeight);
		this.image = image;
		shrinkImage();
		if (exportImage(outFile, forceOverwrite)) {
			return outFile;
		} else {
			return null;
		}
	}

	public File createThumbnail(BufferedImage image, File imageFile, int thumbMaxWidth, int thumbMaxHeight, boolean forceOverwrite) {
		dimensions = new Rectangle();
		dimensions.setBounds(0, 0, thumbMaxWidth, thumbMaxHeight);
		this.image = image;
		String str = imageFile.getName();
		String name = str.substring(0, (str.length() - 4)) + ".png";
		File outFile = new File(thumbnailFolder.getAbsolutePath() + File.separator + "tn_" + name);
		shrinkImage();
		if (exportImage(outFile, forceOverwrite)) {
			return outFile;
		} else {
			return null;
		}
	}

	public File createThumbnailforEntry(File imageFile, int thumbMaxWidth, int thumbMaxHeight, File outFile, boolean forceOverwrite) {
		try {
			BufferedImage tempImage = ImageIO.read(imageFile);
			int w = tempImage.getWidth();
			int h = tempImage.getHeight();
			BufferedImage smallImg = new BufferedImage(thumbMaxWidth, thumbMaxHeight, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D gc = smallImg.createGraphics();
			gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			gc.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			double ratioHsize = Double.parseDouble(Integer.toString(thumbMaxWidth)) / w * h;
			gc.drawImage(tempImage, 0, 0, thumbMaxWidth, (int) ratioHsize, 0, 0, w, h, null);
			gc.dispose();
			try {
				ImageIO.write(smallImg, "jpg", outFile);
			} catch (IOException e) {
				IdentiFrog.LOGGER.writeException(e);
				return null;
			}
		} catch (IOException e) {
			IdentiFrog.LOGGER.writeException(e);
			return null;
		}
		// garbage collector
		System.gc();
		return outFile;
	}

	private boolean exportImage(File newImageFile, boolean forceOverwrite) {
		if (newImageFile.exists()) {
			if (!forceOverwrite && ChoiceDialog.choiceMessage("The file'" + newImageFile.getName() + "' already exists.\nAre you sure you want to continue?") != 0) {
				return false;
			}
		}
		try {
			ImageIO.write(image, "png", newImageFile);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	private void shrinkImage() {
		double width = image.getWidth();
		double height = image.getHeight();
		if (width > dimensions.getWidth() && height > dimensions.getHeight()) {
			double origWidth = width;
			width = dimensions.getWidth();
			double ratioW = width / origWidth;
			double origHeight = height;
			height = dimensions.getHeight();
			double ratioH = height / origHeight;
			if (ratioW < ratioH) {
				scaleImage(ratioW, ratioW, width, height);
			} else {
				scaleImage(ratioH, ratioH, width, height);
			}
		}
	}

	private void scaleImage(double rw, double rh, double width, double height) {
		if (image == null) {
			return;
		}
		AffineTransform transform = AffineTransform.getScaleInstance(rw, rh);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
		Filter(op, (int) width, (int) height);
	}

	private void Filter(BufferedImageOp op, int width, int height) {
		if (image == null) {
			return;
		}
		BufferedImage filteredImage = new BufferedImage(width, height, image.getType());
		op.filter(image, filteredImage);
		image = filteredImage.getSubimage(0, 0, width, height);
		// image = op.filter(image, null); bad
	}*/
}
