package ua.com.ostrog.photo.logic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
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

/**
 * Holds all the file analysing and moving logic that used to live in the
 * {@code Extractor} Swing frame. The UI now only collects the source files,
 * the destination directory and the "use EXIF date" flag and hands them to
 * {@link #analyze} / {@link #process}.
 */
public class Controller {
	private static final String EXISTS_DIR = "exists";

	private static final String VIDEO_DIR = "video";


	private String source = "";
	private File existDir = null;
	private static final String[] ignoredFileName = new String[] {"thumbs.db","picasa.ini"};

	private static final String[] extensionImage = new String[] { "jpg",
			"jpeg", "crw","png" };
	private static final String[] extensionRaw = new String[] { "crw","cr2","nef" };

	private static final String[] extensionVideo = new String[] { "mov", "avi",
			"thm", "mp4","3gp","wmv" };
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");
	private boolean useExifDate = false;
	
	private AnalysisResult analysisResult;



	public void analyze(CoreFilesData coreFilesData) {
		this.useExifDate = coreFilesData.isUseExifDate();
		this.source = coreFilesData.getSourceFiles()[0].getPath();
		analysisResult = new AnalysisResult(this.source, coreFilesData.getDestinationDir().getPath());
		existDir = new File(source + File.separator + EXISTS_DIR);

		for (int i = 0; i < coreFilesData.getSourceFiles().length; i++) {
			File fl = coreFilesData.getSourceFiles()[i];
			if (fl.isDirectory()) {
				analyzeDir(fl, coreFilesData.getDestinationDir());
			} else {
				analyzeFile(fl, coreFilesData.getDestinationDir());
			}
		}
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
		    this.analysisResult.addFileEntry(fileEntry);
			return;
		case RAW:
			destinationFileName = getDestinationFileNameForRAW(fl, destinationDir);
			break;

		}

		File destinationFile = new File(destinationFileName);
		fileEntry.setDestination(destinationFile);
		fileEntry.setExifDate(consistentExifDate);
		if (!destinationFile.exists()) {
			fileEntry.setTypeDestination(FileDestination.NEW);
		} else {
			if (isTheSameFile(fl, destinationFile)) {
				fileEntry.setTypeDestination(FileDestination.EXIST);
			} else {
				fileEntry.setTypeDestination(FileDestination.ERROR);
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
		return Objects.equals(sDateExif, sDateFile);
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
		for (int i = 0; i < list.length; i++) {
			File entry = list[i];
			if (entry.isDirectory()) {
				analyzeDir(entry, destinationDir);
			} else {
				analyzeFile(entry, destinationDir);
			}
		}

	}


	String getExtensioOfFile(File file) {
		if (file == null) {
			return null;
		}
		String fileName = file.getName();
		if (isEmpty(fileName)) {
			return null;
		}
		int n = fileName.lastIndexOf(".");
		if (n < 0) {
			return null;
		}
		String extensionFile = fileName.substring(n + 1);
		return extensionFile.toLowerCase();
	}
	String getPanorama(String fileName) {
		String s = fileName.toLowerCase();
		if (s.startsWith("st")) {
			if (s.length() > 8 && s.charAt(3) == '_') {
				String num = s.substring(4, 8);
				if (isNumeric(num)) {
					int index = s.charAt(2) - 'a';
					String  numPanorama = String.valueOf(Integer.parseInt(num) - index);
					String panoramaDir = "panorama_" + leftPad(numPanorama, 4, '0') ;
					return panoramaDir;
				}
			}
		}
		return "";
	}

	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	private static boolean isNumeric(String value) {
		if (isEmpty(value)) {
			return false;
		}
		for (int i = 0; i < value.length(); i++) {
			if (!Character.isDigit(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static String leftPad(String value, int size, char padChar) {
		StringBuilder padded = new StringBuilder();
		for (int i = value.length(); i < size; i++) {
			padded.append(padChar);
		}
		return padded.append(value).toString();
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
		if (isEmpty(extension)) {
			return true;
		}
		return !isRaw(extension.toLowerCase());
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



	boolean isTheSameFile(File fl, File fileDest) {
		boolean IsSameLength = fileDest.length() == fl.length();
		if (!IsSameLength) {
			return false;
		}
		boolean isSameContent =compareBytes(fl, fileDest, 32000);
		if (!isSameContent){
			return false;
		}
		return true;
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
		if (isEmpty(extension)) {
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

	public AnalysisResult getAnalysisResult() {
		return analysisResult;
	}

	/**
	 * Moves every {@link FileEntry} whose {@link FileDestination} is
	 * {@code NEW} to its destination; entries with any other status are left
	 * where they are. {@code listener} (may be {@code null}) is notified
	 * after each entry is handled and is asked how to proceed when a move
	 * fails.
	 */
	public void processFiles(ProcessProgressListener listener) {
		List<FileEntry> filesEntries = analysisResult.getFilesEntries();
		int total = filesEntries.size();
		boolean ignoreAllErrors = false;
		for (int i = 0; i < total; i++) {
			FileEntry fileEntry = filesEntries.get(i);
			if (fileEntry.getTypeDestination() == FileDestination.NEW) {
				try {
					moveFile(fileEntry);
				} catch (IOException e) {
					System.err.println("Error moving file " + fileEntry.getSource() + " to "
							+ fileEntry.getDestination()+ e.getMessage());
					if (!ignoreAllErrors) {
						ErrorAction action = listener == null ? ErrorAction.STOP : listener.onError(fileEntry, e);
						if (action == ErrorAction.STOP) {
							break;
						}
						if (action == ErrorAction.IGNORE_ALL) {
							ignoreAllErrors = true;
						}
					}
				}
			}
			if (listener != null) {
				listener.onProgress(i + 1, total, fileEntry);
			}
		}
	}

	private void moveFile(FileEntry fileEntry) throws IOException {
		Path sourcePath = fileEntry.getSource().toPath();
		Path destinationPath = fileEntry.getDestination().toPath();
		Files.createDirectories(destinationPath.getParent());
		Files.move(sourcePath, destinationPath);
	}

}
