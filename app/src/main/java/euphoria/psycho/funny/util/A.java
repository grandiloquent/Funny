package euphoria.psycho.funny.util;

import android.os.Build;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class A {

    public static RecyclerView setupRecyclerView(AppCompatActivity activity, int resId) {
        RecyclerView recyclerView = activity.findViewById(resId);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.getItemAnimator().setChangeDuration(0);
        return recyclerView;
    }

    public static Toolbar setupToolbar(AppCompatActivity activity, int resId) {
        Toolbar toolbar = activity.findViewById(resId);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> activity.onBackPressed());
        toolbar.setContentInsetStartWithNavigation(0);
        return toolbar;
    }
}
