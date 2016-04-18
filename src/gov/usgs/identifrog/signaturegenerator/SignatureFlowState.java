package gov.usgs.identifrog.signaturegenerator;

import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Represents a step in the signature flow. Values in this object (which are
 * stored in order) are used to recreate the steps that a user has performed, or
 * undo an operation that one no longer wants.
 * 
 * @author mjperez
 *
 */
public class SignatureFlowState {
	ArrayList<Point2D> values;

	public SignatureFlowState(ArrayList<Point2D> stackItems) {
		this.values = stackItems;
	}

	public ArrayList<Point2D> getValues() {
		return values;
	}

	public void setValues(ArrayList<Point2D> values) {
		this.values = values;
	}
}
