package euphoria.psycho.funny.downloader;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.funny.util.AndroidContext;

public class DataManager {
    private final Context mContext;

    private DataManager(Context context) {
        mContext = context;
    }

    public DownloaderItem findItemById(long id) {
        return null;
    }

    public List<DownloaderItem> findItemByStatus(int status) {
        List<DownloaderItem> items = new ArrayList<>();
        return items;
    }

    public List<DownloaderItem> loadDownloadingData() {
        List<DownloaderItem> items = new ArrayList<>();
        return items;
    }

    public static DataManager instance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final DataManager INSTANCE =
                new DataManager(AndroidContext.instance().get());
    }


}
