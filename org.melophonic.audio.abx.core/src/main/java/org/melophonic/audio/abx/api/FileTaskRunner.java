package org.melophonic.audio.abx.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to apply a set of FileTasks to a set of AbxFiles as part of a longer-running
 * operation.
 *
 * @param <F>
 */
public class FileTaskRunner<F extends AbxFile> implements Runnable {
	
	final Logger log = LoggerFactory.getLogger(getClass());

	final String description;
	final Collection<F> files;
	final ArrayList<String> errors = new ArrayList<>();
	final LinkedHashMap<FileTask<?, F>, Double> functions = new LinkedHashMap<>();

	ProgressHandler delegate;
	Runnable endRunner;
	
	public FileTaskRunner(String description, Collection<F> files) {
		super();
		this.description = description;
		this.files = files;
	}
	
	public void addTask(FileTask<?, F> fileTask, double work) {
		functions.put(fileTask, work);
	}
	
	public void start(ProgressHandler delegate, Runnable endRunner) {
		this.delegate = delegate;
		this.endRunner = endRunner;
		new Thread(this).start();
	}
	

	@Override
	public void run() {
		try {
			for (FileTask<?, F> fx : functions.keySet()) {
				double progressPerFile = functions.get(fx) / (double) files.size();
				for (F abxFile : files) {
					try {
						fx.performTask(abxFile);
					} catch (Exception e) {
						e.printStackTrace();
						errors.add(abxFile.toString() + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
					} finally {
						delegate.setProgress(progressPerFile, true);
					}
				}

				if (!errors.isEmpty()) {
					StringBuffer sb = new StringBuffer();
					for (String error : errors)
						sb.append(error).append("\n");
					delegate.handleError("Error " + description, sb.toString());
				}
				
			}
			

		} catch (Exception e) {
			delegate.handleError("Error " + description, e);
		} finally {
			if (endRunner != null) {
				log.info("Starting nextRunner");
				new Thread(endRunner).start();
			} else {
				delegate.setProgress(1.0, false);
			}
		}
	}

}	
