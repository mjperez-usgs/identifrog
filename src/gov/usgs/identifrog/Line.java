package gov.usgs.identifrog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * <p>
 * Title: Line.java
 * </p>
 * <p>
 * Description: holds information about the center line
 * </p>
 * <p>
 * This software is released into the public domain.
 * </p>
 * 
 * @author Steven P. Miller from <b>IdentiFrog</b> <i>2005</i>
 */

public class Line {
	private double x_1;
	private double y_1;
	private double x_2;
	private double y_2;

	public Line(double x1, double y1, double x2, double y2) {
		x_1 = x1;
		y_1 = y1;
		x_2 = x2;
		y_2 = y2;
	}

	public void drawLine(Graphics2D g2d, Color color, BasicStroke bs) {
		g2d.setStroke(bs);
		g2d.setColor(color);
		g2d.drawLine((int) x_1, (int) y_1, (int) x_2, (int) y_2);
	}

	public double getLength() {
		double a = y_1 - y_2;
		double b = x_1 - x_2;
		return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
	}

	public double getX_1() {
		return x_1;
	}

	public double getX_2() {
		return x_2;
	}

	public double getY_1() {
		return y_1;
	}

	public double getY_2() {
		return y_2;
	}

	public void setLineProps(double x1, double y1, double x2, double y2) {
		x_1 = x1;
		y_1 = y1;
		x_2 = x2;
		y_2 = y2;
	}

	public void setX_1(double x1) {
		x_1 = x1;
	}

	public void setX_2(double x2) {
		x_2 = x2;
	}

	public void setY_1(double y1) {
		y_1 = y1;
	}

	public void setY_2(double y2) {
		y_2 = y2;
	}

}
