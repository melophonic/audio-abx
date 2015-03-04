package org.melophonic.audio.abx.api;

/**
 * Lightweight delegate for reporting progress/errors, with default implementations
 * that can be overridden in a UI context
 *
 */
public interface ProgressHandler {
	
	default void setProgress(double progress, boolean increment) {
		
	}
	
	default void handleError(String ctx, Throwable x) {
		x.printStackTrace();
		handleError(ctx, String.format("%s[%s]", x.getClass().getSimpleName(), x.getMessage()));
	}
	
	default void handleError(String ctx, String msg) {
		System.err.format("%s: %s", ctx, msg).println();
	}

}