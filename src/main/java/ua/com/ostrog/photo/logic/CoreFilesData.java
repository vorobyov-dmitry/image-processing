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
public void setDestinationDir(File destinationDir) {
    this.destinationDir = destinationDir;
}
public File[] getSourceFiles() {
    return sourceFiles;
}
public void setSourceFiles(File[] sourceFiles) {
    this.sourceFiles = sourceFiles;
}
public boolean isUseExifDate() {
    return useExifDate;
}
public void setUseExifDate(boolean useExifDate) {
    this.useExifDate = useExifDate;
}


}
