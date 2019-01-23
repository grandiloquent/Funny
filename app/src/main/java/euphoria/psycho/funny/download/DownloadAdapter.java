package euphoria.psycho.funny.download;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.funny.R;
import euphoria.psycho.funny.util.FileUtils;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {
    private List<DownloadInfo> mDownloadInfos;

    public void setDownloadInfos(List<DownloadInfo> downloadInfos) {
        mDownloadInfos = downloadInfos;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mDownloadInfos.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DownloadInfo downloadInfo = mDownloadInfos.get(position);
        holder.title.setText(downloadInfo.fileName);
        holder.speed.setText(FileUtils.formatSize(downloadInfo.speed));
        holder.progressBar.setProgress((int) (downloadInfo.currentBytes * 100 / downloadInfo.totalBytes));
        holder.size.setText(FileUtils.formatSize(downloadInfo.currentBytes) + "/" + FileUtils.formatSize(downloadInfo.totalBytes));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView percent;
        ProgressBar progressBar;
        TextView size;
        TextView speed;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
            speed = itemView.findViewById(R.id.speed);
            size = itemView.findViewById(R.id.size);
            percent = itemView.findViewById(R.id.percent);
            title = itemView.findViewById(R.id.title);
        }
    }
}
