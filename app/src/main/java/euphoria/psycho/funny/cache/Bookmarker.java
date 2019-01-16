package euphoria.psycho.funny.cache;

import android.content.Context;

import com.google.android.exoplayer2.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import euphoria.psycho.funny.util.BlobCache;

public class Bookmarker {
    private static final String BOOKMARK_CACHE_FILE = "bookmark";
    private static int BOOKMARK_CACHE_MAX_BYTES = 10 * 1024;
    private static int BOOKMARK_CACHE_MAX_ENTRIES = 100;
    private static int BOOKMARK_CACHE_VERSION = 1;
    private final Context mContext;
    private BlobCache mBlobCache;
    public Bookmarker(Context context) {
        mContext = context;
    }
    public long getBookmark(String path) {
        if (mBlobCache == null) {
            mBlobCache = CacheManager.getCache(mContext,
                    BOOKMARK_CACHE_FILE,
                    BOOKMARK_CACHE_MAX_ENTRIES,
                    BOOKMARK_CACHE_MAX_BYTES,
                    BOOKMARK_CACHE_VERSION);
        }
        try {
            byte[] buffer = mBlobCache.lookup(path.hashCode());
            if (buffer == null) return 0;
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buffer));
            String uri = DataInputStream.readUTF(dis);
            long bookmark = dis.readLong();
            if (!uri.equals(path)) return 0;
            Util.closeQuietly(dis);
            return bookmark;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public void setBookmark(String path, long bookmark) {
        if (mBlobCache == null) {
            mBlobCache = CacheManager.getCache(mContext,
                    BOOKMARK_CACHE_FILE,
                    BOOKMARK_CACHE_MAX_ENTRIES,
                    BOOKMARK_CACHE_MAX_BYTES,
                    BOOKMARK_CACHE_VERSION);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(bos);
        try {
            dataOutputStream.writeUTF(path);
            dataOutputStream.writeLong(bookmark);
            dataOutputStream.flush();
            mBlobCache.insert(path.hashCode(), bos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Util.closeQuietly(dataOutputStream);
        }
    }
}
