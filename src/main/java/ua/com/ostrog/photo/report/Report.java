package ua.com.ostrog.photo.report;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import ua.com.ostrog.photo.logic.AnalysisResult;
import ua.com.ostrog.photo.logic.Controller;
import ua.com.ostrog.photo.logic.ErrorAction;
import ua.com.ostrog.photo.logic.FileDestination;
import ua.com.ostrog.photo.logic.FileEntry;
import ua.com.ostrog.photo.logic.ImageType;
import ua.com.ostrog.photo.logic.ProcessProgressListener;

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
	}

	private static final ImageIcon EXIF_MISMATCH_ICON = loadIcon("icons/check-square_red.png");

	private static final Map<FileDestination, String> STATE_LABELS = new EnumMap<>(FileDestination.class);
	static {
		STATE_LABELS.put(FileDestination.NEW, "New");
		STATE_LABELS.put(FileDestination.EXIST, "Exists");
		STATE_LABELS.put(FileDestination.IGNORED, "Ignored");
		STATE_LABELS.put(FileDestination.ERROR, "Errors");
	}

	private static ImageIcon loadIcon(String resourcePath) {
		URL url = Report.class.getClassLoader().getResource(resourcePath);
		return url == null ? null : new ImageIcon(url);
	}

	private static final Comparator<FileEntry> ENTRY_COMPARATOR = Comparator
			.comparing((FileEntry entry) -> pathOrEmpty(entry.getDestination()))
			.thenComparing(entry -> pathOrEmpty(entry.getSource()));

	private static String pathOrEmpty(File file) {
		return file == null ? "" : file.getPath();
	}

	private final JLabel sourceDirLabel = new JLabel();
	private final JLabel destinationDirLabel = new JLabel();
	private final JPanel volumesPanel = new JPanel();
	private final JTable table = new JTable();
	private final JButton save = new JButton("Save");
	private final JButton print = new JButton("Print");
	private final JButton process = new JButton("Process");
	private final JProgressBar progressBar = new JProgressBar();

	private Controller controller;
	private AnalysisResult lastAnalysisResult;

	/**
	 * Builds the window (source/destination labels, volumes, table and
	 * Save/Print/Process buttons) but does not show it. Call
	 * {@link #showReport} to populate and display it.
	 */
	public Report() {
		setTitle("Analysis report");
		setLayout(new BorderLayout());
		add(buildDirsPanel(), BorderLayout.NORTH);
		add(new JScrollPane(table), BorderLayout.CENTER);
		add(buildSouthPanel(), BorderLayout.SOUTH);
		setSize(new Dimension(1020, 800));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		save.addActionListener(e -> saveReport());
		print.addActionListener(e -> printReport());
		process.addActionListener(e -> processFiles());
	}

	private void processFiles() {
		if (controller == null) {
			return;
		}
		process.setEnabled(false);
		progressBar.setMaximum(controller.getAnalysisResult().getFilesEntries().size());
		progressBar.setValue(0);
		progressBar.setString("0 / " + progressBar.getMaximum());
		progressBar.setStringPainted(true);
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() {
				controller.processFiles(new ProcessProgressListener() {
					@Override
					public void onProgress(int processed, int total, FileEntry fileEntry) {
						SwingUtilities.invokeLater(() -> {
							progressBar.setMaximum(total);
							progressBar.setValue(processed);
							progressBar.setString(processed + " / " + total);
						});
					}

					@Override
					public ErrorAction onError(FileEntry fileEntry, Exception exception) {
						return showErrorDialog(fileEntry, exception);
					}
				});
				return null;
			}

			@Override
			protected void done() {
				process.setEnabled(true);
			}
		}.execute();
	}

	private ErrorAction showErrorDialog(FileEntry fileEntry, Exception exception) {
		ErrorAction[] chosenAction = { ErrorAction.STOP };
		Runnable showDialog = () -> {
			String message = "Error moving file " + fileEntry.getSource() + ": " + exception.getMessage();
			String[] options = { "Stop", "Continue", "Ignore All" };
			int choice = JOptionPane.showOptionDialog(Report.this, message, "Processing error",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[1]);
			chosenAction[0] = switch (choice) {
				case 1 -> ErrorAction.CONTINUE;
				case 2 -> ErrorAction.IGNORE_ALL;
				default -> ErrorAction.STOP;
			};
		};
		if (SwingUtilities.isEventDispatchThread()) {
			showDialog.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(showDialog);
			} catch (Exception e) {
				return ErrorAction.STOP;
			}
		}
		return chosenAction[0];
	}

	private void printReport() {
		if (lastAnalysisResult == null) {
			return;
		}
		try {
			MessageFormat headerFormat = new MessageFormat(buildPrintHeaderText() + " - Page {0}");
			table.print(JTable.PrintMode.FIT_WIDTH, headerFormat, null);
		} catch (PrinterException e) {
			JOptionPane.showMessageDialog(this, "Error printing report: " + e.getMessage(), "Print report",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private String buildPrintHeaderText() {
		StringBuilder header = new StringBuilder();
		header.append("Source dir ").append(lastAnalysisResult.getSourceDirectory());
		header.append("   Target dir ").append(lastAnalysisResult.getDestinationDirectory());
		for (String label : computeVolumeLabels(lastAnalysisResult.getFilesEntries())) {
			header.append("   ").append(label);
		}
		return header.toString();
	}

	private static final Map<String, Rectangle> PDF_PAGE_SIZES = new LinkedHashMap<>();
	static {
		PDF_PAGE_SIZES.put("A4", PageSize.A4);
		PDF_PAGE_SIZES.put("A3", PageSize.A3);
		PDF_PAGE_SIZES.put("Letter", PageSize.LETTER);
		PDF_PAGE_SIZES.put("Legal", PageSize.LEGAL);
	}

	private record PdfPageOptions(Rectangle pageSize, boolean landscape) {
	}

	private void saveReport() {
		if (lastAnalysisResult == null) {
			return;
		}
		PdfPageOptions options = choosePdfPageOptions();
		if (options == null) {
			return;
		}
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save report as PDF");
		fileChooser.setSelectedFile(new File("report.pdf"));
		fileChooser.setFileFilter(new FileNameExtensionFilter("PDF files (*.pdf)", "pdf"));
		if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = fileChooser.getSelectedFile();
		if (!file.getName().toLowerCase().endsWith(".pdf")) {
			file = new File(file.getParentFile(), file.getName() + ".pdf");
		}
		try {
			writePdfReport(file, options);
			JOptionPane.showMessageDialog(this, "Report saved to " + file.getPath());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error saving report: " + e.getMessage(), "Save report",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private PdfPageOptions choosePdfPageOptions() {
		JComboBox<String> pageSizeCombo = new JComboBox<>(PDF_PAGE_SIZES.keySet().toArray(new String[0]));
		JComboBox<String> orientationCombo = new JComboBox<>(new String[] { "Portrait", "Landscape" });
		JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
		panel.add(new JLabel("Page size"));
		panel.add(pageSizeCombo);
		panel.add(new JLabel("Orientation"));
		panel.add(orientationCombo);
		int choice = JOptionPane.showConfirmDialog(this, panel, "PDF options", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (choice != JOptionPane.OK_OPTION) {
			return null;
		}
		Rectangle pageSize = PDF_PAGE_SIZES.get((String) pageSizeCombo.getSelectedItem());
		boolean landscape = "Landscape".equals(orientationCombo.getSelectedItem());
		return new PdfPageOptions(pageSize, landscape);
	}

	private void writePdfReport(File file, PdfPageOptions options) throws Exception {
		Rectangle pageSize = options.landscape() ? options.pageSize().rotate() : options.pageSize();
		Document document = new Document(pageSize, 36, 36, 36, 36);
		try (OutputStream out = new FileOutputStream(file)) {
			PdfWriter.getInstance(document, out);
			document.open();
			document.add(new Paragraph("Source dir " + lastAnalysisResult.getSourceDirectory()));
			document.add(new Paragraph("Target dir " + lastAnalysisResult.getDestinationDirectory()));
			List<FileEntry> filesEntries = new ArrayList<>(lastAnalysisResult.getFilesEntries());
			filesEntries.sort(ENTRY_COMPARATOR);
			for (String label : computeVolumeLabels(filesEntries)) {
				document.add(new Paragraph(label));
			}
			document.add(new Paragraph(" "));
			document.add(buildPdfTable(filesEntries, lastAnalysisResult.getSourceDirectory(),
					lastAnalysisResult.getDestinationDirectory()));
			document.close();
		}
	}

	private PdfPTable buildPdfTable(List<FileEntry> filesEntries, String sourceDirectory,
			String destinationDirectory) throws Exception {
		PdfPTable pdfTable = new PdfPTable(COLUMN_NAMES.length);
		pdfTable.setWidthPercentage(100);
		pdfTable.setWidths(COLUMN_WIDTH_CHARS);
		Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
		for (String columnName : COLUMN_NAMES) {
			PdfPCell headerCell = new PdfPCell(new Phrase(columnName, headerFont));
			headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			pdfTable.addCell(headerCell);
		}
		pdfTable.setHeaderRows(1);
		for (FileEntry fileEntry : filesEntries) {
			pdfTable.addCell(new Phrase(formatSource(fileEntry, sourceDirectory)));
			pdfTable.addCell(new Phrase(formatDestination(fileEntry, destinationDirectory)));
			pdfTable.addCell(iconCell(STATE_ICONS.get(fileEntry.getTypeDestination())));
			pdfTable.addCell(iconCell(formatExif(fileEntry.getExifDate())));
			pdfTable.addCell(new Phrase(formatImageType(fileEntry.getImageType())));
		}
		return pdfTable;
	}

	private PdfPCell iconCell(ImageIcon icon) throws Exception {
		PdfPCell cell = new PdfPCell();
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		if (icon != null) {
			Image pdfImage = Image.getInstance(icon.getImage(), null);
			pdfImage.scaleToFit(14, 14);
			cell.addElement(pdfImage);
		}
		return cell;
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

	private JPanel buildSouthPanel() {
		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(progressBar, BorderLayout.CENTER);
		southPanel.add(buildButtonsPanel(), BorderLayout.EAST);
		return southPanel;
	}

	private JPanel buildButtonsPanel() {
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonsPanel.add(save);
		buttonsPanel.add(print);
		buttonsPanel.add(process);
		return buttonsPanel;
	}

	/**
	 * Populates the table and header from {@code analysisResult} and shows
	 * the window. Entries are sorted by destination path then source path;
	 * per-{@link FileDestination} volumes are computed and shown above the
	 * table. Also enables Save and Print (Process stays as previously set by
	 * {@link #setEnabledProcessButton} / {@link #setController}).
	 *
	 * @param analysisResult the result to display; becomes the source of
	 *                       truth for subsequent {@link #saveReport()} and
	 *                       {@link #printReport()} calls
	 */
	public void showReport(AnalysisResult analysisResult) {
		this.lastAnalysisResult = analysisResult;
		sourceDirLabel.setText("Source dir " + analysisResult.getSourceDirectory());
		destinationDirLabel.setText("Target dir " + analysisResult.getDestinationDirectory());
		List<FileEntry> filesEntries = new ArrayList<>(analysisResult.getFilesEntries());
		filesEntries.sort(ENTRY_COMPARATOR);
		showVolumes(filesEntries);
		Object[][] data = new Object[filesEntries.size()][COLUMN_NAMES.length];
		String sourceDirectory = analysisResult.getSourceDirectory();
		String destinationDirectory = analysisResult.getDestinationDirectory();
		for (int i = 0; i < filesEntries.size(); i++) {
			FileEntry fileEntry = filesEntries.get(i);
			data[i][0] = formatSource(fileEntry, sourceDirectory);
			data[i][1] = formatDestination(fileEntry, destinationDirectory);
			data[i][STATE_COLUMN] = STATE_ICONS.get(fileEntry.getTypeDestination());
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
		volumesPanel.removeAll();
		for (String label : computeVolumeLabels(filesEntries)) {
			volumesPanel.add(new JLabel(label));
		}
		volumesPanel.revalidate();
		volumesPanel.repaint();
	}

	private List<String> computeVolumeLabels(List<FileEntry> filesEntries) {
		Map<FileDestination, Integer> volumes = new EnumMap<>(FileDestination.class);
		for (FileEntry fileEntry : filesEntries) {
			FileDestination typDestination = fileEntry.getTypeDestination();
			if (typDestination != null) {
				volumes.merge(typDestination, 1, Integer::sum);
			}
		}
		List<String> labels = new ArrayList<>();
		for (FileDestination typDestination : FileDestination.values()) {
			int volume = volumes.getOrDefault(typDestination, 0);
			if (volume > 0) {
				labels.add(STATE_LABELS.get(typDestination) + " " + volume);
			}
		}
		return labels;
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
			return null;
		}
		return exifDate ? null : EXIF_MISMATCH_ICON;
	}

	private String formatImageType(ImageType imageType) {
		if (imageType == null || 'N' == imageType.name().charAt(0)) {
			return null;
		}
		return imageType.name().substring(0, 1);
	}

	private String formatDestination(FileEntry fileEntry, String destinationDirectory) {
		File destination = fileEntry.getDestination();
		if (destination == null) {
			return null;
		}
		String parent = destination.getParent();
		if (destinationDirectory != null && parent.startsWith(destinationDirectory)) {
			return parent.substring(destinationDirectory.length()+1);
		}
		return parent;
	}
	/**
	 * Enables or disables the Process button directly, independent of
	 * whether a {@link Controller} has been set. Useful for a read-only
	 * report (e.g. an "analyze only" view) where processing should stay
	 * disabled even though a controller exists.
	 *
	 * @param enabled {@code true} to enable the Process button
	 */
	public  void setEnabledProcessButton(boolean enabled){
		this.process.setEnabled(enabled);
	}

	/**
	 * Supplies the {@link Controller} used to actually move files when the
	 * Process button is clicked. Without a controller, clicking Process does
	 * nothing.
	 *
	 * @param controller the controller whose {@link Controller#processFiles}
	 *                   will be invoked
	 */
	public void setController(Controller controller) {
		this.controller=controller;
	}
}
