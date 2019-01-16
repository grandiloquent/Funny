package euphoria.psycho.funny.util;

import android.app.Application;
import android.os.Environment;
import android.os.StrictMode;

import java.io.File;

import euphoria.psycho.funny.util.debug.Log;


public class App extends Application {
    private static final String LOG_DIRECTOR_NAME = ".log";

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        AndroidContext.initialize(getApplicationContext());
        File logDirectory = new File(Environment.getExternalStorageDirectory(), LOG_DIRECTOR_NAME);
        FileUtils.createDirectory(logDirectory);
        Log.setDir(logDirectory);
    }
}
