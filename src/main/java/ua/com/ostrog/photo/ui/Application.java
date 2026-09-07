package ua.com.ostrog.photo.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ua.com.ostrog.photo.logic.Controller;
import ua.com.ostrog.photo.logic.CoreFilesData;
import ua.com.ostrog.photo.report.Report;
/**
 * The application window. This is the former {@code Extractor} frame and the
 * former {@code ApplicationPanel} merged into a single class: it builds the UI
 * and delegates every file operation to {@link Controller}.
 */
public class Application extends JFrame {
	private static final long serialVersionUID = -423490175756620363L;

	public static final String versionOfProduct = "photo v.3.03";

	private String destination = "";
	private String source = "";

	private final SelectFilePanel sourcePanel = new SelectFilePanel();
	private final SelectFilePanel destinationPanel = new SelectFilePanel();
	private final JButton start = new JButton("Start");
	private final JButton analyze = new JButton("Analyze");
	private final JPanel settingPanel = new JPanel();
	private final JCheckBox exifDate = new JCheckBox("Use exif date of image", true);

	private final Controller controller = new Controller();

	public Application() {
		restoreSavedSettings();
		buildUi();
	}

	private void buildUi() {
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		this.sourcePanel.setDir(source);
		this.destinationPanel.setDir(destination);
		this.destinationPanel.setMultiSelectionEnabled(false);
		this.destinationPanel.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		this.destinationPanel.setTitleBorder("Target dir");
		content.add(sourcePanel);
		content.add(destinationPanel);
		fillSettingPanel();
		content.add(settingPanel);
		JPanel startPanel = new JPanel();
		startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.X_AXIS));
		startPanel.add(analyze);
		analyze.addActionListener(e -> analyzeFiles());
		startPanel.add(Box.createHorizontalGlue());
		startPanel.add(start);
		startPanel.setBorder(BorderFactory.createTitledBorder(""));
		content.add(startPanel);
		start.addActionListener(e -> processFiles());
		setContentPane(content);
	}

	private void fillSettingPanel() {
		this.settingPanel.setLayout(new GridBagLayout());
		this.settingPanel.add(exifDate, new GridBagConstraints());
	}

	private void analyzeFiles() {
		File destinationDir = this.destinationPanel.getDestinationDirectory();
		File[] sourceFiles = this.sourcePanel.getSelectedFiles();
		this.destination = destinationDir.getPath();
		this.source = sourceFiles[0].getPath();
		this.controller.analyze(new CoreFilesData(destinationDir, sourceFiles, this.exifDate.isSelected()));
		Report report = new Report();
		report.setEnabledProcessButton(false);
		report.showReport(this.controller.getAnalysisResult());
	}

	private void processFiles() {
		File destinationDir = this.destinationPanel.getDestinationDirectory();
		File[] sourceFiles = this.sourcePanel.getSelectedFiles();
		this.destination = destinationDir.getPath();
		this.source = sourceFiles[0].getPath();
		this.controller.analyze(new CoreFilesData(destinationDir, sourceFiles, this.exifDate.isSelected()));
		Report report = new Report();
		report.setController(this.controller);
		report.showReport(this.controller.getAnalysisResult());
	}

	private void restoreSavedSettings() {
		Preferences prefs = Preferences.userNodeForPackage(Application.class);
		this.source = prefs.get("sourceDirectory", "source");
		this.destination = prefs.get("destinationDirectory", "destination");
	}

	private void writeSettings() {
		Preferences prefs = Preferences.userNodeForPackage(Application.class);
		prefs.put("sourceDirectory", this.source);
		prefs.put("destinationDirectory", this.destination);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	protected void showApplication() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(1024, 400);
		this.setLocation((screenSize.width - this.getWidth()) / 2,
				(screenSize.height - this.getHeight()) / 2);
		this.setVisible(true);
		this.setTitle(versionOfProduct);
	}

	public static void main(String[] args) {
		Application application = new Application();
		application.showApplication();
		application.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				application.writeSettings();
				System.exit(0);
			}
		});
	}
}
