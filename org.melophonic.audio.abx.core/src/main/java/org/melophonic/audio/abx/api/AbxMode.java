package org.melophonic.audio.abx.api;

/**
 * The test mode to use.
 * - ABX (default): The listener is challenged to identify specific audio files presented randomly in order to test for audible differences
 * - Shootout: The listener is asked to rate a specific audio files presented randomly in order to test preferences
 */
public enum AbxMode {

	ABX,
	Shootout;
	
}
