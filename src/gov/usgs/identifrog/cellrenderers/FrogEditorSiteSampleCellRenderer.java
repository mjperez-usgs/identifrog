package gov.usgs.identifrog.cellrenderers;

import gov.usgs.identifrog.DataObjects.SiteSample;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class FrogEditorSiteSampleCellRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		//super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof SiteSample) {
			SiteSample sample = (SiteSample) value;
			setText(sample.getDateCapture()+" "+sample.getSurveyID());
		}
		return this;
	}
}