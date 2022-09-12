package com.audio.recode;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

/**
 * 录音
 */
public class AudioRecordUtil {

    private int recBufSize, playBufSize;//采集缓冲区的大小，播放缓冲区的大小
    private static final int sampleRateInHz = 44100;//采样率
    private static final int channelInConfig = AudioFormat.CHANNEL_IN_MONO;//采集通道数
    private static final int channelOutConfig = AudioFormat.CHANNEL_OUT_MONO;//播放通道数
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;//位数

    private AudioRecord mAudioRecord;
    private AudioTrack mAudioTrack;

    private boolean isRecording;//采集状态

    public AudioRecordUtil(Context context) {
        initRecode(context);
    }

    private void initRecode(Context context) {
        recBufSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelInConfig, audioFormat);
        playBufSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelOutConfig, audioFormat);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"没有录音权限",Toast.LENGTH_SHORT).show();
            return;
        }
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRateInHz, channelInConfig, audioFormat, recBufSize);

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRateInHz, channelOutConfig, audioFormat, playBufSize, AudioTrack.MODE_STREAM);
    }

    public void start_recode() {
        isRecording = true;
        new RecordThread().start();
    }

    public void stop_recode() {
        isRecording = false;
    }

    private class RecordThread extends Thread {
        @Override
        public void run() {

            //采集的音频缓冲区
            byte[] buffer = new byte[recBufSize];
            //开始采集
            mAudioRecord.startRecording();
            //采集的同时播放
            mAudioTrack.play();

            while (isRecording) {
                //从音频缓冲区取出声音数据
                int bufferReadResult = mAudioRecord.read(buffer, 0, recBufSize);
                //播放音频缓冲区
                byte[] tempBuffer = new byte[bufferReadResult];

                //把音频数据拷贝到播放缓冲区
                System.arraycopy(buffer, 0, tempBuffer, 0, bufferReadResult);
                //播放声音
                mAudioTrack.write(tempBuffer, 0, tempBuffer.length);

            }
            //结束播放和采集
            mAudioTrack.stop();
            mAudioRecord.stop();
        }
    }
}
