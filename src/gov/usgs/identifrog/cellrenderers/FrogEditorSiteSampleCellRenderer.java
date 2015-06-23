package gov.usgs.identifrog.cellrenderers;

import gov.usgs.identifrog.DataObjects.SiteSample;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class FrogEditorSiteSampleCellRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus); //null - don't run toString()
		if (value instanceof SiteSample) {
			SiteSample sample = (SiteSample) value;
			if (sample.getDateCapture() == null && sample.getSurveyID() == null) {
				setText("New Survey");
			} else {
				setText(sample.getDateCapture()+" "+sample.getSurveyID());
			}
		}
		return this;
	}
}