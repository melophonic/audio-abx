package org.melophonic.audio.abx.api;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.util.Duration;

import javax.sound.sampled.AudioFormat;

/**
 * 
 * The Media/File/Player represents a reference to the dedicated 
 * media player component for one and only one media file.
 * 
 * The methods here are a subset of methods in javafx.scene.media.MediaPlayer
 * and implementations should model that behavior.
 * 
 */
public interface AbxMedia {	
	
	AudioFormat getFormat();
	ReadOnlyObjectProperty<Duration> durationProperty();
	ReadOnlyObjectProperty<Duration> currentTimeProperty();
	DoubleProperty volumeProperty();
	void updatePlayerTime(final Duration startTime, final Duration stopTime, final boolean seek);
	Duration getDuration();
	
	boolean isPlaying();
	boolean isReady();
	boolean isAccessible();
	void play();
	void pause();
	void stop();
	void setVolume(double volume);
	void seek(Duration duration);
	double getVolume();
	Duration getCurrentTime();
	
	void setOnPlaying(Runnable r);
	void setOnPaused(Runnable r);
	void setOnStopped(Runnable r);
	void setOnReady(Runnable r);
	void setOnEndOfMedia(Runnable r);
	void setLoop(boolean loop);
	
	void activate();
	
}
