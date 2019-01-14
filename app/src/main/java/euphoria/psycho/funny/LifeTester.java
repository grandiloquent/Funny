package euphoria.psycho.funny;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import euphoria.psycho.funny.util.debug.Log;

public class LifeTester implements LifecycleObserver {
    private static final String TAG = "_tag_";

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void doSome() {
        Log.e(TAG, "[doSome] ---> "+"onCreate");
    }
}
