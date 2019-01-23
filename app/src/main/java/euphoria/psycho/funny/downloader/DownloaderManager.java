package euphoria.psycho.funny.downloader;


import android.content.Context;

import euphoria.psycho.funny.util.AndroidContext;

public class DownloaderManager {
    private final Context mContext;

    private DownloaderManager(Context context) {
        mContext = context;
    }

    public void addUpdater(DownloaderStatusUpdater updater) {

    }

    public void removeUpdater(DownloaderStatusUpdater updater) {

    }




    public static DownloaderManager instance() {
        return Singleton.INSTANCE;
    }

    public interface DownloaderStatusUpdater {
        void complete(DownloaderTask task);

        void update(DownloaderTask task);
    }

    private static class Singleton {
        private static final DownloaderManager INSTANCE =
                new DownloaderManager(AndroidContext.instance().get());
    }
}
