package ua.com.ostrog.photo.ui;

import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 * Content of the "Consistency" tab: a single directory picker and an
 * Analyze button pinned to the bottom-right. Carries no logic of its own -
 * {@link Application} attaches the actual behavior via {@link #addAnalyzeListener}.
 */
public class ConsistencyPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final SelectFilePanel sourcePanel = new SelectFilePanel();
	private final JButton analyze = new JButton("Analyze");

	public ConsistencyPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		sourcePanel.setMultiSelectionEnabled(false);
		sourcePanel.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		sourcePanel.setTitleBorder("Source dir");
		add(sourcePanel);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(analyze);
		buttonsPanel.setBorder(BorderFactory.createTitledBorder(""));
		add(buttonsPanel);
	}

	/**
	 * @return the directory currently selected in the source picker, or
	 *         {@code null} if nothing has been selected yet
	 */
	public File getSourceDirectory() {
		return sourcePanel.getDestinationDirectory();
	}

	/**
	 * @param listener invoked when the Analyze button is clicked
	 */
	public void addAnalyzeListener(ActionListener listener) {
		analyze.addActionListener(listener);
	}
}
