package euphoria.psycho.funny.download;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.Formatter;

import com.liulishuo.filedownloader.model.FileDownloadStatus;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class DownloadService extends Service implements DownloadManager.DownloadStatusUpdater {
    public static final String CHANNEL_ID = "download";
    private static final int ID_NOTIFICATION = 2;

    private boolean shouldStop() {
        return DownloadDatabase.getInstance(this).getPendingTasks().size() == 0;
    }

    private void startNotification(String fileName, int progress, String fileSize, long speed) {

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

    private void updateNotification(DownloadInfo task, long currentBytes, long totalBytes) {
        int progress = (int) (((float) currentBytes / totalBytes) * 100);
        String fileSize = Formatter.formatFileSize(DownloadService.this, currentBytes) + "/" +
                Formatter.formatFileSize(DownloadService.this, totalBytes);

        if (task != null) {
            if (task.finished) {
                List<DownloadInfo> downloaderItemList = DownloadDatabase.getInstance(this).getPendingTasks();
                if (shouldStop()) {
                    stopForeground(true);
                } else {
                    startNotification(task.fileName, progress, fileSize, task.speed);
                }
            }
        } else {

            if (shouldStop()) {
                stopForeground(true);
            }
        }
    }

    @Override
    public void complete(DownloadInfo task) {
        updateNotification(task, task.currentBytes, task.totalBytes);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DownloadManager.instance().addUpdater(this);
    }

    @Override
    public void onDestroy() {
        DownloadManager.instance().removeUpdater(this);
        stopForeground(true);
        super.onDestroy();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    @Override
    public void update(DownloadInfo task) {
        updateNotification(task, task.currentBytes, task.totalBytes);
    }
}
