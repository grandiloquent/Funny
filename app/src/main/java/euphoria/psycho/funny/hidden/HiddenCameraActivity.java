package euphoria.psycho.funny.hidden;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import euphoria.psycho.funny.R;

public class HiddenCameraActivity extends AppCompatActivity implements CameraCallbacks {
    private CameraPreview mCameraPreview;

    @Override
    public void onCameraError(int code) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden);
        mCameraPreview = new CameraPreview(this, this);
        LinearLayout layout = findViewById(R.id.layout);
        layout.addView(mCameraPreview, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mCameraPreview.startCamera();
    }

    @Override
    public void onImageCapture(File image) {

    }
}
