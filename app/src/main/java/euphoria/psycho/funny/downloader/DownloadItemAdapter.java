package euphoria.psycho.funny.downloader;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.funny.R;

public class DownloadItemAdapter extends RecyclerView.Adapter<DownloadItemAdapter.ViewHolder> {
    private final Context mContext;
    private final List<DownloaderItem> mItems = new ArrayList<>();

    private View.OnClickListener mOnClickListener;

    public DownloadItemAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DownloaderItem downloaderItem = mItems.get(position);
        holder.tv91pornItemTitle.setText(downloaderItem.getTitle());
        holder.progressBarDownload.setProgress(downloaderItem.getProgress());
        holder.tvDownloadProgress.setText(String.valueOf(downloaderItem.getProgress()) + "%");
        holder.tvDownloadFilesize.setText(Formatter.formatFileSize(mContext, downloaderItem.getSoFarBytes()) + "/" + Formatter.formatFileSize(mContext, downloaderItem.getTotalBytes()));

        if (downloaderItem.getStatus() == FileDownloadStatus.completed) {
            holder.tvDownloadSpeed.setText("已完成");
            holder.ivDownloadControl.setVisibility(View.GONE);
        } else {
            holder.ivDownloadControl.setVisibility(View.VISIBLE);
            if (FileDownloader.getImpl().isServiceConnected()) {
                holder.ivDownloadControl.setImageResource(R.drawable.pause_download);
                switch (downloaderItem.getStatus()) {

                    case FileDownloadStatus.pending: {
                        holder.tvDownloadSpeed.setText("准备中");

                        break;
                    }
                    case FileDownloadStatus.started: {
                        holder.tvDownloadSpeed.setText("开始下载");

                        break;
                    }
                    case FileDownloadStatus.connected: {
                        holder.tvDownloadSpeed.setText("连接中");

                        break;
                    }
                    case FileDownloadStatus.progress: {
                        holder.tvDownloadSpeed.setText(downloaderItem.getSpeed() + " KB/S");
                        break;
                    }

                    case FileDownloadStatus.retry: {
                        holder.tvDownloadSpeed.setText("重试中");

                        break;
                    }
                    case FileDownloadStatus.error: {
                        holder.tvDownloadSpeed.setText("下载错误");

                        break;
                    }
                    case FileDownloadStatus.paused: {
                        holder.tvDownloadSpeed.setText("暂停");

                        break;
                    }
                    case FileDownloadStatus.warn: {
                        holder.tvDownloadSpeed.setText("警告");
                        holder.ivDownloadControl.setImageResource(R.drawable.start_download);
                        break;
                    }

                }


            } else {
                holder.tvDownloadSpeed.setText("暂停中");
                holder.ivDownloadControl.setImageResource(R.drawable.start_download);
            }
        }
        if (mOnClickListener != null) {
            holder.ivDownloadControl.setOnClickListener(mOnClickListener);
            holder.rightMenuDelete.setOnClickListener(mOnClickListener);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_downloader, parent, false);
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout flImg;
        ImageView iv91pornItemImg;
        ImageView ivDownloadControl;
        ProgressBar progressBarDownload;
        RelativeLayout progressBarLayout;
        AppCompatTextView tv91pornItemTitle;
        TextView tvDownloadFilesize;
        TextView tvDownloadProgress;
        TextView tvDownloadSpeed;
        TextView rightMenuDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            flImg = itemView.findViewById(R.id.fl_img);
            iv91pornItemImg = itemView.findViewById(R.id.iv_91porn_item_img);
            ivDownloadControl = itemView.findViewById(R.id.iv_download_control);
            tv91pornItemTitle = itemView.findViewById(R.id.tv_91porn_item_title);
            progressBarLayout = itemView.findViewById(R.id.progressBar_layout);
            tvDownloadFilesize = itemView.findViewById(R.id.tv_download_filesize);
            progressBarDownload = itemView.findViewById(R.id.progressBar_download);
            tvDownloadSpeed = itemView.findViewById(R.id.tv_download_speed);
            tvDownloadProgress = itemView.findViewById(R.id.tv_download_progress);
            rightMenuDelete = itemView.findViewById(R.id.right_menu_delete);

        }
    }
}
