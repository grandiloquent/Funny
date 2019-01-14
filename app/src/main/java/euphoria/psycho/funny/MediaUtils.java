package euphoria.psycho.funny;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import java.io.File;
import euphoria.psycho.common.Shared;
import static euphoria.psycho.common.CheckUtils.isEmpty;
import static euphoria.psycho.utils.FileUtils.getFileName;
public class MediaUtils {
    private final static String TAG = "MediaUtils";
    private static String[] sAudioExtensions;
    static {
        sAudioExtensions = new String[]{
                ".mp3"
        };
    }
    public static MediaItem getMediaItem(File path) {
        MediaItem item = new MediaItem();
        item.path = path.getAbsolutePath();
        if (path.isDirectory()) {
            item.isBrowseable = true;
            item.title = path.getName();
            return item;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path.getAbsolutePath());
        item.title = getFileName(path.getAbsolutePath());
//        item.title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//        if (isEmpty(item.title)) {
//            item.title = getFileName(path.getAbsolutePath());
//        }
        item.artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        item.duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        item.isBrowseable = false;
        byte[] bitmap = retriever.getEmbeddedPicture();
        if (bitmap != null) {
            item.bitmap = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
        }
        //        int[] keyArray = new int[]{
//                MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER,
//                /*1*/  MediaMetadataRetriever.METADATA_KEY_ALBUM,
//                /*2*/    MediaMetadataRetriever.METADATA_KEY_ARTIST,
//                MediaMetadataRetriever.METADATA_KEY_AUTHOR,
//                MediaMetadataRetriever.METADATA_KEY_COMPOSER,
//                MediaMetadataRetriever.METADATA_KEY_DATE,
//                MediaMetadataRetriever.METADATA_KEY_GENRE,
//                /*7*/ MediaMetadataRetriever.METADATA_KEY_TITLE,
//                MediaMetadataRetriever.METADATA_KEY_YEAR,
//                /*9*/   MediaMetadataRetriever.METADATA_KEY_DURATION,
//                /*10*/     MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS,
//                MediaMetadataRetriever.METADATA_KEY_WRITER,
//                /*12*/ MediaMetadataRetriever.METADATA_KEY_MIMETYPE,
//                MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST,
//                MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER,
//                MediaMetadataRetriever.METADATA_KEY_COMPILATION,
//                /*16*/   MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO,
//                MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO,
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH,
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT,
//                /*20*/ MediaMetadataRetriever.METADATA_KEY_BITRATE,
//                MediaMetadataRetriever.METADATA_KEY_LOCATION,
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION,
//                MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE,
//                MediaMetadataRetriever.METADATA_KEY_HAS_IMAGE,
//                MediaMetadataRetriever.METADATA_KEY_IMAGE_COUNT,
//                MediaMetadataRetriever.METADATA_KEY_IMAGE_PRIMARY,
//                MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH,
//                MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT,
//                MediaMetadataRetriever.METADATA_KEY_IMAGE_ROTATION,
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT,
//        };
//
//        for (int i = 0; i < keyArray.length; i++) {
//            String meta = retriever.extractMetadata(keyArray[i]);
//            Log.e(TAG, keyArray[i] + ": " + meta);
//        }
        return item;
    }
    public static boolean isAudioFile(File file) {
        String name = file.getName();
        for (String sAudioExtension : sAudioExtensions) {
            if (Shared.endWiths(name, sAudioExtension, true)) {
                return true;
            }
        }
        return false;
    }
    public static File[] listAudioFiles(File dir) {
        return dir.listFiles(pathname -> pathname.isFile() && isAudioFile(pathname));
    }
}
