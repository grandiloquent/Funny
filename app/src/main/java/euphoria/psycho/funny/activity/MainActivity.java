package euphoria.psycho.funny.activity;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import euphoria.psycho.funny.R;
import euphoria.psycho.funny.fragment.DownloadFragment;
import euphoria.psycho.funny.fragment.FileFragment;
import euphoria.psycho.funny.fragment.TranslateFragment;
import euphoria.psycho.funny.util.AndroidServices;
import euphoria.psycho.funny.util.BaseAppCompatActivity;
import euphoria.psycho.funny.util.debug.Log;

public class MainActivity extends BaseAppCompatActivity {
    private static final String TAG = "Funny/MainActivity";
    AppBarLayout mAppbar;
    DrawerLayout mDrawer;
    ActionBarDrawerToggle mDrawerToggle;
    NavigationView mNavigation;
    private OnBackPressed mOnBackPressed;
    private SearchView mSearchView;

    private void actionOpenDrawer() {
        mDrawer.openDrawer(GravityCompat.START);
    }

    private boolean isFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FileFragment.class.getCanonicalName());
        return fragment != null && fragment.isVisible();
    }

    private boolean selectDrawerItem(MenuItem menuItem) {
        Menu menu = mNavigation.getMenu();


        Class fragmentClass = null;

        int id = menuItem.getItemId();

        switch (id) {
            case R.id.action_fragment_file: {
                fragmentClass = FileFragment.class;
                menuItem.setChecked(true);
                menu.findItem(R.id.action_download).setChecked(false);
                menu.findItem(R.id.action_translate).setChecked(false);
                break;
            }
            case R.id.action_download: {
                fragmentClass = DownloadFragment.class;

                menuItem.setChecked(true);
                menu.findItem(R.id.action_translate).setChecked(false);
                menu.findItem(R.id.action_fragment_file).setChecked(false);
                break;
            }
            case R.id.action_translate: {
                fragmentClass = TranslateFragment.class;
                menuItem.setChecked(true);
                menu.findItem(R.id.action_download).setChecked(false);
                menu.findItem(R.id.action_fragment_file).setChecked(false);
                break;
            }
            case R.id.action_hidden_camera:
                AndroidServices.instance().requestOverlayPermission();
                break;
            case R.id.action_server:
                Intent intent = new Intent(this, ServerActivity.class);
                startActivity(intent);
                mDrawer.closeDrawers();
                return true;

        }
        if (fragmentClass != null) {
            try {
                showFragment(fragmentClass);
            } catch (Exception e) {
                Log.e(TAG, "[selectDrawerItem] ---> ", e);
            }

            mDrawer.closeDrawers();
        }
        return false;

    }

    public void setOnBackPressed(OnBackPressed onBackPressed) {
        mOnBackPressed = onBackPressed;
    }

    private void showFragment(Class klass) throws InstantiationException, IllegalAccessException {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        String tag = klass.getCanonicalName();
        transaction.addToBackStack(tag);
        //transaction.setCustomAnimations(R.anim.slide_out_down, R.anim.slide_in_down);
        transaction.replace(R.id.content, (Fragment) klass.newInstance(), tag)
                .commit();
    }

    @Override
    public void bindViews() {
        super.bindViews();

        mDrawer = findViewById(R.id.drawer);
        mAppbar = findViewById(R.id.appbar);
        mNavigation = findViewById(R.id.navigation);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected String[] getNeedPermissions() {
        return new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        };
    }

    @Override
    protected int getOptionsMenu() {
        return R.menu.activity_options;
    }

    @Override
    public void initView() {
        super.initView();
        Toolbar toolbar = getToolbar();

        toolbar.setNavigationIcon(android.R.drawable.ic_notification_clear_all);
        toolbar.setNavigationOnClickListener(v -> {
            mDrawer.openDrawer(GravityCompat.START);
        });
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, getToolbar(), R.string.drawer_open, R.string.drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mNavigation.setNavigationItemSelectedListener(this::selectDrawerItem);

    }

    @Override
    public void initialize() {
        super.initialize();
        Log.d(TAG, "[initialize] ---> ");
        try {
            showFragment(FileFragment.class);
        } catch (Exception e) {
            Log.e(TAG, "[initialize] ---> ", e);
        }


    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "[onBackPressed] ---> ");
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawers();
        } else if (!isFragment() || (mOnBackPressed == null || !mOnBackPressed.onPressed())) {
            Log.d(TAG, "[onBackPressed] ---> " + getSupportFragmentManager().getBackStackEntryCount());
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                actionOpenDrawer();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    public interface OnBackPressed {
        boolean onPressed();

    }
}
