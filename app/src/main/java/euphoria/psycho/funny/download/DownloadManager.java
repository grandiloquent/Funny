package euphoria.psycho.funny.download;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import euphoria.psycho.funny.util.AndroidContext;

public class DownloadManager {
    private final Context mContext;
    private final Executor mExecutor;


    private final List<DownloadStatusUpdater> mStatusUpdaters = new ArrayList<>();


    public void addUpdater(DownloadStatusUpdater updater) {
        if (mStatusUpdaters.contains(updater)) return;
        mStatusUpdaters.add(updater);
    }

    public void removeUpdater(DownloadStatusUpdater updater) {
        if (!mStatusUpdaters.contains(updater)) return;
        mStatusUpdaters.remove(updater);
    }

    private DownloadManager(Context context) {
        mContext = context;
        mExecutor = Executors.newFixedThreadPool(1);
    }

    public static DownloadManager instance() {
        return Singleton.INSTANCE;
    }

    public interface DownloadStatusUpdater {
        void complete(DownloadInfo task);

        void update(DownloadInfo task);
    }

    private static class Singleton {
        private static final DownloadManager INSTANCE =
                new DownloadManager(AndroidContext.instance().get());
    }
}
