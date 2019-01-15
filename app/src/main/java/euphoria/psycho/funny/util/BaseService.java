package euphoria.psycho.funny.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.graphics.Color;

public abstract class BaseService extends Service {
    protected static final String PRIMARY_CHANNEL = "default";
    private static final String PRIMARY_CHANNEL_STRING = "Primary Channel";
    protected NotificationManager mNotificationManager;
    public static final String EXTRA_COMMAND = "command";

    protected void createChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(PRIMARY_CHANNEL,
                            PRIMARY_CHANNEL_STRING,
                            NotificationManager.IMPORTANCE_LOW);

            // public int getLightColor ()
            // Returns the notification light color for notifications
            // posted to this channel. Irrelevant unless shouldShowLights().
            channel.setLightColor(Color.GREEN);

            // public void setLockscreenVisibility (int lockscreenVisibility)
            // Sets whether notifications posted to this channel appear
            // on the lockscreen or not, and if so, whether they appear
            // in a redacted form. See e.g. Notification.VISIBILITY_SECRET.
            // Only modifiable by the system and notification ranker.

            // public static final int VISIBILITY_PRIVATE
            // Notification visibility: Show this notification on
            // all lockscreens, but conceal sensitive or private information
            // on secure lockscreens.

            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mNotificationManager.createNotificationChannel(channel);
        }

    }
}
