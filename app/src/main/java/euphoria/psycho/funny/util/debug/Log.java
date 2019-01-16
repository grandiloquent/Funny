package euphoria.psycho.funny.util.debug;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import euphoria.psycho.funny.util.FileUtils;

public class Log {
    private static final DateFormat DATE_FORMAT =
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private static final boolean DEBUG = true;
    private static final boolean ENABLED = true;
    private static final String FILE_NAME_PREFIX = "log-";
    private static final long MAX_LOG_FILE_SIZE = 4 << 20;  // 4 mb
    private static Handler sHandler = null;
    private static File sLogsDirectory = null;

    public static int d(String tag, String msg) {
        if (DEBUG)
            return android.util.Log.d(tag, msg);
        else return -1;
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (DEBUG)
            return android.util.Log.d(tag, msg, tr);
        else return -1;
    }

    private static void dumpFile(PrintWriter out, String fileName) {
        File logFile = new File(sLogsDirectory, fileName);
        if (logFile.exists()) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(logFile));
                out.println();
                out.println("--- logfile: " + fileName + " ---");
                String line;
                while ((line = in.readLine()) != null) {
                    out.println(line);
                }
            } catch (Exception e) {

            } finally {
                FileUtils.closeSilently(in);
            }
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        android.util.Log.e(tag, msg, tr);
        print(tag, msg, tr);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(tag, msg);
        print(tag, msg);
    }

    public static void flushAll(PrintWriter out) throws InterruptedException {
        if (!ENABLED) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        Message.obtain(getHandler(), LogWriterCallback.MSG_FLUSH,
                Pair.create(out, latch)).sendToTarget();
        latch.await(2, TimeUnit.SECONDS);
    }

    private static Handler getHandler() {
        synchronized (DATE_FORMAT) {
            if (sHandler == null) {
                HandlerThread thread = new HandlerThread("file-logger");
                thread.start();
                sHandler = new Handler(thread.getLooper(), new LogWriterCallback());
            }
        }
        return sHandler;
    }

    public static void print(String tag, String msg) {
        print(tag, msg, null);
    }

    public static void print(String tag, String msg, Throwable e) {
        if (!ENABLED) {
            return;
        }
        String out = String.format("%s %s %s", DATE_FORMAT.format(new Date()), tag, msg);
        if (e != null) {
            out += "\n" + android.util.Log.getStackTraceString(e);
        }
        Message.obtain(getHandler(), LogWriterCallback.MSG_WRITE, out).sendToTarget();
    }

    public static void setDir(File logsDir) {
        if (ENABLED) {
            synchronized (DATE_FORMAT) {
                if (sHandler != null && !logsDir.equals(sLogsDirectory)) {
                    ((HandlerThread) sHandler.getLooper().getThread()).quit();
                    sHandler = null;
                }
            }
        }
        sLogsDirectory = logsDir;
    }

    public static int wtf(String tag, String msg) {
        return android.util.Log.wtf(tag, msg);
    }

    private static class LogWriterCallback implements Handler.Callback {
        private static final long CLOSE_DELAY = 5000;
        private static final int MSG_CLOSE = 2;
        private static final int MSG_FLUSH = 3;
        private static final int MSG_WRITE = 1;
        private String mCurrentFileName = null;
        private PrintWriter mCurrentWriter = null;

        private void closeWriter() {
            FileUtils.closeSilently(mCurrentWriter);
            mCurrentWriter = null;
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (sLogsDirectory == null || !ENABLED) {
                return true;
            }
            switch (msg.what) {
                case MSG_WRITE: {
                    Calendar cal = Calendar.getInstance();
                    String fileName = FILE_NAME_PREFIX + (cal.get(Calendar.DAY_OF_YEAR) & 1);
                    if (!fileName.equals(mCurrentFileName)) {
                        closeWriter();
                    }
                    try {
                        if (mCurrentWriter == null) {
                            mCurrentFileName = fileName;
                            boolean append = false;
                            File logFile = new File(sLogsDirectory, fileName);
                            if (logFile.exists()) {
                                Calendar modifiedTime = Calendar.getInstance();
                                modifiedTime.setTimeInMillis(logFile.lastModified());
                                modifiedTime.add(Calendar.HOUR, 36);
                                append = cal.before(modifiedTime)
                                        && logFile.length() < MAX_LOG_FILE_SIZE;
                            }
                            mCurrentWriter = new PrintWriter(new FileOutputStream(logFile, append));
                        }
                        mCurrentWriter.println((String) msg.obj);
                        mCurrentWriter.flush();
                        sHandler.removeMessages(MSG_CLOSE);
                        sHandler.sendEmptyMessageDelayed(MSG_CLOSE, CLOSE_DELAY);

                    } catch (Exception e) {
                        android.util.Log.e("FileLog", "Error writing logs to file", e);
                        closeWriter();
                    }
                    return true;
                }
                case MSG_CLOSE: {
                    closeWriter();
                    return true;
                }
                case MSG_FLUSH: {
                    closeWriter();
                    Pair<PrintWriter, CountDownLatch> p =
                            (Pair<PrintWriter, CountDownLatch>) msg.obj;
                    if (p.first != null) {
                        dumpFile(p.first, FILE_NAME_PREFIX + 0);
                        dumpFile(p.first, FILE_NAME_PREFIX + 1);
                    }
                    p.second.countDown();
                    return true;
                }
            }
            return true;
        }
    }
}
