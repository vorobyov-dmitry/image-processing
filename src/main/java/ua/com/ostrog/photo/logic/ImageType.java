package ua.com.ostrog.photo.logic;

/**
 * Classification of a source file, assigned by
 * {@link Controller#analyze} based on its file extension and, for
 * {@link #PANORAMA}, its filename pattern.
 */
public enum ImageType {
		/** A regular still image (jpg, jpeg, crw, png). */
		IMAGE,
		/** A video file (mov, avi, thm, mp4, 3gp, wmv). */
		VIDEO,
		/** Not a recognized image/video/RAW file; left where it is. */
		NOT_PROCESS,
		/** A JPEG that is part of a panorama sequence (filename like {@code stA_1234...}). */
		PANORAMA,
		/** A camera RAW file (crw, cr2, nef). */
		RAW
	}

