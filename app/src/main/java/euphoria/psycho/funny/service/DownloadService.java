package euphoria.psycho.funny.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.LongSparseArray;
import android.widget.Toast;


import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import euphoria.psycho.funny.activity.DownloadActivity;
import euphoria.psycho.funny.model.DownloadInfo;
import euphoria.psycho.funny.DownloadInfoDatabase;
import euphoria.psycho.funny.DownloadThread;
import euphoria.psycho.funny.util.BaseService;
import euphoria.psycho.funny.util.FileUtils;
import euphoria.psycho.funny.util.HttpUtils;
import euphoria.psycho.funny.util.Simple;
import euphoria.psycho.funny.util.task.PriorityThreadFactory;

public class DownloadService extends BaseService implements DownloadInfo.Listener {
    public static final int COMMAND_START = 0;
    public static final int COMMAND_START_ALL = 1;
    public static final int COMMAND_STOP = 2;
    public static final String EXTRA_ID = "id";
    private static final int CORE_POOL_SIZE = 4;
    private static final boolean DEBUG = true;
    private static final String DEFAULT_DIRECTORY_NAME = "Videos";
    private static final int KEEP_ALIVE_TIME = 10; // 10 seconds
    private static final int MAX_POOL_SIZE = 8;
    private static final String TAG = "_tag_";
    private final Handler mHandler = new Handler();
    private final StringBuilder mStringBuilder = new StringBuilder();
    private ClipboardManager mClipboardManager;
    private String mDirectory;
    ExecutorService mExecutorService;
    private Formatter mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
    private LongSparseArray<DownloadInfo> mMap = new LongSparseArray<>();
    private List<DownloadThread> mTasks = new ArrayList<>();
    private final ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = this::clipboardTask;

    private void clipboardTask() {
        Log.e(TAG, "===> [clipboardTask]");

        ClipData clipData = mClipboardManager.getPrimaryClip();

        if (clipData != null && clipData.getItemCount() > 0) {
            String s = Simple.getString(clipData.getItemAt(0).getText());
            if (!Simple.isNullOrWhiteSpace(s)) {
                if (HttpUtils.isValidUrl(s)) {
                    DownloadInfo downloadInfo = new DownloadInfo();
                    downloadInfo.fileName = new File(mDirectory, HttpUtils.getFileNameFromUrl(s)).getAbsolutePath();
                    downloadInfo.url = s;

                    long id = database().insertTask(downloadInfo);

                    submitTask(id, downloadInfo);


                }
            }
        }
    }

    private DownloadInfoDatabase database() {
        return DownloadInfoDatabase.getInstance(this);
    }

    private void executeDownload() {

        List<DownloadInfo> downloadInfos = database().getPendingTasks();
        if (downloadInfos.size() == 0) {
            Toast.makeText(this, "没有待下载的任务.", Toast.LENGTH_LONG).show();
            return;
        }
        for (DownloadInfo info : downloadInfos) {
            info.listener = this;
            mMap.put(info.id, info);
            DownloadThread thread = new DownloadThread(info, this);
            mTasks.add(thread);
            mExecutorService.submit(thread);
        }
    }

    private int indexOfTask(DownloadThread thread) {
        Log.e(TAG, "===> [indexOfTask]");

        if (mTasks.size() == 0) return -1;
        for (int i = 0; i < mTasks.size(); i++) {
            if (mTasks.get(i).getTaskId() == thread.getTaskId())
                return i;
        }
        return -1;
    }

    private void makeNotification(long id, long speed, String message) {
        Log.e(TAG, "===> [makeNotification]");

        DownloadInfo info = mMap.get(id);
        String tag = "tag" + Long.toString(id);
        if (info == null) {
            mNotificationManager.cancel(tag, 0);
            return;
        }
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, PRIMARY_CHANNEL);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
        if (message != null) {
            builder.setContentTitle(FileUtils.getFileName(info.fileName));
            builder.setContentText(message);
        } else {
            if (info.totalBytes > 0L) {
                builder.setProgress(100, (int) (info.currentBytes * 100 / info.totalBytes), false);
                long remaining = getRemainingMillis(info.totalBytes, info.currentBytes, speed);
                builder.setContentTitle(FileUtils.formatSize(speed));
                builder.setContentText(Util.getStringForTime(mStringBuilder, mFormatter, remaining) + " " + FileUtils.formatSize(info.currentBytes) + "/" + FileUtils.formatSize(info.totalBytes));
                mStringBuilder.setLength(0);

            } else {
                builder.setProgress(100, 0, true);
            }
        }


        mNotificationManager.notify(tag, 0, builder.build());


    }

    private synchronized void removeTask(long id) {
        Log.e(TAG, "===> [removeTask]");

        mMap.remove(id);

        for (int i = 0; i < mTasks.size(); i++) {
            if (mTasks.get(i).getTaskId() == id) {
                mTasks.remove(i);
                break;
            }

        }
        mNotificationManager.cancel("tag" + Long.toString(id), 0);
    }

    private void setUpDirectory() {
        Log.e(TAG, "===> [setUpDirectory]");


        File dir = new File(Environment.getExternalStorageDirectory(), DEFAULT_DIRECTORY_NAME);
        if (!dir.isDirectory()) {
            boolean r = dir.mkdir();
            if (!r) {
            }
        }
        mDirectory = dir.getAbsolutePath();
    }


    private void setupClipboard() {
        Log.e(TAG, "===> [setupClipboard]");

        mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        assert mClipboardManager != null;
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
    }


    private void setupExecutor() {
        Log.e(TAG, "===> [setupExecutor]");

        mExecutorService = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                new PriorityThreadFactory("thread-pool",
                        android.os.Process.THREAD_PRIORITY_BACKGROUND));
    }


    private void startTask(long id) {
        Log.e(TAG, "===> [startTask]");

        DownloadInfo downloadInfo = DownloadInfoDatabase.getInstance(this).getDownloadInfo(id);

        if (downloadInfo == null) {
            return;
        }

        if (mMap.get(downloadInfo.id) != null) {
            Toast.makeText(this, "任务 " + FileUtils.getFileName(downloadInfo.fileName) + " 已执行", Toast.LENGTH_SHORT).show();
            return;
        }
        downloadInfo.listener = this;
        mMap.put(downloadInfo.id, downloadInfo);
        DownloadThread thread = new DownloadThread(downloadInfo, this);
        if (indexOfTask(thread) == -1) {
            mTasks.add(thread);
            mExecutorService.submit(thread);
        } else {
            Toast.makeText(this, "任务 " + FileUtils.getFileName(downloadInfo.fileName) + " 已执行", Toast.LENGTH_SHORT).show();
        }

    }


    private synchronized void stopTask(long id) {
        DownloadInfo downloadInfo = DownloadInfoDatabase.getInstance(this).getDownloadInfo(id);

        if (downloadInfo == null) {

            return;
        }

        if (mMap.get(downloadInfo.id) == null) {
            Log.e(TAG, "[stopTask] ---> mMap 不包含待停止的任务信息");
        }
        mMap.remove(downloadInfo.id);
        for (int i = 0; i < mTasks.size(); i++) {
            if (mTasks.get(i).getTaskId() == downloadInfo.id) {
                mTasks.get(i).stopDownload();
                mTasks.remove(i);
                mNotificationManager.cancel("tag" + Long.toString(id), 0);
                break;
            }

        }


    }


    private void submitTask(long id, DownloadInfo downloadInfo) {
        Log.e(TAG, "===> [submitTask]");

        if (id < 0) return;
        downloadInfo.listener = this;
        downloadInfo.id = id;
        submitTask(downloadInfo);
        Intent intent = new Intent(DownloadActivity.ACTION_UPDATE);
        sendBroadcast(intent);
        Toast.makeText(this, " 从剪切板添加任务: " + id, Toast.LENGTH_LONG).show();
    }


    private void submitTask(DownloadInfo info) {
        Log.e(TAG, "===> [submitTask]");

        DownloadThread thread = new DownloadThread(info, this);
        if (indexOfTask(thread) != -1) {
            return;
        }
        mMap.put(info.id, info);
        mTasks.add(thread);
        mExecutorService.submit(thread);

    }


    private static long getRemainingMillis(long total, long current, long speed) {
        Log.e(TAG, "===> [getRemainingMillis]");

        return ((total - current) * 1000) / speed;
    }


    @Override
    public void notifySpeed(long id, long speed) {
        Log.e(TAG, "===> [notifySpeed]");

        mHandler.post(() -> {
            makeNotification(id, speed, null);
        });
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "===> [onBind]");

        return null;
    }


    @Override
    public void onCreate() {
        Log.e(TAG, "===> [onCreate]");

        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        setupExecutor();
        setUpDirectory();
        setupClipboard();
        clipboardTask();

    }


    @Override
    public void onDestroy() {
        Log.e(TAG, "===> [onDestroy]");


        mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        super.onDestroy();
    }


    @Override
    public void onError(long id, String message) {
        Log.e(TAG, "===> [onError]");

        mHandler.post(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            removeTask(id);
        });


    }


    @Override
    public void onFinished(long id) {
        Log.e(TAG, "===> [onFinished]");

        mHandler.post(() -> {
            removeTask(id);
        });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "===> [onStartCommand]");


        int command = intent.getIntExtra(EXTRA_COMMAND, -1);
        if (command == COMMAND_START || command == COMMAND_STOP) {
            long id = intent.getLongExtra(EXTRA_ID, -1L);
            if (id != -1L) {
                if (COMMAND_START == command) {
                    startTask(id);
                } else {
                    stopTask(id);
                }
            }
        } else if (command == COMMAND_START_ALL) {
            executeDownload();
        }
        //setupNotification();

        return START_NOT_STICKY;
    }


    @Override
    public void onStatusChanged(long id, String message) {
        Log.e(TAG, "===> [onStatusChanged]");

        mHandler.post(() -> {
            makeNotification(id, 0L, message);
        });
    }
}
