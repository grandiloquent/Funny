package euphoria.psycho.funny.util;

import android.content.Context;

public class AndroidContext {
    private static AndroidContext sInstance;

    public static void initialize(Context context) {
        if (sInstance == null) {
            sInstance = new AndroidContext(context);
        }
    }

    public static AndroidContext instance() {
        if (sInstance == null) {
            throw new IllegalStateException("Android context was not initialized.");
        }
        return sInstance;
    }

    private final Context mContext;

    private AndroidContext(Context context) {
        mContext = context;
    }

    public Context get() {
        return mContext;
    }
}
