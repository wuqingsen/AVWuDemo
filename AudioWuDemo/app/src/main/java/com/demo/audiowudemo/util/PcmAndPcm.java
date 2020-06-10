package com.demo.audiowudemo.util;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * wuqingsen on 2020-06-05
 * Mailbox:1243411677@qq.com
 * annotation:
 */
public class PcmAndPcm {
    private String filePath = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.pcm";
    private String filePath1 = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo1.pcm";
    private String filePath2 = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo2.pcm";

//    public void start() {
//        PlayPCMRecord playPCMRecord = new PlayPCMRecord();
//        playPCMRecord.start();
//    }

    public static void meargeAudio() {
        String filePath = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo.pcm";
        String filePath1 = Environment.getExternalStorageDirectory() + "/" + "cameraWuDemo1.pcm";
        List<File> filesToMearge = new ArrayList<>();
        filesToMearge.add(new File(filePath));
        filesToMearge.add(new File(filePath1));

        while (filesToMearge.size() != 1) {

            try {
                FileInputStream fistream1 = new FileInputStream(new File(filesToMearge.get(0).getPath()));  //(/storage/emulated/0/Audio Notes/1455194356500.mp3) first source file
                FileInputStream fistream2 = new FileInputStream(new File(filesToMearge.get(1).getPath()));//second source file

                File file1 = new File(filesToMearge.get(0).getPath());
                boolean deleted = file1.delete();
                File file2 = new File(filesToMearge.get(1).getPath());
                boolean deleted1 = file2.delete();

                SequenceInputStream sistream = new SequenceInputStream(fistream1, fistream2);
                FileOutputStream fostream = new FileOutputStream(new File(filesToMearge.get(0).getPath()), true);//destinationfile

                int temp;

                while ((temp = sistream.read()) != -1) {
                    // System.out.print( (char) temp ); // to print at DOS prompt
                    fostream.write(temp);   // to write to file
                }

                filesToMearge.add(0, new File(filesToMearge.get(0).getPath()));
                filesToMearge.remove(1);
                filesToMearge.remove(1);


                fostream.close();
                sistream.close();
                fistream1.close();
                fistream2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class PlayPCMRecord extends Thread {
        @Override
        public void run() {
            int bufferSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            int bufferSize1 = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            FileInputStream fis = null;
            FileInputStream fis1 = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(filePath);
                fis1 = new FileInputStream(filePath1);
                fos = new FileOutputStream(filePath2);
                byte[] buffer = new byte[bufferSize];
                byte[] buffer1 = new byte[bufferSize1];

                int len = 0;
                int len1 = 0;
                while ((len = fis.read(buffer)) != -1 && (len1 = fis1.read(buffer1)) != -1) {
                    Log.d("wqs", "playPCMRecord1:  " + fis.read(buffer));
                    Log.d("wqs", "playPCMRecord2:  " + fis1.read(buffer1));

//                    byte[] newData = new byte[len + len1];
//                    for (int i = 0; i < len1; i++) {
//                        newData[i] = (byte) ((buffer[i]) + buffer1[i]);
//                    }

//                    byte[] newData = new byte[buffer.length + buffer1.length];
//                    for (int i = 0; i < buffer.length; i++)
//                        newData[i] = buffer[i];
//                    for (int i = 0; i < buffer1.length; i++)
//                        newData[i + buffer.length] = buffer1[i];


//                    byte[] newData = new byte[len + len1];
//                    for (int i = 0; i < buffer1.length; i++) {
//                        buffer[i] = (byte) ((buffer1[i] + buffer[i]) / 2);
//                    }
//
//                    for (int i = 0; i < buffer.length; i++) {
//                        newData[i * 2] = (byte) (buffer[i] & 0x00FF);
//                        newData[i * 2 + 1] = (byte) ((buffer[i] & 0xFF00) >> 8);
//                    }

//                    byte[] byteBuffer = new byte[bufferSize];
//                    Log.d("wqs", "playPCMRecord3:  " + newData.length);
////                    int end = audioRecord.read(byteBuffer, 0, byteBuffer.length);
//                    fos.write(newData, 0, newData.length);
//                    fos.flush();
//                    HS(buffer, buffer1);
                }

            } catch (Exception e) {
                Log.e("wqs", "playPCMRecord: e : " + e);

            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {

                }
            }
        }
    }

    public byte[] mixRawAudioBytes(byte[][] bMulRoadAudios) {

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

//    public byte[] mixRawAudioBytes(byte[][] bMulRoadAudios) {
//
//        int roadLen = bMulRoadAudios.length;
//
//        //单路音轨
//        if (roadLen == 1)
//            return bMulRoadAudios[0];
//
//        int maxRoadByteLen = 0;
//
//        for (byte[] audioData : bMulRoadAudios) {
//            if (maxRoadByteLen < audioData.length) {
//                maxRoadByteLen = audioData.length;
//            }
//        }
//
//        byte[] resultMixData = new byte[maxRoadByteLen * roadLen];
//
//        for (int i = 0; i != maxRoadByteLen; i = i + 2) {
//            for (int r = 0; r != roadLen; r++) {
//                resultMixData[i * roadLen + 2 * r] = bMulRoadAudios[r][i];
//                resultMixData[i * roadLen + 2 * r + 1] = bMulRoadAudios[r][i + 1];
//            }
//        }
//        return resultMixData;
//    }

    /**
     * 混合音频，使用平均算法
     *
     * @param mixedBytes 输出混合后的数据到该byte数组
     * @param shorts1    需要混合的short数组1
     * @param shorts2    需要混合的short数组2
     */
    private void mixRawAudioBytes(byte[] mixedBytes, short[] shorts1, short[] shorts2) {
        for (int i = 0; i < shorts2.length; i++) {
            shorts1[i] = (short) ((shorts2[i] + shorts1[i]) / 2);
        }

        for (int i = 0; i < shorts1.length; i++) {
            mixedBytes[i * 2] = (byte) (shorts1[i] & 0x00FF);
            mixedBytes[i * 2 + 1] = (byte) ((shorts1[i] & 0xFF00) >> 8);
        }
    }

    private void HS(byte[] buffer, byte[] buffer1) {

        //混音
//        short[] newData = new short[buffer.length + buffer1.length];
//        for (int i = 0; i < buffer.length; i++)
//            newData[i] = buffer[i];
//        for (int i = 0; i < buffer1.length; i++)
//            newData[i + buffer.length] = buffer1[i];


    }

    public void mixAudios(File[] rawAudioFiles) {

        final int fileSize = rawAudioFiles.length;

        FileInputStream[] audioFileStreams = new FileInputStream[fileSize];
        File audioFile = null;

        FileInputStream inputStream;
        byte[][] allAudioBytes = new byte[fileSize][];
        boolean[] streamDoneArray = new boolean[fileSize];
        byte[] buffer = new byte[512];
        int offset;

        try {

            for (int fileIndex = 0; fileIndex < fileSize; ++fileIndex) {
                audioFile = rawAudioFiles[fileIndex];
                audioFileStreams[fileIndex] = new FileInputStream(audioFile);
            }

            while (true) {

                for (int streamIndex = 0; streamIndex < fileSize; ++streamIndex) {

                    inputStream = audioFileStreams[streamIndex];
                    if (!streamDoneArray[streamIndex] && (offset = inputStream.read(buffer)) != -1) {
                        allAudioBytes[streamIndex] = Arrays.copyOf(buffer, buffer.length);
                    } else {
                        streamDoneArray[streamIndex] = true;
                        allAudioBytes[streamIndex] = new byte[512];
                    }
                }

                byte[] mixBytes = mixRawAudioBytes(allAudioBytes);

                //mixBytes 就是混合后的数据

                boolean done = true;
                for (boolean streamEnd : streamDoneArray) {
                    if (!streamEnd) {
                        done = false;
                    }
                }

                if (done) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                for (FileInputStream in : audioFileStreams) {
                    if (in != null)
                        in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 每一行是一个音频的数据
     */
    byte[] averageMix(byte[][] bMulRoadAudioes) {

        if (bMulRoadAudioes == null || bMulRoadAudioes.length == 0)
            return null;

        byte[] realMixAudio = bMulRoadAudioes[0];

        if (bMulRoadAudioes.length == 1)
            return realMixAudio;

        for (int rw = 0; rw < bMulRoadAudioes.length; ++rw) {
            if (bMulRoadAudioes[rw].length != realMixAudio.length) {
                Log.e("app", "column of the road of audio + " + rw + " is diffrent.");
                return null;
            }
        }

        int row = bMulRoadAudioes.length;
        int coloum = realMixAudio.length / 2;
        short[][] sMulRoadAudioes = new short[row][coloum];

        for (int r = 0; r < row; ++r) {
            for (int c = 0; c < coloum; ++c) {
                sMulRoadAudioes[r][c] = (short) ((bMulRoadAudioes[r][c * 2] & 0xff) | (bMulRoadAudioes[r][c * 2 + 1] & 0xff) << 8);
            }
        }

        short[] sMixAudio = new short[coloum];
        int mixVal;
        int sr = 0;
        for (int sc = 0; sc < coloum; ++sc) {
            mixVal = 0;
            sr = 0;
            for (; sr < row; ++sr) {
                mixVal += sMulRoadAudioes[sr][sc];
            }
            sMixAudio[sc] = (short) (mixVal / row);
        }

        for (sr = 0; sr < coloum; ++sr) {
            realMixAudio[sr * 2] = (byte) (sMixAudio[sr] & 0x00FF);
            realMixAudio[sr * 2 + 1] = (byte) ((sMixAudio[sr] & 0xFF00) >> 8);
        }

        return realMixAudio;
    }
}
