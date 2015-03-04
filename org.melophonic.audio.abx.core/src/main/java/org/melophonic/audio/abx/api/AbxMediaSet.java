package org.melophonic.audio.abx.api;

import java.io.File;
import java.util.Collection;

/**
 * AbxMediaSet is an AbxFileSet combined with an AbxMediaFactory
 * Files are loaded as playable media (see AbxMedia) and the AbxMediaSet can be reused
 * 
 */
public interface AbxMediaSet<F extends AbxFile> extends AbxFileSet<F> {

	AbxMediaFactory getMediaFactory();

	void clear();

	void load(Collection<File> audioFiles) throws Exception;

	void load(AbxFileSet<?> fileSet) throws Exception;

	void save(File serializedFile) throws Exception;

	void addFile(File file) throws Exception;

	void addFile(F abxFile);

	void removeFile(F abxFile);

	AbxTest<F> createTest(AbxMode mode);

}
