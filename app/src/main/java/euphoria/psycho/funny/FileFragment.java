package euphoria.psycho.funny;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import euphoria.psycho.funny.ui.SwipeLayout;
import euphoria.psycho.funny.util.AndroidServices;
import euphoria.psycho.funny.util.FileUtils;
import euphoria.psycho.funny.util.Simple;
import euphoria.psycho.funny.util.ThreadUtils;
import euphoria.psycho.funny.util.lifecycle.ObservableFragment;

// https://developer.android.com/guide/components/fragments
// https://developer.android.com/reference/android/app/Fragment
public class FileFragment extends ObservableFragment {
    private static final String KEY_DIRECTORY = "directory";
    private static final String KEY_SORT_BY = "sort_by";
    private static final String KEY_SORT_DIRECTION = "sort_direction";
    private static final String STATE_SCROLL_POSITION = "";
    private static final String TAG = "FileFragment";
    private File mDirectory;
    private FileAdapter mFileAdapter;
    private List<File> mFiles;
    private LinearLayoutManager mLayoutManager;
    private SharedPreferences mPreferences;
    private RecyclerView mRecyclerView;
    private FileItem.FileSort mSort;
    private boolean mSortAscending;
    private SwipeLayout mSwipeLayout;

    private void actionAscending() {

        sortByDirection(true);
    }

    private void actionDescending() {
        sortByDirection(false);
    }

    private void actionSortByLastModified() {
        mSort = FileItem.FileSort.LAST_MODIFED;
        refreshRecyclerView();
    }

    private void actionSortByName() {
        mSort = FileItem.FileSort.NAME;
        refreshRecyclerView();
    }

    private void actionSortBySize() {
        mSort = FileItem.FileSort.SIZE;
        refreshRecyclerView();
    }


    private List<FileItem> getFileItems(File directory) {
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) return null;

        List<FileItem> fileItems = new ArrayList<>();

        for (File f : files) {

            boolean skip = false;
            FileItem fileItem = new FileItem();


            if (f.isDirectory()) {
                fileItem.setType(FileItem.FileType.DIRECTORY);
                fileItem.setSize(f.listFiles().length);
                fileItem.setDescription(fileItem.getSize() + "  个");
            } else {
                if (FileUtils.isVideo(f)) {
                    fileItem.setType(FileItem.FileType.VIDEO);
                } else if (FileUtils.isAudio(f)) {
                    fileItem.setType(FileItem.FileType.AUDIO);
                } else if (FileUtils.isSubTitle(f)) {
                    fileItem.setType(FileItem.FileType.TEXT);
                } else {
                    skip = true;
                }
                fileItem.setSize(f.length());
                fileItem.setDescription(Simple.formatSize(fileItem.getSize()));
            }
            fileItem.setName(f.getName());
            fileItem.setPath(f.getAbsolutePath());
            fileItem.setLastModified(f.lastModified());
            if (!skip)
                fileItems.add(fileItem);
        }
        Collator collator = Collator.getInstance(Locale.CHINA);
        Comparator<FileItem> comparator = (o1, o2) -> {
            boolean b1 = o1.getType() == FileItem.FileType.DIRECTORY;
            boolean b2 = o2.getType() == FileItem.FileType.DIRECTORY;
            // if(b1==b2) cause:
            if ((b1 && b2) || (!b1 && !b2)) {
                switch (mSort) {
                    case NAME:
                        return mSortAscending ? collator.compare(o1.getName(), o2.getName()) : collator.compare(o1.getName(), o2.getName()) * -1;
                    case SIZE:
                        return o1.getSize() <= o2.getSize() ? (mSortAscending ? 1 : -1) : (mSortAscending ? -1 : 1);
                    case LAST_MODIFED:
                        return o1.getLastModified() <= o2.getLastModified() ? (mSortAscending ? 1 : -1) : (mSortAscending ? -1 : 1);


                }

                // return o1.getName().compareTo(o2.getName());
            } else if (b1) {
                return 1;
            } else {
                return -1;
            }
            return 0;
//            if (o1 == null || o2 == null) {
//                Log.e(TAG, "[getFileItems] ---> ");
//
//                return 0;
//            }
//            boolean b1 = o1.getType() == FileItem.FileType.DIRECTORY;
//            boolean b2 = o2.getType() == FileItem.FileType.DIRECTORY;
//
//            if (b1 == b2) {
//                switch (mSort) {
//                    case NAME: {
//                        if (mSortAscending) {
//
//                            return o1.getName().compareTo(o2.getName());
//                        } else {
//                            Log.e(TAG, "2 [getFileItems] ---> " + collator.compare(o1.getName(), o2.getName()) * -1);
//
//                            return o1.getName().compareTo(o2.getName()) * -1;
//                        }
//                    }
//                }
//            }
////            else if (b1) {
////                return mSortAscending ? 1 : -1;
////            }
//            return 0;
        };

        Collections.sort(fileItems, comparator);
        return fileItems;
    }

    @WorkerThread
    private void refreshRecyclerView() {
        List<FileItem> items = getFileItems(mDirectory);
        ThreadUtils.postOnMainThread(() -> {
            mFileAdapter.setFileItems(items);
            mSwipeLayout.setRefreshing(false);
        });
    }

    private void sortByDirection(boolean ascending) {
        mSortAscending = ascending;
        mPreferences.edit().putInt(KEY_SORT_DIRECTION, mSortAscending ? 1 : 0).apply();
        refreshRecyclerView();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = AndroidServices.instance().providePreferences();

        String dir = mPreferences.getString(KEY_DIRECTORY, Simple.getExternalStorageDirectoryPath());

        mDirectory = new File(dir);
        int sortBy = mPreferences.getInt(KEY_DIRECTORY, FileItem.FileSort.NAME.getValue());
        mSort = FileItem.FileSort.get(sortBy);
        int sortDirection = mPreferences.getInt(KEY_SORT_DIRECTION, 1);
        mSortAscending = sortDirection == 1;
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.options_file, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        View view = inflater.inflate(R.layout.fragment_file, container, false);
        mSwipeLayout = view.findViewById(R.id.swipe);
        mSwipeLayout.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ThreadUtils.postOnBackgroundThread(FileFragment.this::refreshRecyclerView);
            }
        });
        mRecyclerView = view.findViewById(R.id.recycler);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        if (savedInstanceState != null) {
            int scrollPosition = savedInstanceState.getInt(STATE_SCROLL_POSITION);
            mLayoutManager.scrollToPosition(scrollPosition);
        }
        // if dont set Layout Manager the notifydatachanged will not fire
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mFileAdapter = new FileAdapter(getContext(), getLifecycle());
        mRecyclerView.setAdapter(mFileAdapter);
        ThreadUtils.postOnBackgroundThread(this::refreshRecyclerView);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_sort_by_name: {
                actionSortByName();
                return true;
            }

            case R.id.action_sort_by_last_modified: {
                actionSortByLastModified();
                return true;
            }

            case R.id.action_sort_by_size: {
                actionSortBySize();
                return true;
            }

            case R.id.action_ascending: {
                actionAscending();
                return true;
            }

            case R.id.action_descending: {
                actionDescending();
                return true;
            }

        }
        return false;


    }
}
