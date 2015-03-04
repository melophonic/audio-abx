package org.melophonic.audio.abx.impl;

import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.melophonic.audio.abx.api.AbxFile;
import org.melophonic.audio.abx.api.AbxMedia;

public class AbxFileImpl implements AbxFile {
	
	final StringProperty id = new SimpleStringProperty();

	final ObjectProperty<File> file = new SimpleObjectProperty<File>(null);
	final DoubleProperty gain;
	final ObjectProperty<Duration> offset = new SimpleObjectProperty<Duration>(Duration.ZERO);
	final BooleanProperty enabled = new SimpleBooleanProperty(true);
	final ReadOnlyObjectProperty<Duration> duration;
	final ObjectProperty<AbxMedia> media = new SimpleObjectProperty<AbxMedia>();
	
	byte[] fingerprint;
	File normalizedFile;
	int normalizedSize;
	Double averageLoudness;
	
	public AbxFileImpl(DoubleProperty gain, ReadOnlyObjectProperty<Duration> duration) {
		super();
		this.gain = gain;
		this.duration = duration;
	}
	
	public AbxFileImpl() {
		this(new SimpleDoubleProperty(1.0), new SimpleObjectProperty<Duration>(Duration.UNKNOWN));
	}
	
	public AbxFileImpl(File file) {
		this();
		setFile(file);
	}
	
	public AbxFileImpl(DoubleProperty gain, ReadOnlyObjectProperty<Duration> duration, File file) {
		this(gain, duration);
		setFile(file);
	}
	
	public AbxMedia getMedia() {
		return media.get();
	}
	
	public void setMedia(AbxMedia media) {
		this.media.set(media);
	}
	
	@Override
	public Double getGain() {
		return gain.get();
	}

	@Override
	public void setGain(Double gain) {
		this.gain.set(gain);
	}

	@Override
	public Duration getOffset() {
		return offset.get();
	}

	@Override
	@XmlJavaTypeAdapter(DurationAdapter.class)
	public void setOffset(Duration offset) {
		this.offset.set(offset);
	}

	@Override
	public File getFile() {
		return file.get();
	}

	@Override
	public void setFile(File file) {
		this.file.set(file);
	}

	@Override
	public boolean isEnabled() {
		return enabled.get();
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled.set(enabled);;
	}
	
	public BooleanProperty enabledProperty() {return enabled;}
	public ObjectProperty<File> fileProperty() {return file;}
	public DoubleProperty gainProperty() {return gain;}
	public ObjectProperty<Duration> offsetProperty() {return offset;}
	public ReadOnlyObjectProperty<Duration> durationProperty() {return duration;}
	
	@Override
	public byte[] getFingerprint() {
		return fingerprint;
	}

	@Override
	public void setFingerprint(byte[] fingerprint) {
		this.fingerprint = fingerprint;
	}
	
	@Override
	public Duration getDuration() {
		return duration.get();
	}

	@Override
	public String getId() {
		return id.get();
	}
	
	@Override
	@XmlTransient
	public void setId(String id) {
		this.id.set(id);
	}
	
	@Override
	public StringProperty idProperty() {return this.id;}		
	
	@Override
	public File getNormalizedFile() {
		return normalizedFile;
	}

	@Override
	@XmlTransient
	public void setNormalizedFile(File normalizedFile) {
		this.normalizedFile = normalizedFile;
	}

	@Override
	public int getNormalizedSize() {
		return normalizedSize;
	}

	@Override
	@XmlTransient
	public void setNormalizedSize(int normalizedSize) {
		this.normalizedSize = normalizedSize;
	}

	@Override
	public Double getAverageLoudness() {
		return averageLoudness;
	}

	@Override
	@XmlTransient
	public void setAverageLoudness(Double averageLoudness) {
		this.averageLoudness = averageLoudness;
	}	
	
	@Override
	public String toString() {
		return getFile() == null ? "<no-file>" : getFile().getName();
	}

}
