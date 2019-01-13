package euphoria.psycho.funny;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;

import euphoria.psycho.funny.util.BitmapUtils;
import euphoria.psycho.funny.util.KeyUtils;
import euphoria.psycho.funny.util.LruCache;
import euphoria.psycho.funny.util.Simple;
import euphoria.psycho.funny.util.task.ThreadPool;

import static euphoria.psycho.funny.util.Simple.closeSilently;


public class ImageLoader {
    private final CacheService mCacheService;
    private final ThreadPool mThreadPool;

    public ImageLoader() {
        mThreadPool = new ThreadPool(2, 3);
        mCacheService = new CacheService();
    }

    public void load(FileItem fileItem, ImageView imageView) {
        long key = KeyUtils.crc64Long(fileItem.getPath());
        BitmapDrawable drawable = mCacheService.get(key);
        if (drawable != null) {
            imageView.setImageDrawable(drawable);
//            if (DEBUG) Log.d(TAG, "loading form cache");
            return;
        }
        imageView.setTag(key);
        mThreadPool.submit(new LoadJob(imageView, fileItem.getPath(), key), future -> {

            Item item = future.get();
            if (item == null) {
                return;
            }
            Object object = item.getImageView().getTag();
            BitmapDrawable bitmapDrawable = new BitmapDrawable(item.getBitmap());
            mCacheService.set(key, bitmapDrawable);
            if (object == null
                    || (long) object != item.key) {
            } else {
                ImageView imageView1 = item.getImageView();
                imageView1.post(() -> imageView1.setImageDrawable(bitmapDrawable));
            }
        });
    }

    private static class CacheService {
        private final LruCache<Long, BitmapDrawable> mCache = new LruCache<>(256);
        private final File mDirectory = new File(Environment.getExternalStorageDirectory(), ".cache");

        public CacheService() {
            if (!mDirectory.exists()) {
                mDirectory.mkdir();
            }
        }

        public BitmapDrawable get(long key) {
            BitmapDrawable drawable = mCache.get(key);
            if (drawable == null) {
                File file = new File(mDirectory, Long.toString(key));
                if (file.exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                    drawable = new BitmapDrawable(bitmap);
                    set(key, drawable);
                    return drawable;
                }
            }
            return mCache.get(key);
        }

        public void set(long key, BitmapDrawable bitmapDrawable) {
            if (bitmapDrawable.getBitmap() == null) {
                return;
            }
            File file = new File(mDirectory, Long.toString(key));
            if (!file.exists()) {
                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    bitmapDrawable.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    closeSilently(outputStream);
                } catch (Exception e) {

                }
            }
            mCache.put(key, bitmapDrawable);
        }
    }

    private static class LoadJob implements ThreadPool.Job<Item> {
        private final ImageView mImageView;
        private final long mKey;
        private final String mPath;
        private int mSize;

        private LoadJob(ImageView imageView, String path, long key) {
            mImageView = imageView;
            mPath = path;
            mKey = key;
            mSize = Simple.dpToPixel(50);

        }

        @Override
        public Item run(ThreadPool.JobContext jc) {
            try {
                if (jc.isCancelled()) return null;
                Bitmap bitmap;
                Bitmap sourceBitmap = BitmapUtils.createVideoThumbnail(mPath);
                bitmap = BitmapUtils.resizeAndCropCenter(sourceBitmap, mSize, true);
                return new Item().setBitmap(bitmap).setImageView(mImageView).setKey(mKey);
            } catch (Exception e) {

            }
            return null;
        }
    }

    private static class Item {
        private Bitmap bitmap;
        private ImageView imageView;
        private long key;

        public Bitmap getBitmap() {
            return bitmap;
        }

        public Item setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            return this;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public Item setImageView(ImageView imageView) {
            this.imageView = imageView;
            return this;
        }


        public Item setKey(long key) {
            this.key = key;
            return this;
        }
    }
}
