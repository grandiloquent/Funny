package euphoria.psycho.funny;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.download.persistence.DownloadInfo;
import euphoria.psycho.player.R;
import euphoria.psycho.utils.FileUtils;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {
    private final List<DownloadInfo> mDownloadInfos = new ArrayList<>();
    private final MenuListener mMenuListener;

    public DownloadAdapter(MenuListener menuListener) {
        mMenuListener = menuListener;
    }

    public void addAll(List<DownloadInfo> downloadInfos) {
        mDownloadInfos.clear();
        mDownloadInfos.addAll(downloadInfos);
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return mDownloadInfos.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DownloadInfo info = mDownloadInfos.get(position);
        holder.fileName.setText(FileUtils.getFileName(info.fileName));

        holder.fileMenu.setOnClickListener(v -> {
            mMenuListener.onClicked(v, info);
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download, parent, false);

        return new ViewHolder(view);
    }

    interface MenuListener {
        void onClicked(View view, DownloadInfo info);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView fileMenu;
        TextView fileName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            fileMenu = itemView.findViewById(R.id.file_menu);
        }
    }
}
