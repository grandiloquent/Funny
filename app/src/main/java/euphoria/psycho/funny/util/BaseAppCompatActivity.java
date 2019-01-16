package euphoria.psycho.funny.util;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import euphoria.psycho.funny.R;

public abstract class BaseAppCompatActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_PERMISSIONS = 11;
    private Toolbar mToolbar;

    public void bindViews() {

    }

    protected abstract int getLayoutId();

    protected abstract String[] getNeedPermissions();

    protected abstract int getOptionsMenu();

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void initView() {

    }

    public void initialize() {
        setContentView(getLayoutId());
        setToolbar();
        bindViews();
        initView();
    }

    public void setToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar != null) {

            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] permissions = getNeedPermissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && permissions != null
                && permissions.length > 0) {
            requestPermissions(permissions, REQUEST_CODE_PERMISSIONS);
        } else {
            initialize();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getOptionsMenu() > 0) {
            getMenuInflater().inflate(getOptionsMenu(), menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Missing necessary permission: " + permissions[i], Toast.LENGTH_LONG).show();
                return;
            }
        }
        initialize();
    }
}
