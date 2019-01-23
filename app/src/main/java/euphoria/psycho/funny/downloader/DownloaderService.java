package euphoria.psycho.funny.downloader;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.Formatter;

import com.liulishuo.filedownloader.model.FileDownloadStatus;

import java.text.Format;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class DownloaderService extends Service implements DownloaderManager.DownloaderStatusUpdater {
    public static final String CHANNEL_ID = "download";
    private static final int ID_NOTIFICATION = 2;

    private void startNotification(String fileName, int progress, String fileSize, int speed) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle("正在下载");
        builder.setOnlyAlertOnce(true);
        builder.setSmallIcon(android.R.drawable.stat_sys_download);
        builder.setProgress(100, progress, false);
        builder.setContentText(fileSize + " - " + speed + "KB/s");
        builder.setContentInfo(fileName);
        Intent intent = new Intent(this, DownloadActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = builder.build();
        startForeground(ID_NOTIFICATION, notification);
    }

    private void updateNotification(DownloaderTask task, int soFarBytes, int totalBytes) {
        int progress = (int) (((float) soFarBytes / totalBytes) * 100);
        String fileSize = Formatter.formatFileSize(DownloaderService.this, soFarBytes) + "/" +
                Formatter.formatFileSize(DownloaderService.this, totalBytes);
        DownloaderItem downloaderItem = DataManager.instance().findItemById(task.getId());
        if (downloaderItem != null) {
            if (task.getStatus() == FileDownloadStatus.completed) {
                List<DownloaderItem> items = DataManager.instance().findItemByStatus(FileDownloadStatus.progress);
                if (items.size() == 0) {
                    stopForeground(true);
                } else {
                    startNotification(downloaderItem.getTitle(), progress, fileSize, task.getSpeed());
                }
            }
        } else {
            List<DownloaderItem> downloaderItemList = DataManager.instance().loadDownloadingData();
            if (downloaderItemList.size() == 0) {
                stopForeground(true);
            }
        }
    }

    @Override
    public void complete(DownloaderTask task) {
        updateNotification(task, task.getSmallFileSoFarBytes(), task.getSmallFileTotalBytes());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DownloaderManager.instance().addUpdater(this);
    }

    @Override
    public void onDestroy() {
        DownloaderManager.instance().removeUpdater(this);
        stopForeground(true);
        super.onDestroy();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    @Override
    public void update(DownloaderTask task) {
        updateNotification(task, task.getSmallFileSoFarBytes(), task.getSmallFileTotalBytes());
    }
}
