package ua.com.ostrog.photo.logic;

import java.io.File;

/**
 * One file discovered by {@link Controller#analyze}, together with the
 * outcome of analyzing it: its computed destination, whether that
 * destination move is possible, and how its EXIF/file dates compared.
 */
public class FileEntry {
private File source;
private File destination;
private FileDestination typeDestination;
private Boolean  exifDate;
private ImageType imageType;

/**
 * @param source the file as found on disk; never changes after construction
 */
public FileEntry(File source) {
    this.source = source;
}

/**
 * @return the file as found on disk
 */
public File getSource() {
    return source;
}

/**
 * @return the computed destination path, or {@code null} if none has been
 *         computed (e.g. the file's type is {@link ImageType#NOT_PROCESS})
 */
public File getDestination() {
    return destination;
}

/**
 * @param destination the computed destination path for {@link #getSource()}
 */
public void setDestination(File destination) {
    this.destination = destination;
}

/**
 * @return the analysis outcome for this entry, or {@code null} if none has
 *         been determined yet
 */
public FileDestination getTypeDestination() {
    return typeDestination;
}

/**
 * @param typeDestination the analysis outcome for this entry
 */
public void setTypeDestination(FileDestination typeDestination) {
    this.typeDestination = typeDestination;
}

/**
 * @return {@code true} if the file's EXIF date and its filesystem
 *         last-modified date agree, {@code false} if they disagree, or
 *         {@code null} if either date could not be determined
 */
public Boolean getExifDate() {
    return exifDate;
}

/**
 * @param exifDate result of comparing the file's EXIF date against its
 *                 filesystem last-modified date; {@code null} if either
 *                 date could not be determined
 */
public void setExifDate(Boolean exifDate) {
    this.exifDate = exifDate;
}

/**
 * @return the file's classification, or {@code null} if it has not been
 *         classified yet
 */
public ImageType getImageType() {
    return imageType;
}

/**
 * @param imageType the file's classification
 */
public void setImageType(ImageType imageType) {
    this.imageType = imageType;
}

@Override
public String toString() {
    return "FileEntry [source=" + source + ", destination=" + destination + ", typDestination=" + typeDestination
            + ", exifDate=" + exifDate + ", imageType=" + imageType + "]";
}

}
