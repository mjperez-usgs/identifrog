/**
 * This sample code is made available as part of the book "Digital Image
 * Processing - An Algorithmic Introduction using Java" by Wilhelm Burger
 * and Mark J. Burge, Copyright (C) <i>2005</i>-2008 Springer-Verlag Berlin,
 * Heidelberg, New York.
 * Note that this code comes with absolutely no warranty of any kind.
 * See http://www.imagingbook.com for details and licensing conditions.
 * This software is released under the terms of the GNU Lesser General Public License (LGPL).
 * Date: 2007/11/10
 */

package gov.usgs.identifrog;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;

public class BinMorpher {

	float[][] se; // structuring element
	boolean ground = false;

	// constructor methods

	BinMorpher() {
	}

	public BinMorpher(float[][] structuringElement) {
		se = structuringElement.clone();
	}

	public void makeDisk(float radius) {
		int r = (int) Math.rint(radius);
		if (r <= 1) {
			r = 1;
		}
		int size = r + r + 1;
		se = new float[size][size];
		double r2 = radius * radius;

		for (int v = -r; v <= r; v++) {
			for (int u = -r; u <= r; u++) {
				if (u * u + v * v <= r2) {
					se[v + r][u + r] = 1;
				}
			}
		}
	}

	// morphology methods

	public BufferedImage dilate(BufferedImage ip, float disksize) {
		Kernel kernel;
		makeDisk(disksize);

		BufferedImage dilatedImg = new BufferedImage(ip.getWidth(), ip.getHeight(), ip.getType());
		if (disksize == 0.0) {
			return null;
		}
		// IdentiFrog.LOGGER.writeMessage("ground " + ground);
		// convert H to 1D array
		if (!ground) { // dilation applied to foreground
			float[] filter = new float[se.length * se[0].length];
			int i = 0;
			for (int col = 0; col < se[0].length; ++col) {
				for (int row = 0; row < se.length; ++row) {
					filter[i] = se[row][col];
					++i;
				}
			}
			kernel = new Kernel(se[0].length, se.length, filter);

		} else { // dilation applied to background
			float[][] fse = new float[se.length][se[0].length];
			fse = reflect(se);

			float[] filter = new float[fse.length * fse[0].length];
			int i = 0;
			for (int col = 0; col < fse[0].length; ++col) {
				for (int row = 0; row < fse.length; ++row) {
					filter[i] = fse[row][col];
					++i;
				}
			}
			kernel = new Kernel(fse[0].length, fse.length, filter);
		}

		ConvolveOp op = new ConvolveOp(kernel);
		op.filter(ip, dilatedImg);
		return dilatedImg;
	}

	public BufferedImage erode(BufferedImage ip, float disksize) {

		BufferedImage invertedImg = new BufferedImage(ip.getWidth(), ip.getHeight(), ip.getType());
		BufferedImage dilatedImg = new BufferedImage(ip.getWidth(), ip.getHeight(), ip.getType());
		BufferedImage erodedImg = new BufferedImage(ip.getWidth(), ip.getHeight(), ip.getType());

		// dilates the background
		invertedImg = invert(ip);
		ground = true;
		dilatedImg = dilate(invertedImg, disksize);
		erodedImg = invert(dilatedImg);
		ground = false;
		return erodedImg;
	}

	public BufferedImage open(BufferedImage ip, float disksize) {

		BufferedImage openImg = new BufferedImage(ip.getWidth(), ip.getHeight(), ip.getType());
		BufferedImage erodedImg = new BufferedImage(ip.getWidth(), ip.getHeight(), ip.getType());

		erodedImg = erode(ip, disksize);
		ground = false;
		openImg = dilate(erodedImg, disksize);
		return openImg;
	}

	public BufferedImage close(BufferedImage ip, float disksize) {

		BufferedImage closeImg = new BufferedImage(ip.getWidth(), ip.getHeight(), ip.getType());
		BufferedImage dilatedImg = new BufferedImage(ip.getWidth(), ip.getHeight(), ip.getType());

		dilatedImg = dilate(ip, disksize);
		closeImg = erode(dilatedImg, disksize);
		return closeImg;
	}

	public BufferedImage invert(BufferedImage ip) {
		BufferedImage invertedImg = new BufferedImage(ip.getWidth(), ip.getHeight(), ip.getType());

		short[] invert = new short[256];
		for (int i = 0; i < 256; i++) {
			invert[i] = (short) (255 - i);
		}

		BufferedImageOp invertOp = new LookupOp(new ShortLookupTable(0, invert), null);
		invertOp.filter(ip, invertedImg);

		return invertedImg;
	}

	float[][] reflect(float[][] se) {
		// mirrors the structuring element around the center (hot spot)
		// used to implement erosion by a dilation
		int N = se.length; // number of rows
		int M = se[0].length; // number of columns
		float[][] fse = new float[N][M];
		for (int j = 0; j < N; j++) {
			for (int i = 0; i < M; i++) {
				fse[j][i] = se[N - j - 1][M - i - 1];
			}
		}
		return fse;
	}

}
