package ua.com.ostrog.photo.logic;

import java.util.LinkedList;
import java.util.List;

public class AnalysisResult {
    private List<FileEntry> filesEntries = new LinkedList<>();
    private String destinationDirectory;
    private String sourceDirectory;
    
    public AnalysisResult(String sourceDirectory, String destinationDirectory) {
        this.sourceDirectory = sourceDirectory;
        this.destinationDirectory = destinationDirectory;
    }

    public String getDestinationDirectory() {
        return destinationDirectory;
    }

    public void setDestinationDirectory(String destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }



    public void addFileEntry(FileEntry fileEntry) {
        this.filesEntries.add(fileEntry);
    }

    public void showData() {
        for (FileEntry fileEntry : filesEntries) {
            System.out.println(fileEntry);
        }
    }

    public List<FileEntry> getFilesEntries() {
        return filesEntries;
    }

}
