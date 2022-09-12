package com.example.chatb.view;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatb.R;
import com.example.chatb.live.DecodecPlayerLiveH264;
import com.example.chatb.live.SocketLive;
import com.example.chatb.util.AudioRecordUtil;
import com.example.chatb.widget.LocalSurfaceView;

public class MainActivity extends AppCompatActivity implements SocketLive.SocketCallback {
    SurfaceView removeSurfaceView;
    LocalSurfaceView localSurfaceView;
    DecodecPlayerLiveH264 decodecPlayerLiveH264;
    Surface surface;
    private EditText ed;
    private AudioRecordUtil audioRecordUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        initView();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO}, 200);
        }
    }

    private void initView() {
        ed = findViewById(R.id.edit);
      removeSurfaceView = findViewById(R.id.remoteSurfaceView);
      localSurfaceView = findViewById(R.id.localSurfaceView);
      removeSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
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

        audioRecordUtil = new AudioRecordUtil(this, new SocketLive.SocketCallback() {
            @Override
            public void callBack(byte[] data) {
                localSurfaceView.sendData(data);
            }
        });
    }
    public void connect(View view) {
        String port = ed.getText().toString();
        if(!TextUtils.isEmpty(port)){
            SocketLive.port = Integer.parseInt(port);
        }
        localSurfaceView.startCapture(this);
        audioRecordUtil.start_recode();
    }
//    socket 接收到了另外一段的数据
    @Override
    public void callBack(byte[] data) {

        if (decodecPlayerLiveH264 != null) {
            decodecPlayerLiveH264.callBack(data);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        localSurfaceView.close();
    }
}