package org.melophonic.audio.abx.api;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains test data & results
 *
 * @param <F>
 */
public interface AbxTest<F extends AbxFile> {

	AbxMode getMode();
	void addTrial(Trial<F, ?> trial);
	<R> Iterator<AbxTest.Trial<F, R>> iterator();
	<R> List<Trial<F, R>> getTrials();
	int size();
	
	default void addTrial(F file) {
		addTrial(newTrial(file));
	}
	
	default Trial<F, ?> newTrial(F file) {
		switch (getMode()) {
			case Shootout: return new Trial<F, Boolean>(file);
			default: return new Trial<F, F>(file);
		}
	}
	
	@SuppressWarnings("unchecked")
	default <R> List<R> getPossibleResponses(List<F> enabledFiles) {
		switch (getMode()) {
			case Shootout: return (List<R>) Arrays.asList(Boolean.TRUE, Boolean.FALSE);
			default: return (List<R>) enabledFiles;
		}
	}

	default boolean isMatch(Trial<F, ?> trial) {
		switch (getMode()) {
			case Shootout: return ((Boolean) trial.getResponse()).booleanValue();
			default: return trial.getFile().equals(trial.getResponse());
		}
		
	}

	default int getMatches() {
		int matches = 0;
		for (AbxTest.Trial<F, ?> trial : getTrials()) {	
			if (isMatch(trial)) matches++;
		}		
		return matches;
	}
	

	default Map<F, Result> getResultsByFile() {
		Map<F, Result> results = new LinkedHashMap<F, Result>();
		for (AbxTest.Trial<F, ?> trial : getTrials()) {
			if (!results.containsKey(trial.getFile())) results.put(trial.getFile(), new Result());
			Result result = results.get(trial.getFile());
			result.trials++;
			if (isMatch(trial)) result.hits++;
		}
		return results;
	}

	default double getAccuracy() {
		double matches = getMatches();
		return matches == 0 ? 0 : matches / (double) size();
	}
	
	public class Result {
		
		private int trials = 0;
		private int hits = 0;
		
		public int trials() {
			return trials;
		}
		
		public int hits() {
			return hits;
		}
		
		public double score() {
			return hits > 0 && trials > 0 ? (double) hits / (double) trials : 0.0;	
		}
		
	}
	
	public class Trial<F extends AbxFile, R> {
		
		final F file;
		R response;
		
		public Trial(F file) {
			super();
			this.file = file;
		}
		
		public F getFile() {
			return file;
		}
		
		public R getResponse() {
			return response;
		}
		
		public void setResponse(R response) {
			this.response = response;
		}
		

	}

}