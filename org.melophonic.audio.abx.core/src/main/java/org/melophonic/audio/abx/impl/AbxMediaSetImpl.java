package org.melophonic.audio.abx.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import org.melophonic.audio.abx.AbxApp;
import org.melophonic.audio.abx.api.AbxFile;
import org.melophonic.audio.abx.api.AbxFileSet;
import org.melophonic.audio.abx.api.AbxMedia;
import org.melophonic.audio.abx.api.AbxMediaFactory;
import org.melophonic.audio.abx.api.AbxMediaSet;
import org.melophonic.audio.abx.api.AbxMode;
import org.melophonic.audio.util.AudioUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbxMediaSetImpl extends AbxFileSetImpl<AbxMediaSetImpl.AbxMediaFile> implements AbxMediaSet<AbxMediaSetImpl.AbxMediaFile>, ListChangeListener<AbxMediaSetImpl.AbxMediaFile> {

	final static Logger log = LoggerFactory.getLogger(AbxMediaSetImpl.class);

	public class AbxMediaFile extends AbxFileImpl {

		final AbxMedia media;
		final Duration initialDuration;

		public AbxMediaFile(File file, AbxMedia media) throws Exception {
			super(media.volumeProperty(), media.durationProperty(), file);
			this.media = media;
			this.initialDuration = new Duration(AudioUtil.getDurationInSeconds(media.getFormat(), getFile().length()) * 1000);

			startTimeProperty().addListener(new ChangeListener<Duration>() {
				@Override
				public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
					updatePlayerTime(newValue, null, true);
				}
			});

			endTimeProperty().addListener(new ChangeListener<Duration>() {
				@Override
				public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
					updatePlayerTime(null, newValue, false);
				}
			});

			// media.durationProperty().addListener(this::setDuration);
			offsetProperty().addListener(new ChangeListener<Duration>() {
				@Override
				public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
					if (oldValue != newValue) {
						updatePlayerTime(null, null, true);
					}
				}
			});

			log.info("init: " + getFile().getName() + ": " + getDuration());
		}

		public AbxMedia getMedia() {
			return media;
		}

		@Override
		public Duration getDuration() {
			return super.getDuration() == null || super.getDuration().isUnknown() ? initialDuration : super.getDuration();
		}

		void updatePlayerTime(boolean seek) {
			Duration startTime = getStartTime();
			Duration endTime = getEndTime();
			updatePlayerTime(startTime != null ? startTime : Duration.ZERO, endTime != null ? endTime : Duration.INDEFINITE, seek);
		}

		public void updatePlayerTime(final Duration startTime, final Duration stopTime, final boolean seek) {

			log.info("updatePlayerTime({}, {}, {})", startTime, stopTime, seek);
			Duration start = startTime != null ? startTime.add(getOffset()) : null;
			Duration stop = stopTime != null ? stopTime.add(getOffset()) : null;
			media.updatePlayerTime(start, stop, seek);
		}

	}

	final AbxMediaFactory factory;

	public AbxMediaSetImpl(AbxMediaFactory factory, ObservableList<AbxMediaFile> files) throws Exception {
		super(files);
		this.factory = factory;
		files.addListener(this);
	}

	public AbxMediaSetImpl(AbxMediaFactory factory) throws Exception {
		super();
		this.factory = factory;
		files.addListener(this);
	}

	public AbxMediaFactory getMediaFactory() {
		return factory;
	}

	public void clear() {
		filesProperty().clear();
		startTimeProperty().set(null);
		endTimeProperty().set(null);
		currentFile.set(null);
		shortestFile.set(null);
		quietestFile.set(null);
		serializedFileProperty().set(null);
	}

	@Override
	public void load(AbxFileSet<?> fileSet) throws Exception {
		clear();

		List<AbxMediaFile> managedFiles = new ArrayList<>();
		for (AbxFile abxFile : fileSet.getFiles())
			managedFiles.add(newFile(abxFile));

		filesProperty().addAll(managedFiles);

		startTimeProperty().set(fileSet.getStartTime());
		endTimeProperty().set(fileSet.getEndTime());
		serializedFileProperty().set(fileSet.getSerializedFile());
	}

	@Override
	public void load(Collection<File> files) throws Exception {
		AbxFileSetImpl<AbxFileImpl> fileSet = new AbxFileSetImpl<>();
		for (File file : files)
			fileSet.add(new AbxFileImpl(file));
		load(fileSet);

	}

	public AbxMediaFile newFile(File file) throws Exception {
		AbxMediaFile abxFile = new AbxMediaFile(file, factory.createMedia(file));

		return abxFile;
	}

	public AbxMediaFile newFile(AbxFile abxFile) throws Exception {
		AbxMediaFile f = newFile(abxFile.getFile());
		f.setOffset(abxFile.getOffset());
		f.setGain(abxFile.getGain());
		f.setFingerprint(abxFile.getFingerprint());
		return f;

	}

	public void addFile(File file) throws Exception {
		addFile(newFile(file));
	}

	public void addFile(AbxMediaFile abxFile) {
		getFileList().add(abxFile);
	}

	public void removeFile(AbxMediaFile abxFile) {
		getFileList().remove(abxFile);
		if (getCurrentFile() != null && getCurrentFile().equals(abxFile)) setCurrentFile(null);
	}

	@Override
	public void onChanged(Change<? extends AbxMediaFile> c) {
		int fileId = 1;
		for (AbxMediaFile abxFile : getFileList()) {
			abxFile.setId(AbxApp.getCharForNumber(fileId++));

		}
		setShortestFile(null);
	}

	public ObservableList<AbxMediaFile> getFileList() {
		return filesProperty();
	}

	public void save(File serializedFile) throws Exception {
		setSerializedFile(serializedFile);
		marshal(this, serializedFile);

	}

	@Override
	public AbxTestImpl<AbxMediaFile> createTest(AbxMode mode) {
		return new AbxTestImpl<AbxMediaFile>(mode);

	}

}
