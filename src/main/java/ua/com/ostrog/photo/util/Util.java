package ua.com.ostrog.photo.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;

import ua.com.ostrog.photo.logic.Controller;
import ua.com.ostrog.photo.logic.ImageType;

/**
 * Stateless file/EXIF helpers used by {@link Controller} when analyzing
 * source files: date extraction/comparison, file-type classification, and
 * byte-for-byte content comparison. Every method here is static and side
 * effect free (besides the {@code printStackTrace()} calls carried over
 * from the original code).
 */
public final class Util {

	private static final String[] EXTENSION_IMAGE = { "jpg", "jpeg", "crw", "png" };
	private static final String[] EXTENSION_RAW = { "crw", "cr2", "nef" };
	private static final String[] EXTENSION_VIDEO = { "mov", "avi", "thm", "mp4", "3gp", "wmv" };

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private Util() {
	}

	/**
	 * @return {@code true} if {@code fl}'s EXIF date and its filesystem
	 *         last-modified date agree, {@code false} if they disagree, or
	 *         {@code null} if either date could not be determined
	 */
	public static Boolean checkConsistenceOfExifData(File fl) {
		String sDateExif = extractExifDate(fl);
		String sDateFile = null;
		try {
			sDateFile = extractLastModifiedAsString(fl);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (sDateExif == null || sDateFile == null) {
			return null;
		}
		return Objects.equals(sDateExif, sDateFile);
	}

	/**
	 * @return {@code fl}'s filesystem last-modified date, formatted as
	 *         {@code yyyy-MM-dd}
	 * @throws FileNotFoundException if {@code fl} is {@code null} or does not exist
	 */
	public static String extractLastModifiedAsString(File fl) throws FileNotFoundException {
		if (fl == null) {
			throw new FileNotFoundException(" The null file");
		}
		if (!fl.exists()) {
			throw new FileNotFoundException(" The file" + fl.getPath() + " does not exist!!!");
		}
		GregorianCalendar calendar = new GregorianCalendar();
		long millis = fl.lastModified();
		calendar.setTimeInMillis(millis);
		if (calendar.get(Calendar.HOUR_OF_DAY) == 0) {
			calendar.roll(Calendar.HOUR_OF_DAY, false);
			calendar.roll(Calendar.DAY_OF_MONTH, false);
		}
		return DATE_FORMAT.format(calendar.getTime());
	}

	/**
	 * Classifies {@code fl} by its file extension and, for a jpg whose name
	 * starts with "st" and has a '_' as its 4th character, as a
	 * {@link ImageType#PANORAMA} frame.
	 */
	public static ImageType getTypeFile(File fl) {
		String fileName = fl.getName().toLowerCase();
		String extensionFile = getExtensioOfFile(fl);
		if (extensionFile == null) {
			return ImageType.NOT_PROCESS;
		}
		if (EXTENSION_IMAGE[0].equals(extensionFile) && fileName.startsWith("st") && fileName.charAt(3) == '_') {
			return ImageType.PANORAMA;
		}
		for (String extension : EXTENSION_IMAGE) {
			if (extension.equals(extensionFile)) {
				return ImageType.IMAGE;
			}
		}
		for (String extension : EXTENSION_VIDEO) {
			if (extension.equals(extensionFile)) {
				return ImageType.VIDEO;
			}
		}
		for (String extension : EXTENSION_RAW) {
			if (extension.equals(extensionFile)) {
				return ImageType.RAW;
			}
		}
		return ImageType.NOT_PROCESS;
	}

	/**
	 * @return {@code file}'s extension, lower-cased, or {@code null} if it
	 *         is {@code null} or has no extension
	 */
	public static String getExtensioOfFile(File file) {
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

	/**
	 * @return {@code true} if {@code value} is {@code null} or empty
	 */
	public static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	/**
	 * @return {@code true} if {@code value} is non-empty and every character
	 *         is a digit
	 */
	public static boolean isNumeric(String value) {
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

	/**
	 * @return {@code value} left-padded with {@code padChar} up to
	 *         {@code size} characters; {@code value} itself if it is already
	 *         at least that long
	 */
	public static String leftPad(String value, int size, char padChar) {
		StringBuilder padded = new StringBuilder();
		for (int i = value.length(); i < size; i++) {
			padded.append(padChar);
		}
		return padded.append(value).toString();
	}

	/**
	 * @return the {@code panorama_NNNN} subdirectory name for a panorama
	 *         frame named like {@code stB_1234...}, where {@code NNNN} is
	 *         the sequence's first frame number (the digits shifted back by
	 *         the frame's letter offset from 'a'); {@code ""} if
	 *         {@code fileName} does not match that pattern
	 */
	public static String getPanorama(String fileName) {
		String s = fileName.toLowerCase();
		if (s.startsWith("st")) {
			if (s.length() > 8 && s.charAt(3) == '_') {
				String num = s.substring(4, 8);
				if (isNumeric(num)) {
					int index = s.charAt(2) - 'a';
					String numPanorama = String.valueOf(Integer.parseInt(num) - index);
					String panoramaDir = "panorama_" + leftPad(numPanorama, 4, '0');
					return panoramaDir;
				}
			}
		}
		return "";
	}

	/**
	 * @return {@code file}'s EXIF date if it has one, otherwise its
	 *         filesystem last-modified date; {@code null} if neither could
	 *         be determined
	 */
	public static String getExifDate(File file) {
		String s = extractExifDate(file);
		if (s != null) {
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

	/**
	 * @return the EXIF "date taken" of {@code file} if it is a non-RAW image
	 *         carrying one, otherwise {@code null}
	 */
	public static String extractExifDate(File file) {
		if (isImage(file) && isNotRaw(file)) {
			try {
				final ImageMetadata metadata = Imaging.getMetadata(file);
				final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
				final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
				TiffField field = exifMetadata.findField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
				if (field != null) {
					String s = String.valueOf(field.getValue());
					if (s.length() >= 10) {
						s = s.substring(0, 4) + "-" + s.substring(5, 7) + "-" + s.substring(8, 10);
						if (DATE_FORMAT.format(DATE_FORMAT.parse(s)).equals(s)) {
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

	/**
	 * @return {@code true} if {@code file} is not a camera RAW file (or has
	 *         no extension at all)
	 */
	public static boolean isNotRaw(File file) {
		String extension = getExtensioOfFile(file);
		if (isEmpty(extension)) {
			return true;
		}
		return !isRaw(extension.toLowerCase());
	}

	/**
	 * @return {@code true} if {@code fl} and {@code fileDest} have the same
	 *         length and identical content in their first 32000 bytes
	 */
	public static boolean isTheSameFile(File fl, File fileDest) {
		boolean IsSameLength = fileDest.length() == fl.length();
		if (!IsSameLength) {
			return false;
		}
		boolean isSameContent = compareBytes(fl, fileDest, 32000);
		if (!isSameContent) {
			return false;
		}
		return true;
	}

	/**
	 * @return {@code true} if the first {@code i} bytes of {@code fl} and
	 *         {@code fileDest} are identical (fewer bytes are compared if
	 *         either file is shorter than {@code i})
	 */
	public static boolean compareBytes(File fl, File fileDest, int i) {
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

	private static void close(InputStream fs1) {
		if (fs1 != null) {
			try {
				fs1.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void readBytes(BufferedInputStream fs1, byte[] b1) throws IOException {
		int offset = 0;
		int numRead = 0;
		while ((numRead = fs1.read(b1, offset, b1.length - offset)) > 0) {
			offset += numRead;
			if (offset == b1.length) {
				break;
			}
		}
	}

	private static boolean isImage(File file) {
		String extension = getExtensioOfFile(file);
		if (isEmpty(extension)) {
			return false;
		}
		return isImage(extension.toLowerCase());
	}

	private static boolean isImage(String extension) {
		for (String candidate : EXTENSION_IMAGE) {
			if (candidate.equals(extension)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isRaw(String extension) {
		for (String candidate : EXTENSION_RAW) {
			if (candidate.equals(extension)) {
				return true;
			}
		}
		return false;
	}
}
