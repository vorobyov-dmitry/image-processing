package ua.com.ostrog.photo.logic;

import java.util.LinkedList;
import java.util.List;

public class AnalysisResult {
    private List<FileEntry>  filesEntries = new LinkedList<>();
public void addFileEntry(FileEntry fileEntry){
    this.filesEntries.add(fileEntry);
}
public void showData(){
    for (FileEntry fileEntry : filesEntries) {
        System.out.println(fileEntry);
    }
}
public List<FileEntry> getFilesEntries(){
    return filesEntries;
}
}
