package org.melophonic.audio.abx.impl;

import java.io.File;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.melophonic.audio.abx.api.AbxMedia;
import org.melophonic.audio.abx.api.AbxMediaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbxMediaFactoryImpl implements AbxMediaFactory {
	
	final static Logger log = LoggerFactory.getLogger(AbxMediaFactoryImpl.class);
	
	public class AbxMediaImpl implements AbxMedia {
		
		final AudioFormat format;
		final Media media;
		final MediaPlayer player;
		
		public AbxMediaImpl(AudioFormat format, Media media, MediaPlayer player) throws Exception {
			super();	
			this.format = format;
			this.media = media;
			this.player = player;
		}
		
		public void updatePlayerTime(final Duration startTime, final Duration stopTime, final boolean seek) {
			
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					if (startTime != null) player.setStartTime(startTime);
					if (stopTime != null) player.setStopTime(stopTime);
					if (startTime != null && seek) player.seek(startTime);
				}
			});
		}	


		public DoubleProperty volumeProperty() {
			return player.volumeProperty();
		}
		
		public Duration getDuration() {
			return media.getDuration();
		}
	

		
		public void seek(Duration d) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					player.seek(d);
				}
			});
		}
		
		public ReadOnlyObjectProperty<Duration> durationProperty() {return media.durationProperty();}
		public ReadOnlyObjectProperty<Duration> currentTimeProperty() {return player.currentTimeProperty();}
		

		
		@Override
		public void setOnPlaying(Runnable r) {
			player.setOnPlaying(r);
		}

		@Override
		public void setOnPaused(Runnable r) {
			player.setOnPaused(r);
		}

		@Override
		public void setOnReady(Runnable r) {
			player.setOnReady(r);
			
		}

		@Override
		public void setOnStopped(Runnable r) {
			player.setOnStopped(r);
			
		}	
		
		@Override
		public void setOnEndOfMedia(Runnable r) {
			player.setOnEndOfMedia(r);
			
		}

		public void setLoop(boolean loop) {
			player.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
		}

		public AudioFormat getFormat() {
			return this.format;
		}

		public Media getMedia() {
			return media;
		}

		public MediaPlayer getPlayer() {
			return player;
		}

		@Override
		public boolean isPlaying() {
			return player.getStatus() == Status.PLAYING;
		}
		
		@Override
		public boolean isReady() {
			Status status = player.getStatus();
			return (status == Status.PAUSED || status == Status.READY || status == Status.STOPPED) ? true : false;
		}

		public boolean isAccessible() {
			Status status = player.getStatus();
			return (status == Status.UNKNOWN || status == Status.HALTED) ? false : true;
			
		}

		@Override
		public double getVolume() {
			return player.getVolume();
		}

		@Override
		public Duration getCurrentTime() {
			return player.getCurrentTime();
		}

		@Override
		public void play() {
			player.play();
		}

		@Override
		public void pause() {
			player.pause();
		}

		@Override
		public void stop() {
			player.stop();
		}

		@Override
		public void setVolume(double volume) {
			player.setVolume(volume);	
		}

		@Override
		public void activate() {
			mediaView.setMediaPlayer(player);
			
		}

	}	
	
	final MediaView mediaView = new MediaView();
	
	@Override
	public AbxMediaImpl createMedia(File audioFile) throws Exception {
		try (AudioInputStream in = AudioSystem.getAudioInputStream(audioFile)) {
			AudioFormat format = in.getFormat();
			Media media = new Media(audioFile.toURI().toString());
			MediaPlayer player = new MediaPlayer(media);
			return new AbxMediaImpl(format, media, player);
		}
	}


}
