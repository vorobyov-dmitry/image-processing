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
 * Content of the "Synchronize" tab: source and copy directory pickers, with
 * Compare pinned bottom-left and Synchronize pinned bottom-right. Carries no
 * logic of its own - {@link Application} attaches the actual behavior via
 * {@link #addCompareListener} / {@link #addSynchronizeListener}.
 */
public class SynchronizePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private final SelectFilePanel sourcePanel = new SelectFilePanel();
	private final SelectFilePanel copyPanel = new SelectFilePanel();
	private final JButton compare = new JButton("Compare");
	private final JButton synchronize = new JButton("Synchronize");

	public SynchronizePanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		sourcePanel.setMultiSelectionEnabled(false);
		sourcePanel.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		sourcePanel.setTitleBorder("Source dir");
		copyPanel.setMultiSelectionEnabled(false);
		copyPanel.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		copyPanel.setTitleBorder("Copy dir");
		add(sourcePanel);
		add(copyPanel);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		buttonsPanel.add(compare);
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(synchronize);
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
	 * @return the directory currently selected in the copy picker, or
	 *         {@code null} if nothing has been selected yet
	 */
	public File getCopyDirectory() {
		return copyPanel.getDestinationDirectory();
	}

	/**
	 * @param listener invoked when the Compare button is clicked
	 */
	public void addCompareListener(ActionListener listener) {
		compare.addActionListener(listener);
	}

	/**
	 * @param listener invoked when the Synchronize button is clicked
	 */
	public void addSynchronizeListener(ActionListener listener) {
		synchronize.addActionListener(listener);
	}
}
