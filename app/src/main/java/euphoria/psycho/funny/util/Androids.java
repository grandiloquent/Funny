package euphoria.psycho.funny.util;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Androids {

    public static String getExternalStoragePath(String fileName) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + fileName;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        NotificationManager notificationManager = AndroidServices.instance().provideNotificationManager();
        if (notificationManager == null) {
            return;
        }
        notificationManager.createNotificationChannel(channel);
    }

    public static CharSequence getClipboardText() {
        ClipboardManager manager = AndroidServices.instance().provideClipboardManager();
        ClipData clipData = manager.getPrimaryClip();
        if (clipData == null) return null;
        if (clipData.getItemCount() > 0) {
            return clipData.getItemAt(0).getText();
        }
        return null;
    }

    public static RecyclerView setupRecyclerView(AppCompatActivity activity, int resId) {
        RecyclerView recyclerView = activity.findViewById(resId);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.getItemAnimator().setChangeDuration(0);
        return recyclerView;
    }

    public static Toolbar setupToolbar(AppCompatActivity activity, int resId) {
        Toolbar toolbar = activity.findViewById(resId);
        if (toolbar == null) return null;
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> activity.onBackPressed());
        toolbar.setContentInsetStartWithNavigation(0);
        return toolbar;
    }

    public static String toString(CharSequence charSequence) {
        if (charSequence == null) return null;
        return charSequence.toString();
    }
}
