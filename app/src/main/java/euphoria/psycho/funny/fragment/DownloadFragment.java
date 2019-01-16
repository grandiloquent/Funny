package euphoria.psycho.funny.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.funny.DownloadInfoDatabase;
import euphoria.psycho.funny.R;
import euphoria.psycho.funny.adapter.DownloadAdapter;
import euphoria.psycho.funny.model.DownloadInfo;
import euphoria.psycho.funny.service.DownloadService;

public class DownloadFragment extends Fragment implements DownloadAdapter.MenuListener {
    private static final String TAG = "Funny/DownloadFragment";
    public static final String ACTION_UPDATE = "euphoria.psycho.download.ACTION_UPDATE";
    private final UpdateReceiver mReceiver = new UpdateReceiver();
    private DownloadAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private void actionDelete(DownloadInfo info) {
        DownloadInfoDatabase.getInstance(getContext()).deleteDownloadInfo(info.id);
        refreshListView();
    }

    private void actionMarkFinished(DownloadInfo downloadInfo) {
        downloadInfo.finished = true;
        DownloadInfoDatabase.getInstance(getContext()).updateTask(downloadInfo);
        refreshListView();
    }

    @Override
    public void onClicked(View view, DownloadInfo info) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mRecyclerView = view.findViewById(R.id.recyclerView);

        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        if (mAdapter == null) {
            mAdapter = new DownloadAdapter(this);
        }
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    private void actionShowAll() {

        refreshList(DownloadInfoDatabase.getInstance(getContext()).getAllTasks());
    }

    private void actionStart(DownloadInfo info) {
        Intent intent = new Intent(getContext(), DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_COMMAND, DownloadService.COMMAND_START);
        intent.putExtra(DownloadService.EXTRA_ID, info.id);
        getActivity().startService(intent);

    }

    private void actionStartAll() {
        Intent intent = new Intent(getContext(), DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_COMMAND, DownloadService.COMMAND_START_ALL);
        getActivity().startService(intent);
    }

    private void actionStop(DownloadInfo info) {
        Intent intent = new Intent(getContext(), DownloadService.class);
        intent.putExtra(DownloadService.EXTRA_COMMAND, DownloadService.COMMAND_STOP);
        intent.putExtra(DownloadService.EXTRA_ID, info.id);
        getActivity().startService(intent);
    }

    private void actionStopAll() {
        Intent intent = new Intent(getContext(), DownloadService.class);
        getActivity().stopService(intent);
    }


    private void refreshList(List<DownloadInfo> downloadInfos) {
        mAdapter.addAll(downloadInfos);
    }

    private void refreshListView() {
        List<DownloadInfo> tasks = DownloadInfoDatabase.getInstance(getContext()).getPendingTasks();
        // Log.d(TAG, "[refreshListView] ---> pending tasks size = " + tasks.size());
        refreshList(tasks);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startService();
    }

    void startService() {
        Intent intent = new Intent(getContext(), DownloadService.class);
        getActivity().startService(intent);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.download, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

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
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, new IntentFilter(ACTION_UPDATE));
        refreshListView();

    }

    @Override
    public void onPause() {

        getActivity().unregisterReceiver(mReceiver);
        super.onPause();
    }

    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshListView();

        }
    }
}
