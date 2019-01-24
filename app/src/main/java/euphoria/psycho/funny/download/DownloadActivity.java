package euphoria.psycho.funny.download;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.funny.Javas;
import euphoria.psycho.funny.R;
import euphoria.psycho.funny.util.AndroidContext;
import euphoria.psycho.funny.util.AndroidServices;
import euphoria.psycho.funny.util.Androids;

public class DownloadActivity extends AppCompatActivity implements DownloadManager.DownloadStatusUpdater, ClipboardManager.OnPrimaryClipChangedListener {
    private DownloadAdapter mAdapter;
    private ClipboardManager mClipboardManager;
    private Decoration mDecoration;
    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private static final String TAG = "TAG/" + DownloadActivity.class.getSimpleName();

    private void startActivityWithAnimation(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.side_out_left);
    }

    @Override
    public void complete(DownloadInfo task) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.side_out_left);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        mToolbar = Androids.setupToolbar(this, R.id.toolbar);
        mRecyclerView = Androids.setupRecyclerView(this, R.id.recyclerView);
        mDecoration = new Decoration(this);
        mRecyclerView.addItemDecoration(mDecoration);
        mAdapter = new DownloadAdapter();
        mAdapter.setDownloadInfos(DownloadDatabase.getInstance(this).getPendingTasks());
        mRecyclerView.setAdapter(mAdapter);

        mClipboardManager = AndroidServices.instance().provideClipboardManager();
        mClipboardManager.addPrimaryClipChangedListener(this);
        DownloadManager.instance().addUpdater(this);
        DownloadManager.instance().startDownload();
    }

    @Override
    protected void onDestroy() {
        DownloadManager.instance().removeUpdater(this);
        mClipboardManager.removePrimaryClipChangedListener(this);
        super.onDestroy();
    }

    private String getDefaultDirectory() {
        return Androids.getExternalStoragePath("Videos");
    }

    @Override
    public void onPrimaryClipChanged() {

        String url = Androids.toString(Androids.getClipboardText());
        if (url == null) return;

        URL u;
        try {
            u = new URL(url);
            u.openConnection();
        } catch (IOException e) {
            return;
        }
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.url = url;
        downloadInfo.fileName = getDefaultDirectory() + File.separatorChar + Javas.substringAfterLast(url, "/");
        DownloadDatabase.getInstance(this).insertTask(downloadInfo);
        refreshView();
    }

    private void refreshView() {
        mAdapter.setDownloadInfos(DownloadDatabase.getInstance(this).getPendingTasks());
    }

    public static final boolean DEBUG = true;

    @Override
    public void update(DownloadInfo task) {
        if (DEBUG) {
            Log.d(TAG, "update: ");
        }
        mAdapter.updateDownloadInfo(task);
    }

    public static class Decoration extends RecyclerView.ItemDecoration {
        protected final Context mContext;
        protected final int mDividerHeight;
        protected final Paint mPaint;

        public Decoration(Context context) {
            mContext = context;
            mPaint = new Paint();
            updateDividerColor();
            mDividerHeight = mContext.getResources()
                    .getDimensionPixelSize(R.dimen.divider_height);
        }

        /**
         * Updates the list divider color which may have changed due to a day night transition.
         */
        public void updateDividerColor() {
            mPaint.setColor(mContext.getResources().getColor(R.color.list_divider));
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            final int left = 0;//getLeft(parent.getChildAt(0));
            final int right = parent.getWidth() - parent.getPaddingRight();
            int top;
            int bottom;
            c.drawRect(left, 0, right, mDividerHeight, mPaint);
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                bottom = child.getBottom() - params.bottomMargin;
                top = bottom - mDividerHeight;
                if (top > 0) {
                    c.drawRect(left, top, right, bottom, mPaint);
                }
            }
        }


    }

}
