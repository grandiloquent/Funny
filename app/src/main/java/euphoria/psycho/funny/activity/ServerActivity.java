package euphoria.psycho.funny.activity;


import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import euphoria.psycho.funny.R;
import euphoria.psycho.funny.service.ServerService;
import euphoria.psycho.funny.util.AndroidServices;
import euphoria.psycho.funny.util.BaseAppCompatActivity;
import euphoria.psycho.funny.util.Simple;
import euphoria.psycho.funny.util.debug.Log;

public class ServerActivity extends BaseAppCompatActivity {
    private static final int BLACK = 0xFF000000;
    private static final String TAG = "Funny/ServerActivity";
    private static final int WHITE = 0xFFFFFFFF;
    private final Handler mHandler = new Handler();
    private TextView mAddress;
    private Bitmap mBitmap;
    boolean mBound = false;
    private ImageView mQrCodeView;
    ServerService mService;
    private ServiceConnection mConnection;
    private String mURL;

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int dimension) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(contentsToEncode, format, dimension, dimension, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private void onStarted(String url) {

        mHandler.post(() -> {
            try {
                mBitmap = encodeAsBitmap(url, BarcodeFormat.QR_CODE, (int) AndroidServices.instance().dp2px(200));
                if (mQrCodeView != null)
                    mQrCodeView.setImageDrawable(new BitmapDrawable(mBitmap));
            } catch (WriterException e) {
                Log.e(TAG, "[onStarted] ---> ", e);
            }
            if (mAddress != null)
                mAddress.setText("请使用浏览器打开: " + url + "\n" +
                        "或扫描下方二维码");
        });
    }

    private void share() {
        if (mBitmap == null) return;

        Date t = new Date();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

        File barcodeFile = new File(Environment.getExternalStorageDirectory(), simpleDateFormat.format(t) + "-server.png");
        try (FileOutputStream fos = new FileOutputStream(barcodeFile)) {
            mBitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
        } catch (IOException ioe) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " - " + "视频分享地址");
        intent.putExtra(Intent.EXTRA_TEXT, mURL);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + barcodeFile.getAbsolutePath()));
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent, null));
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    @Override
    public void bindViews() {
        super.bindViews();
        mAddress = findViewById(R.id.address);
        mQrCodeView = findViewById(R.id.qrcode);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_server;
    }

    @Override
    protected String[] getNeedPermissions() {
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        };
    }

    @Override
    protected int getOptionsMenu() {
        return R.menu.server;
    }

    @Override
    public void initView() {
        super.initView();
    }

    @Override
    public void initialize() {
        super.initialize();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //new SimpleServer(deviceIp, directories, this, this);
        final View rootView = getWindow().getDecorView().getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        mConnection = new ServiceConnection() {

                            @Override
                            public void onServiceConnected(ComponentName className,
                                                           IBinder service) {
                                // We've bound to LocalService, cast the IBinder and get LocalService instance
                                ServerService.ServerBinder binder = (ServerService.ServerBinder) service;
                                mService = binder.getService();
                                mBound = true;

                                onStarted(mService.getURL());
                            }

                            @Override
                            public void onServiceDisconnected(ComponentName arg0) {
                                mBound = false;
                            }
                        };
                        Intent intent = new Intent(ServerActivity.this, ServerService.class);
                        startService(intent);
                        if (!mBound)
                            //android.app.ServiceConnectionLeaked: Activity euphoria.psycho.funny.activity.ServerActivity has leaked ServiceConnection
                            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.action_share: {
                share();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            try {
                unbindService(mConnection);
                mBound = false;
            } catch (Exception e) {
                Log.e(TAG, "[onStop] ---> ", e);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}
