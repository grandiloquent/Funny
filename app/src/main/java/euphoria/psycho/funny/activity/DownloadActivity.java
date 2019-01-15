package euphoria.psycho.funny.activity;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.funny.adapter.DownloadAdapter;
import euphoria.psycho.funny.model.DownloadInfo;
import euphoria.psycho.funny.DownloadInfoDatabase;
import euphoria.psycho.funny.R;
import euphoria.psycho.funny.service.DownloadService;
import euphoria.psycho.funny.util.BaseAppCompatActivity;

public class DownloadActivity extends BaseAppCompatActivity implements DownloadAdapter.MenuListener {
    public static final String ACTION_UPDATE = "euphoria.psycho.download.ACTION_UPDATE";

    static final boolean DEBUG = true;
    static final String TAG = "DownloadActivity";
    final UpdateReceiver mReceiver = new UpdateReceiver();
    DownloadAdapter mAdapter;
    RecyclerView mRecyclerView;

    void actionDelete(DownloadInfo info) {
        DownloadInfoDatabase.getInstance(this).deleteDownloadInfo(info.id);
        refreshListView();
    }

    void actionMarkFinished(DownloadInfo downloadInfo) {
        downloadInfo.finished = true;
        DownloadInfoDatabase.getInstance(this).updateTask(downloadInfo);
        refreshListView();
    }

    void actionShowAll() {

        refreshList(DownloadInfoDatabase.getInstance(this).getAllTasks());
    }

    void actionStart(DownloadInfo info) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_COMMAND, DownloadService.COMMAND_START);
        intent.putExtra(DownloadService.EXTRA_ID, info.id);
        startService(intent);

    }

    void actionStartAll() {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_COMMAND, DownloadService.COMMAND_START_ALL);
        startService(intent);
    }

    void actionStop(DownloadInfo info) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_COMMAND, DownloadService.COMMAND_STOP);
        intent.putExtra(DownloadService.EXTRA_ID, info.id);
        startService(intent);
    }

    void actionStopAll() {
        Intent intent = new Intent(this, DownloadService.class);
        stopService(intent);
    }


    void refreshList(List<DownloadInfo> downloadInfos) {
        mAdapter.addAll(downloadInfos);
    }

    private void refreshListView() {
        refreshList(DownloadInfoDatabase.getInstance(this).getPendingTasks());

    }

    void startService() {
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
    }


    @Override
    public void bindViews() {
        mRecyclerView = findViewById(R.id.recyclerView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_download;
    }

    @Override
    protected String[] getNeedPermissions() {
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        };
    }

    @Override
    protected int getOptionsMenu() {
        return R.menu.download;
    }

    @Override
    public void initView() {
        super.initView();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        if (mAdapter == null) {
            mAdapter = new DownloadAdapter(this);
        }
        mRecyclerView.setAdapter(mAdapter);
    }

    //  Entry Point
    @Override
    public void initialize() {
        super.initialize();

        refreshListView();
        startService();


    }

    @Override
    public void onClicked(View view, DownloadInfo info) {

        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.context_download);
        popupMenu.setOnMenuItemClickListener(item ->
        {
            switch (item.getItemId()) {
                case R.id.action_mark_finished:
                    actionMarkFinished(info);
                    break;
                case R.id.action_start:
                    actionStart(info);
                    break;
                case R.id.action_stop:
                    actionStop(info);
                    break;
                case R.id.action_delete:
                    actionDelete(info);
                    break;
            }
            return true;
        });
        popupMenu.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_start_all:
                actionStartAll();
                return true;

            case R.id.action_stop_all:
                actionStopAll();
                return true;
            case R.id.action_show_all:
                actionShowAll();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);

        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();

        registerReceiver(mReceiver, new IntentFilter(ACTION_UPDATE));

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    public class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshListView();

        }
    }
}
