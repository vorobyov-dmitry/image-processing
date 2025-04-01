package ua.com.ostrog.photo;

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
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class SelectFilePanel extends JPanel {

	private File file = null;
	private File[] selectedFiles = null;
	private JButton buttonSelectFiles = new JButton("Select file");
	private JTextField fieldFleName = new JTextField();
	private TitledBorder border = (TitledBorder) BorderFactory
			.createTitledBorder("Select dir/files");
	private boolean multiSelectionEnabled = true;
	private int fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES;
	private File[] openedFiles = new File[0]; 
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
//				if (files != null) {
//					processFiles(files);
//				}
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

	void  openFiles() {
		JFileChooser open = new JFileChooser();
		open.setDialogTitle("Open source dir/files ");
		open.setDialogType(JFileChooser.OPEN_DIALOG);
		open.setVisible(true);
		open.setEnabled(true);
		open.setMultiSelectionEnabled(multiSelectionEnabled);
		open.setFileSelectionMode(fileSelectionMode);
		if (file != null) {
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

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
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

	public void setTitleBorder(String title) {
		this.border.setTitle(title);
	}

	public boolean isMultiSelectionEnabled() {
		return multiSelectionEnabled;
	}

	public void setMultiSelectionEnabled(boolean multiSelectionEnabled) {
		this.multiSelectionEnabled = multiSelectionEnabled;
	}

	public int getFileSelectionMode() {
		return fileSelectionMode;
	}

	public void setFileSelectionMode(int fileSelectionMode) {
		this.fileSelectionMode = fileSelectionMode;
	}

	public void setDir(String fileName) {
		File fileTemp = new File(fileName);
		if (fileTemp.exists()) {
			this.file = fileTemp;
			this.fieldFleName.setText(this.file.getPath());
			this.selectedFiles = new File[] { this.file };
		}
	}

	public File[] getSelectedFiles() {
		return selectedFiles;
	}
}
