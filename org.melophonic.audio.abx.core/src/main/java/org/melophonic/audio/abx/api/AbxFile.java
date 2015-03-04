package org.melophonic.audio.abx.api;

import java.io.File;

import javafx.beans.property.StringProperty;
import javafx.util.Duration;

/**
 * An audio file to test/analyze
 *
 */
public interface AbxFile {
	
	Double getGain();
	void setGain(Double gain);
	Duration getOffset();
	void setOffset(Duration offset);
	File getFile();
	void setFile(File file);
	boolean isEnabled();
	void setEnabled(boolean enabled);
	byte[] getFingerprint();
	void setFingerprint(byte[] fingerprint);
	Duration getDuration();
	String getId();
	void setId(String id);
	StringProperty idProperty();
	File getNormalizedFile();
	void setNormalizedFile(File normalizedFile);
	int getNormalizedSize();
	void setNormalizedSize(int normalizedSize);
	Double getAverageLoudness();
	void setAverageLoudness(Double averageLoudness);

	String toString();

}