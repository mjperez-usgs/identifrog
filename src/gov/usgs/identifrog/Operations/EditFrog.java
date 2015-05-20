package gov.usgs.identifrog.Operations;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.MainFrame;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.Frames.ErrorDialog;

/**
 * <p>
 * Title: DBeditfrog.java
 * <p>
 * Description: Edits selected row in the frog table.
 * 
 * @author Oksana V. Kelly 2008
 */
@SuppressWarnings("serial")
public class EditFrog extends JDialog {
	private JLabel jLabel1 = new JLabel("Gender");
	private JLabel jLabel2 = new JLabel("Species");
	private JLabel jLabel3 = new JLabel("Weight");
	private JLabel jLabel4 = new JLabel("SUL");
	private JLabel jLabel5 = new JLabel("Date");
	// frog_id
	private JLabel jLabel6 = new JLabel("Frog ID");

	private JTextField jTextField1 = new JTextField("", 3);
	private JTextField jTextField2 = new JTextField("", 10);
	private JTextField jTextField3 = new JTextField("", 5);
	private JTextField jTextField4 = new JTextField("", 5);
	private JTextField jTextField5 = new JTextField("", 8);
	// frog_id
	private JTextField jTextField6 = new JTextField("", 10);

	private JButton bUpdate = new JButton("Update Frog", new ImageIcon(MainFrame.class.getResource("IconSave32.png")));
	private JButton bCancel = new JButton("Cancel", new ImageIcon(MainFrame.class.getResource("IconCancel32.png")));

	private Frog frog;
	@SuppressWarnings("unused")
	private MainFrame parent;
	private JPanel jPanel = new JPanel();

	public EditFrog() {
	}

	public EditFrog(MainFrame frame, String title, boolean modal, Frog frog) {
		super(frame, title, modal);

		parent = frame;
		this.frog = frog;

		try {
			init();
		} catch (Exception e) {
			IdentiFrog.LOGGER.writeMessage("DBEditFrog.DBEditFrog()");
			IdentiFrog.LOGGER.writeMessage(e.getMessage());
		}
	}

	public Frog getFrog() {
		return frog;
	}

	public void setFrog(Frog frog) {
		this.frog = frog;
	}

	private void init() throws Exception {
		jTextField1.setText(frog.getGender());
		jTextField2.setText(frog.getSpecies());
		jTextField3.setText(frog.getMass());
		jTextField4.setText(frog.getLength());
		jTextField5.setText(frog.getDateCapture());
		jTextField6.setText(frog.getID());

		bUpdate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bUpdate_actionPerformed(e);
			}
		});

		bCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bCancel_actionPerformed(e);
			}
		});

		jPanel.add(jLabel1, null);
		jPanel.add(jTextField1, null);
		jPanel.add(jLabel2, null);
		jPanel.add(jTextField2, null);
		jPanel.add(jLabel3, null);
		jPanel.add(jTextField3, null);
		jPanel.add(jLabel4, null);
		jPanel.add(jTextField4, null);
		jPanel.add(jLabel5, null);
		jPanel.add(jTextField5, null);
		jPanel.add(jLabel6, null);
		jPanel.add(jTextField6, null);
		jPanel.add(bCancel, null);
		jPanel.add(bUpdate, null);

		setModal(true);
		getContentPane().add(jPanel);
	}

	void bUpdate_actionPerformed(ActionEvent e) {
		if (jTextField1.getText().length() == 0) {
			new ErrorDialog("Please fill in the GENDER field.");
			return;
		}
		if (jTextField2.getText().length() == 0) {
			new ErrorDialog("Please fill in the SPECIES field.");
			return;
		}

		frog.setID(jTextField6.getText());
		frog.setGender(jTextField1.getText().trim());
		frog.setSpecies(jTextField2.getText().trim());
		frog.setMass(jTextField3.getText().trim());
		frog.setLength(jTextField4.getText().trim());
		frog.setDateCapture(jTextField5.getText().trim());

		dispose();
	}

	void bCancel_actionPerformed(ActionEvent e) {
		dispose();
	}
}