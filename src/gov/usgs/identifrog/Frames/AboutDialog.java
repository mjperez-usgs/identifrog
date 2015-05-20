package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.MainFrame;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * <p>
 * Title: AboutDialog.java
 * <p>
 * Description: Describes IdentiFrog
 * 
 * @author Oksana V. Kelly 2008
 * @author Steven P. Miller for IdentiFrog Team 2005
 */

@SuppressWarnings("serial")
public class AboutDialog extends JDialog implements ActionListener {

	JPanel panel1 = new JPanel();
	JPanel panel2 = new JPanel();
	JPanel insetsPanel1 = new JPanel();
	JPanel insetsPanel2 = new JPanel();
	JPanel insetsPanel3 = new JPanel();
	JButton bOK = new JButton();
	JLabel imageLabel = new JLabel(new ImageIcon(MainFrame.class.getResource("SplashScreen.png")));
	JLabel label1 = new JLabel("IdentiFrog is a frog identification system with an");
	JLabel label2 = new JLabel("Application to the Northern Leopard Frog");
	JLabel label3 = new JLabel("(Lithobates pipiens)");
	JLabel label4 = new JLabel("2012-08-05 IF Beta 0.11");

	BorderLayout borderLayout1 = new BorderLayout();
	BorderLayout borderLayout2 = new BorderLayout();
	FlowLayout flowLayout1 = new FlowLayout(); // @jve:decl-index=0:
	GridLayout gridLayout1 = new GridLayout();

	AboutDialog() {
		this(null);
	}

	public AboutDialog(Frame parent) {
		super(parent);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			init();
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeException(e);
		}
	}

	/**
	 * Component Initialization
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		Color titlecolor = new Color(80, 0, 120);
		setTitle("About IdentiFrog Beta");
		panel1.setLayout(borderLayout1);
		panel2.setLayout(borderLayout2);
		insetsPanel1.setLayout(flowLayout1);
		insetsPanel2.setLayout(flowLayout1);
		insetsPanel3.setMaximumSize(new Dimension(350, 90));
		insetsPanel3.setMinimumSize(new Dimension(350, 90));
		insetsPanel3.setPreferredSize(new Dimension(350, 90));
		insetsPanel3.setBounds(0, 0, 150, 100);
		gridLayout1.setRows(2);
		gridLayout1.setColumns(1);
		label1.setBounds(10, 10, 350, 15);
		label1.setFont(new Font("MS Sans Serif", Font.BOLD, 12));
		label1.setForeground(titlecolor);
		label2.setBounds(50, 25, 350, 15);
		label2.setFont(new Font("MS Sans Serif", Font.BOLD, 12));
		label2.setForeground(titlecolor);
		label3.setBounds(120, 40, 350, 15);
		label3.setFont(new Font("MS Sans Serif", Font.ITALIC, 12));
		label3.setForeground(titlecolor);
    label4.setBounds(140, 40, 350, 15);
    label4.setFont(new Font("MS Sans Serif", Font.ITALIC, 11));
    label4.setForeground(titlecolor);
		bOK.setText("OK");
		bOK.addActionListener(this);
		insetsPanel2.add(imageLabel, null);
		panel2.add(insetsPanel2, BorderLayout.CENTER);
		getContentPane().add(panel1, null);
		insetsPanel3.add(label1, null);
		insetsPanel3.add(label2, null);
		insetsPanel3.add(label3, null);
		insetsPanel3.add(label4, null);
		panel2.add(insetsPanel3, BorderLayout.NORTH);
		insetsPanel1.add(bOK, null);
		panel1.add(panel2, BorderLayout.CENTER);
		panel1.add(insetsPanel1, BorderLayout.SOUTH);
		setResizable(false);
	}

	// overridden so we can exit when window is closed
	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			dispose();
		}
		super.processWindowEvent(e);
	}

	// close the dialog on a button event
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bOK) {
			dispose();
		}
	}
}
