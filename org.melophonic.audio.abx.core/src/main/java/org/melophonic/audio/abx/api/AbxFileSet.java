package org.melophonic.audio.abx.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.util.Duration;

/**
 * 
 * The root data model object for an ABX testing session
 * - manages a list of AbxFile objects
 * - the start/end time for the audio clip to test
 * - tracks the shortest and the quiestest files in the set for reference
 *
 * @param <F>
 */
public interface AbxFileSet<F extends AbxFile> {

	File getSerializedFile();

	void setSerializedFile(File file);

	List<F> getFiles();

	void setFiles(List<F> files);

	void add(F abxFile) throws Exception;

	Duration getStartTime();
	void setStartTime(Duration startTime);
	Duration getEndTime();
	void setEndTime(Duration endTime);

	F getCurrentFile();
	void setCurrentFile(F newCurrentFile);

	F getShortestFile();
	void setShortestFile(F newShortestFile);
	
	
	F getQuietestFile();
	void setQuietestFile(F newQuietestFile);

	
	default F getShortestFile(boolean recalculateIfNull) {
		F shortest = getShortestFile();
		if (shortest == null && recalculateIfNull) {
			List<F> list = getFilesByDuration();
			shortest = list.isEmpty() ? null : list.get(0);		
			setShortestFile(shortest);
		}
		return shortest;
	}	
	
	
	default F getQuietestFile(boolean recalculateIfNull) {
		F quietest = getQuietestFile();
		if (quietest == null && recalculateIfNull) {
			quietest = getFilesByAverageLoudness().get(0);
			setQuietestFile(quietest);
		}
		return quietest;
	}
	
	
	default List<F> getFilesByDuration() {
		ArrayList<F> list = new ArrayList<F>(getFiles());
		list.sort(new Comparator<F>() {
			@Override
			public int compare(F o1, F o2) {
				return o1.getDuration().compareTo(o2.getDuration());
			}
			
		});
		return list;
	}
	
	
	default List<F> getFilesByAverageLoudness() {
		ArrayList<F> list = new ArrayList<F>(getFiles());
		list.sort(new Comparator<F>() {
			@Override
			public int compare(F o1, F o2) {
				return o1.getAverageLoudness().compareTo(o2.getAverageLoudness());
			}
		});
		return list;
	}	
	
	default List<F> getEnabledFiles() {
		ArrayList<F> list = new ArrayList<F>();
		for (F abxFile : getFiles()) {
			if (abxFile.isEnabled()) list.add(abxFile);
		}
		return list;
	}

	
	
}