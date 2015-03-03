package gov.usgs.identifrog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * <p>
 * Title: FilePlacer.java
 * </p>
 * <p>
 * Description: copies or moves files from one location to another
 * </p>
 * <p>
 * This software is released into the public domain.
 * </p>
 * 
 * @author Steven P. Miller from <b>IdentiFrog</b> <i>2005</i>
 */

public class FilePlacer {
	public FilePlacer() {
	}

	/**
	 * Copies a file without destroying the original file
	 * 
	 * @param in
	 *            File The File to be copied
	 * @param out
	 *            File Where the file is to be copied
	 * @throws Exception
	 */
	public static void copyFile(File in, File out) throws Exception {
		FileInputStream fis = new FileInputStream(in);
		FileOutputStream fos = new FileOutputStream(out);
		byte[] buf = new byte[1024];
		int i = 0;
		while ((i = fis.read(buf)) != -1) {
			fos.write(buf, 0, i);
		}
		fis.close();
		fos.close();
	}// end copy file

	/**
	 * Copies a file and destroys the original file
	 * 
	 * @param in
	 *            File The File to be moved and deleted
	 * @param out
	 *            File Where the file is to be moved
	 * @throws Exception
	 */
	public static void moveFile(File in, File out) throws Exception {
		try {
			copyFile(in, out);
		} catch (Exception ex) {
			throw ex;
		}
		in.delete();
	}
}
