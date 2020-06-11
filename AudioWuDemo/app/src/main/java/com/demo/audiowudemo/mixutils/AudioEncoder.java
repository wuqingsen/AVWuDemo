package com.demo.audiowudemo.mixutils;

/**
 * wuqingsen on 2020-06-11
 * Mailbox:1243411677@qq.com
 * annotation:音频混音
 */
public abstract class AudioEncoder {

	String rawAudioFile;

	AudioEncoder(String rawAudioFile) {
		this.rawAudioFile = rawAudioFile;
	}

	public static AudioEncoder createAccEncoder(String rawAudioFile) {
		return new AACAudioEncoder(rawAudioFile);
	}

	public abstract void encodeToFile(String outEncodeFile);
}
