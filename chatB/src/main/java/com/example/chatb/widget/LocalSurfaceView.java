package com.example.chatb.widget;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.chatb.live.EncodecPushLiveH264;
import com.example.chatb.live.SocketLive;

import java.io.IOException;

//本地surfaceView
public class LocalSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera.Size size;
    private Camera camera;
    EncodecPushLiveH264 encodecPushLiveH264;
    public static final int FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;
    public static final int BACK = Camera.CameraInfo.CAMERA_FACING_BACK;

    public LocalSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (listener != null) {
            listener.onCreate();
        }
    }

    byte[] buffer;

    //开始预览
    public void startPreview(Activity activity, int CameraId) {
        releaseCamera();
        camera = Camera.open(CameraId);
        Camera.Parameters parameters = camera.getParameters();
        size = parameters.getPreviewSize();
        try {
            camera.setPreviewDisplay(getHolder());
            camera.setDisplayOrientation(calculateCameraPreviewOrientation(activity, CameraId));
            buffer = new byte[size.width * size.height * 3 / 2];
            camera.addCallbackBuffer(buffer);
            camera.setPreviewCallbackWithBuffer(this);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (this.listener != null) {
            listener.onCreate();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    public void startCapture(SocketLive.SocketCallback socketCallback) {
        encodecPushLiveH264 = new EncodecPushLiveH264(socketCallback, size.width, size.height);
        encodecPushLiveH264.startLive();
    }

    public void close(){
        encodecPushLiveH264.close();
    }

    public void sendData(byte[] bytes) {
        encodecPushLiveH264.sendData(bytes);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
//        获取到摄像头的原始数据yuv
//        开始    视频通话
        if (encodecPushLiveH264 != null) {
            encodecPushLiveH264.encodeFrame(bytes);
        }
        this.camera.addCallbackBuffer(bytes);
    }

    //计算旋转角度
    private int calculateCameraPreviewOrientation(Activity activity, int cameraID) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraID, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    CameraListener listener;

    public void setListener(CameraListener listener) {
        this.listener = listener;
    }

    public interface CameraListener {
        void onCreate();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
