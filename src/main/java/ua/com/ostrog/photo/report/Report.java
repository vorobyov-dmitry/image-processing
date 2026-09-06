package ua.com.ostrog.photo.report;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ua.com.ostrog.photo.logic.AnalysisResult;
import ua.com.ostrog.photo.logic.FileEntry;

/**
 * Simple Swing window that displays an {@link AnalysisResult} as a table.
 */
public class Report extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final String[] COLUMN_NAMES = { "Source", "Destination",
			"Type destination", "Exif date", "Image type" };

	private final JTable table = new JTable();

	public Report() {
		setTitle("Analysis report");
		setContentPane(new JScrollPane(table));
		setSize(new Dimension(900, 500));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	public void showReport(AnalysisResult analysisResult) {
		List<FileEntry> filesEntries = analysisResult.getFilesEntries();
		Object[][] data = new Object[filesEntries.size()][COLUMN_NAMES.length];
		for (int i = 0; i < filesEntries.size(); i++) {
			FileEntry fileEntry = filesEntries.get(i);
			data[i][0] = fileEntry.getSource();
			data[i][1] = fileEntry.getDestination();
			data[i][2] = fileEntry.getTypDestination();
			data[i][3] = fileEntry.getExifDate();
			data[i][4] = fileEntry.getImageType();
		}
		table.setModel(new DefaultTableModel(data, COLUMN_NAMES));
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
