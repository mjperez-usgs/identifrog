package gov.usgs.identifrog.extendedclasses;

import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;

/**
 * Borrowed from
 * http://stackoverflow.com/questions/17936180/snapping-to-certain-
 * values-in-jslider-possible This allows us to have a JSlider that only accepts
 * specific values
 * 
 * @author mjperez
 *
 */
public class EdgeSteppingSlider extends JSlider {
	private static final long serialVersionUID = -1195270044097152629L;
	private static Integer[] VALUES = { 100,150,200,250,300 };
	private static final Hashtable<Integer, JLabel> LABELS = new Hashtable<>();
	static {
		for (int i = 0; i < VALUES.length; ++i) {
			LABELS.put(i, new JLabel(VALUES[i].toString()));
		}
	}

	public EdgeSteppingSlider() {
		super(0, VALUES.length - 1, 0);
		setLabelTable(LABELS);
		setPaintTicks(true);
		setPaintLabels(false);
		setSnapToTicks(true);
		setMajorTickSpacing(1);
	}

	public int getDomainValue() {
		return VALUES[getValue()];
	}
}
