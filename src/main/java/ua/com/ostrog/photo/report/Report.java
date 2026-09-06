package ua.com.ostrog.photo.report;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.net.URL;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ua.com.ostrog.photo.logic.AnalysisResult;
import ua.com.ostrog.photo.logic.FileDestination;
import ua.com.ostrog.photo.logic.FileEntry;
import ua.com.ostrog.photo.logic.ImageType;

/**
 * Simple Swing window that displays an {@link AnalysisResult} as a table.
 */
public class Report extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final String[] COLUMN_NAMES = { "Source", "Destination",
			"STATE", "Exif", "Type" };

	private static final int[] COLUMN_WIDTH_CHARS = { 30, 50, 7, 7, 7 };

	private static final int STATE_COLUMN = 2;
	private static final int EXIF_COLUMN = 3;

	private static final Map<FileDestination, ImageIcon> STATE_ICONS = new EnumMap<>(FileDestination.class);
	static {
		STATE_ICONS.put(FileDestination.EXIST, loadIcon("icons/check-square_blue.png"));
		STATE_ICONS.put(FileDestination.NEW, loadIcon("icons/check-square_green.png"));
		STATE_ICONS.put(FileDestination.IGNORED, loadIcon("icons/frown.png"));
		STATE_ICONS.put(FileDestination.ERROR, loadIcon("icons/close-square_red.png"));
		STATE_ICONS.put(FileDestination.DATE_DIFF, loadIcon("icons/exif_date.png"));
	}

	private static final ImageIcon EXIF_UNKNOWN_ICON = loadIcon("icons/check-square.png");
	private static final ImageIcon EXIF_MISMATCH_ICON = loadIcon("icons/check-square_red.png");

	private static final Map<FileDestination, String> STATE_LABELS = new EnumMap<>(FileDestination.class);
	static {
		STATE_LABELS.put(FileDestination.NEW, "New");
		STATE_LABELS.put(FileDestination.EXIST, "Exists");
		STATE_LABELS.put(FileDestination.IGNORED, "Ignored");
		STATE_LABELS.put(FileDestination.ERROR, "Errors");
		STATE_LABELS.put(FileDestination.DATE_DIFF, "Date dif");
	}

	private static ImageIcon loadIcon(String resourcePath) {
		URL url = Report.class.getClassLoader().getResource(resourcePath);
		return url == null ? null : new ImageIcon(url);
	}

	private final JLabel sourceDirLabel = new JLabel();
	private final JLabel destinationDirLabel = new JLabel();
	private final JPanel volumesPanel = new JPanel();
	private final JTable table = new JTable();
	private final JButton save = new JButton("Save");
	private final JButton print = new JButton("Print");
	private final JButton process = new JButton("Process");

	public Report() {
		setTitle("Analysis report");
		setLayout(new BorderLayout());
		add(buildDirsPanel(), BorderLayout.NORTH);
		add(new JScrollPane(table), BorderLayout.CENTER);
		add(buildButtonsPanel(), BorderLayout.SOUTH);
		setSize(new Dimension(1020, 800));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private JPanel buildDirsPanel() {
		JPanel dirsPanel = new JPanel();
		dirsPanel.setLayout(new BoxLayout(dirsPanel, BoxLayout.Y_AXIS));
		dirsPanel.add(sourceDirLabel);
		dirsPanel.add(destinationDirLabel);
		volumesPanel.setLayout(new BoxLayout(volumesPanel, BoxLayout.Y_AXIS));
		dirsPanel.add(volumesPanel);
		return dirsPanel;
	}

	private JPanel buildButtonsPanel() {
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonsPanel.add(save);
		buttonsPanel.add(print);
		buttonsPanel.add(process);
		return buttonsPanel;
	}

	public void showReport(AnalysisResult analysisResult) {
		sourceDirLabel.setText("Source dir " + analysisResult.getSourceDirectory());
		destinationDirLabel.setText("Target dir " + analysisResult.getDestinationDirectory());
		List<FileEntry> filesEntries = analysisResult.getFilesEntries();
		showVolumes(filesEntries);
		Object[][] data = new Object[filesEntries.size()][COLUMN_NAMES.length];
		String sourceDirectory = analysisResult.getSourceDirectory();
		String destinationDirectory = analysisResult.getDestinationDirectory();
		for (int i = 0; i < filesEntries.size(); i++) {
			FileEntry fileEntry = filesEntries.get(i);
			data[i][0] = formatSource(fileEntry, sourceDirectory);
			data[i][1] = formatDestination(fileEntry, destinationDirectory);
			data[i][STATE_COLUMN] = STATE_ICONS.get(fileEntry.getTypDestination());
			data[i][EXIF_COLUMN] = formatExif(fileEntry.getExifDate());
			data[i][4] = formatImageType(fileEntry.getImageType());
		}
		table.setModel(new DefaultTableModel(data, COLUMN_NAMES) {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return columnIndex == STATE_COLUMN || columnIndex == EXIF_COLUMN
						? ImageIcon.class : Object.class;
			}
		});
		configureColumnWidths();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void showVolumes(List<FileEntry> filesEntries) {
		Map<FileDestination, Integer> volumes = new EnumMap<>(FileDestination.class);
		for (FileEntry fileEntry : filesEntries) {
			FileDestination typDestination = fileEntry.getTypDestination();
			if (typDestination != null) {
				volumes.merge(typDestination, 1, Integer::sum);
			}
		}
		volumesPanel.removeAll();
		for (FileDestination typDestination : FileDestination.values()) {
			int volume = volumes.getOrDefault(typDestination, 0);
			if (volume > 0) {
				volumesPanel.add(new JLabel(STATE_LABELS.get(typDestination) + " " + volume));
			}
		}
		volumesPanel.revalidate();
		volumesPanel.repaint();
	}

	private void configureColumnWidths() {
		int charWidth = table.getFontMetrics(table.getFont()).charWidth('m');
		for (int column = 0; column < COLUMN_WIDTH_CHARS.length; column++) {
			table.getColumnModel().getColumn(column).setPreferredWidth(COLUMN_WIDTH_CHARS[column] * charWidth);
		}
	}

	private String formatSource(FileEntry fileEntry, String sourceDirectory) {
		File source = fileEntry.getSource();
		if (source == null) {
			return "NULL";
		}
		String path = source.getPath();
		if (sourceDirectory != null && path.startsWith(sourceDirectory)) {
			return path.substring(sourceDirectory.length()+1);
		}
		return path;
	}

	private ImageIcon formatExif(Boolean exifDate) {
		if (exifDate == null) {
			return EXIF_UNKNOWN_ICON;
		}
		return exifDate ? null : EXIF_MISMATCH_ICON;
	}

	private String formatImageType(ImageType imageType) {
		if (imageType == null) {
			return "NULL";
		}
		return imageType.name().substring(0, 1);
	}

	private String formatDestination(FileEntry fileEntry, String destinationDirectory) {
		File destination = fileEntry.getDestination();
		if (destination == null) {
			return "NULL";
		}
		String parent = destination.getParent();
		if (destinationDirectory != null && parent.startsWith(destinationDirectory)) {
			return parent.substring(destinationDirectory.length()+1);
		}
		return parent;
	}
}
