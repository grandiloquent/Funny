package euphoria.psycho.funny.downloader;

import android.app.DownloadManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liulishuo.filedownloader.FileDownloadConnectListener;
import com.liulishuo.filedownloader.FileDownloader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.funny.R;

public class DownloaderFragment extends Fragment {

    private FileDownloadConnectListener mConnectListener = new FileDownloadConnectListener() {
        @Override
        public void connected() {

        }

        @Override
        public void disconnected() {

        }
    };
    private RecyclerView mRecyclerView;

    private void setupRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.getItemAnimator().setChangeDuration(0);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileDownloader.getImpl().addServiceConnectListener(mConnectListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        setupRecyclerView(view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FileDownloader.getImpl().removeServiceConnectListener(mConnectListener);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}
