package gov.usgs.identifrog.cellrenderers;

import gov.usgs.identifrog.interfaces.InUseFlag;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class WrappedStringListCellRenderer extends DefaultListCellRenderer {
   public static final String HTML_1 = "<html><body style='width: ";
   public static final String HTML_2 = "px'>";
   public static final String HTML_3 = "</html>";
   private int width;

   public WrappedStringListCellRenderer(int width) {
      this.width = width;
   }

   @Override
   public Component getListCellRendererComponent(JList list, Object value,
         int index, boolean isSelected, boolean cellHasFocus) {
      String text = HTML_1 + String.valueOf(width) + HTML_2 + value.toString()
            + HTML_3;
      if (value instanceof InUseFlag) {
    	  InUseFlag iuf = (InUseFlag) value;
    	  //get in use. if in use its not selected.
    	  boolean isInUse = iuf.isInUse();
    	  JLabel element = (JLabel) super.getListCellRendererComponent(list, text, index, isInUse ? false : isSelected,
  	            cellHasFocus);
    	  if (isInUse){
    		  element.setForeground( Color.GRAY );
    	  }
    	  return element;
      }
      return super.getListCellRendererComponent(list, text, index, isSelected,
            cellHasFocus);
   }
}
