package org.melophonic.audio.abx.impl;

import java.util.Iterator;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.melophonic.audio.abx.api.AbxFile;
import org.melophonic.audio.abx.api.AbxMode;
import org.melophonic.audio.abx.api.AbxTest;

public class AbxTestImpl<F extends AbxFile> implements AbxTest<F> {

	final ObjectProperty<AbxMode> mode = new SimpleObjectProperty<AbxMode>(AbxMode.ABX);
	final ObservableList<AbxTest.Trial<F, ?>> trials;	
	
	public AbxTestImpl(ObservableList<AbxTest.Trial<F, ?>> trials) {
		super();
		this.trials = trials;
	}
	
	public AbxTestImpl() {
		this(FXCollections.observableArrayList());
	}
		
	public AbxTestImpl(AbxMode mode) {
		this();
		this.mode.set(mode);	
	}
	
	public AbxMode getMode() {
		return mode.get();
	}
	
	@Override
	public void addTrial(Trial<F, ?> trial) {
		trials.add(trial);
	}
	
	@Override
	public int size() {
		return trials.size();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<AbxTest.Trial<F, ?>> iterator() {
		return trials.iterator();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AbxTest.Trial<F, ?>> getTrials() {
		return trials;
	}
	
}
