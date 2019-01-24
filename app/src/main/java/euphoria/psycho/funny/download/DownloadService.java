package euphoria.psycho.funny.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;

import com.liulishuo.filedownloader.model.FileDownloadStatus;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import euphoria.psycho.funny.util.Androids;

public class DownloadService extends Service implements DownloadManager.DownloadStatusUpdater {
    public static final String CHANNEL_ID = "download";
    public static final boolean DEBUG = false;
    private static final int ID_NOTIFICATION = 2;
    private static final String TAG = "TAG/" + DownloadService.class.getSimpleName();

    private boolean shouldStop() {
        return DownloadDatabase.getInstance(this).getPendingTasks().size() == 0;
    }

    private void startNotification(String fileName, int progress, String fileSize, long speed) {
        if (DEBUG) {
            Log.d(TAG, "startNotification: ");
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle("正在下载");
        builder.setOnlyAlertOnce(true);
        builder.setSmallIcon(android.R.drawable.stat_sys_download);
        builder.setProgress(100, progress, false);
        builder.setContentText(fileSize + " - " + Formatter.formatFileSize(this, speed) + "/s");
        builder.setContentInfo(fileName);
        Intent intent = new Intent(this, DownloadActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = builder.build();
        if (DEBUG) {
            Log.d(TAG, "startNotification: " + "builder");
        }
        startForeground(ID_NOTIFICATION, notification);
    }

    private void updateNotification(DownloadInfo task, long currentBytes, long totalBytes) {
        if (DEBUG) {
            Log.d(TAG, "updateNotification: " + "task = " + task + ", " + "currentBytes = " + currentBytes + ", " + "totalBytes = " + totalBytes + ", ");
        }
        int progress = totalBytes == 0 ? 0 : (int) (((float) currentBytes / totalBytes) * 100);
        String fileSize = totalBytes == 0 ? "" : Formatter.formatFileSize(DownloadService.this, currentBytes) + "/" +
                Formatter.formatFileSize(DownloadService.this, totalBytes);

        if (task != null) {
            if (!task.finished) {
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

    public static void initChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = CHANNEL_ID;
            String channelName = "视频下载";
            int importance = NotificationManager.IMPORTANCE_LOW;
            Androids.createNotificationChannel(channelId, channelName, importance);

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
        initChannel();
        DownloadManager.instance().addUpdater(this);


        if (DEBUG) {
            Log.d(TAG, "onCreate: ");
        }
    }

    @Override
    public void onDestroy() {
        DownloadManager.instance().removeUpdater(this);
        stopForeground(true);
        super.onDestroy();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) {
            Log.d(TAG, "onStartCommand: ");
        }
        return START_NOT_STICKY;
    }


    @Override
    public void update(DownloadInfo task) {
        if (DEBUG) {
            Log.d(TAG, "update: ");
        }
        updateNotification(task, task.currentBytes, task.totalBytes);
    }
}
