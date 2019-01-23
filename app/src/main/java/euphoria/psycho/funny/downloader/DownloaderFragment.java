package euphoria.psycho.funny.downloader;

import android.app.DownloadManager;
import android.os.Bundle;

import com.liulishuo.filedownloader.FileDownloadConnectListener;
import com.liulishuo.filedownloader.FileDownloader;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DownloaderFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileDownloader.getImpl().addServiceConnectListener(mConnectListener);
    }

    private FileDownloadConnectListener mConnectListener = new FileDownloadConnectListener() {
        @Override
        public void connected() {

        }

        @Override
        public void disconnected() {

        }
    };

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
