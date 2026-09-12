package ua.com.ostrog.photo.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import ua.com.ostrog.photo.logic.Controller;
import ua.com.ostrog.photo.logic.CoreFilesData;
import ua.com.ostrog.photo.report.Report;

/**
 * Content of the "Sort Photos" tab: source and target directory pickers, the
 * "Use exif date of image" setting, and Analyze/Start buttons pinned to the
 * bottom-left/right. Carries no logic of its own - {@link Application}
 * attaches the actual behavior via {@link #addAnalyzeListener} /
 * {@link #addStartListener}.
 */
public class SortPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private String destination = "";
	private String source = "";
	private final SelectFilePanel sourcePanel = new SelectFilePanel();
	private final SelectFilePanel destinationPanel = new SelectFilePanel();
	private final JCheckBox exifDate = new JCheckBox("Use exif date of image", true);
	private final JButton analyze = new JButton("Analyze");
	private final JButton start = new JButton("Start");

	private Controller controller;

	public SortPanel() {
		restoreSavedSettings();
		setSourceDir(source);
		setDestinationDir(destination);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		destinationPanel.setMultiSelectionEnabled(false);
		destinationPanel.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		destinationPanel.setTitleBorder("Target dir");
		add(sourcePanel);
		add(destinationPanel);

		JPanel settingPanel = new JPanel(new GridBagLayout());
		settingPanel.add(exifDate, new GridBagConstraints());
		add(settingPanel);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		buttonsPanel.add(analyze);
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(start);
		buttonsPanel.setBorder(BorderFactory.createTitledBorder(""));
		add(buttonsPanel);
		this.analyze.addActionListener(e -> analyzeFiles());
		this.start.addActionListener(e -> processFiles());

	}

	/**
	 * Pre-selects the source directory, e.g. to restore a previously saved
	 * value on startup. Does nothing if the path does not exist.
	 */
	private void setSourceDir(String path) {
		sourcePanel.setDir(path);
	}

	/**
	 * Pre-selects the target directory, e.g. to restore a previously saved
	 * value on startup. Does nothing if the path does not exist.
	 */
	private void setDestinationDir(String path) {
		destinationPanel.setDir(path);
	}

	/**
	 * @return every file/directory currently selected as a source, or
	 *         {@code null} if nothing has been selected yet
	 */
	private File[] getSourceFiles() {
		return sourcePanel.getSelectedFiles();
	}

	/**
	 * @return the currently selected target directory, or {@code null} if
	 *         nothing has been selected yet
	 */
	private File getDestinationDirectory() {
		return destinationPanel.getDestinationDirectory();
	}

	/**
	 * @return {@code true} if the "Use exif date of image" checkbox is checked
	 */
	private boolean isUseExifDate() {
		return exifDate.isSelected();
	}

	

	/**
	 * @param listener invoked when the Start button is clicked
	 */
	public void addStartListener(ActionListener listener) {
		start.addActionListener(listener);
	}
	private void analyzeFiles() {
		File destinationDir = this.getDestinationDirectory();
		File[] sourceFiles = this.getSourceFiles();
		this.destination = destinationDir.getPath();
		this.source = sourceFiles[0].getPath();
		this.controller.analyze(new CoreFilesData(destinationDir, sourceFiles, isUseExifDate()));
		Report report = new Report();
		report.setEnabledProcessButton(false);
		report.showReport(this.controller.getAnalysisResult());
	}

	private void processFiles() {
		File destinationDir = this.getDestinationDirectory();
		File[] sourceFiles = this.getSourceFiles();
		this.destination = destinationDir.getPath();
		this.source = sourceFiles[0].getPath();
		this.controller.analyze(new CoreFilesData(destinationDir, sourceFiles, isUseExifDate()));
		Report report = new Report();
		report.setController(this.controller);
		report.showReport(this.controller.getAnalysisResult());
	}

    public void setController(Controller controller) {
        this.controller= controller;
    }
	public void writeSettings() {
		Preferences prefs = Preferences.userNodeForPackage(Application.class);
		prefs.put("sourceDirectory", this.source);
		prefs.put("destinationDirectory", this.destination);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	private void restoreSavedSettings() {
		Preferences prefs = Preferences.userNodeForPackage(Application.class);
		this.source = prefs.get("sourceDirectory", "source");
		this.destination = prefs.get("destinationDirectory", "destination");
	}
}
