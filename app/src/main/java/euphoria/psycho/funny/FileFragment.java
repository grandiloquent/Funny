package euphoria.psycho.funny;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

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
public class FileFragment extends ObservableFragment implements FileAdapter.Callback {
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

    private void actionConvertToUtf8(FileItem item) {

    }

    private void actionCopyName(FileItem item) {

    }

    private void actionDelete(FileItem item) {

    }

    private void actionDescending() {


        sortByDirection(false);
    }

    private void actionProperty(FileItem item) {

    }

    private void actionRename(FileItem item) {

    }

    private void actionSdcard() {
        mDirectory = new File(Environment.getExternalStorageDirectory(), "Videos");
        refreshRecyclerView();
    }

    private void actionSearchSubtitle(FileItem item) {

        String movieName = FileUtils.getFileNameWithoutExtension(item.getName());
        ThreadUtils.postOnBackgroundThread(() -> {
            try {
                String json = fetchSubtitleJson(movieName);
                if (json.length() == 0) throw new IllegalStateException("JSON is empty.");
                String link = parseSubtitleJson(json);
                ThreadUtils.postOnMainThread(() -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(browserIntent);
                });
            } catch (Exception e) {
                ThreadUtils.postOnMainThread(() -> {
                    Simple.toast(this, R.string.message_subtitle_not_found, true);
                });
            }
        });
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

    private void actionStorage() {
        mDirectory = new File(FileUtils.getRemovableStoragePath(), "Videos");
        refreshRecyclerView();

    }

    @WorkerThread
    private String fetchSubtitleJson(String movieName) throws IOException {

        String query = URLEncoder.encode(movieName, "UTF-8");
        URL url = new URL("https://rest.opensubtitles.org/search/query-" + query);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "TemporaryUserAgent");
        int status = connection.getResponseCode();
        if (status != 200) throw new IllegalStateException("请求字幕JSON失败。Status Code = " + status);
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf8"));
        StringBuilder sb = new StringBuilder();
        String line = "";

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
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
                fileItem.setDescription(FileUtils.formatSize(fileItem.getSize()));
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

    private String parseSubtitleJson(String json) throws JSONException {

        JSONArray jsonArray = new JSONArray(json);
        if (jsonArray.length() > 0) {
            return jsonArray.getJSONObject(0).getString("SubtitlesLink");
        }
        return null;
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

        String dir = mPreferences.getString(KEY_DIRECTORY, FileUtils.getExternalStorageDirectoryPath());

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
        Context context = getContext();
        if (context == null) {
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
        mLayoutManager = new LinearLayoutManager(context);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        if (savedInstanceState != null) {
            int scrollPosition = savedInstanceState.getInt(STATE_SCROLL_POSITION);
            mLayoutManager.scrollToPosition(scrollPosition);
        }
        // if dont set Layout Manager the notifydatachanged will not fire
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mFileAdapter = new FileAdapter(context, this, getLifecycle());
        mRecyclerView.setAdapter(mFileAdapter);
        ThreadUtils.postOnBackgroundThread(this::refreshRecyclerView);
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mSortAscending) {
            menu.findItem(R.id.action_ascending).setChecked(true);
        } else {
            menu.findItem(R.id.action_descending).setChecked(false);
        }
        super.onPrepareOptionsMenu(menu);

    }

    @Override
    public void onItemCheckedChanged(FileItem fileItem, boolean selected) {

    }

    @Override
    public void onItemClicked(int position, FileItem fileItem) {

    }

    @Override
    public boolean onItemLongClicked(int position, FileItem fileItem) {
        return false;
    }

    @Override
    public void onMenuClicked(View view, final FileItem item) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.inflate(R.menu.context_file);
        FileItem.FileType fileType = item.getType();
        switch (fileType) {
            case TEXT:
                popupMenu.getMenu().findItem(R.id.action_convert_to_utf8).setVisible(true);
                break;
            case VIDEO:
                popupMenu.getMenu().findItem(R.id.action_search_subtitle).setVisible(true);
                break;
        }
        popupMenu.setOnMenuItemClickListener(menuItem -> {


            switch (menuItem.getItemId()) {
                case R.id.action_rename: {
                    actionRename(item);
                    return true;
                }

                case R.id.action_delete: {
                    actionDelete(item);
                    return true;
                }

                case R.id.action_copy_name: {
                    actionCopyName(item);
                    return true;
                }

                case R.id.action_search_subtitle: {
                    actionSearchSubtitle(item);
                    return true;
                }

                case R.id.action_property: {
                    actionProperty(item);
                    return true;
                }

                case R.id.action_convert_to_utf8: {
                    actionConvertToUtf8(item);
                    return true;
                }

            }
            return false;


        });
        popupMenu.show();
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
                item.setChecked(true);
                actionAscending();
                return true;
            }

            case R.id.action_descending: {
                item.setChecked(true);
                actionDescending();
                return true;
            }
            case R.id.action_storage: {
                actionStorage();
                return true;
            }
            case R.id.action_sdcard: {
                actionSdcard();
                return true;
            }
        }
        return false;


    }
}
