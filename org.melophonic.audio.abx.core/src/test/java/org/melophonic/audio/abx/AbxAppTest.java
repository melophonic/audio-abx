package org.melophonic.audio.abx;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.junit.Before;
import org.junit.Test;
import org.melophonic.audio.abx.impl.AbxMediaSetImpl;
import org.melophonic.audio.spi.AnalysisService;
import org.melophonic.audio.spi.FingerprintService;
import org.melophonic.audio.spi.musicg.MGFingerprintService;
import org.melophonic.audio.spi.tarsos.TarsosAnalysisService;


public class AbxAppTest {
	
	static final FileFilter filter = new SuffixFileFilter(".wav");
	static final File resources = new File("./src/main/resources");
	
	AbxApp<AbxMediaSetImpl.AbxMediaFile, AbxMediaSetImpl> app;

	@Before
	public void setUp() throws Exception {
		FingerprintService fingerprintService = new MGFingerprintService();
		AnalysisService analysisService = new TarsosAnalysisService();
		AbxMediaSetImpl manager = new AbxMediaSetImpl(new MockAbxMediaFactory());
		app = new AbxApp<>(manager, fingerprintService, analysisService);
	}

	@Test
	public void testFromScratch() throws Exception {
		for (File wavFile : resources.listFiles(filter)) {
			app.getMediaSet().addFile(wavFile);
		}
		assertFalse(app.getMediaSet().getFiles().isEmpty());
	
	}


	
}
