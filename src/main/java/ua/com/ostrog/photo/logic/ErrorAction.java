package ua.com.ostrog.photo.logic;

/**
 * The user's decision when {@link Controller#processFiles} fails to move a file.
 */
public enum ErrorAction {
	STOP, CONTINUE, IGNORE_ALL
}
