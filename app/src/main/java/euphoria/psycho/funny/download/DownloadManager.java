package euphoria.psycho.funny.download;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import euphoria.psycho.funny.util.AndroidContext;

public class DownloadManager {
    private final Context mContext;
    private final Executor mExecutor;
    private final List<DownloadStatusUpdater> mStatusUpdaters = new ArrayList<>();
    private final DownloadStatusUpdater mUpdater = new DownloadStatusUpdater() {
        @Override
        public void complete(DownloadInfo task) {
            for (DownloadStatusUpdater updater : mStatusUpdaters) {
                updater.complete(task);
            }
        }

        @Override
        public void update(DownloadInfo task) {
            for (DownloadStatusUpdater updater : mStatusUpdaters) {
                updater.update(task);
            }
        }
    };
    private DownloadInfo mDownloadInfo;

    private DownloadManager(Context context) {
        mContext = context;
        mExecutor = Executors.newFixedThreadPool(1);
    }

    public void addUpdater(DownloadStatusUpdater updater) {
        if (mStatusUpdaters.contains(updater)) return;
        mStatusUpdaters.add(updater);
    }

    public void removeUpdater(DownloadStatusUpdater updater) {
        if (!mStatusUpdaters.contains(updater)) return;
        mStatusUpdaters.remove(updater);
    }

    public void startDownload() {
        if (mDownloadInfo == null || (mDownloadInfo != null && mDownloadInfo.finished)) {
            mDownloadInfo = null;
            List<DownloadInfo> infos = DownloadDatabase.getInstance(mContext).getPendingTasks();
            if (infos.size() == 0) return;
            mDownloadInfo = infos.get(0);
        }
        if (mDownloadInfo == null) return;

        Intent intent = new Intent(mContext, DownloadService.class);
        mContext.startService(intent);
        DownloadThread thread = new DownloadThread(mDownloadInfo, mContext, mUpdater);
        mExecutor.execute(thread);
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
