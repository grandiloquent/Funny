package euphoria.psycho.funny.util.debug;

public class Log {
    private static final boolean DEBUG = true;

    public static int d(String tag, String msg) {
        if (DEBUG)
            return android.util.Log.d(tag, msg);
        else return -1;
    }

    public static int wtf(String tag, String msg) {
        return android.util.Log.wtf(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (DEBUG)
            return android.util.Log.d(tag, msg, tr);
        else return -1;
    }

    public static int e(String tag, String msg, Throwable tr) {
        return android.util.Log.e(tag, msg, tr);
    }

    public static int e(String tag, String msg) {
        return android.util.Log.e(tag, msg);
    }
}
