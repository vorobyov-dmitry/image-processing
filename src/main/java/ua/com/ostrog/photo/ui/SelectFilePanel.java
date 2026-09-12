package ua.com.ostrog.photo.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 * A titled panel with a read-only text field showing the current
 * path(s) and a button that opens a {@link JFileChooser} to change the
 * selection. Used for both the source (files/directories, multi-select) and
 * destination (single directory) pickers in {@link Application}.
 */
public class SelectFilePanel extends JPanel {

	private File file = null;
	private File[] selectedFiles = null;
	private JButton buttonSelectFiles = new JButton("Select directory/files");
	private JTextField fieldFleName = new JTextField();
	private TitledBorder border = (TitledBorder) BorderFactory
			.createTitledBorder("Select dir/files");
	private boolean multiSelectionEnabled = true;
	private int fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES;
	private File[] openedFiles = new File[0];

	/**
	 * Builds the panel with its default title ("Select dir/files"), an empty
	 * path field and a "Select directory/files" button. Multi-selection of
	 * both files and directories is enabled by default; use
	 * {@link #setMultiSelectionEnabled} and {@link #setFileSelectionMode} to
	 * change that before the panel is shown.
	 */
	public SelectFilePanel() {
		setBorder(border);
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(0, 10, 0, 0);
		constraints.gridy = 0;
		constraints.gridx = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		fieldFleName.setColumns(60);
		add(fieldFleName, constraints);
		buttonSelectFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openFiles();
			}
		});
		constraints.gridx++;
		constraints.insets= new Insets(0, 20, 0, 20);
		constraints.fill = GridBagConstraints.NONE;
		add(buttonSelectFiles, constraints);

	}

	void processFiles() {
		this.fieldFleName.setText("");
		this.selectedFiles = openedFiles;
		if (this.selectedFiles.length > 0) {
			this.file = openedFiles[0];
			this.fieldFleName.setText(this.file.getPath());
		}
	}

	void openFiles() {
		JFileChooser open = new JFileChooser();
		open.setDialogTitle("Open source dir/files ");
		open.setDialogType(JFileChooser.OPEN_DIALOG);
		open.setVisible(true);
		open.setEnabled(true);
		open.setMultiSelectionEnabled(multiSelectionEnabled);
		open.setFileSelectionMode(fileSelectionMode);
		if (this.fieldFleName.getText() != null) {
			String s = this.fieldFleName.getText();
			File fileTemp = new File(s);
			if (fileTemp.exists()) {
				open.setSelectedFile(fileTemp);
			}
		} else if (file != null) {
			if (file.isDirectory()) {
				if (file.exists()) {
					open.setCurrentDirectory(file);
				}
			} else {
				String s = file.getParent();
				if (s != null) {
					File dir = new File(s);
					if (dir.exists()) {
						open.setCurrentDirectory(file);
					}
				}
			}
		}
		
		int result = open.showOpenDialog(SwingUtilities
				.windowForComponent(this));
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] openFiles = open.getSelectedFiles();
			if (openFiles.length==0){
				File f1 = open.getSelectedFile();
				if (f1!=null){
					this.openedFiles =  new File[]{f1};
				}
				else{
					this.openedFiles =  new File[0];
				}
			}
			else {
				this.openedFiles =  openFiles;
			}
			processFiles();
		}
	}

	/**
	 * @return the first selected file/directory, or {@code null} if nothing
	 *         has been selected yet. Despite the name, this returns whatever
	 *         was picked (file or directory) — callers using this panel as a
	 *         directory-only picker are expected to restrict the choice via
	 *         {@link #setFileSelectionMode(int)}.
	 */
	public File getDestinationDirectory() {
		return file;
	}

	/**
	 * Demonstrates the panel standalone in its own {@link JFrame}, centered
	 * on screen.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		SelectFilePanel selectFilePanel = new SelectFilePanel();
		frame.setContentPane(selectFilePanel);
		frame.setSize(1024, 800);
		frame.setVisible(true);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((screenSize.width - frame.getWidth()) / 2,
				(screenSize.height - frame.getHeight()) / 2);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	/**
	 * @param title text shown on the panel's titled border, replacing the
	 *              default "Select dir/files"
	 */
	public void setTitleBorder(String title) {
		this.border.setTitle(title);
	}

	/**
	 * Controls whether the {@link JFileChooser} opened by the panel's button
	 * allows selecting more than one file/directory at once. Must be called
	 * before the user opens the chooser.
	 *
	 * @param multiSelectionEnabled {@code true} to allow multiple selections
	 */
	public void setMultiSelectionEnabled(boolean multiSelectionEnabled) {
		this.multiSelectionEnabled = multiSelectionEnabled;
	}

	/**
	 * Controls what kind of entries the {@link JFileChooser} opened by the
	 * panel's button allows selecting. Must be called before the user opens
	 * the chooser.
	 *
	 * @param fileSelectionMode one of {@link JFileChooser#FILES_ONLY},
	 *                          {@link JFileChooser#DIRECTORIES_ONLY} or
	 *                          {@link JFileChooser#FILES_AND_DIRECTORIES}
	 */
	public void setFileSelectionMode(int fileSelectionMode) {
		this.fileSelectionMode = fileSelectionMode;
	}

	/**
	 * Pre-selects a path without opening the file chooser, e.g. to restore a
	 * previously saved directory on startup. Does nothing if the path does
	 * not exist.
	 *
	 * @param fileName path of the file/directory to pre-select
	 */
	public void setDir(String fileName) {
		File fileTemp = new File(fileName);
		if (fileTemp.exists()) {
			this.file = fileTemp;
			this.fieldFleName.setText(this.file.getPath());
			this.selectedFiles = new File[] { this.file };
		}
	}

	/**
	 * @return every file/directory currently selected in the panel, or
	 *         {@code null} if nothing has been selected yet
	 */
	public File[] getSelectedFiles() {
		return selectedFiles;
	}
}
