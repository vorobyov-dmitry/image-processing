package ua.com.ostrog.photo.logic;

/**
 * Callback used by {@link Controller#processFiles} to report how many files
 * were processed and to ask how to proceed when moving a file fails.
 */
public interface ProcessProgressListener {

	void onProgress(int processed, int total, FileEntry fileEntry);

	ErrorAction onError(FileEntry fileEntry, Exception exception);
}
