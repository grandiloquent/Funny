package euphoria.psycho.funny;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import euphoria.psycho.funny.service.MusicService;
import euphoria.psycho.funny.util.AndroidServices;
import euphoria.psycho.funny.util.debug.Log;

public class NotificationHelper {
    private static final int MUSIC_SERVICE = 1 << 1;
    private static final String CHANNEL_ID = "default";
    private static final String CHANNEL_NAME = "Musice Service";
    private static final String ACTION_AUDIO_PLAY = "euphoria.psycho.fun.ACTION_AUDIO_PLAY";
    private final MusicService mService;
    private final NotificationManager mNotificationManager;
    private RemoteViews mRemoteView;
    private Notification mNotification;

    public NotificationHelper(MusicService service) {
        mNotificationManager = AndroidServices.instance().provideNotificationManager();
        mService = service;
    }

    public void buildNotification(String trackName, String artistName, Bitmap album, boolean isPlaying) {
        Log.e("MusicService","[buildNotification] ---> ");
        mRemoteView = new RemoteViews(mService.getPackageName(), R.layout.notification);
        initializeCollapsedLayout(trackName, artistName, album);
        PendingIntent intent = PendingIntent.getActivity(mService, 0, new Intent(ACTION_AUDIO_PLAY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(channel);
        }
        mNotification = new NotificationCompat.Builder(mService, CHANNEL_ID)
                .setSmallIcon(R.drawable.stat_notify_music)
                //.setContentIntent(intent)
                .setContent(mRemoteView)
                .build();
        initializePlaybackActions(isPlaying);
        mService.startForeground(MUSIC_SERVICE, mNotification);
    }

    private void initializeCollapsedLayout(String trackName, String artistName, Bitmap album) {
        mRemoteView.setTextViewText(R.id.title, trackName);
        mRemoteView.setTextViewText(R.id.description, artistName);
        mRemoteView.setImageViewBitmap(R.id.imageView, album);
    }

    private void initializePlaybackActions(boolean isPlaying) {
        mRemoteView.setOnClickPendingIntent(R.id.previous, retrievePlaybackActions(3));
        mRemoteView.setOnClickPendingIntent(R.id.play, retrievePlaybackActions(1));
        mRemoteView.setOnClickPendingIntent(R.id.next, retrievePlaybackActions(2));
        mRemoteView.setImageViewResource(R.id.play, isPlaying ? R.drawable.btn_playback_pause : R.drawable.btn_playback_play);
    }

    public void killNotification() {
        mService.stopForeground(true);
        mNotification = null;
    }

    private PendingIntent retrievePlaybackActions(int which) {
        Intent action;
        PendingIntent pendingIntent;
        ComponentName serviceName = new ComponentName(mService, MusicService.class);
        switch (which) {
            case 1:
                action = new Intent(MusicService.ACTION_TOGGLE_PAUSE);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 1, action, 0);
                return pendingIntent;
            case 2:
                action = new Intent(MusicService.ACTION_NEXT);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 2, action, 0);
                return pendingIntent;
            case 3:
                action = new Intent(MusicService.ACTION_PREVIOUS);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 3, action, 0);
                return pendingIntent;
            case 4:
                action = new Intent(MusicService.ACTION_STOP);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(mService, 4, action, 0);
                return pendingIntent;
        }
        return null;
    }

    public void updatePlayState(boolean isPlaying) {
        if (mNotification == null || mNotificationManager == null) return;
        if (mRemoteView != null) {
            mRemoteView.setImageViewResource(R.id.play, isPlaying ? R.drawable.btn_playback_pause : R.drawable.btn_playback_play);
        }
        mNotificationManager.notify(MUSIC_SERVICE, mNotification);
    }
}
