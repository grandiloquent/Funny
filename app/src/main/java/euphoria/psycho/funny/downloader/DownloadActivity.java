package euphoria.psycho.funny.downloader;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import euphoria.psycho.funny.R;
import euphoria.psycho.funny.fragment.DownloadFragment;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends AppCompatActivity {
    private static final String TAG = "TAG/" + DownloadActivity.class.getCanonicalName();
    private ViewPager mViewPager;
    private TabLayout mTableLayout;
    private Toolbar mToolbar;
    private List<Fragment> mFragmentList = new ArrayList<>();
    private DownloaderFragment mDownloaderFragment;
    private DownloaderFragmentAdapter mAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        mToolbar = findViewById(R.id.toolbar);
        initToolBar(mToolbar);
        mDownloaderFragment = new DownloaderFragment();
        mFragmentList.add(mDownloaderFragment);
        mFragmentList.add(new DownloadFragment());
        setupTabLayout();
    }

    private void setupTabLayout() {
        mAdapter = new DownloaderFragmentAdapter(getSupportFragmentManager());
        mAdapter.setData(mFragmentList);
        mTableLayout = findViewById(R.id.download_tab);
        mViewPager = findViewById(R.id.download_viewpager);
        mViewPager.setAdapter(mAdapter);
        mTableLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.side_out_left);
    }

    private void startActivityWithAnimation(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.side_out_left);
    }

    private void initToolBar(Toolbar toolbar) {
        if (toolbar == null) {
            return;
        }
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setContentInsetStartWithNavigation(0);
    }

}
