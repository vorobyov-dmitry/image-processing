package ua.com.ostrog.photo.logic;

/**
 * Callback used by {@link Controller#processFiles} to report how many files
 * were processed and to ask how to proceed when moving a file fails.
 */
public interface ProcessProgressListener {

	/**
	 * Called after each {@link FileEntry} has been handled (moved, skipped,
	 * or failed), including after a failed move regardless of the
	 * {@link ErrorAction} chosen.
	 *
	 * @param processed number of entries handled so far, including this one
	 * @param total     total number of entries being processed
	 * @param fileEntry the entry that was just handled
	 */
	void onProgress(int processed, int total, FileEntry fileEntry);

	/**
	 * Called when moving a file fails, to decide how {@link Controller#processFiles}
	 * should proceed.
	 *
	 * @param fileEntry the entry whose move failed
	 * @param exception the exception raised by the failed move
	 * @return {@link ErrorAction#STOP} to abandon the remaining entries,
	 *         {@link ErrorAction#CONTINUE} to skip this entry and ask again
	 *         on the next failure, or {@link ErrorAction#IGNORE_ALL} to skip
	 *         this and all future failures silently
	 */
	ErrorAction onError(FileEntry fileEntry, Exception exception);
}
