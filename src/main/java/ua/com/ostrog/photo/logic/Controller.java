package ua.com.ostrog.photo.logic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ua.com.ostrog.photo.report.Report;

/**
 * Holds all the file analysing and moving logic that used to live in the
 * {@code Extractor} Swing frame. The UI now only collects the source files,
 * the destination directory and the "use EXIF date" flag and hands them to
 * {@link #analyze} / {@link #process}.
 */
public class Controller {
	private static final String EXISTS_DIR = "exists";

	private static final String VIDEO_DIR = "video";


	private static Logger logger = LogManager.getLogger(Controller.class);

	private String source = "";
	private File existDir = null;
	private int countCopy = 0;
	private int countIdentical = 0;
	private int countMistake = 0;
	private static final String[] ignoredFileName = new String[] {"thumbs.db","picasa.ini"};

	private static final String[] extensionImage = new String[] { "jpg",
			"jpeg", "crw","png" };
	private static final String[] extensionRaw = new String[] { "crw","cr2","nef" };

	private static final String[] extensionVideo = new String[] { "mov", "avi",
			"thm", "mp4","3gp","wmv" };
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	private boolean useExifDate = false;
	
	private List<String> filesProcessed = new ArrayList<String>();
	private List<String> filesNew = new ArrayList<String>();
	private List<String> filesErrors = new ArrayList<String>();
	private List<String> filesIgnored = new ArrayList<String>();
	private List<String> filesWrongDate = new ArrayList<String>();
	private AnalysisResult analysisResult = new AnalysisResult();



	private StringWriter writer = null;
	private String sMessage = null;

	public void analyze(CoreFilesData coreFilesData) {
		message("Start analyze files");
		this.useExifDate = coreFilesData.isUseExifDate();
		message("Start use exif date = " + useExifDate);
		this.source = coreFilesData.getSourceFiles()[0].getPath();
		existDir = new File(source + File.separator + EXISTS_DIR);
		this.filesErrors.clear();
		this.filesNew.clear();
		this.filesProcessed.clear();
		this.filesIgnored.clear();
		this.filesWrongDate.clear();
		sMessage = null;
		this.writer = new StringWriter();

		for (int i = 0; i < coreFilesData.getSourceFiles().length; i++) {
			File fl = coreFilesData.getSourceFiles()[i];
			message("From " + fl.getPath() + " to " + coreFilesData.getDestinationDir().getPath());
			if (fl.isDirectory()) {
				analyzeDir(fl, coreFilesData.getDestinationDir());
			} else {
				analyzeFile(fl, coreFilesData.getDestinationDir());
			}
		}
		message("============== finished =======");
		message("Processed " + this.filesProcessed.size());
		message("New  " + this.filesNew.size());
		message("error " + this.filesErrors.size());
		message("ignored " + this.filesIgnored.size());
		message("wrong date  " + this.filesWrongDate.size());
		// try {
		// 	FileUtils
		// 			.writeLines(new File(source+File.separator+"processed.lst"), this.filesProcessed);
		// 	FileUtils.writeLines(new File(source+File.separator+"new.lst"), this.filesNew);
		// 	FileUtils.writeLines(new File(source+File.separator+"errors.lst"), this.filesErrors);
		// 	FileUtils.writeLines(new File(source+File.separator+"ignored.lst"), this.filesIgnored);
		// 	if (this.filesWrongDate.size()>0){
		// 		this.filesWrongDate.add(0, "EXIF DATE  FILE DATE             FILE ");
		// 	}
		// 	FileUtils.writeLines(new File(source+File.separator+"wrongDate.lst"), this.filesWrongDate);
		// 	IOUtils.writeLines(this.filesProcessed,"\n" , writer);
		// 	writer.write("  New \n");
		// 	IOUtils.writeLines(this.filesNew, "\n", writer);
		// 	sMessage= writer.toString();
		// 	writer.close();

		// } catch (IOException exception) {
		// 	exception.printStackTrace();
		// }
		dispReport();
	}

	private void dispReport() {
		if (this.sMessage!=null)
		System.out.println(this.sMessage);
		this.analysisResult.showData();
		Report report = new Report();
		report.showReport(analysisResult);

	}

	private void analyzeFile(File fl, File destinationDir) {
		ImageType typeFile = getTypeFile(fl);
		String destinationFileName = null;
		FileEntry fileEntry = new FileEntry(fl);
		fileEntry.setImageType(typeFile);
		Boolean consistentExifDate = null;
		switch (typeFile) {
		case PANORAMA:
			destinationFileName = getDestinationFileName(fl, destinationDir,
					getPanorama(fl.getName()));
			consistentExifDate = checkConsistenceOfExifData(fl);
			break;
		case IMAGE:
			destinationFileName = getDestinationFileName(fl, destinationDir, "");
			consistentExifDate = checkConsistenceOfExifData(fl);
			break;
		case VIDEO:
			destinationFileName = getDestinationFileName(fl, destinationDir,
					VIDEO_DIR);
			break;
		case NOT_PROCESS:
			for (int i = 0; i < ignoredFileName.length; i++) {
				if (ignoredFileName[i].equalsIgnoreCase(fl.getName())){
					return;
				}
			}
			this.filesIgnored.add(fl.getPath());
			return;
		case RAW:
			destinationFileName = getDestinationFileNameForRAW(fl, destinationDir);
			break;

		}

		File destinationFile = new File(destinationFileName);
		fileEntry.setDestination(destinationFile);
		fileEntry.setExifDate(consistentExifDate);
		if (!destinationFile.exists()) {
			this.filesNew.add(fl.getPath());
			fileEntry.setTypDestination(FileDestination.NEW);
		} else {
			if (isTheSameFile(fl, destinationFile)) {
				this.filesProcessed.add(fl.getPath());
				fileEntry.setTypDestination(FileDestination.EXIST);
			} else {
				fileEntry.setTypDestination(FileDestination.ERROR);
				this.filesErrors.add(fl.getPath());
			}
		}
		this.analysisResult.addFileEntry(fileEntry);

	}

	private Boolean  checkConsistenceOfExifData(File fl) {
		String sDateExif = extractExifDate(fl);
		String sDateFile=null;
		try {
			sDateFile = extractLastModifiedAsString(fl);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block	
			e.printStackTrace();
		}
		if (sDateExif == null || sDateFile==null){
			return null;
		}
		Boolean b =  Objects.equals(sDateExif, sDateFile);
		if (!b){
				this.filesWrongDate.add(sDateExif+" "+sDateFile+" "+fl.getPath());
		}
		return b;
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

	private ImageType getTypeFile(File fl) {
		String fileName = fl.getName().toLowerCase();
		String extensionFile = getExtensioOfFile(fl);
		if (extensionFile == null) {
			return ImageType.NOT_PROCESS;
		}
		if (extensionImage[0].equals(extensionFile)
				&& fileName.startsWith("st") && fileName.charAt(3) == '_') {
			return ImageType.PANORAMA;
		}
		for (int i = 0; i < extensionImage.length; i++) {
			if (extensionImage[i].equals(extensionFile)) {
				return ImageType.IMAGE;
			}
		}
		for (int i = 0; i < extensionVideo.length; i++) {
			if (extensionVideo[i].equals(extensionFile)) {
				return ImageType.VIDEO;
			}
		}
		for (int i = 0; i < extensionRaw.length; i++) {
			if (extensionRaw[i].equals(extensionFile)) {
				return ImageType.RAW;
			}
		}
		return ImageType.NOT_PROCESS;

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

	public void process(File[] sourceFiles, File destinationDir, boolean useExifDate) {
		message("Start");
		countCopy = 0;
		countIdentical = 0;
		countMistake = 0;
		this.useExifDate = useExifDate;
		message(" use exif date = " + useExifDate);
		this.source = sourceFiles[0].getPath();
		existDir = new File(source + File.separator + EXISTS_DIR);

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
				copyPanoramaFile(fl, destinationDir, VIDEO_DIR);
			}
		}
	}

	String getPanorama(String fileName) {
		String s = fileName.toLowerCase();
		if (s.startsWith("st")) {
			if (s.length() > 8 && s.charAt(3) == '_') {
				String num = s.substring(4, 8);
				if (StringUtils.isNumeric(num)) {
					int index = s.charAt(2) - 'a';
					int numPanorama = Integer.parseInt(num) - index;
					String panoramaDir = "panorama_" + num;
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
				final ImageMetadata metadata = Imaging.getMetadata(file);
				final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
				final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
				TiffField field = exifMetadata
						.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
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
		else {
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

	private String getDestinationFileNameForRAW(File fl, File destinationDir) {
		String fileDate="NNNNNN";
		try {
			fileDate = extractLastModifiedAsString(fl);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sDate = this.useExifDate ? getExifDate(fl) : fileDate;
		String destinationName = destinationDir.getPath() + File.separator
				+ sDate.substring(0, 4) + File.separator + "RAW"
				+ File.separator
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
			ImageMetadata metadata = Imaging.getMetadata(fileImage);
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			TiffImageMetadata exifMetadata = jpegMetadata.getExif();
			Object o1 = exifMetadata.findField(TiffTagConstants.TIFF_TAG_MODEL)
					.getValue();
			Object o2 = exifMetadata
					.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL)
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
