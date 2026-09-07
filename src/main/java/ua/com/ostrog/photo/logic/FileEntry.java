package ua.com.ostrog.photo.logic;

import java.io.File;

public class FileEntry {
private File source;
private File destination;
private FileDestination typeDestination;
private Boolean  exifDate;
private ImageType imageType;

public FileEntry(File source) {
    this.source = source;
}
public File getSource() {
    return source;
}
public File getDestination() {
    return destination;
}
public void setDestination(File destination) {
    this.destination = destination;
}
public FileDestination getTypeDestination() {
    return typeDestination;
}
public void setTypeDestination(FileDestination typeDestination) {
    this.typeDestination = typeDestination;
}
public Boolean getExifDate() {
    return exifDate;
}
public void setExifDate(Boolean exifDate) {
    this.exifDate = exifDate;
}
public ImageType getImageType() {
    return imageType;
}
public void setImageType(ImageType imageType) {
    this.imageType = imageType;
}
@Override
public String toString() {
    return "FileEntry [source=" + source + ", destination=" + destination + ", typDestination=" + typeDestination
            + ", exifDate=" + exifDate + ", imageType=" + imageType + "]";
}

}
