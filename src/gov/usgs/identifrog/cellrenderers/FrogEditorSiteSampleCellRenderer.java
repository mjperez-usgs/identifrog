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
			String text = "";
			if (sample.getDateCapture() == null && sample.getSurveyID() == null) {
				text += "null..";
			} else {
				text += sample.getDateCapture()+": "+sample.getSurveyID();
			}
			setText(text);
		}
		return this;
	}
}