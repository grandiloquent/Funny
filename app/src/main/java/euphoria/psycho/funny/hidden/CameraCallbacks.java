package euphoria.psycho.funny.hidden;

import java.io.File;

public interface CameraCallbacks {
    void onImageCapture(File image);

    void onCameraError(int code);
}
