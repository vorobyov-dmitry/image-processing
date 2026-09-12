package ua.com.ostrog.photo.logic;

import java.io.File;

/**
 * Immutable input to {@link Controller#analyze}: the files/directories to
 * scan, the directory to move them into, and whether the EXIF date should be
 * preferred over the file's last-modified date when computing destinations.
 */
public class CoreFilesData {
private  File  destinationDir;
private File[] sourceFiles;
private boolean useExifDate;

/**
 * @return directory that files would be moved into
 */
public File getDestinationDir() {
    return destinationDir;
}

/**
 * @param destinationDir directory that files would be moved into
 * @param sourceFiles    files and/or directories to analyze; directories
 *                       are scanned recursively
 * @param useExifDate    {@code true} to prefer each image's EXIF date over
 *                       its filesystem last-modified date when computing
 *                       its destination
 */
public CoreFilesData(File destinationDir, File[] sourceFiles, boolean useExifDate) {
    this.destinationDir = destinationDir;
    this.sourceFiles = sourceFiles;
    this.useExifDate = useExifDate;
}

/**
 * @return files and/or directories to analyze
 */
public File[] getSourceFiles() {
    return sourceFiles;
}

/**
 * @return {@code true} if the EXIF date should be preferred over the
 *         file's last-modified date
 */
public boolean isUseExifDate() {
    return useExifDate;
}

}
