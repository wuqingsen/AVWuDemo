package com.demo.audiowudemo.mixutils16;

import java.io.IOException;

/**
 * wuqingsen on 2020-06-11
 * Mailbox:1243411677@qq.com
 * annotation:音频混音
 */
public abstract class AudioDecoder {

	String mEncodeFile;

	OnAudioDecoderListener mOnAudioDecoderListener;

	AudioDecoder(String encodefile) {
		this.mEncodeFile = encodefile;
	}

	public static AudioDecoder createDefualtDecoder(String encodefile) {
		return new AndroidAudioDecoder(encodefile);
	}

	public void setOnAudioDecoderListener(OnAudioDecoderListener l) {
		this.mOnAudioDecoderListener = l;
	}

	/**
	 * ����
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract RawAudioInfo decodeToFile(String outFile)
			throws IOException;

	public static class RawAudioInfo {
		public String tempRawFile;
		public int size;
		public long sampleRate;
		public int channel;
	}

	public interface OnAudioDecoderListener {
		/**
		 * monitor when processing decode
		 * 
		 * @param decodedBytes
		 * @param progress
		 *            range 0~1
		 * @throws IOException
		 */
		void onDecode(byte[] decodedBytes, double progress) throws IOException;
	}
}
