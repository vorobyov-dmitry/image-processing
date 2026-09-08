package ua.com.ostrog.photo.logic;

/**
 * The user's decision when {@link Controller#processFiles} fails to move a file.
 */
public enum ErrorAction {
	/** Abandon the remaining entries; processing ends now. */
	STOP,
	/** Skip the failed entry and keep going; ask again on the next failure. */
	CONTINUE,
	/** Skip the failed entry and every future failure without asking again. */
	IGNORE_ALL
}
