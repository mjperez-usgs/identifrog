package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.DialogFileChooser;
import gov.usgs.identifrog.ExtensionFileFilter;
import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.Discriminator;
import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.SiteImage;
import gov.usgs.identifrog.DataObjects.SiteSample;
import gov.usgs.identifrog.DataObjects.User;
import gov.usgs.identifrog.Handlers.XMLFrogDatabase;
import gov.usgs.identifrog.ui.ProgressStatusBar;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SignatureStrengthTestFrame extends JDialog {
	JTextField exportFolderLocation, xlsxImportLocation;
	private ImageIcon imageHeartbeat = new ImageIcon(MainFrame.class.getResource("/resources/IconHeartbeat16.png"));
	private ImageIcon imageGrayHeartbeat = new ImageIcon(MainFrame.class.getResource("/resources/IconGrayHeartbeat16.png"));

	private ProgressStatusBar psb;
	private JButton generateButton, exportBrowseButton, importBrowseButton, importButton;
	private JCheckBox useImagenameFormat;
	private MainFrame parentFrame;
	private TemplateGeneratorWorker tgw;
	private TemplateImportWorker tiw;

	public SignatureStrengthTestFrame(MainFrame parentFrame) {
		this.parentFrame = parentFrame;
		setupUI();
		setLocationRelativeTo(parentFrame);
		setVisible(true);
		
		addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		        if (tgw != null) {
		        	tgw.cancel(true);
		        }
		        if (tiw != null) {
		        	tiw.cancel(true);
		        }
		    }
		});
	}

	private void setupUI() {
		setIconImages(IdentiFrog.ICONS);
		setTitle("Signature Strength Test");
		setModal(true);

		JPanel batchPanel = new JPanel(new GridBagLayout());
		batchPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagConstraints c = new GridBagConstraints();
		Insets topInsets = new Insets(5, 0, 0, 0);

		//Generate
		JLabel generateInfo = new JLabel(
				"<html><div style=\"width:300px;\">A Signature Strength test will test all frogs signatures against every other frog's, as well as other signatures in the same frog."
				+ "The purpose of this tool is that a frog's own signatures should always rank higher for similarity than other frogs.</div></html>");

		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = 2;
		c.weighty = 0;
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		batchPanel.add(generateInfo, c);

		exportFolderLocation = new JTextField();
		exportBrowseButton = new JButton("Select Folder Directory");
		generateButton = new JButton("Generate");
		psb = new ProgressStatusBar();

		exportFolderLocation.setEnabled(false);
		exportFolderLocation.setColumns(30);

		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		c.insets = topInsets;
		batchPanel.add(exportFolderLocation, c);
		c.gridx = 1;
		batchPanel.add(exportBrowseButton, c);

		generateButton.setEnabled(false);
		generateButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				generate();
			}
		});

		c.gridy = 2;
		c.gridx = 0;
		useImagenameFormat = new JCheckBox("Auto fill ID using filename format ID_*.jpg");
		useImagenameFormat.setToolTipText("<html><div style=\"width:300px;\">"
				+ "Signature strength testing maeks sure all frog images for the same frog match to each other better than other frog images do.<br>"
				+ "</div></html>");
		batchPanel.add(useImagenameFormat, c);

		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;
		batchPanel.add(generateButton, c);

		//==========import
		JLabel importInfo = new JLabel(
				"<html><div style=\"width:300px;\">Importing a completed batch template will automatically import images into the database and create new frog entries as specified in the XLSX file.</div></html>");

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		batchPanel.add(importInfo, c);

		xlsxImportLocation = new JTextField();
		importBrowseButton = new JButton("Select Template File");
		importButton = new JButton("Import");

		xlsxImportLocation.setEnabled(false);
		xlsxImportLocation.setColumns(30);

		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 4;
		c.insets = topInsets;
		batchPanel.add(xlsxImportLocation, c);
		c.gridx = 1;
		batchPanel.add(importBrowseButton, c);

		importButton.setEnabled(false);
		importButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				importXLSX();
			}
		});
		c.gridy = 5;
		c.gridx = 1;
		c.anchor = GridBagConstraints.EAST;
		batchPanel.add(importButton, c);

		JPanel contentPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();

		psb.setMessage("");
		psb.setIcon(imageGrayHeartbeat);
		psb.getProgressBar().setVisible(false);

		c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		contentPanel.add(batchPanel, c);

		c.gridy = 50;
		c.anchor = GridBagConstraints.SOUTH;
		c.weighty = 1;
		c.weightx = 1;
		contentPanel.add(psb, c);

		add(contentPanel);
		setMinimumSize(new Dimension(420, 310));
		//setResizable(false);
	}

	private void generate() {
		IdentiFrog.LOGGER.writeMessage("Generating xlsx template");
		psb.setIcon(imageHeartbeat);
		importBrowseButton.setEnabled(false);
		importButton.setEnabled(false);
		exportBrowseButton.setEnabled(false);
		generateButton.setEnabled(false);
		tgw = new TemplateGeneratorWorker(exportFolderLocation.getText(), useImagenameFormat.isSelected(), psb.getStatusLabel(), psb.getProgressBar());
		tgw.execute();
	}

	private void importXLSX() {
		IdentiFrog.LOGGER.writeMessage("Importing xlsx template");
		psb.setIcon(imageHeartbeat);
		importBrowseButton.setEnabled(false);
		importButton.setEnabled(false);
		exportBrowseButton.setEnabled(false);
		generateButton.setEnabled(false);
		tiw = new TemplateImportWorker(xlsxImportLocation.getText(), psb.getStatusLabel(), psb.getProgressBar());
		tiw.execute();
	}

	class TemplateGeneratorWorker extends SwingWorker<Integer, String> {
		private JProgressBar progressBar;
		private JLabel status;
		private String inputFolder;
		private boolean useFilenameFormat;

		public TemplateGeneratorWorker(String inputFolder, boolean useFilenameFormat, JLabel status, JProgressBar progressBar) {
			this.progressBar = progressBar;
			this.useFilenameFormat = useFilenameFormat;
			this.status = status;
			this.inputFolder = inputFolder;

			addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						TemplateGeneratorWorker.this.progressBar.setValue((Integer) evt.getNewValue());
					}
				}
			});

			progressBar.setVisible(true);
			setProgress(0);
		}

		@Override
		protected Integer doInBackground() throws Exception {
			publish("Preprocessing");
			setProgress(1);

			XSSFWorkbook book = new XSSFWorkbook();
			generateDiscriminators(book);
			generateUsers(book);
			generateImages(book);

			// open an OutputStream to save written data into XLSX file
			if (!inputFolder.endsWith(File.separator)) {
				inputFolder = inputFolder + File.separator;
			}
			String filepath = inputFolder + "template.xlsx"; //testing only
			File file = new File(filepath);
			try {
				file.createNewFile();
				FileOutputStream os = new FileOutputStream(new File(filepath));
				book.write(os);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Complete
			publish("File generated");
			setProgress(100);
			return 1;
		}

		@Override
		protected void process(List<String> chunks) {
			// Messages received from the doInBackground() (when invoking the publish() method)
			String latestUpdate = chunks.get(chunks.size() - 1);
			status.setText(latestUpdate);
		}

		private void generateDiscriminators(XSSFWorkbook book) {
			XSSFSheet discriminatorSheet = book.createSheet("Discriminators");

			Map<Integer, Object[]> data = new HashMap<Integer, Object[]>();
			data.put(0, new Object[] { "Discriminator Description" });

			// Set to Iterate and add rows into XLS file
			Set<Integer> newRows = data.keySet();

			// get the last row number to append new data          
			int rownum = discriminatorSheet.getLastRowNum();

			for (Integer key : newRows) {

				// Creating a new Row in existing XLSX sheet
				Row row = discriminatorSheet.createRow(rownum++);
				Object[] rowData = data.get(key);
				int cellnum = 0;
				for (Object obj : rowData) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}

		private void generateUsers(XSSFWorkbook book) {
			XSSFSheet recorderSheet = book.createSheet("Recorders");
			XSSFSheet observerSheet = book.createSheet("Observers");

			Map<Integer, Object[]> data = new HashMap<Integer, Object[]>();
			data.put(0, new Object[] { "First Name", "Last Name" });

			// Set to Iterate and add rows into XLS file
			Set<Integer> newRows = data.keySet();

			// get the last row number to append new data          
			int rownum = recorderSheet.getLastRowNum();

			for (Integer key : newRows) {

				// Creating a new Row in existing XLSX sheet
				Row rrow = recorderSheet.createRow(rownum);
				Row orow = observerSheet.createRow(rownum);
				rownum++;
				Object[] rowData = data.get(key);
				int cellnum = 0;
				for (Object obj : rowData) {

					Cell rcell = rrow.createCell(cellnum);
					Cell ocell = orow.createCell(cellnum);
					cellnum++;
					if (obj instanceof String) {
						rcell.setCellValue((String) obj);
						ocell.setCellValue((String) obj);
					} else if (obj instanceof Boolean) {
						rcell.setCellValue((Boolean) obj);
						ocell.setCellValue((Boolean) obj);
					} else if (obj instanceof Date) {
						rcell.setCellValue((Date) obj);
						ocell.setCellValue((Date) obj);
					} else if (obj instanceof Double) {
						rcell.setCellValue((Double) obj);
						ocell.setCellValue((Double) obj);
					}
				}
			}
		}

		/**
		 * Generates the images sheet
		 * 
		 * @param book
		 */
		private void generateImages(XSSFWorkbook book) {
			XSSFSheet imageSheet = book.createSheet("Images");
			File inputFolderFile = new File(inputFolder);
			String[] EXTENSIONS = new String[] { "gif", "png", "bmp", "jpg" // and other formats you need
			};
			final FilenameFilter IMAGE_FILTER = new FilenameFilter() {

				@Override
				public boolean accept(final File dir, final String name) {
					for (final String ext : EXTENSIONS) {
						if (name.toLowerCase().endsWith("." + ext)) {
							return (true);
						}
					}
					return (false);
				}
			};

			Map<Integer, Object[]> data = new HashMap<Integer, Object[]>();
			data.put(0, new Object[] { "Image Path", "Frog ID" });

			if (inputFolderFile.isDirectory()) { // make sure it's a directory
				int rowNum = 1;
				File[] files = inputFolderFile.listFiles(IMAGE_FILTER);
				int numFiles = files.length;
				for (final File f : files) {
					if (isCancelled()){
						return;
					}
					
					BufferedImage img = null;
					try {
						IdentiFrog.LOGGER.writeMessage("Reading image: " + f.toString());
						publish("Verifying " + FilenameUtils.getName(f.toString()));
						img = ImageIO.read(f); //verifies file can load
						if (useFilenameFormat) {
							data.put(rowNum, new Object[] { f.toString(), getFrogID(f.toString()) });
						} else {
							data.put(rowNum, new Object[] { f.toString() });
						}
						//since rownum started at 1, and we have now completed a "new" image, we can use this as the number of completed
						//as long as we count it before we increment it
						int progress = (int) (((double) rowNum / numFiles) * 100); //love how it only takes ints. also int/int truncates when dividing.
						setProgress(progress);

						rowNum++;
						img = null;
						System.gc();
						// you probably want something more involved here
						// to display in your UI
						//IdentiFrog.LOGGER.writeMessage("image: " + f.getName());
						//IdentiFrog.LOGGER.writeMessage(" width : " + img.getWidth());
						//IdentiFrog.LOGGER.writeMessage(" height: " + img.getHeight());
						//IdentiFrog.LOGGER.writeMessage(" size  : " + f.length());
					} catch (final IOException e) {
						// handle errors here
					}
				}
			}

			// Set to Iterate and add rows into XLS file
			Set<Integer> newRows = data.keySet();

			// get the last row number to append new data          
			int rownum = imageSheet.getLastRowNum();

			for (Integer key : newRows) {

				// Creating a new Row in existing XLSX sheet
				Row row = imageSheet.createRow(rownum++);
				Object[] rowData = data.get(key);
				int cellnum = 0;
				for (Object obj : rowData) {
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
			imageSheet.autoSizeColumn(0);
		}

		/**
		 * Extracts the frog ID from a file path. Requires the filename to match
		 * the following regex: [0-9]+_*.jpg
		 * 
		 * @param string
		 *            filepath
		 * @return frog ID as a string
		 */
		private String getFrogID(String filepath) {
			String base = FilenameUtils.getBaseName(filepath);
			int endIndex = base.indexOf('_');
			if (endIndex > 0) {
				String id = base.substring(0, endIndex);
				try {
					int intid = Integer.parseInt(id); 
					return Integer.toString(intid); //removes leading zeros
				} catch (NumberFormatException e) {
					IdentiFrog.LOGGER.writeError("Invalid frog ID in XLSX file, must be integer: "+id);
					return "";
				}
			}

			return "";
		}

		@Override
		protected void done() {
			//Runs on UI thread
			progressBar.setVisible(false);
			setProgress(0);
			psb.setIcon(imageGrayHeartbeat);
			enableButtons();
		}
	}

	class TemplateImportWorker extends SwingWorker<Integer, String> {
		private JProgressBar progressBar;
		private JLabel status;
		private String inputFile;
		private HashMap<Integer, SiteSample> idFrogMap;

		public TemplateImportWorker(String inputFile, JLabel status, JProgressBar progressBar) {
			this.progressBar = progressBar;
			this.status = status;
			this.inputFile = inputFile;

			addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						TemplateImportWorker.this.progressBar.setValue((Integer) evt.getNewValue());
					}
				}
			});

			progressBar.setVisible(true);
			setProgress(0);
		}

		@Override
		protected Integer doInBackground() throws Exception {
			publish("Preprocessing");
			setProgress(1);
			FileInputStream fis = new FileInputStream(new File(inputFile));

			// Finds the workbook instance for XLSX file
			XSSFWorkbook book = new XSSFWorkbook(fis);
			importDiscriminators(book);
			importUsers(book);
			importImages(book);

			// open an OutputStream to save written data into XLSX file
			/*
			 * String filepath = "C:\\users\\mjperez\\desktop\\output.xlsx";
			 * //testing only File file = new File(filepath); try {
			 * file.createNewFile(); FileOutputStream os = new
			 * FileOutputStream(new File(filepath)); book.write(os); } catch
			 * (IOException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */

			// Complete
			publish("Frogs imported");
			setProgress(100);
			return 1;
		}

		@Override
		protected void process(List<String> chunks) {
			// Messages received from the doInBackground() (when invoking the publish() method)
			String latestUpdate = chunks.get(chunks.size() - 1);
			status.setText(latestUpdate);
		}

		private void importDiscriminators(XSSFWorkbook book) {
			publish("Importing Discriminators");

			XSSFSheet discriminatorSheet = book.getSheet("Discriminators");
			// Get iterator to all the rows in current sheet
			Iterator<Row> rowIterator = discriminatorSheet.iterator();
			rowIterator.next(); //skip first row as it's descriptions
			HashSet<Discriminator> discriminators = new HashSet<Discriminator>();
			// Traversing over each row of XLSX file
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				// For each row, iterate through each columns
				Iterator<Cell> cellIterator = row.cellIterator();
				//ID, Description, we dont use ID in this instance
				Discriminator d = new Discriminator();
				d.setID(XMLFrogDatabase.getNextAvailableDiscriminatorID());
				Cell cell = cellIterator.next();
				d.setText(cell.getStringCellValue());
				discriminators.add(d);
				IdentiFrog.LOGGER.writeMessage("Imported: " + d);
			}
		}

		private void importUsers(XSSFWorkbook book) {
			XSSFSheet recorderSheet = book.getSheet("Recorders");
			XSSFSheet observerSheet = book.getSheet("Observers");

			publish("Importing Users");
			//RECORDERS
			// Get iterator to all the rows in current sheet
			Iterator<Row> rowIterator = recorderSheet.iterator();
			rowIterator.next(); //skip first row as it's descriptions
			HashSet<User> recorders = new HashSet<User>();
			// Traversing over each row of XLSX file
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				// For each row, iterate through each columns
				Iterator<Cell> cellIterator = row.cellIterator();
				//ID, Description
				User recorder = new User();
				recorder.setID(XMLFrogDatabase.getNextAvailableRecorderID());
				Cell cell = cellIterator.next();
				recorder.setFirstName(cell.getStringCellValue());
				cell = cellIterator.next();
				recorder.setLastName(cell.getStringCellValue());

				recorders.add(recorder);
				IdentiFrog.LOGGER.writeMessage("Imported: " + recorder);
			}

			//OBSERVERS
			// Get iterator to all the rows in current sheet
			Iterator<Row> obsIterator = observerSheet.iterator();
			obsIterator.next(); //skip first row as it's descriptions
			HashSet<User> observers = new HashSet<User>();
			// Traversing over each row of XLSX file
			while (obsIterator.hasNext()) {
				Row row = obsIterator.next();
				// For each row, iterate through each columns
				Iterator<Cell> cellIterator = row.cellIterator();
				//ID, Description
				User observer = new User();
				observer.setID(XMLFrogDatabase.getNextAvailableObserverID());
				Cell cell = cellIterator.next();
				observer.setFirstName(cell.getStringCellValue());
				cell = cellIterator.next();
				observer.setLastName(cell.getStringCellValue());
				observers.add(observer);
				IdentiFrog.LOGGER.writeMessage("Imported: " + observer);
			}
		}

		/**
		 * Imports images into the DB
		 * 
		 * @param book
		 */
		private void importImages(XSSFWorkbook book) {
			XSSFSheet imageSheet = book.getSheet("Images");
			//get number of items to do
			Iterator<Row> numberIterator = imageSheet.iterator();
			numberIterator.next(); //skip first row as it's descriptions
			int numToProcess = 0;
			while (numberIterator.hasNext()) {
				numberIterator.next();
				numToProcess++;
			}
			Iterator<Row> imagesIterator = imageSheet.iterator();
			imagesIterator.next(); //skip first row as it's descriptions
			idFrogMap = new HashMap<Integer, SiteSample>();
			//HashSet<SiteImage> observers = new HashSet<SiteImage>();
			// Traversing over each row of XLSX file
			int numProcessed = 0;
			while (imagesIterator.hasNext()) {
				if (isCancelled()) {
					return;
				}
				Row row = imagesIterator.next();
				// For each row, iterate through each columns
				Iterator<Cell> cellIterator = row.cellIterator();
				//Filepath, Frog ID (in the sheet - not in the DB)
				Cell cell = cellIterator.next();
				String filepath = cell.getStringCellValue();
				publish("Importing " + FilenameUtils.getName(filepath));

				cell = cellIterator.next();
				int xlsID = (int) cell.getNumericCellValue();
				SiteSample sample = idFrogMap.get(xlsID);
				if (sample == null) {
					sample = new SiteSample();
					sample.setDateEntry(IdentiFrog.dateFormat.format(new Date()));
				}
				SiteImage img = new SiteImage();
				img.setSourceFilePath(filepath);
				img.processImageIntoDB(false);
				sample.addSiteImage(img);
				idFrogMap.put(xlsID, sample);
				numProcessed++;
				setProgress((int) (((double) numProcessed / numToProcess) * 100));
				//int progress = (int) (((double) rowNum / numFiles) * 100); //love how it only takes ints. also int/int truncates when dividing.

				IdentiFrog.LOGGER.writeMessage("Imported: " + img);
			}

		}

		@Override
		protected void done() {
			//Runs on UI thread
			progressBar.setVisible(false);
			setProgress(0);
			psb.setIcon(imageGrayHeartbeat);
			enableButtons();
			for (Map.Entry<Integer, SiteSample> entry : idFrogMap.entrySet()) {
				int key = entry.getKey();
				SiteSample value = entry.getValue();
				Frog f = new Frog();
				f.setID(XMLFrogDatabase.getNextAvailableFrogID());
				f.addSiteSample(value);
				f.setFreshImport(true);
				XMLFrogDatabase.addFrog(f);
				parentFrame.addFrog(f);
			}
			XMLFrogDatabase.writeXMLFile();
			IdentiFrog.LOGGER.writeMessage("Import complete");
		}
	}

	public void enableButtons() {
		importBrowseButton.setEnabled(true);
		exportBrowseButton.setEnabled(true);

		generateButton.setEnabled(new File(exportFolderLocation.getText()).exists());
		importButton.setEnabled(new File(xlsxImportLocation.getText()).exists());
	}
}
