package euphoria.psycho.funny.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import euphoria.psycho.funny.SimpleServer;
import euphoria.psycho.funny.util.FileUtils;
import euphoria.psycho.funny.util.HttpUtils;

public class ServerService extends Service {
    private static final String CHANNEL_ID = "_tag_";
    private static final int FOREGROUND_ID = 1 << 1;
    private static final int PORT = 8090;
    private final IBinder mBinder = new ServerBinder();
    private SimpleServer mSimpleServer;

    private Notification buildForegroundNotification() {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID);

        b
                .setContentTitle("视频")
                .setContentText("开启视频分享服务")
                .setSmallIcon(android.R.drawable.stat_sys_download);

        return (b.build());
    }

    public String getURL() {
        return mSimpleServer.getURL();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "视频";
            String description = "视频分享服务";
            int importance = NotificationManager.IMPORTANCE_NONE;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        File sdcardDirectory = new File(Environment.getExternalStorageDirectory(), "Videos");
        FileUtils.createDirectory(sdcardDirectory);
        File storageDirectory = new File(FileUtils.getRemovableStoragePath(), "Videos");
        FileUtils.createDirectory(sdcardDirectory);

        File cacheDirectory = new File(Environment.getExternalStorageDirectory(), ".cache");
        FileUtils.createDirectory(cacheDirectory);

        mSimpleServer = new SimpleServer.Builder(HttpUtils.getDeviceIP(this), PORT)
                .setStaticDirectory(cacheDirectory.getAbsolutePath())
                .setVideoDirectory(new String[]{
                        storageDirectory.getAbsolutePath(),
                        sdcardDirectory.getAbsolutePath(),
                })
                .build();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        startForeground(FOREGROUND_ID, buildForegroundNotification());
        return super.onStartCommand(intent, flags, startId);
    }

    public class ServerBinder extends Binder {
        public ServerService getService() {
            return ServerService.this;
        }
    }
}
