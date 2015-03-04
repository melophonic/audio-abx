package org.melophonic.audio.abx.api;

import java.io.File;

/**
 * Factory class for AbxMedia objects
 *
 */
public interface AbxMediaFactory {
	
	AbxMedia createMedia(File audioFile) throws Exception;
	
}
