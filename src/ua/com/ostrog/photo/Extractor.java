package ua.com.ostrog.photo;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class Extractor extends JFrame {
	private static final String EXISTS_DIR = "exists";
	
	private static Logger logger = Logger.getLogger(Extractor.class);
	public static final String versionOfProduct = "photo v.3.01";
	
	private String destination = "D:\\ourPhoto";
	private String source = "d:\\toProcessPhoto";
	private  File existDir = null;
	private SelectFilePanel sourcePanel = new SelectFilePanel();
	private SelectFilePanel destinationPanel = new SelectFilePanel();
	private JButton start = new JButton("Start");
	private JButton analyze = new JButton("Analyze");
	private int countCopy = 0;
	private int countIdentical = 0;
	private int countMistake = 0;
	private static final String[] ignoredFileName = new String[] {"thumbs.db","picasa.ini"};

	private static final String[] extensionImage = new String[] { "jpg",
			"jpeg", "crw" };
	private static final String[] extensionRaw = new String[] { "crw","cr2","nef" };

	private static final String[] extensionVideo = new String[] { "mov", "avi",
			"thm", "mp4","3gp","wmv" };
	private static final String IMAGE = "IMAGE";
	private static final String VIDEO = "video";
	private static final String NOT_PROCESS = "None";
	private static final String PANORAMA = "panorama";
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	private JPanel settingPanel = new JPanel();
	private JCheckBox exifDate = new JCheckBox("Use exif date of image", false);
	private JCheckBox moveFiles = new JCheckBox("move new files ", true);
	private boolean useExifDate = false;
	private List<String> filesProcessed = new ArrayList<String>();
	private List<String> filesNew = new ArrayList<String>();
	private List<String> filesErrors = new ArrayList<String>();
	private List<String> filesIgnored = new ArrayList<String>();
	private List<String> filesWrongDate = new ArrayList<String>();
	private StringWriter writer = null;
	private String sMessage = null;

	public Extractor() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		this.sourcePanel.setDir(source);
		this.destinationPanel.setDir(destination);
		this.destinationPanel.setMultiSelectionEnabled(false);
		this.destinationPanel
				.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		this.destinationPanel.setTitleBorder("Destination dir");
		panel.add(sourcePanel);
		panel.add(destinationPanel);
		fillSettingPanel();
		panel.add(settingPanel);
		JPanel startPanel = new JPanel();
		startPanel.setLayout(new BoxLayout(startPanel, BoxLayout.X_AXIS));
		// startPanel.setAlignmentX(1f);
		startPanel.add(analyze);
		analyze.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				analyze();

			}
		});
		startPanel.add(Box.createHorizontalGlue());
		startPanel.add(start);
		startPanel.setBorder(BorderFactory.createTitledBorder(""));
		panel.add(startPanel);
		start.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				process();
			}
		});
		this.setContentPane(panel);
		existDir = new File(source +"\\"+EXISTS_DIR);
	}

	protected void analyze() {
		useExifDate = this.exifDate.isSelected();
		message("Start use exif date = " + useExifDate);
		File destinationDir = this.destinationPanel.getFile();
		File[] sourceFiles = this.sourcePanel.getSelectedFiles();

		this.filesErrors.clear();
		this.filesNew.clear();
		this.filesProcessed.clear();
		this.filesIgnored.clear();
		this.filesWrongDate.clear();
		sMessage = null;
		this.writer = new StringWriter();

		for (int i = 0; i < sourceFiles.length; i++) {
			File fl = sourceFiles[i];
			message("From " + fl.getPath() + " to " + destinationDir.getPath());
			if (fl.isDirectory()) {
				analyzeDir(fl, destinationDir);
			} else {
				analyzeFile(fl, destinationDir);
			}
		}
		message("============== finished =======");
		message("Processed " + this.filesProcessed.size());
		message("New  " + this.filesNew.size());
		message("error " + this.filesErrors.size());
		message("ignored " + this.filesIgnored.size());
		message("wrong date  " + this.filesWrongDate.size());
		try {
			FileUtils
					.writeLines(new File(source+"\\processed.lst"), this.filesProcessed);
			FileUtils.writeLines(new File(source+"\\new.lst"), this.filesNew);
			FileUtils.writeLines(new File(source+"\\errors.lst"), this.filesErrors);
			FileUtils.writeLines(new File(source+"\\ignored.lst"), this.filesIgnored);
			if (this.filesWrongDate.size()>0){
				this.filesWrongDate.add(0, "EXIF DATE  FILE DATE             FILE ");
			}
			FileUtils.writeLines(new File(source+"\\wrongDate.lst"), this.filesWrongDate);
			IOUtils.writeLines(this.filesProcessed,"\n" , writer);
			writer.write("  New \n");
			IOUtils.writeLines(this.filesNew, "\n", writer);
			sMessage= writer.toString();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		dispReport();
	}

	private void dispReport() {
		if (this.sMessage!=null)
		System.out.println(this.sMessage);
		
	}

	private void analyzeFile(File fl, File destinationDir) {
		String typeFile = getTypeFile(fl);
		if (NOT_PROCESS.equals(typeFile)) {
			for (int i = 0; i < ignoredFileName.length; i++) {
				if (ignoredFileName[i].equalsIgnoreCase(fl.getName())){
					return;
				}
			}
			this.filesIgnored.add(fl.getPath());
			return;
		}
		String destinationFileName = null;
		checkConsistenceOfExifData(fl);
		switch (typeFile) {
		case PANORAMA:
			destinationFileName = getDestinationFileName(fl, destinationDir,
					getPanorama(fl.getName()));
			break;
		case IMAGE:
			destinationFileName = getDestinationFileName(fl, destinationDir, "");
			break;
		case VIDEO:
			destinationFileName = getDestinationFileName(fl, destinationDir,
					VIDEO);
			break;
		}
		File destinationFile = new File(destinationFileName);
		if (!destinationFile.exists()) {
			this.filesNew.add(fl.getPath());
		} else {
			if (isTheSameFile(fl, destinationFile)) {
				this.filesProcessed.add(fl.getPath());
			} else {
				this.filesErrors.add(fl.getPath());
			}
		}
		// if (isImage(extensionFile)) {
		// String panorama = getPanorama(fileName);
		// if (StringUtils.isEmpty(panorama)) {
		// copyImageFile(fl, destinationDir);
		// } else {
		// copyPanoramaFile(fl, destinationDir, panorama);
		// }
		// } else{
		// if (isVideo(extensionFile)) {
		// String panorama = "video";
		// copyPanoramaFile(fl, destinationDir, panorama);
		// }
		//
		// }

	}

	private void checkConsistenceOfExifData(File fl) {
		String sDateExif = extractExifDate(fl); 
		String sDateFile=null;
		try {
			sDateFile = extractLastModifiedAsString(fl);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if (sDateExif!=null){
			if (!sDateExif.equals(sDateFile)){
				this.filesWrongDate.add(sDateExif+" "+sDateFile+" "+fl.getPath());
			}
		}
	}

	private String extractLastModifiedAsString(File fl) throws FileNotFoundException {
		if (fl==null) {
			throw new FileNotFoundException(" The null file");
		}
		if (!fl.exists()){
			throw new FileNotFoundException(" The file"+fl.getPath() +" does not exist!!!");
		}
		GregorianCalendar calendar = new GregorianCalendar();
		long millis=fl.lastModified();
		calendar.setTimeInMillis(millis);
		if (calendar.get(Calendar.HOUR_OF_DAY)==0){
			calendar.roll(Calendar.HOUR_OF_DAY, false);
			calendar.roll(Calendar.DAY_OF_MONTH, false);
		}
		String s =simpleDateFormat	.format(calendar.getTime());
		return s;
	}

	private String getTypeFile(File fl) {
		String fileName = fl.getName().toLowerCase();
		String extensionFile = getExtensioOfFile(fl);
		if (extensionFile == null) {
			return NOT_PROCESS;
		}
		if (extensionImage[0].equals(extensionFile)
				&& fileName.startsWith("st") && fileName.charAt(3) == '_') {
			return PANORAMA;
		}
		for (int i = 0; i < extensionImage.length; i++) {
			if (extensionImage[i].equals(extensionFile)) {
				return IMAGE;
			}
		}
		for (int i = 0; i < extensionVideo.length; i++) {
			if (extensionVideo[i].equals(extensionFile)) {
				return VIDEO;
			}
		}
		return NOT_PROCESS;

	}

	private void analyzeDir(File fl, File destinationDir) {
		if (fl.getPath().equalsIgnoreCase(existDir.getPath())){
			return;
		}
		File[] list = fl.listFiles();
		message(" analyze dir " + fl.getPath());
		for (int i = 0; i < list.length; i++) {
			File entry = list[i];
			if (entry.isDirectory()) {
				analyzeDir(entry, destinationDir);
			} else {
				analyzeFile(entry, destinationDir);
			}
		}

	}

	void fillSettingPanel() {
		this.settingPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		this.settingPanel.add(exifDate, constraints);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Extractor extractor = new Extractor();
		extractor.showExtractor();
		extractor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	protected void showExtractor() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(1024, 400);
		this.setLocation((screenSize.width - this.getWidth()) / 2,
				(screenSize.height - this.getHeight()) / 2);
		this.setVisible(true);
		this.setTitle(versionOfProduct);
	}

	void process() {
		message("Start");
		countCopy = 0;
		countIdentical = 0;
		countMistake = 0;
		useExifDate = this.exifDate.isSelected();
		message(" use exif date = " + useExifDate);

		File destinationDir = this.destinationPanel.getFile();
		File[] sourceFiles = this.sourcePanel.getSelectedFiles();
		for (int i = 0; i < sourceFiles.length; i++) {
			File fl = sourceFiles[i];
			message("From " + fl.getPath() + " to " + destinationDir.getPath());
			if (fl.isDirectory()) {
				processDir(fl, destinationDir);
			} else {
				processFile(fl, destinationDir);
			}
		}
		message("============== finished =======");
		message("Copied " + this.countCopy);
		message("exists " + this.countIdentical);
		message("error " + this.countMistake);
	}

	void message(String message) {
		System.out.println(message);
		logger.info(message);
		if (writer!=null){
			try {
				writer.write(message);
				writer.write("\n");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	void processDir(File fl, File destinationDir) {
		if (fl.equals(existDir)){
			return;
		}
		File[] list = fl.listFiles();
		String sPath=fl.getPath();
		if (sPath.endsWith(EXISTS_DIR)){
			message(" this dir ignored " + sPath);
			return;
		}
		message(" process dir " + sPath);
		for (int i = 0; i < list.length; i++) {
			File entry = list[i];
			if (entry.isDirectory()) {
				processDir(entry, destinationDir);
			} else {
				processFile(entry, destinationDir);
			}
		}
	}

	String getExtensioOfFile(File file) {
		if (file == null) {
			return null;
		}
		String fileName = file.getName();
		if (StringUtils.isEmpty(fileName)) {
			return null;
		}
		int n = fileName.lastIndexOf(".");
		if (n < 0) {
			return null;
		}
		String extensionFile = fileName.substring(n + 1);
		return extensionFile.toLowerCase();
	}

	void processFile(File fl, File destinationDir) {
		String fileName = fl.getName();
		String extensionFile = getExtensioOfFile(fl);
		if (extensionFile == null) {
			return;
		}
		extensionFile = extensionFile.toLowerCase();
		if (isImage(extensionFile)) {
			String panorama = getPanorama(fileName);
			if (StringUtils.isEmpty(panorama)) {
				copyPanoramaFile(fl, destinationDir, null);

			} else {
				copyPanoramaFile(fl, destinationDir, panorama);
			}
		} else {
			if (isVideo(extensionFile)) {
				copyPanoramaFile(fl, destinationDir, VIDEO);
			}
		}
	}

	String getPanorama(String fileName) {
		String s = fileName.toLowerCase();
		if (s.startsWith("st")) {
			if (s.length() > 8 && s.charAt(3) == '_') {
				String num = s.substring(5, 8);
				if (StringUtils.isNumeric(num)) {
					int index = s.charAt(2) - 'a';
					int numPanorama = Integer.parseInt(num) - index;
					String panoramaDir = "panorama_" + numPanorama;
					return panoramaDir;
				}
			}
		}
		return "";
	}

	String getExifDate(File file) {
		String s = extractExifDate(file);
		if (s!=null){
			return s;
		}
		try {
			return extractLastModifiedAsString(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	String extractExifDate(File file) {
		if (isImage(file) && isNotRaw(file)) {
			try {
				final IImageMetadata metadata = Imaging.getMetadata(file);
				final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
				final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
				TiffField field = exifMetadata.findField(new TagInfo(
						"DateTimeOriginal", 36867, FieldType.ASCII));
				if (field != null) {
					String s = String.valueOf(field.getValue());
					if (s.length() >= 10) {
						s = s.substring(0, 4) + "-" + s.substring(5, 7) + "-"
								+ s.substring(8, 10);
						if (simpleDateFormat.format(simpleDateFormat.parse(s))
								.equals(s)) {
							return s;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private boolean isNotRaw(File file) {
		String extension = getExtensioOfFile(file);
		if (StringUtils.isEmpty(extension)) {
			return true;
		}
		return !isRaw(extension.toLowerCase());
	}

	void copyPanoramaFile(File fl, File destinationDir, String panoramaDir) {

		String destinationName = getDestinationFileName(fl, destinationDir,
				panoramaDir);
		File fileDest = new File(destinationName);
		File parentDir = fileDest.getParentFile();
		if (!parentDir.exists()) {
			boolean res = parentDir.mkdirs();
			if (!res) {
				System.err.println("Error during creating dir "
						+ parentDir.getPath());
				System.exit(0);
			}
		}
		if (!fileDest.exists()) {
			if (this.moveFiles.isSelected()){
				// FileMove
				message("Move file from " + fl.getPath() + " to "
						+ fileDest.getPath());
				try {
					FileUtils.moveFile(fl, fileDest);
					this.countCopy++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			else{
			// FileCopy
			message("Copy file from " + fl.getPath() + " to "
					+ fileDest.getPath());
			try {
				FileUtils.copyFile(fl, fileDest);
				this.countCopy++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		} else {
			if (isTheSameFile(fl, fileDest)) {
				message("File already Exists " + fl.getPath());
				try {
					String copy  = existDir.getPath()+File.separator+fl.getName();
					File fileCopy = new File(copy);
					if (fileCopy.exists()) {
						if (isTheSameFile(fileCopy, fl))
							{
								FileUtils.deleteQuietly(fl);
							}
					}
					else{
						FileUtils.moveFileToDirectory(fl, existDir, true);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.countIdentical++;
			} else {
				this.countMistake++;
				errorMessage(" The file with the same name , but they are different"
						+ fl.getPath() + " to " + fileDest.getPath());
			}
		}
	}

	private String getDestinationFileName(File fl, File destinationDir,
			String panoramaDir) {
		String fileDate="NNNNNN";
		try {
			fileDate = extractLastModifiedAsString(fl);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sDate = this.useExifDate ? getExifDate(fl) : fileDate;
		String destinationName = destinationDir.getPath() + File.separator
				+ sDate.substring(0, 4) + File.separator + sDate
				+ File.separator
				+ (panoramaDir == null ? "" : panoramaDir + File.separator)
				+ fl.getName().toLowerCase();
		return destinationName;
	}

	private void errorMessage(String message) {
		System.err.println(message);
		logger.error(message);

	}


	boolean isTheSameFile(File fl, File fileDest) {
		boolean IsSameLength = fileDest.length() == fl.length();
		if (!IsSameLength) {
			logger.error("Length is different " + fl.getPath() + " "
					+ fileDest.getPath());
			return false;
		}
		boolean isSameContent =compareBytes(fl, fileDest, 32000);
		if (!isSameContent){
			logger.error("Content is different " + fl.getPath() + " "
					+ fileDest.getPath());
			return false;
		}
		return true;
	}

	String metaOfPhoto(File fileImage) {
		try {
			IImageMetadata metadata = Imaging.getMetadata(fileImage);
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			TiffImageMetadata exifMetadata = jpegMetadata.getExif();
			Object o1 = exifMetadata.findField(
					new TagInfo("Model", 272, FieldType.ASCII)).getValue();
			Object o2 = exifMetadata.findField(
					new TagInfo("DateTimeOriginal", 36867, FieldType.ASCII))
					.getValue();
			if ((o1 != null) && (o2 != null)) {
				return o1.toString() + o2.toString();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "" + System.currentTimeMillis() + "" + Math.random() + ""
				+ fileImage.hashCode();
	}

	private boolean compareBytes(File fl, File fileDest, int i) {
		BufferedInputStream fs1 = null;
		BufferedInputStream fs2 = null;
		try {
			fs1 = new BufferedInputStream(new FileInputStream(fl));
			fs2 = new BufferedInputStream(new FileInputStream(fileDest));
			byte[] b1 = new byte[i];
			byte[] b2 = new byte[i];
			readBytes(fs1, b1);
			readBytes(fs2, b2);
			for (int j = 0; j < b2.length; j++) {
				if (b2[j] != b1[j]) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		} finally {
			close(fs1);
			close(fs2);
		}
		return true;
	}

	private void close(InputStream fs1) {
		if (fs1 != null) {
			try {
				fs1.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void readBytes(BufferedInputStream fs1, byte[] b1)
			throws IOException {
		int offset = 0;
		int numRead = 0;
		while ((numRead = fs1.read(b1, offset, b1.length - offset)) > 0) {
			offset += numRead;
			if (offset == b1.length) {
				break;
			}
		}

	}

	boolean isImage(File file) {
		String extension = getExtensioOfFile(file);
		if (StringUtils.isEmpty(extension)) {
			return false;
		}
		return isImage(extension.toLowerCase());
	}

	boolean isImage(String extension) {
		for (int i = 0; i < extensionImage.length; i++) {
			if (extensionImage[i].equals(extension)) {
				return true;
			}
		}
		return false;
	}

	boolean isRaw(String extension) {
		for (int i = 0; i < extensionRaw.length; i++) {
			if (extensionRaw[i].equals(extension)) {
				return true;
			}
		}
		return false;
	}

	boolean isVideo(String extension) {
		for (int i = 0; i < extensionVideo.length; i++) {
			if (extensionVideo[i].equals(extension)) {
				return true;
			}
		}
		return false;
	}

}
