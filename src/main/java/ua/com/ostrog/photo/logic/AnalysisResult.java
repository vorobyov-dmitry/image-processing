package ua.com.ostrog.photo.logic;

import java.util.LinkedList;
import java.util.List;

/**
 * Result of a single {@link Controller#analyze} run: the source and
 * destination directories that were analyzed, plus one {@link FileEntry} per
 * file that was found under the source directory.
 */
public class AnalysisResult {
    private List<FileEntry> filesEntries = new LinkedList<>();
    private String destinationDirectory;
    private String sourceDirectory;

    /**
     * Creates an empty result for the given source and destination
     * directories; entries are added afterwards via {@link #addFileEntry}.
     *
     * @param sourceDirectory      path of the directory that was analyzed
     * @param destinationDirectory path of the directory files would be moved to
     */
    public AnalysisResult(String sourceDirectory, String destinationDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.destinationDirectory = destinationDirectory;
    }

    /**
     * @return path of the directory files are (or would be) moved to
     */
    public String getDestinationDirectory() {
        return destinationDirectory;
    }

    /**
     * @return path of the directory that was analyzed
     */
    public String getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * Appends one analyzed file to the result.
     *
     * @param fileEntry the entry to add
     */
    public void addFileEntry(FileEntry fileEntry) {
        this.filesEntries.add(fileEntry);
    }

    /**
     * @return the entries collected so far, one per analyzed file
     */
    public List<FileEntry> getFilesEntries() {
        return filesEntries;
    }

}
