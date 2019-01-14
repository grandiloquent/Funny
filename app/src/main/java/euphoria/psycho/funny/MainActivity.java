package euphoria.psycho.funny;

import android.Manifest;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import euphoria.psycho.funny.util.debug.Log;
import euphoria.psycho.funny.util.BaseAppCompatActivity;

public class MainActivity extends BaseAppCompatActivity {
    private static final String TAG = "MainActivity";
    AppBarLayout mAppbar;
    DrawerLayout mDrawer;
    ActionBarDrawerToggle mDrawerToggle;
    NavigationView mNavigation;

    private void actionOpenDrawer() {
        mDrawer.openDrawer(GravityCompat.START);
    }


    private boolean selectDrawerItem(MenuItem menuItem) {
        Class fragmentClass = null;

        int id = menuItem.getItemId();

        switch (id) {
            case R.id.action_fragment_file: {
                fragmentClass = FileFragment.class;
                break;
            }
            case R.id.action_hidden_camera:
                break;
        }
        if (fragmentClass != null) {
            try {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, (Fragment) fragmentClass.newInstance())
                        .commit();
            } catch (Exception e) {
                Log.e(TAG, "[selectDrawerItem] ---> ", e);
            }
            menuItem.setChecked(true);
            mDrawer.closeDrawers();
        }
        return false;

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
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    @Override
    protected int getOptionsMenu() {
        return R.menu.activity_options;
    }

    @Override
    public void initView() {
        super.initView();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, getToolbar(), R.string.drawer_open, R.string.drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        mNavigation.setNavigationItemSelectedListener(this::selectDrawerItem);
    }


    @Override
    public void initialize() {
        super.initialize();

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
}
