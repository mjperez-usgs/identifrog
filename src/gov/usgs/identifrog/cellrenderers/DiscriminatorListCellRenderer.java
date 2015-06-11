package gov.usgs.identifrog.cellrenderers;

import gov.usgs.identifrog.DataObjects.Discriminator;
import gov.usgs.identifrog.DataObjects.User;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class DiscriminatorListCellRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof Discriminator) {
			Discriminator disc = (Discriminator) value;
			super.getListCellRendererComponent(list, value, index, disc.isInUse() ? false : isSelected, cellHasFocus);
			setText(disc.getText());
		}
		return this;
	}
}