package org.melophonic.audio.abx.api;

public interface FileTask<T, F extends AbxFile> {

	T performTask(F abxFile) throws Exception;

}