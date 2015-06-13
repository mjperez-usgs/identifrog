package gov.usgs.identifrog;

import java.awt.Dimension;

	import javax.swing.JFrame;
	import javax.swing.JList;
	
	public class Test {
		public static void main(String[] args) {
			// TODO Auto-generated method stub
			JFrame frame = new JFrame();
			frame.setMinimumSize(new Dimension(400,400));
			String[] items = new String[]{"ITEM1","ITEM2","ITEM3","ITEM4"};
			JList<String> list = new JList<String>(items);
			list.setVisibleRowCount(-1);
			list.setLayoutOrientation( JList.HORIZONTAL_WRAP );
			frame.add(list);
			frame.setVisible(true);		
		}
	}
