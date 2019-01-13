package euphoria.psycho.funny.util.lifecycle;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.OnLifecycleEvent;
import euphoria.psycho.funny.util.ThreadUtils;
import euphoria.psycho.funny.util.debug.Log;
import euphoria.psycho.funny.util.lifecycle.events.OnAttach;
import euphoria.psycho.funny.util.lifecycle.events.OnCreate;
import euphoria.psycho.funny.util.lifecycle.events.OnCreateOptionsMenu;
import euphoria.psycho.funny.util.lifecycle.events.OnDestroy;
import euphoria.psycho.funny.util.lifecycle.events.OnOptionsItemSelected;
import euphoria.psycho.funny.util.lifecycle.events.OnPause;
import euphoria.psycho.funny.util.lifecycle.events.OnPrepareOptionsMenu;
import euphoria.psycho.funny.util.lifecycle.events.OnResume;
import euphoria.psycho.funny.util.lifecycle.events.OnSaveInstanceState;
import euphoria.psycho.funny.util.lifecycle.events.OnStart;
import euphoria.psycho.funny.util.lifecycle.events.OnStop;
import euphoria.psycho.funny.util.lifecycle.events.SetPreferenceScreen;

import static androidx.lifecycle.Lifecycle.Event.ON_ANY;

/**
 * Dispatcher for lifecycle events.
 */
public class Lifecycle extends LifecycleRegistry {
    private static final String TAG = "LifecycleObserver";
    private final List<LifecycleObserver> mObservers = new ArrayList<>();
    private final LifecycleProxy mProxy = new LifecycleProxy();
    /**
     * Creates a new LifecycleRegistry for the given provider.
     * <p>
     * You should usually create this inside your LifecycleOwner class's constructor and hold
     * onto the same instance.
     *
     * @param provider The owner LifecycleOwner
     */
    public Lifecycle(@NonNull LifecycleOwner provider) {
        super(provider);
        addObserver(mProxy);
    }
    /**
     * Registers a new observer of lifecycle events.
     */
    @UiThread
    @Override
    public void addObserver(androidx.lifecycle.LifecycleObserver observer) {
        ThreadUtils.ensureMainThread();
        super.addObserver(observer);
        if (observer instanceof LifecycleObserver) {
            mObservers.add((LifecycleObserver) observer);
        }
    }
    @UiThread
    @Override
    public void removeObserver(androidx.lifecycle.LifecycleObserver observer) {
        ThreadUtils.ensureMainThread();
        super.removeObserver(observer);
        if (observer instanceof LifecycleObserver) {
            mObservers.remove(observer);
        }
    }
    public void onAttach(Context context) {
        for (int i = 0, size = mObservers.size(); i < size; i++) {
            final LifecycleObserver observer = mObservers.get(i);
            if (observer instanceof OnAttach) {
                ((OnAttach) observer).onAttach(context);
            }
        }
    }
    // This method is not called from the proxy because it does not have access to the
    // savedInstanceState
    public void onCreate(Bundle savedInstanceState) {
        for (int i = 0, size = mObservers.size(); i < size; i++) {
            final LifecycleObserver observer = mObservers.get(i);
            if (observer instanceof OnCreate) {
                ((OnCreate) observer).onCreate(savedInstanceState);
            }
        }
    }
    private void onStart() {
        for (int i = 0, size = mObservers.size(); i < size; i++) {
            final LifecycleObserver observer = mObservers.get(i);
            if (observer instanceof OnStart) {
                ((OnStart) observer).onStart();
            }
        }
    }
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        for (int i = 0, size = mObservers.size(); i < size; i++) {
            final LifecycleObserver observer = mObservers.get(i);
            if (observer instanceof SetPreferenceScreen) {
                ((SetPreferenceScreen) observer).setPreferenceScreen(preferenceScreen);
            }
        }
    }
    private void onResume() {
        for (int i = 0, size = mObservers.size(); i < size; i++) {
            final LifecycleObserver observer = mObservers.get(i);
            if (observer instanceof OnResume) {
                ((OnResume) observer).onResume();
            }
        }
    }
    private void onPause() {
        for (int i = 0, size = mObservers.size(); i < size; i++) {
            final LifecycleObserver observer = mObservers.get(i);
            if (observer instanceof OnPause) {
                ((OnPause) observer).onPause();
            }
        }
    }
    public void onSaveInstanceState(Bundle outState) {
        for (int i = 0, size = mObservers.size(); i < size; i++) {
            final LifecycleObserver observer = mObservers.get(i);
            if (observer instanceof OnSaveInstanceState) {
                ((OnSaveInstanceState) observer).onSaveInstanceState(outState);
            }
        }
    }
    private void onStop() {
        for (int i = 0, size = mObservers.size(); i < size; i++) {
            final LifecycleObserver observer = mObservers.get(i);
            if (observer instanceof OnStop) {
                ((OnStop) observer).onStop();
            }
        }
    }
    private void onDestroy() {
        for (int i = 0, size = mObservers.size(); i < size; i++) {
            final LifecycleObserver observer = mObservers.get(i);
            if (observer instanceof OnDestroy) {
                ((OnDestroy) observer).onDestroy();
            }
        }
    }
    public void onCreateOptionsMenu(final Menu menu, final @Nullable MenuInflater inflater) {
        for (int i = 0, size = mObservers.size(); i < size; i++) {
            final LifecycleObserver observer = mObservers.get(i);
            if (observer instanceof OnCreateOptionsMenu) {
                ((OnCreateOptionsMenu) observer).onCreateOptionsMenu(menu, inflater);
            }
        }
    }
    public void onPrepareOptionsMenu(final Menu menu) {
        for (int i = 0, size = mObservers.size(); i < size; i++) {
            final LifecycleObserver observer = mObservers.get(i);
            if (observer instanceof OnPrepareOptionsMenu) {
                ((OnPrepareOptionsMenu) observer).onPrepareOptionsMenu(menu);
            }
        }
    }
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        for (int i = 0, size = mObservers.size(); i < size; i++) {
            final LifecycleObserver observer = mObservers.get(i);
            if (observer instanceof OnOptionsItemSelected) {
                if (((OnOptionsItemSelected) observer).onOptionsItemSelected(menuItem)) {
                    return true;
                }
            }
        }
        return false;
    }
    private class LifecycleProxy
            implements androidx.lifecycle.LifecycleObserver {
        @OnLifecycleEvent(ON_ANY)
        public void onLifecycleEvent(LifecycleOwner owner, Event event) {
            switch (event) {
                case ON_CREATE:
                    // onCreate is called directly since we don't have savedInstanceState here
                    break;
                case ON_START:
                    onStart();
                    break;
                case ON_RESUME:
                    onResume();
                    break;
                case ON_PAUSE:
                    onPause();
                    break;
                case ON_STOP:
                    onStop();
                    break;
                case ON_DESTROY:
                    onDestroy();
                    break;
                case ON_ANY:
                    Log.wtf(TAG, "Should not receive an 'ANY' event!");
                    break;
            }
        }
    }
}