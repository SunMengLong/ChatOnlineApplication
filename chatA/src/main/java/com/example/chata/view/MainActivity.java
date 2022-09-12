package com.example.chata.view;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chata.R;
import com.example.chata.live.DecodecPlayerLiveH264;
import com.example.chata.live.SocketLive;
import com.example.chata.widget.LocalSurfaceView;

public class MainActivity extends AppCompatActivity implements SocketLive.SocketCallback {
    SurfaceView remoteSurface;
    LocalSurfaceView localSurfaceView;
    DecodecPlayerLiveH264 decodecPlayerLiveH264;
    Surface surface;
    private EditText ed;

    private AudioTrack mAudioTrack;

    private int recBufSize, playBufSize;//采集缓冲区的大小，播放缓冲区的大小
    private static final int sampleRateInHz = 44100;//采样率
    private static final int channelInConfig = AudioFormat.CHANNEL_IN_MONO;//采集通道数
    private static final int channelOutConfig = AudioFormat.CHANNEL_OUT_MONO;//播放通道数
    private static final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;//位数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO}, 21);
        }

        ed = findViewById(R.id.edit);

        recBufSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelInConfig, audioFormat);
        playBufSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelOutConfig, audioFormat);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRateInHz, channelOutConfig, audioFormat, playBufSize, AudioTrack.MODE_STREAM);
    }

    private void initView() {
        remoteSurface = findViewById(R.id.removeSurfaceView);
        localSurfaceView = findViewById(R.id.localSurfaceView);
        remoteSurface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                surface = holder.getSurface();
                decodecPlayerLiveH264 = new DecodecPlayerLiveH264();
                decodecPlayerLiveH264.initDecoder(surface);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            }
        });
        localSurfaceView.setListener(() -> localSurfaceView.startPreview(MainActivity.this, LocalSurfaceView.BACK));
    }

    //呼叫
    public void connect(View view) {
        String port = ed.getText().toString();
        if(!TextUtils.isEmpty(port)){
            SocketLive.port = port;
        }
        localSurfaceView.startCapture(this);
        new RecordThread().start();
    }

    byte[] data = new byte[0];

    @Override
    public void callBack(byte[] data) {
        byte[] newByte = new byte[data.length-1];
        System.arraycopy(data,1, newByte, 0, data.length-1);
        if(data[0] == 1){
            Log.i("smllllll", "callBack: ......视频数据:"+data[0]);
            // 视频
            if (decodecPlayerLiveH264 != null) {
                decodecPlayerLiveH264.callBack(newByte);
            }
        } else {
            this.data = newByte;
            // 音频
            Log.i("smllllllll", "callBack: ....音频数据");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        localSurfaceView.stop();
    }

    private boolean isStart = true;

    private class RecordThread extends Thread {
        @Override
        public void run() {
            //采集的同时播放
            mAudioTrack.play();

            while (isStart) {
                //播放声音
                if(data.length > 0){
                    mAudioTrack.write(data, 0, data.length);
                }
            }
            //结束播放和采集
            mAudioTrack.stop();
        }
    }
}