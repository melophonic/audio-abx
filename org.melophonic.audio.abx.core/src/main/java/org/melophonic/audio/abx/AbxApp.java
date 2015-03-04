package org.melophonic.audio.abx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javafx.util.Duration;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.apache.commons.io.IOUtils;
import org.melophonic.audio.abx.api.AbxFile;
import org.melophonic.audio.abx.api.AbxMediaSet;
import org.melophonic.audio.abx.api.AbxMode;
import org.melophonic.audio.abx.api.AbxTest;
import org.melophonic.audio.abx.api.FileTaskRunner;
import org.melophonic.audio.spi.AnalysisService;
import org.melophonic.audio.spi.FingerprintService;
import org.melophonic.audio.spi.FingerprintService.FingerprintComparison;
import org.melophonic.audio.util.AudioConverter;
import org.melophonic.audio.util.AudioUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main controller class for the application.
 */
public class AbxApp<F extends AbxFile, M extends AbxMediaSet<F>> {

	final static Logger log = LoggerFactory.getLogger(AbxApp.class);

	final static AudioConverter.Parameters normalizeParams = new AudioConverter.Parameters(Encoding.PCM_SIGNED, AudioFileFormat.Type.WAVE, 44100F, 16);
	final static double offsetDivisor = 2.0; // TODO: determine if constant
	final static double silenceThresholdDb = AnalysisService.DEFAULT_SILENCE_THRESHOLD_DB;

	final M mediaSet;
	final FingerprintService fingerprintService;
	final AnalysisService analysisService;
	final Path tempDir;
	final Random rng = new Random();

	public AbxApp(M mediaSet, FingerprintService fingerprintService, AnalysisService analysisService) throws Exception {
		super();
		this.mediaSet = mediaSet;
		this.fingerprintService = fingerprintService;
		this.analysisService = analysisService;
		tempDir = Files.createTempDirectory("abxtemp");
		tempDir.toFile().deleteOnExit();
	}

	public M getMediaSet() {
		return mediaSet;
	}
	
	public void load(String name, URL...audioUrls) throws Exception {
		File temp = new File(tempDir.toFile(), name);
		temp.mkdirs();

		List<File> audioFiles = new ArrayList<>();
		for (URL audioUrl : audioUrls) {
			File audioFile = new File(temp, AudioUtil.getResourceName(audioUrl.toURI()));
			try (InputStream in  = audioUrl.openStream()) {
				try (FileOutputStream out = new FileOutputStream(audioFile)) {
					IOUtils.copy(in, out);
					out.flush();
				}
			}
			audioFiles.add(audioFile);
		}
		
		getMediaSet().load(audioFiles);
	}	
	
	public FileTaskRunner<F> createFileTaskRunner(String description) {
		return new FileTaskRunner<F>(description, mediaSet.getFiles());
	}	
	
	public FileTaskRunner<F> createCalculateGainTaskRunner() {
		FileTaskRunner<F> taskRunner = createFileTaskRunner("Calculating gain values");
		taskRunner.addTask(this::normalizeFile, 0.1);
		taskRunner.addTask(this::calculateAverageLoudness, 0.3);
		taskRunner.addTask(this::calculateGain, 0.6);
		return taskRunner;
	}

	public FileTaskRunner<F> createCalculateOffsetTaskRunner() {
		FileTaskRunner<F> taskRunner = createFileTaskRunner("Calculating offset values");
		taskRunner.addTask(this::normalizeFile, 0.1);
		taskRunner.addTask(this::calculateFingerprint, 0.3);
		taskRunner.addTask(this::calculateOffset, 0.6);
		return taskRunner;
	}
	
	
	public File normalizeFile(F abxFile) throws Exception {
		if (abxFile.getNormalizedFile() == null) {
			File normalizedFile = new File(tempDir.toFile(), abxFile.getFile().getName());
			AudioConverter.convert(abxFile.getFile(), normalizedFile, normalizeParams);
			abxFile.setNormalizedFile(normalizedFile);
			// abxFile.setNormalizedSize(size);
			// setDuration(AudioUtil.getDurationInSeconds(getFormat(), size));
		}
		return abxFile.getNormalizedFile();
	}

	public byte[] calculateFingerprint(F abxFile) throws Exception {
		if (abxFile.getFingerprint() == null) {
			long start = System.currentTimeMillis();
			abxFile.setFingerprint(fingerprintService.calculateFingerprint(abxFile.getNormalizedFile().toURI()));
			long diff = System.currentTimeMillis() - start;
			log.info(String.format("Fingerprinted: %s (%s ms)", abxFile.getFile().getName(), diff));
		}
		return abxFile.getFingerprint();
	}

	public Double calculateAverageLoudness(F abxFile) throws Exception {
		if (abxFile.getAverageLoudness() == null) {

			Map<Double, Double> spl = analysisService.getSoundPressureLevels(abxFile.getNormalizedFile().toURI(), true, silenceThresholdDb);
			abxFile.setAverageLoudness(avg(spl.values()));
		}
		return abxFile.getAverageLoudness();
	}

	public static double avg(Collection<Double> values) {
		double total = 0.0;
		for (Double value : values)
			total += value;
		return total / values.size();
	}

	public Duration calculateOffset(F abxFile) throws Exception {
		log.info("calculateOffset: " + abxFile);

		if (abxFile.equals(mediaSet.getShortestFile(true))) {
			abxFile.setOffset(Duration.ZERO);
		} else {

			long start = System.currentTimeMillis();
			FingerprintComparison similarity = fingerprintService.compareFingerprints(mediaSet.getShortestFile().getFingerprint(), abxFile.getFingerprint());
			long diff = System.currentTimeMillis() - start;

			double time = similarity.getMostSimilarTime();
			time = Math.abs(time);
			Duration offset = time == 0.0 ? Duration.ZERO : new Duration((time / offsetDivisor) * 1000);
			abxFile.setOffset(offset);

			String msg = String.format("Fingerprint[%s]: Similarity[%s] Time[%s] Offset[%s] (%s ms)", abxFile, similarity.getSimilarity(), time,
					abxFile.getOffset(), diff);

			if (similarity.getSimilarity() == 1.0F) log.info(msg);
			else log.warn(msg);
		}

		return abxFile.getOffset();
	}

	public Double calculateGain(final F abxFile) throws Exception {
		log.info("calculateGain: " + abxFile);

		F quietestFile = mediaSet.getQuietestFile(true);

		if (abxFile.equals(quietestFile)) {
			abxFile.setGain(1.0);
		} else {
			double pct = Math.abs(quietestFile.getAverageLoudness()) / Math.abs(abxFile.getAverageLoudness());
			abxFile.setGain(pct * 1.0);
		}

		String msg = String.format("Loudness[%s]: %s Gain[%s] (%s)", abxFile.toString(), abxFile.getAverageLoudness(), abxFile.getGain(),
				abxFile.equals(quietestFile));
		log.info(msg);

		return abxFile.getGain();
	}
		
	public AbxTest<F> createTest(AbxMode mode, int trialCount) {
		List<F> enabled = getMediaSet().getEnabledFiles();
		AbxTest<F> abxTest = getMediaSet().createTest(mode);
		for (int i = 0; i < trialCount; i++) {
			abxTest.addTrial(enabled.get(rng.nextInt(enabled.size())));
		}
		return abxTest;
	}
	
	/** utility methods **/
	
	public static String getCharForNumber(int i) {
	    return i > 0 && i < 27 ? String.valueOf((char)(i + 64)) : null;
	}

	public static URL[] toUrls(Class<?> ctx, String basePath, String...resourceNames) throws Exception {
		URL[] urls = new URL[resourceNames.length];
		for (int i = 0; i < resourceNames.length; i++) {
			String resourcePath = String.format("%s%s%s", basePath, basePath.endsWith("/") ? "" : "/", resourceNames[i]);
			urls[i] = ctx.getResource(resourcePath);
			if (urls[i] == null) throw new Exception("Resource not found: " + resourcePath);
		}
		return urls;
	}
	
	public static List<File> unzip(URL zipFile, File targetPath, String namePattern) throws Exception {
		List<File> files = new ArrayList<File>();
		try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.openStream())) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				if (!zipEntry.isDirectory() && zipEntry.getName().matches(namePattern)) {
					log.info("Unzip: {}", zipEntry.getName());
					File targetFile = new File(targetPath, zipEntry.getName());
					try (FileOutputStream output = new FileOutputStream(targetFile)) {
						IOUtils.copyLarge(zipInputStream, output);
						output.flush();
					}
					files.add(targetFile);
				}
				zipInputStream.closeEntry();
				zipEntry = zipInputStream.getNextEntry();
			}
		}
		return files;
	}
	

}
