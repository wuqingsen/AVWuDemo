package com.demo.audiowudemo.util;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

/**
 * wuqingsen on 2020-06-10
 * Mailbox:1243411677@qq.com
 * annotation:两个pcm文件合成，必须长度相同，采样相同
 */
public class PcmMixer {

    public static void startMix(String voicePcm1, String voicePcm2, String voiceWav) {
        int filesize = 2;
        int bufferSize = 320;
        FileInputStream[] audioFileStreams = new FileInputStream[filesize];
        File audioFile = null;

        FileInputStream inputStream = null;
        FileOutputStream out = null;

        byte[][] allAudioBytes = new byte[filesize][];
        boolean[] streamDoneArray = new boolean[filesize];
        byte[] buffer = new byte[filesize];
        int offset;
        try {
            audioFile = new File(voicePcm1);
            audioFileStreams[0] = new FileInputStream(audioFile);
            audioFile = new File(voicePcm2);
            audioFileStreams[1] = new FileInputStream(audioFile);

            out = new FileOutputStream(voiceWav);
            long totalDataLen = audioFileStreams[0].getChannel().size() + 36;
            long byteRate = 16 * 16000 * 1 / 8;
            try {
                //添加头，不添加合成格式为pcm格式
                writeWaveFileHeader(out, totalDataLen,
                        320, 16000, 1, byteRate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(audioFileStreams[0].getChannel().size());
            System.out.println(audioFileStreams[1].getChannel().size());

            while (true) {

                for (int streamIndex = 0; streamIndex < filesize; ++streamIndex) {

                    inputStream = audioFileStreams[streamIndex];

                    if (!streamDoneArray[streamIndex] && (offset = inputStream.read(buffer)) != -1) {
                        allAudioBytes[streamIndex] = Arrays.copyOf(buffer, buffer.length);
                    } else {
                        streamDoneArray[streamIndex] = true;
                        allAudioBytes[streamIndex] = new byte[bufferSize];
                    }
                }

                byte[] mixBytes = mixRawAudioBytes(allAudioBytes);


                if (mixBytes != null) {
                    //写文件
                    out.write(mixBytes);
                }
                boolean done = true;
                for (boolean streamEnd : streamDoneArray) {
                    if (!streamEnd) {
                        done = false;
                    }
                }
                if (done) {
                    System.out.print(out.getChannel().size());
                    break;
                }


            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                for (FileInputStream in : audioFileStreams) {
                    if (in != null)
                        in.close();
                }
                if (out != null)
                    out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public static byte[] mixRawAudioBytes(byte[][] bMulRoadAudios) {

        if (bMulRoadAudios == null || bMulRoadAudios.length == 0)
            return null;

        byte[] realMixAudio = bMulRoadAudios[0];
        if (realMixAudio == null) {
            return null;
        }

        final int row = bMulRoadAudios.length;

        //单路音轨
        if (bMulRoadAudios.length == 1)
            return realMixAudio;

        //不同轨道长度要一致，不够要补齐

        for (int rw = 0; rw < bMulRoadAudios.length; ++rw) {
            if (bMulRoadAudios[rw] == null || bMulRoadAudios[rw].length != realMixAudio.length) {
                //System.out.println("column of the road of audio + " + rw + " is diffrent.");
                return null;
            }
        }

        /**
         * 精度为 16位
         */
        int col = realMixAudio.length / 2;
        short[][] sMulRoadAudios = new short[row][col];

        for (int r = 0; r < row; ++r) {
            for (int c = 0; c < col; ++c) {
                sMulRoadAudios[r][c] = (short) ((bMulRoadAudios[r][c * 2] & 0xff) | (bMulRoadAudios[r][c * 2 + 1] & 0xff) << 8);
            }
        }

        short[] sMixAudio = new short[col];
        int mixVal;
        int sr = 0;
        for (int sc = 0; sc < col; ++sc) {
            mixVal = 0;
            sr = 0;
            for (; sr < row; ++sr) {
                mixVal += sMulRoadAudios[sr][sc];
            }
            sMixAudio[sc] = (short) (mixVal / row);
        }

        for (sr = 0; sr < col; ++sr) {
            realMixAudio[sr * 2] = (byte) (sMixAudio[sr] & 0x00FF);
            realMixAudio[sr * 2 + 1] = (byte) ((sMixAudio[sr] & 0xFF00) >> 8);
        }

        return realMixAudio;
    }

    public static void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                           long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws Exception {
        byte[] header = new byte[44];
        // RIFF/WAVE header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        //WAVE
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // 'fmt ' chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // 4 bytes: size of 'fmt ' chunk
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // format = 1
        header[20] = 1;
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // block align
        header[32] = (byte) (2 * 16 / 8);
        header[33] = 0;
        // bits per sample
        header[34] = 16;
        header[35] = 0;
        //data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

}
