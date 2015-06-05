package gov.usgs.identifrog.cellrenderers;

import gov.usgs.identifrog.DataObjects.User;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class UserListCellRenderer extends DefaultListCellRenderer {
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value instanceof User) {
			User user = (User) value;
			setText(user.getName());
		}
		return this;
	}
}