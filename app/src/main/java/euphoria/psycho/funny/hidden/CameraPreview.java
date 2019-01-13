package euphoria.psycho.funny.hidden;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import euphoria.psycho.funny.util.debug.Log;
import euphoria.psycho.funny.util.Simple;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";
    private static final String FOCUS_MODE_CONTINUOUS_PICTURE = "continuous-picture";
    private Camera mCamera;
    private SurfaceHolder mHolder;
    private final CameraCallbacks mCallbacks;
    private boolean mEnable = false;


    public CameraPreview(Context context, CameraCallbacks callbacks) {
        super(context);

        mCallbacks = callbacks;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera == null) {
            mCallbacks.onCameraError(CameraError.ERROR_CAMERA_OPEN_FAILED);
            return;
        } else if (holder.getSurface() == null) {
            mCallbacks.onCameraError(CameraError.ERROR_CAMERA_OPEN_FAILED);
            return;
        }
        mCamera.stopPreview();
        Camera.Parameters parameters = mCamera.getParameters();
        setupPictureSize(parameters);
        setupFocusMode(parameters);

        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(holder);

        } catch (IOException e) {
            mCallbacks.onCameraError(CameraError.ERROR_CAMERA_OPEN_FAILED);
            return;
        }
        mCamera.startPreview();
        mEnable = true;

    }


    private void setupPictureSize(Camera.Parameters parameters) {
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        Collections.sort(pictureSizes, (o1, o2) -> (o1.width * o1.height) - (o2.width * o2.height));
        dumpSupportPictureSizes(pictureSizes);
        Camera.Size biggestSize = pictureSizes.get(pictureSizes.size() - 1);
        parameters.setPictureSize(biggestSize.width, biggestSize.height);

    }

    private void setupFocusMode(Camera.Parameters parameters) {
        List<String> supportedModes = parameters.getSupportedFocusModes();

        if (Simple.linearSearch(supportedModes, FOCUS_MODE_CONTINUOUS_PICTURE) != -1) {
            parameters.setFocusMode(FOCUS_MODE_CONTINUOUS_PICTURE);
        }
       /*
           infinity
    auto
    macro
    continuous-video
    continuous-picture
        */
        // dumpSupportFocusModes(supportedModes);
    }

    private void dumpSupportPictureSizes(List<Camera.Size> sizes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Camera Support Sizes: ---> \n");
        for (Camera.Size size : sizes) {
            sb.append(size.width).append('x').append(size.height).append('\n');
        }
        Log.d(TAG, sb.toString());
    }

    private void dumpSupportFocusModes(List<String> focusModes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Camera Focus Modes: ---> \n");
        for (String focusMode : focusModes) {
            sb.append(focusMode).append('\n');
        }
        Log.d(TAG, sb.toString());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    public void startCamera() {
        release();
        mCamera = Camera.open();

        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                Log.e(TAG, "[startCamera] ---> ", e);
                return;
            }
            mCamera.startPreview();

        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
        }

    }

    private void release() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
