package gov.usgs.identifrog.cellrenderers;

import gov.usgs.identifrog.DataObjects.Template;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class TemplateListCellRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof Template) {
			Template template = (Template) value;
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			setText(template.getName());
		}
		return this;
	}
}