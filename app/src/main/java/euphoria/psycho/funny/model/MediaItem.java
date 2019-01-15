package euphoria.psycho.funny.model;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
public class MediaItem {
    public boolean isBrowseable;
    public String album;
    public String artist;
    public String path;
    public String title;
    public String duration;
    public Bitmap bitmap;
    @NonNull
    @Override
    public String toString() {
        return title;
    }
}
