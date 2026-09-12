package ua.com.ostrog.photo.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * "TEST Analyze / Testing IMAGE" scenarios from CLAUDE.MD.
 * <p>
 * {@code sourceDir} and {@code destinationDir} are JUnit-managed
 * {@link TempDir}s: a fresh pair is created before each test and removed
 * afterwards, matching the "before/after each test" setup described there.
 */
class AnalyzeImageFilesTest {

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

	@Test
	@DisplayName("Test Case 1: all OK with input data for image files")
	void testCase1_allOkForImageFiles() {
		Controller controller = new Controller();
		controller.analyze(new CoreFilesData(destinationDir.toFile(), new File[] { sourceDir.toFile() }, true));
		AnalysisResult result = controller.getAnalysisResult();

		Map<String, FileEntry> bySourceSuffix = indexBySourceSuffix(result);
		assertEquals(22, bySourceSuffix.size(), "number of analyzed files");

		assertPhoto(bySourceSuffix, "01/sta_0626.jpg", "2013/2013-12-05/panorama_0626", ImageType.PANORAMA);
		assertPhoto(bySourceSuffix, "01/stb_0627.jpg", "2013/2013-12-05/panorama_0626", ImageType.PANORAMA);
		assertPhoto(bySourceSuffix, "01/stc_0628.jpg", "2013/2013-12-05/panorama_0626", ImageType.PANORAMA);
		assertPhoto(bySourceSuffix, "02/00001.jpg", "2017/2017-05-31", ImageType.IMAGE);
		assertPhoto(bySourceSuffix, "02/img_7388.jpg", "2019/2019-03-03", ImageType.IMAGE);
		assertPhoto(bySourceSuffix, "02/img_7389.jpg", "2019/2019-03-03", ImageType.IMAGE);
		assertPhoto(bySourceSuffix, "02/img_7390.jpg", "2019/2019-03-03", ImageType.IMAGE);
		assertPhoto(bySourceSuffix, "img_7391.jpg", "2019/2019-03-03", ImageType.IMAGE);
		assertPhoto(bySourceSuffix, "img_7392.jpg", "2019/2019-03-03", ImageType.IMAGE);
		assertPhoto(bySourceSuffix, "img_7393.jpg", "2019/2019-03-03", ImageType.IMAGE);
		assertPhoto(bySourceSuffix, "03/sta_7455.jpg", "2019/2019-03-03/panorama_7455", ImageType.PANORAMA);
		assertPhoto(bySourceSuffix, "03/stb_7456.jpg", "2019/2019-03-03/panorama_7455", ImageType.PANORAMA);
		assertPhoto(bySourceSuffix, "03/stc_7457.jpg", "2019/2019-03-03/panorama_7455", ImageType.PANORAMA);
		assertPhoto(bySourceSuffix, "03/std_7458.jpg", "2019/2019-03-03/panorama_7455", ImageType.PANORAMA);
		assertPhoto(bySourceSuffix, "03/ste_7459.jpg", "2019/2019-03-03/panorama_7455", ImageType.PANORAMA);
		assertPhoto(bySourceSuffix, "03/stf_7460.jpg", "2019/2019-03-03/panorama_7455", ImageType.PANORAMA);
		assertPhoto(bySourceSuffix, "img_8301.jpg", "2020/2020-04-28", ImageType.IMAGE);
		assertPhoto(bySourceSuffix, "img_8302.jpg", "2020/2020-04-28", ImageType.IMAGE);
		assertPhoto(bySourceSuffix, "img_8303.jpg", "2020/2020-04-28", ImageType.IMAGE);
		assertPhoto(bySourceSuffix, "img_8304.jpg", "2020/2020-04-28", ImageType.IMAGE);
		assertPhoto(bySourceSuffix, "img_8305.jpg", "2020/2020-04-28", ImageType.IMAGE);

		FileEntry textFile = bySourceSuffix.get("text_file.txt");
		assertNotNull(textFile, "text_file.txt should be analyzed");
		assertNull(textFile.getDestination(), "text_file.txt: destination");
		assertNull(textFile.getTypeDestination(), "text_file.txt: status");
		assertNull(textFile.getExifDate(), "text_file.txt: exif date consistency");
		assertEquals(ImageType.NOT_PROCESS, textFile.getImageType(), "text_file.txt: image type");
	}

	/**
	 * Asserts the outcome CLAUDE.MD's Test Case 1 expects for every photo
	 * file: analyzed as new, EXIF date consistent with the file date, moved
	 * under the given destination subdirectory, and classified as
	 * {@code expectedImageType}.
	 */
	private void assertPhoto(Map<String, FileEntry> bySourceSuffix, String sourceSuffix, String destinationDirSuffix,
			ImageType expectedImageType) {
		FileEntry fileEntry = bySourceSuffix.get(sourceSuffix);
		assertNotNull(fileEntry, sourceSuffix + ": should have been analyzed");
		assertEquals(FileDestination.NEW, fileEntry.getTypeDestination(), sourceSuffix + ": status");
		assertEquals(Boolean.TRUE, fileEntry.getExifDate(), sourceSuffix + ": exif date consistency");
		assertEquals(expectedImageType, fileEntry.getImageType(), sourceSuffix + ": image type");

		String fileName = Path.of(sourceSuffix).getFileName().toString().toLowerCase();
		String expectedDestinationSuffix = destinationDirSuffix + "/" + fileName;
		assertNotNull(fileEntry.getDestination(), sourceSuffix + ": destination");
		String actualDestinationSuffix = destinationDir.relativize(fileEntry.getDestination().toPath()).toString()
				.replace(File.separatorChar, '/');
		assertEquals(expectedDestinationSuffix, actualDestinationSuffix, sourceSuffix + ": destination");
	}

	/**
	 * Test Case 2 from CLAUDE.MD: analyze the source directory, copy a random
	 * half of the resulting {@code NEW} entries straight to their computed
	 * destination (so those destinations now hold byte-identical copies of
	 * the source), then analyze the same source/destination pair again. The
	 * copied half should now come back as {@link FileDestination#EXIST}
	 * while the untouched half stays {@link FileDestination#NEW}.
	 */
	@Test
	@DisplayName("Test Case 2: all OK with input data but some files are present in destination")
	void testCase2_someFilesAlreadyInDestination() throws IOException {
		Controller firstPass = new Controller();
		firstPass.analyze(new CoreFilesData(destinationDir.toFile(), new File[] { sourceDir.toFile() }, true));

		List<FileEntry> newEntries = new ArrayList<>();
		for (FileEntry fileEntry : firstPass.getAnalysisResult().getFilesEntries()) {
			if (fileEntry.getTypeDestination() == FileDestination.NEW) {
				newEntries.add(fileEntry);
			}
		}
		FileEntry[] newEntriesArray = newEntries.toArray(new FileEntry[0]);
		List<FileEntry> shuffled = new ArrayList<>(List.of(newEntriesArray));
		Collections.shuffle(shuffled, new Random(1));
		List<FileEntry> preCopied = shuffled.subList(0, shuffled.size() / 2);

		Set<String> preCopiedSourceSuffixes = new HashSet<>();
		for (FileEntry fileEntry : preCopied) {
			preCopiedSourceSuffixes.add(sourceSuffix(fileEntry));
			Path destinationPath = fileEntry.getDestination().toPath();
			Files.createDirectories(destinationPath.getParent());
			Files.copy(fileEntry.getSource().toPath(), destinationPath, StandardCopyOption.COPY_ATTRIBUTES);
		}
		assertFalse(preCopiedSourceSuffixes.isEmpty(), "at least one file should have been pre-copied");
		assertTrue(preCopiedSourceSuffixes.size() < newEntriesArray.length,
				"only half of the files should have been pre-copied");

		Controller secondPass = new Controller();
		secondPass.analyze(new CoreFilesData(destinationDir.toFile(), new File[] { sourceDir.toFile() }, true));
		Map<String, FileEntry> bySourceSuffix = indexBySourceSuffix(secondPass.getAnalysisResult());
		assertEquals(22, bySourceSuffix.size(), "number of analyzed files");

		for (Map.Entry<String, FileEntry> entry : bySourceSuffix.entrySet()) {
			String suffix = entry.getKey();
			if ("text_file.txt".equals(suffix)) {
				assertNull(entry.getValue().getTypeDestination(), suffix + ": status");
				continue;
			}
			FileDestination expected = preCopiedSourceSuffixes.contains(suffix) ? FileDestination.EXIST
					: FileDestination.NEW;
			assertEquals(expected, entry.getValue().getTypeDestination(), suffix + ": status on second analysis");
		}
	}

	private Map<String, FileEntry> indexBySourceSuffix(AnalysisResult result) {
		Map<String, FileEntry> bySourceSuffix = new HashMap<>();
		for (FileEntry fileEntry : result.getFilesEntries()) {
			bySourceSuffix.put(sourceSuffix(fileEntry), fileEntry);
		}
		return bySourceSuffix;
	}

	private String sourceSuffix(FileEntry fileEntry) {
		return sourceDir.relativize(fileEntry.getSource().toPath()).toString().replace(File.separatorChar, '/');
	}
}
