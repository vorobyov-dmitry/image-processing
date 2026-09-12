package ua.com.ostrog.photo.logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import ua.com.ostrog.photo.util.Util;

/**
 * Holds all the file analysing and moving logic that used to live in the
 * {@code Extractor} Swing frame. The UI now only collects the source files,
 * the destination directory and the "use EXIF date" flag and hands them to
 * {@link #analyze} / {@link #processFiles}.
 */
public class Controller {
	private static final String EXISTS_DIR = "exists";

	private static final String VIDEO_DIR = "video";


	private String source = "";
	private File existDir = null;
	private static final String[] ignoredFileName = new String[] {"thumbs.db","picasa.ini"};

	private boolean useExifDate = false;
	
	private AnalysisResult analysisResult;



	/**
	 * Walks every file/directory in {@code coreFilesData}'s source files and
	 * builds a fresh {@link AnalysisResult} describing where each one would
	 * end up if moved, without touching the filesystem. The result is stored
	 * and can be retrieved with {@link #getAnalysisResult()}, and later moved
	 * for real with {@link #processFiles}.
	 *
	 * @param coreFilesData source files/directories, destination directory
	 *                      and whether to prefer the EXIF date over the file's
	 *                      last-modified date
	 */
	public void analyze(CoreFilesData coreFilesData) {
		this.useExifDate = coreFilesData.isUseExifDate();
		this.source = coreFilesData.getSourceFiles()[0].getPath();
		analysisResult = new AnalysisResult(this.source, coreFilesData.getDestinationDir().getPath());
		existDir = new File(source + File.separator + EXISTS_DIR);

		for (int i = 0; i < coreFilesData.getSourceFiles().length; i++) {
			File fl = coreFilesData.getSourceFiles()[i];
			if (fl.isDirectory()) {
				analyzeDir(fl, coreFilesData.getDestinationDir());
			} else {
				analyzeFile(fl, coreFilesData.getDestinationDir());
			}
		}
	}

	private void analyzeFile(File fl, File destinationDir) {
		ImageType typeFile = Util.getTypeFile(fl);
		String destinationFileName = null;
		FileEntry fileEntry = new FileEntry(fl);
		fileEntry.setImageType(typeFile);
		Boolean consistentExifDate = null;
		switch (typeFile) {
		case PANORAMA:
			destinationFileName = getDestinationFileName(fl, destinationDir,
					Util.getPanorama(fl.getName()));
			consistentExifDate = Util.checkConsistenceOfExifData(fl);
			break;
		case IMAGE:
			destinationFileName = getDestinationFileName(fl, destinationDir, "");
			consistentExifDate = Util.checkConsistenceOfExifData(fl);
			break;
		case VIDEO:
			destinationFileName = getDestinationFileName(fl, destinationDir,
					VIDEO_DIR);
			break;
		case NOT_PROCESS:
			for (int i = 0; i < ignoredFileName.length; i++) {
				if (ignoredFileName[i].equalsIgnoreCase(fl.getName())){
					return;
				}
			}
		    this.analysisResult.addFileEntry(fileEntry);
			return;
		case RAW:
			destinationFileName = getDestinationFileNameForRAW(fl, destinationDir);
			break;

		}

		File destinationFile = new File(destinationFileName);
		fileEntry.setDestination(destinationFile);
		fileEntry.setExifDate(consistentExifDate);
		if (!destinationFile.exists()) {
			fileEntry.setTypeDestination(FileDestination.NEW);
		} else {
			if (Util.isTheSameFile(fl, destinationFile)) {
				fileEntry.setTypeDestination(FileDestination.EXIST);
			} else {
				fileEntry.setTypeDestination(FileDestination.ERROR);
			}
		}
		this.analysisResult.addFileEntry(fileEntry);

	}

	private void analyzeDir(File fl, File destinationDir) {
		if (fl.getPath().equalsIgnoreCase(existDir.getPath())){
			return;
		}
		File[] list = fl.listFiles();
		for (int i = 0; i < list.length; i++) {
			File entry = list[i];
			if (entry.isDirectory()) {
				analyzeDir(entry, destinationDir);
			} else {
				analyzeFile(entry, destinationDir);
			}
		}

	}


	private String getDestinationFileName(File fl, File destinationDir,
			String panoramaDir) {
		String fileDate="NNNNNN";
		try {
			fileDate = Util.extractLastModifiedAsString(fl);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sDate = this.useExifDate ? Util.getExifDate(fl) : fileDate;
		String destinationName = destinationDir.getPath() + File.separator
				+ sDate.substring(0, 4) + File.separator + sDate
				+ File.separator
				+ (panoramaDir == null ? "" : panoramaDir + File.separator)
				+ fl.getName().toLowerCase();
		return destinationName;
	}

	private String getDestinationFileNameForRAW(File fl, File destinationDir) {
		String fileDate="NNNNNN";
		try {
			fileDate = Util.extractLastModifiedAsString(fl);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sDate = this.useExifDate ? Util.getExifDate(fl) : fileDate;
		String destinationName = destinationDir.getPath() + File.separator
				+ sDate.substring(0, 4) + File.separator + "RAW"
				+ File.separator
				+ fl.getName().toLowerCase();
		return destinationName;
	}

	/**
	 * @return the result of the most recent {@link #analyze} call, or
	 *         {@code null} if {@link #analyze} has not been called yet
	 */
	public AnalysisResult getAnalysisResult() {
		return analysisResult;
	}

	/**
	 * Moves every {@link FileEntry} whose {@link FileDestination} is
	 * {@code NEW} to its destination; entries with any other status are left
	 * where they are.
	 *
	 * @param listener notified after each entry is handled and asked how to
	 *                 proceed when a move fails; may be {@code null}, in
	 *                 which case any move failure stops processing
	 */
	public void processFiles(ProcessProgressListener listener) {
		List<FileEntry> filesEntries = analysisResult.getFilesEntries();
		int total = filesEntries.size();
		boolean ignoreAllErrors = false;
		for (int i = 0; i < total; i++) {
			FileEntry fileEntry = filesEntries.get(i);
			if (fileEntry.getTypeDestination() == FileDestination.NEW) {
				try {
					moveFile(fileEntry);
				} catch (IOException e) {
					System.err.println("Error moving file " + fileEntry.getSource() + " to "
							+ fileEntry.getDestination()+ e.getMessage());
					if (!ignoreAllErrors) {
						ErrorAction action = listener == null ? ErrorAction.STOP : listener.onError(fileEntry, e);
						if (action == ErrorAction.STOP) {
							break;
						}
						if (action == ErrorAction.IGNORE_ALL) {
							ignoreAllErrors = true;
						}
					}
				}
			}
			if (listener != null) {
				listener.onProgress(i + 1, total, fileEntry);
			}
		}
	}

	private void moveFile(FileEntry fileEntry) throws IOException {
		Path sourcePath = fileEntry.getSource().toPath();
		Path destinationPath = fileEntry.getDestination().toPath();
		Files.createDirectories(destinationPath.getParent());
		Files.move(sourcePath, destinationPath);
	}

}
