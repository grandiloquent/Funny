package euphoria.psycho.funny;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.funny.util.IconCache;
import euphoria.psycho.funny.util.lifecycle.Lifecycle;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> implements LifecycleObserver {

    private final Context mContext;
    private List<FileItem> mFileItems;
    private final ArrayMap<FileItem.FileType, Drawable> mCache = new ArrayMap<>();
    ;

    public FileAdapter(Context context, Lifecycle lifecycle) {
        mContext = context;
        Resources resources = context.getResources();

        mCache.put(FileItem.FileType.DIRECTORY, resources.getDrawable(R.drawable.ic_folder_yellow));
        mCache.put(FileItem.FileType.TEXT, resources.getDrawable(R.drawable.ic_file_txt));
        mCache.put(FileItem.FileType.AUDIO, resources.getDrawable(R.drawable.ic_file_audio));

        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        mFileItems = new ArrayList<>();
    }

    public void setFileItems(List<FileItem> fileItems) {
        mFileItems.clear();
        mFileItems.addAll(fileItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mFileItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem fileItem = mFileItems.get(position);
        holder.title.setText(fileItem.getName());
        holder.description.setText(fileItem.getDescription());
        holder.musicCover.setImageDrawable(mCache.get(fileItem.getType()));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView musicCover;
        TextView title;
        TextView description;
        ImageView more;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            musicCover = itemView.findViewById(R.id.music_cover);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            more = itemView.findViewById(R.id.more);
        }
    }
}
