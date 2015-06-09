package gov.usgs.identifrog;

import gov.usgs.identifrog.Frames.ErrorDialog;

import java.io.File;

/**
 * <p>
 * Title: DigSigThread.java
 * <p>
 * Description: Runs the Digital Signature Creation and reads results from a file
 * 
 * @author Oksana V. Kelly 2008
 * @author Steven P. Miller for IdentiFrog Team 2005
 */

public class DigSigThread extends Thread {
	private File imageFile = null;
	private boolean threadRunning;
	private static int threadCount = 0;

	/**
	 * Default Constructor
	 */
	public DigSigThread() {
		threadRunning = false;
	}

	/**
	 * Overloaded Constructor
	 * 
	 * @param imageFile
	 *            File the name of the image file which will be used to create the digital signature
	 */
	public DigSigThread(File imageFile) {
		this.imageFile = imageFile;
		threadCount++;
		threadRunning = false;
	}

	@Override
	public void run() {
		threadRunning = true;

		// digsig file
		String newFileName = imageFile.getAbsolutePath();
		int endPlace = newFileName.indexOf(".");
		newFileName = newFileName.substring(0, endPlace);
		File textFile = new File(newFileName + ".dsg");

		// binary image file
		String FileName = imageFile.getName();
		int endName = FileName.indexOf(".");
		String binaryImageName = "binaryImg_" + FileName.substring(7, endName) + ".png";
		String binaryImgPath = imageFile.getParent() + File.separator + "BinaryImages" + File.separator + binaryImageName;
		File biImgFile = new File(binaryImgPath);

		// dors_ dorsal image file
		String parentdir = imageFile.getParent();
		String dir = parentdir.substring(0, (parentdir.length() - 11)) + File.separator + "Thumbs" + File.separator;

		String dors = dir + "dors_" + FileName.substring(7, endName) + ".png";
		// tn_ thumb nail file
		String tn = dir + "tn_" + FileName.substring(7, endName) + ".png";

		File dorimage = new File(dors);
		File thumb = new File(tn);

		try {
			int ctr = 0;
			while (!textFile.exists() && !biImgFile.exists() && !dorimage.exists() && !thumb.exists() && ctr < 120) {
				try {
					sleep(500); // ms
				} catch (Exception E) {
				}
				ctr++;
			}
			threadRunning = false;
		} catch (Exception ex) {
			new ErrorDialog("Error trying to open digital signature creator.");
			ex.printStackTrace();
			threadRunning = false;
		}
	} // run()

	public boolean isThreadRunning() {
		return threadRunning;
	}
} // class