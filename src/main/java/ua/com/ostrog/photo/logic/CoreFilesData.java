package ua.com.ostrog.photo.logic;

import java.io.File;

public class CoreFilesData {
private  File  destinationDir;
private File[] sourceFiles;
private boolean useExifDate;
public File getDestinationDir() {
    return destinationDir;
}
public CoreFilesData(File destinationDir, File[] sourceFiles, boolean useExifDate) {
    this.destinationDir = destinationDir;
    this.sourceFiles = sourceFiles;
    this.useExifDate = useExifDate;
}
public File[] getSourceFiles() {
    return sourceFiles;
}
public boolean isUseExifDate() {
    return useExifDate;
}

}
