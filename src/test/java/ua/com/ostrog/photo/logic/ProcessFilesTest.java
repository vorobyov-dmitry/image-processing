package ua.com.ostrog.photo.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * "TEST Processing" scenarios from CLAUDE.MD. Unlike {@link AnalyzeImageFilesTest},
 * these do not inspect {@link FileEntry} values after the fact - they run
 * {@link Controller#processFiles} for real and check the filesystem
 * afterwards: files that should move are gone from the source and present at
 * the destination, files that should be left alone stay exactly where they
 * were.
 * <p>
 * {@code sourceDir} and {@code destinationDir} are JUnit-managed
 * {@link TempDir}s: a fresh pair is created before each test and removed
 * afterwards, matching the "before/after each test" setup CLAUDE.MD describes.
 */
class ProcessFilesTest {

	@TempDir
	Path sourceDir;

	@TempDir
	Path destinationDir;

	/**
	 * Extracts {@code SmokeTest.zip} into {@code sourceDir}, preserving its
	 * subdirectory structure and each entry's original modification time -
	 * {@link Controller} compares that timestamp against each photo's EXIF
	 * date, so the extracted files must keep their original date.
	 */
	@BeforeEach
	void extractSmokeTestZip() throws IOException {
		try (InputStream zipStream = getClass().getClassLoader().getResourceAsStream("SmokeTest.zip")) {
			assertNotNull(zipStream, "SmokeTest.zip must be on the test classpath");
			try (ZipInputStream zipInputStream = new ZipInputStream(zipStream)) {
				ZipEntry entry;
				while ((entry = zipInputStream.getNextEntry()) != null) {
					Path target = sourceDir.resolve(entry.getName()).normalize();
					if (entry.isDirectory()) {
						Files.createDirectories(target);
					} else {
						Files.createDirectories(target.getParent());
						Files.copy(zipInputStream, target);
						Files.setLastModifiedTime(target, FileTime.fromMillis(entry.getTime()));
					}
				}
			}
		}
	}

	/**
	 * Mirrors Test Case 1: a plain analyze of the untouched source directory
	 * makes every photo file {@code NEW}. Processing should move each of
	 * those away from the source and land them at their computed
	 * destination; the one file that is never given a destination
	 * ({@code text_file.txt}) must stay exactly where it was.
	 */
	@Test
	@DisplayName("Processing Test Case 1: new files are moved to their computed destination")
	void newFilesAreMovedToDestination() {
		Controller controller = new Controller();
		controller.analyze(new CoreFilesData(destinationDir.toFile(), new File[] { sourceDir.toFile() }, true));
		List<FileEntry> filesEntries = controller.getAnalysisResult().getFilesEntries();
		assertTrue(filesEntries.size() > 0, "sanity check: something should have been analyzed");

		controller.processFiles(null);

		for (FileEntry fileEntry : filesEntries) {
			if (fileEntry.getTypeDestination() == FileDestination.NEW) {
				assertFalse(Files.exists(fileEntry.getSource().toPath()),
						fileEntry.getSource() + ": should have been moved away from the source");
				assertTrue(Files.exists(fileEntry.getDestination().toPath()),
						fileEntry.getDestination() + ": should exist after processing");
			} else {
				assertTrue(Files.exists(fileEntry.getSource().toPath()),
						fileEntry.getSource() + ": not a NEW entry, must stay in place");
			}
		}
	}

	/**
	 * Mirrors Test Case 2: half of the {@code NEW} files are pre-copied to
	 * their destination before processing, so a second analyze reports them
	 * as {@code EXIST} rather than {@code NEW}. Processing must leave those
	 * pre-existing files untouched on both ends (nothing moved, nothing
	 * deleted) while still moving the untouched half as in Test Case 1.
	 */
	@Test
	@DisplayName("Processing Test Case 2: files already present in destination are left untouched")
	void existingFilesAreNotMoved() throws IOException {
		Controller firstPass = new Controller();
		firstPass.analyze(new CoreFilesData(destinationDir.toFile(), new File[] { sourceDir.toFile() }, true));

		List<FileEntry> newEntries = new ArrayList<>();
		for (FileEntry fileEntry : firstPass.getAnalysisResult().getFilesEntries()) {
			if (fileEntry.getTypeDestination() == FileDestination.NEW) {
				newEntries.add(fileEntry);
			}
		}
		List<FileEntry> shuffled = new ArrayList<>(newEntries);
		Collections.shuffle(shuffled, new Random(1));
		List<FileEntry> preCopied = shuffled.subList(0, shuffled.size() / 2);

		Set<File> preCopiedSources = new HashSet<>();
		for (FileEntry fileEntry : preCopied) {
			preCopiedSources.add(fileEntry.getSource());
			Path destinationPath = fileEntry.getDestination().toPath();
			Files.createDirectories(destinationPath.getParent());
			Files.copy(fileEntry.getSource().toPath(), destinationPath, StandardCopyOption.COPY_ATTRIBUTES);
		}
		assertFalse(preCopiedSources.isEmpty(), "at least one file should have been pre-copied");

		Controller secondPass = new Controller();
		secondPass.analyze(new CoreFilesData(destinationDir.toFile(), new File[] { sourceDir.toFile() }, true));
		List<FileEntry> filesEntries = secondPass.getAnalysisResult().getFilesEntries();

		secondPass.processFiles(null);

		for (FileEntry fileEntry : filesEntries) {
			if (preCopiedSources.contains(fileEntry.getSource())) {
				assertTrue(Files.exists(fileEntry.getSource().toPath()),
						fileEntry.getSource() + ": pre-existing file must not be moved away");
				assertTrue(Files.exists(fileEntry.getDestination().toPath()),
						fileEntry.getDestination() + ": pre-existing destination must still exist");
			} else if (fileEntry.getTypeDestination() == FileDestination.NEW) {
				assertFalse(Files.exists(fileEntry.getSource().toPath()),
						fileEntry.getSource() + ": should have been moved away from the source");
				assertTrue(Files.exists(fileEntry.getDestination().toPath()),
						fileEntry.getDestination() + ": should exist after processing");
			} else {
				assertTrue(Files.exists(fileEntry.getSource().toPath()),
						fileEntry.getSource() + ": not a NEW entry, must stay in place");
			}
		}
	}
}
