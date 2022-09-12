package com.audio.recode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private AudioRecordUtil audioRecordUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {
                    Manifest.permission.RECORD_AUDIO}, 21);
        }

        audioRecordUtil = new AudioRecordUtil(this);
    }

    /**
     * 开始录音
     * @param view
     */
    public void start_recode(View view) {
        audioRecordUtil.start_recode();
    }

    /**
     * 停止录音
     * @param view
     */
    public void stop_recode(View view) {
        audioRecordUtil.stop_recode();
    }
}