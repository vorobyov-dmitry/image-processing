package ua.com.ostrog.photo;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class ApplicationPanel extends JPanel {
	private static final long serialVersionUID = 474514726680098921L;
	private SelectFilePanel sourcePanel = new SelectFilePanel();
	private SelectFilePanel destinationPanel = new SelectFilePanel();
	private JButton start = new JButton("Start");
	private JButton analyze = new JButton("Analyze");
	private JPanel settingPanel = new JPanel();
	private JCheckBox exifDate = new JCheckBox("Use exif date of image", true);

	public ApplicationPanel(String source, String destination, Consumer<ActionEvent> analyzeFiles,
			Consumer<ActionEvent> processFiles) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.sourcePanel.setDir(source);
		this.destinationPanel.setDir(destination);
		this.destinationPanel.setMultiSelectionEnabled(false);
		this.destinationPanel.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		this.destinationPanel.setTitleBorder("Destination dir");
		add(sourcePanel);
		add(destinationPanel);
		fillSettingPanel();
		add(settingPanel);
		JPanel startPanel = new JPanel();
		startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.X_AXIS));
		// startPanel.setAlignmentX(1f);
		startPanel.add(analyze);
		analyze.addActionListener(analyzeFiles::accept);
		startPanel.add(Box.createHorizontalGlue());
		startPanel.add(start);
		startPanel.setBorder(BorderFactory.createTitledBorder(""));
		add(startPanel);
		start.addActionListener(processFiles::accept);
	}

	private void fillSettingPanel() {
		this.settingPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		this.settingPanel.add(exifDate, constraints);
	}

	public boolean isExifDateSelected() {
		return this.exifDate.isSelected();
	}

	public File getDestinationDirectory() {
		return this.destinationPanel.getDestinationDirectory();
	}

	public File[] getSelectedFiles() {
		return this.sourcePanel.getSelectedFiles();
	}

}
