package gov.usgs.identifrog.Frames;

import gov.usgs.identifrog.IdentiFrog;
import gov.usgs.identifrog.DataObjects.GitHubRelease;
import gov.usgs.identifrog.DataObjects.User;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

@SuppressWarnings("serial")
public class UpdateAvailableFrame extends JDialog implements ActionListener, PropertyChangeListener {
	boolean error = false;
	JLabel versionsLabel, changelogLabel, sizeLabel;
	JButton updateButton, manualDownloadButton;
	JProgressBar downloadProgress;
	JComboBox<GitHubRelease> releasesComboBox;
	private JPanel downloadPanel;
	private DefaultComboBoxModel<GitHubRelease> releasesModel;

	public static void main(String[] args) throws MalformedURLException, IOException {
		//Testing of UpdateAvailableFrame
		System.out.println("Getting testing JSON...");
		String json = IOUtils.toString(new URL("https://api.github.com/repos/mjperez-usgs/identifrog/releases"));
		System.out.println("Received testing JSON, starting UI test");
		Object obj = JSONValue.parse(json);
		IdentiFrog.initLogger();
		new UpdateAvailableFrame((JSONArray) obj, null);
	}

	public UpdateAvailableFrame(JSONArray releasesInfo, JFrame callingWindow) {
		setupWindow();
		parseUpdateInfo(releasesInfo);
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		this.setVisible(true);
	}

	/**
	 * Parses the JSON object received from GITHUB
	 * 
	 * @param releasesInfo
	 *            JSON from GITHUB v3 API
	 */
	private void parseUpdateInfo(JSONArray releasesInfo) {
		for (Object obj : releasesInfo) {
			if (obj instanceof JSONObject) {
				GitHubRelease ghb = new GitHubRelease((JSONObject) obj);
				if (ghb.getAttachments().size() > 0) {
					releasesModel.addElement(ghb);
				}
			}
		}
	}

	private void setupWindow() {
		this.setIconImages(IdentiFrog.ICONS);
		this.setTitle("IdentiFrog Version Switcher");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		JPanel updatePanel = new JPanel();
		updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
		updatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		//calculate local hash
		/*
		 * String buildHash = (String) updateInfo.get("build_md5"); boolean
		 * hashMismatch = false; try { String currentHash =
		 * MD5Checksum.getMD5Checksum("IdentiFrog.exe"); if (buildHash != null
		 * && !buildHash.equals("") && !currentHash.equals(buildHash)) { //hash
		 * mismatch hashMismatch = true; } } catch (Exception e) {
		 * //ModManager.debugLogger
		 * .writeErrorWithException("Unable to hash ME3CMM.exe:", e1); }
		 * 
		 * if (hashMismatch && latest_build_number == ModManager.BUILD_NUMBER) {
		 * introLabel.setText("A minor update for Mod Manager is available."); }
		 * else { introLabel.setText(""); }
		 */

		versionsLabel = new JLabel("<html>Current Version: " + IdentiFrog.HR_VERSION + "</html>");
		versionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel serverVersions = new JLabel("Versions available online:");
		serverVersions.setAlignmentX(Component.LEFT_ALIGNMENT);
		releasesComboBox = new JComboBox<GitHubRelease>();
		releasesComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		releasesModel = new DefaultComboBoxModel<GitHubRelease>();
		releasesComboBox.setModel(releasesModel);
		releasesComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedReleaseChanged();
			}
		});

		String release_notes = "Release Notes";
		changelogLabel = new JLabel("<html><div style=\"width:270px;\">" + release_notes + "</div></html>");
		updateButton = new JButton("Install Selected Version");
		updateButton.addActionListener(this);
		manualDownloadButton = new JButton("Manual Download");
		manualDownloadButton.addActionListener(this);

		downloadProgress = new JProgressBar();
		downloadProgress.setStringPainted(true);
		downloadProgress.setIndeterminate(false);
		downloadProgress.setEnabled(false);

		sizeLabel = new JLabel(" "); //space or it won't pack properly
		sizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		//Panel setup
		JPanel versionPanel = new JPanel();
		versionPanel.setLayout(new BoxLayout(versionPanel, BoxLayout.Y_AXIS));
		versionPanel.add(versionsLabel);
		versionPanel.add(serverVersions);
		versionPanel.add(releasesComboBox);
		versionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Available Versions"));

		JPanel changeLogPanel = new JPanel();
		changeLogPanel.setLayout(new BoxLayout(changeLogPanel, BoxLayout.Y_AXIS));
		changeLogPanel.setBorder(new TitledBorder(new EtchedBorder(), "Release Notes"));

		updatePanel.add(versionPanel);

		changeLogPanel.add(changelogLabel);

		updatePanel.add(changeLogPanel);
		updatePanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		actionPanel.add(updateButton);
		actionPanel.add(manualDownloadButton);
		actionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Actions"));
		actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		updatePanel.add(actionPanel);

		downloadPanel = new JPanel();
		downloadPanel.setLayout(new BoxLayout(downloadPanel, BoxLayout.Y_AXIS));
		downloadPanel.add(downloadProgress);
		downloadPanel.add(sizeLabel);
		downloadPanel.setBorder(new TitledBorder(new EtchedBorder(), "Download Progress"));
		downloadPanel.setVisible(false);
		downloadProgress.setAlignmentX(Component.LEFT_ALIGNMENT);
		updatePanel.add(downloadPanel);

		actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		downloadPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		versionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		changeLogPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		updatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.getContentPane().add(updatePanel);
	}

	private void selectedReleaseChanged() {
		GitHubRelease selectedRelease = releasesModel.getElementAt(releasesComboBox.getSelectedIndex());
		changelogLabel.setText(selectedRelease.getFormattedBody());
		pack();
	}

	void setStatusText(String text) {
		sizeLabel.setText(text);
	}

	/**
	 * Update the progress bar's state whenever the progress of download
	 * changes.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("progress")) {
			int progress = (Integer) evt.getNewValue();
			downloadProgress.setValue(progress);
		}
	}

	/**
	 * Execute file download in a background thread and update the progress.
	 * 
	 * @author www.codejava.net
	 *
	 */
	class DownloadTask extends SwingWorker<Void, Void> {
		private static final int BUFFER_SIZE = 4096;
		private String saveFileLocation;
		private String downloadLink;
		private String newBuildNum;

		//private SwingFileDownloadHTTP gui;

		public DownloadTask(String downloadLink, String saveFileLocation, String newBuildNum) {
			this.saveFileLocation = saveFileLocation;
			this.downloadLink = downloadLink;
			this.newBuildNum = newBuildNum;
			setStatusText("Downloading update...");
		}

		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() throws Exception {
			//Download the update
			try {

				//Download update
				HTTPDownloadUtil util = new HTTPDownloadUtil();
				util.downloadFile(downloadLink);

				// set file information on the GUI

				InputStream inputStream = util.getInputStream();
				// opens an output stream to save into file
				FileOutputStream outputStream = new FileOutputStream(saveFileLocation);

				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead = -1;
				long totalBytesRead = 0;
				int percentCompleted = 0;
				long fileSize = util.getContentLength();

				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
					totalBytesRead += bytesRead;
					percentCompleted = (int) (totalBytesRead * 100 / fileSize);

					setProgress(percentCompleted);
				}

				outputStream.close();

				util.disconnect();

				if (!buildWindowsUpdateScript(newBuildNum)) {
					error = true;
					cancel(true);
				}
				if (!extractUpdate()) {
					error = true;
					cancel(true);
				}
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(UpdateAvailableFrame.this, "Error downloading file: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
				setProgress(0);
				error = true;
				cancel(true);
			}
			return null;
		}

		private boolean extractUpdate() {
			String source = IdentiFrog.getUpdateFileLocation();
			String destination = IdentiFrog.getUpdateFileExtractedDestination();
			try {
				ZipFile zipFile = new ZipFile(source);
				zipFile.extractAll(destination);
				return true;
			} catch (ZipException e) {
				IdentiFrog.LOGGER.writeException(e);
				return false;
			}
		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			//TODO: Install update through the update script
			if (!error) {
				runUpdateScript(newBuildNum);
			} else {
				JOptionPane.showMessageDialog(UpdateAvailableFrame.this, "The update was unable to be extracted.\nThe log file will indicate why.", "Update Error", JOptionPane.ERROR_MESSAGE);
				dispose();
			}
		}
	}

	/**
	 * A utility that downloads a file from a URL.
	 *
	 * @author www.codejava.net
	 *
	 */
	class HTTPDownloadUtil {

		private HttpURLConnection httpConn;

		/**
		 * hold input stream of HttpURLConnection
		 */
		private InputStream inputStream;

		private String fileName;
		private int contentLength;

		/**
		 * Downloads a file from a URL
		 *
		 * @param fileURL
		 *            HTTP URL of the file to be downloaded
		 * @throws IOException
		 */
		public void downloadFile(String fileURL) throws IOException {
			URL url = new URL(fileURL);
			httpConn = (HttpURLConnection) url.openConnection();
			int responseCode = httpConn.getResponseCode();

			// always check HTTP response code first
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String disposition = httpConn.getHeaderField("Content-Disposition");
				String contentType = httpConn.getContentType();
				contentLength = httpConn.getContentLength();

				if (disposition != null) {
					// extracts file name from header field
					int index = disposition.indexOf("filename=");
					if (index > 0) {
						fileName = disposition.substring(index + 10, disposition.length() - 1);
					}
				} else {
					// extracts file name from URL
					fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
				}

				// opens input stream from the HTTP connection
				inputStream = httpConn.getInputStream();

			} else {
				throw new IOException("No file to download. Server replied HTTP code: " + responseCode);

			}
		}

		public void disconnect() throws IOException {
			inputStream.close();
			httpConn.disconnect();
		}

		public String getFileName() {
			return this.fileName;
		}

		public int getContentLength() {
			return this.contentLength;
		}

		public InputStream getInputStream() {
			return this.inputStream;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == updateButton) {
			updateButton.setEnabled(false);
			manualDownloadButton.setEnabled(false);
			downloadPanel.setVisible(true);
			pack();

			GitHubRelease downloadRelease = releasesModel.getElementAt(releasesComboBox.getSelectedIndex());

			DownloadTask task = new DownloadTask(downloadRelease.getAttachments().get(0).getDownloadURL(), IdentiFrog.getUpdateFileLocation(),
					downloadRelease.getName());
			task.addPropertyChangeListener(this);
			task.execute();
		}
	}

	/**
	 * Shuts down Mod Manager and runs the update script
	 */
	public void runUpdateScript(String newBuildNum) {
		String[] command = { "cmd.exe", "/c", "start", "cmd.exe", "/c", IdentiFrog.getUpdaterScript() };
		try {
			ProcessBuilder pb = new ProcessBuilder(command);
			IdentiFrog.LOGGER.writeMessage("Running upgrade script: " + pb.command());
			pb.start();
			IdentiFrog.LOGGER.writeMessage("Upgrading to build " + newBuildNum + ", shutting down.");
			System.exit(0);
		} catch (IOException e) {
			IdentiFrog.LOGGER.writeExceptionWithMessage("FAILED TO RUN AUTO UPDATER:", e);
		}
	}

	/**
	 * Builds the update script (.cmd) to run when swapping files. Only used for
	 * MSWindows.
	 * 
	 * @return True if created, false otherwise.
	 */
	private boolean buildWindowsUpdateScript(String newBuildNum) {
		StringBuilder sb = new StringBuilder();
		sb.append("::Update script built by IdentiFrog Build " + IdentiFrog.INT_VERSION);
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("@echo off");
		sb.append("\r\n");
		sb.append("setlocal");
		sb.append("\r\n");
		sb.append("echo Switching versions of IdentiFrog");
		sb.append("\r\n");
		sb.append("::Wait for 2 seconds so the JVM fully exits.");
		sb.append("\r\n");
		sb.append("PING 1.1.1.1 -n 1 -w 2000 >NUL");
		sb.append("\r\n");
		sb.append("::Update the files");
		sb.append("\r\n");
		sb.append("xcopy /Y /S \"" + IdentiFrog.getUpdateFileExtractedDestination() + "\" \"" + System.getProperty("user.dir") + "\"");
		sb.append("\r\n");
		sb.append("::Run IdentiFrog");
		sb.append("\r\n");
		sb.append("\"" + System.getProperty("user.dir") + File.separator + "IdentiFrog.exe\" --update-from ");
		sb.append(IdentiFrog.HR_VERSION);
		sb.append("\r\n");
		sb.append("endlocal");
		sb.append("\r\n");
		sb.append("(goto) 2>nul & rmdir /S /Q \""+IdentiFrog.getUpdatesFolder()+"\"");
		try {
			String updatePath = new File(IdentiFrog.getUpdaterScript()).getAbsolutePath();
			Files.write(Paths.get(updatePath), sb.toString().getBytes(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			IdentiFrog.LOGGER.writeMessage("Couldn't generate the update script. Must abort.");
			/*
			 * JOptionPane.showMessageDialog(UpdateAvailableWindow.this,
			 * "Error building update script: " +
			 * e.getClass()+"\nCannot continue.", "Updater Error",
			 * JOptionPane.ERROR_MESSAGE);
			 */
			return false;
		}
		return true;
	}
}
