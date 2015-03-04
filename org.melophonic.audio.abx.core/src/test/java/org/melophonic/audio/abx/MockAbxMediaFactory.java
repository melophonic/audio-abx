package org.melophonic.audio.abx;

import java.io.File;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.melophonic.audio.abx.api.AbxMedia;
import org.melophonic.audio.abx.api.AbxMediaFactory;
import org.melophonic.audio.util.AudioUtil;

public class MockAbxMediaFactory implements AbxMediaFactory {

	@Override
	public AbxMedia createMedia(File audioFile) throws Exception {
		return new TestMedia(audioFile);
	}
	
	public class TestMedia implements AbxMedia {
		
		final File file;
		final AudioFormat format;

		final ObjectProperty<Duration> duration = new SimpleObjectProperty<Duration>();
		
		public TestMedia(File file) throws Exception {
			super();
			this.file = file;
			try (AudioInputStream in = AudioSystem.getAudioInputStream(file)) {
				this.format = in.getFormat();
				
			}
			this.duration.set(new Duration(1000*AudioUtil.getDurationInSeconds(file)));
		}

		@Override
		public AudioFormat getFormat() {
			return format;
		}

		@Override
		public ReadOnlyObjectProperty<Duration> durationProperty() {
			
			return duration;
		}

		@Override
		public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DoubleProperty volumeProperty() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void updatePlayerTime(Duration startTime, Duration stopTime, boolean seek) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Duration getDuration() {
			return duration.get();
		}

		@Override
		public boolean isPlaying() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isReady() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isAccessible() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void play() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void pause() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setVolume(double volume) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void seek(Duration duration) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public double getVolume() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Duration getCurrentTime() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setOnPlaying(Runnable r) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setOnPaused(Runnable r) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setOnStopped(Runnable r) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setOnReady(Runnable r) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setOnEndOfMedia(Runnable r) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setLoop(boolean loop) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void activate() {
			// TODO Auto-generated method stub
			
		}
		
		
		
		
		
		
	}
	

}
