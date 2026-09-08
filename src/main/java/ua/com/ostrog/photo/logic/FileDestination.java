package ua.com.ostrog.photo.logic;

/**
 * Outcome of analyzing one {@link FileEntry} against its computed
 * destination. The trailing comment on each constant names the icon
 * {@code ua.com.ostrog.photo.report.Report} displays for it in the STATE
 * column.
 */
public enum FileDestination {
    /** A file with the same content already exists at the destination. */
    EXIST, // check-square_blue.png
    /** The destination does not exist yet; the file is ready to be moved. */
    NEW,   //check-square_green.png
    /**
     * Not currently assigned by {@link Controller}; reserved for a future
     * "skip this file" outcome. {@code ua.com.ostrog.photo.report.Report}
     * already knows how to render it.
     */
    IGNORED, // frown_png
    /** A different file already occupies the destination path. */
    ERROR // close-square_red.png
}
