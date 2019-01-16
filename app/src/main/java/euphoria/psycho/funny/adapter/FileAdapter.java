package euphoria.psycho.funny.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.funny.R;
import euphoria.psycho.funny.cache.ImageLoader;
import euphoria.psycho.funny.model.FileItem;
import euphoria.psycho.funny.util.SelectableAdapter;

public class FileAdapter extends SelectableAdapter<FileAdapter.ViewHolder> implements LifecycleObserver {

    private static final int COLOR_HIGHLIGHT = 0x33EF7306;
    private static final String STATE_LAST_SCROLL_INDEX = "last_scroll_index";
    private final ArrayMap<FileItem.FileType, Drawable> mCache = new ArrayMap<>();
    private final Callback mCallback;
    private final Context mContext;
    private final ImageLoader mImageLoader;
    private List<FileItem> mFileItems;
    private RecyclerView mRecyclerView;

    public FileAdapter(Context context,
                       Callback callback,
                       Lifecycle lifecycle) {
        // setHasStableIds(true);

        // java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid item position 0(offset:-1
        mContext = context;
        Resources resources = context.getResources();
        mImageLoader = new ImageLoader();
        mCache.put(FileItem.FileType.DIRECTORY, resources.getDrawable(R.drawable.ic_folder_yellow));
        mCache.put(FileItem.FileType.TEXT, resources.getDrawable(R.drawable.ic_file_txt));
        mCache.put(FileItem.FileType.AUDIO, resources.getDrawable(R.drawable.ic_file_audio));

        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        mFileItems = new ArrayList<>();
        mCallback = callback;
    }

    public void onSaveInstanceState(Bundle outState) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        outState.putInt(STATE_LAST_SCROLL_INDEX, layoutManager.findFirstVisibleItemPosition());
    }

    public void setFileItems(List<FileItem> fileItems) {
        if (fileItems == null) return;
        mFileItems = fileItems;
        notifyDataSetChanged();
    }

    public void updateFileItems(List<FileItem> fileItems) {
        if (fileItems == null) return;
        if (mFileItems.size() == 0) {
            mFileItems.addAll(fileItems);
            notifyDataSetChanged();
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return mFileItems.get(oldItemPosition).equals(fileItems.get(newItemPosition));
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mFileItems.get(oldItemPosition).getPath().equals(fileItems.get(newItemPosition).getPath());
                }

                @Override
                public int getNewListSize() {
                    return fileItems.size();
                }

                @Override
                public int getOldListSize() {
                    return mFileItems.size();
                }
            });
            mFileItems = fileItems;
            result.dispatchUpdatesTo(this);
        }

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
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem fileItem = mFileItems.get(position);
        if (isSelected(position)) {
            holder.container.setBackgroundColor(COLOR_HIGHLIGHT);
        } else {
            holder.container.setBackgroundColor(0x00000000);
        }
        if (mCallback != null) {
            holder.more.setOnClickListener(v -> mCallback.onMenuClicked(v, fileItem));
        }
        holder.title.setText(fileItem.getName());
        holder.description.setText(fileItem.getDescription());
        Drawable drawable = mCache.get(fileItem.getType());
        if (drawable == null) {
            holder.musicCover.setImageDrawable(null);
            mImageLoader.load(fileItem, holder.musicCover);
        } else {
            holder.musicCover.setImageDrawable(drawable);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    public interface Callback {
        void onItemCheckedChanged(FileItem fileItem, boolean selected);

        void onItemClicked(int position, FileItem fileItem);

        boolean onItemLongClicked(int position, FileItem fileItem);

        void onMenuClicked(View view, FileItem item);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnLongClickListener, View.OnClickListener {

        View container;
        TextView description;
        ImageView more;
        ImageView musicCover;
        TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
            container = itemView.findViewById(R.id.container);
            musicCover = itemView.findViewById(R.id.music_cover);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            more = itemView.findViewById(R.id.more);
        }

        @Override
        public void onClick(View v) {
            if (mCallback != null) {
                int position = getAdapterPosition();
                FileItem item = mFileItems.get(position);
                mCallback.onItemClicked(position, item);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mCallback != null) {
                int position = getAdapterPosition();
                FileItem item = mFileItems.get(position);
                mCallback.onItemLongClicked(position, item);
                return true;
            }
            return false;
        }
    }
}
