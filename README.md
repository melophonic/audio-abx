# audio-abx
An audio testing tool which supports creation of [ABX tests](http://en.wikipedia.org/wiki/ABX_test) from
standard audio files, allowing the user to conduct blind tests to assess listener comprehension or preference.
While there are a number of such tools already available, this application provides features to sync and
automatically adjust playback in cases when the audio files are different enough to make testing problematic. 
Files which contain the same audio but are different lengths (for example, due to a slightly longer lead-in) 
can be automatically aligned for playback based on the shortest file in the set. For files which have varying loudness 
(for example, due to differing compression), the playback gain can be automatically adjusted based on the 
quietest file in the set so that all files sound about as loud on average. It has been shown that listeners 
have a strong bias for even slightly louder audio, and while a flat gain adjustment can't compensate for varying 
dynamic ranges, it can go a long way to reducing obvious differences in volume. 

## Implementation notes
The tool requires Java 8 and is based on [e(fx)clipse platform](http://www.eclipse.org/efxclipse/install.html). 
Audio I/O is handled via the [JavaSound API](http://docs.oracle.com/javase/8/docs/api/javax/sound/sampled/package-summary.html), 
and additional audio formats registered via JavaSound will be visible in most cases to the app. 
The [musicg](https://code.google.com/p/musicg/) library is used to calculate similarity and offsets, 
and SPL analysis is performed using the excellent [TarsosDSP](https://github.com/JorenSix/TarsosDSP) library. 
Playback is implemented using JavaFX but could be extended to integrate with other players.


## Features
* Load an arbitrary number of files that can be selectively included/excluded in tests
* Apply timing offsets and gain adjustments to individual files
* Automatically calculate timing and gain adjustments for all files at once
* Isolate small clips within audio for testing
* ABX test mode can be used to test listener discernment
* Shootout test mode can be used to test listener preference
* Save and reload test sets and all file adjustments
* Preserves original, unaltered audio; normalization used for analysis only

## Limitations
* Add platform-specific builds
* Fx widget styling is broken
* Currently supports only .wav, .flac, .aif files, need support for .mp3, .m4a, .ogg
