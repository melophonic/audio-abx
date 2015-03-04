package org.melophonic.audio.abx.impl;

import java.io.File;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.melophonic.audio.abx.api.AbxFile;
import org.melophonic.audio.abx.api.AbxFileSet;

@XmlRootElement
public class AbxFileSetImpl<F extends AbxFile> implements AbxFileSet<F> {

	final ObjectProperty<File> serializedFile = new SimpleObjectProperty<File>(null);
	final ObservableList<F> files;

	final ObjectProperty<Duration> startTime = new SimpleObjectProperty<Duration>(Duration.ZERO);
	final ObjectProperty<Duration> endTime = new SimpleObjectProperty<Duration>(Duration.INDEFINITE);

	final ObjectProperty<F> currentFile = new SimpleObjectProperty<F>(null);
	final ObjectProperty<F> shortestFile = new SimpleObjectProperty<F>(null);
	final ObjectProperty<F> quietestFile = new SimpleObjectProperty<F>(null);

	public AbxFileSetImpl(ObservableList<F> files) {
		super();
		this.files = files;
	}

	public AbxFileSetImpl() {
		this(FXCollections.observableArrayList());
	}

	@Override
	public File getSerializedFile() {
		return serializedFile.get();
	}

	@Override
	public void setSerializedFile(File file) {
		serializedFile.set(file);
	}

	public ObjectProperty<File> serializedFileProperty() {
		return serializedFile;
	}

	@Override
	public List<F> getFiles() {
		return files;
	}

	@Override
	public void setFiles(List<F> files) {
		this.files.clear();
		this.files.addAll(files);
	}

	@Override
	public void add(F abxFile) throws Exception {
		files.add(abxFile);
	}

	public ObservableList<F> filesProperty() {
		return files;
	}

	@Override
	public Duration getStartTime() {
		return startTime.get();
	}

	@Override
	@XmlJavaTypeAdapter(DurationAdapter.class)
	public void setStartTime(Duration startTime) {
		this.startTime.set(startTime);
	}

	@Override
	public Duration getEndTime() {
		return endTime.get();
	}

	@Override
	@XmlJavaTypeAdapter(DurationAdapter.class)
	public void setEndTime(Duration endTime) {
		this.endTime.set(endTime);
	}

	public ObjectProperty<Duration> startTimeProperty() {
		return startTime;
	}

	public ObjectProperty<Duration> endTimeProperty() {
		return endTime;
	}

	@Override
	public F getCurrentFile() {
		return currentFile.get();
	}

	@Override
	public void setCurrentFile(F newCurrentFile) {
		currentFile.set(newCurrentFile);
	}

	public ObjectProperty<F> currentFileProperty() {
		return currentFile;
	}

	@Override
	public F getShortestFile() {
		return shortestFile.get();
	}

	public void setShortestFile(F shortestFile) {
		this.shortestFile.set(shortestFile);
	}

	public ObjectProperty<F> shortestFileProperty() {
		return shortestFile;
	}

	public F getQuietestFile() {
		return quietestFile.get();
	}

	public void setQuietestFile(F quietestFile) {
		this.quietestFile.set(quietestFile);
	}

	public ObjectProperty<F> quietestFileProperty() {
		return quietestFile;
	}

	public static <S extends AbxFileSetImpl<F>, F extends AbxFileImpl> void marshal(S abxFileSet, File xmlFile) throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(AbxFileSetImpl.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(abxFileSet, xmlFile);
	}

	@SuppressWarnings("unchecked")
	public static AbxFileSetImpl<AbxFileImpl> unmarshal(File xmlFile) throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(AbxFileSetImpl.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return (AbxFileSetImpl<AbxFileImpl>) jaxbUnmarshaller.unmarshal(xmlFile);
	}

}
