package euphoria.psycho.funny.util;

import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.WallpaperManager;
import android.app.backup.BackupManager;
import android.app.usage.NetworkStatsManager;
import android.app.usage.StorageStatsManager;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothManager;
import android.companion.CompanionDeviceManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.RestrictionsManager;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.hardware.ConsumerIrManager;
import android.hardware.camera2.CameraManager;
import android.hardware.input.InputManager;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.rtt.WifiRttManager;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.HardwarePropertiesManager;
import android.os.PowerManager;
import android.os.UserManager;
import android.os.health.SystemHealthManager;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyScanManager;
import android.util.TypedValue;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import android.view.autofill.AutofillManager;
import android.view.inputmethod.InputMethodManager;
import android.view.textclassifier.TextClassificationManager;

import java.sql.DriverManager;

import androidx.appcompat.widget.AppCompatDrawableManager;
import androidx.recyclerview.widget.GridLayoutManager;

public class AndroidServices {
    private final Context mContext;

    private AndroidServices(Context context) {
        mContext = context;
    }

    public float dp2px(float value) {
        return
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        value,
                        mContext.getResources().getDisplayMetrics());

    }

    public AccessibilityManager provideAccessibilityManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(AccessibilityManager.class);
        } else {
            return (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        }
    }

    public AccountManager provideAccountManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(AccountManager.class);
        } else {
            return (AccountManager) mContext.getSystemService(Context.ACCOUNT_SERVICE);
        }
    }

    public ActivityManager provideActivityManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(ActivityManager.class);
        } else {
            return (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        }
    }

    public AlarmManager provideAlarmManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(AlarmManager.class);
        } else {
            return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        }
    }

    public AppOpsManager provideAppOpsManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(AppOpsManager.class);
        } else {
            return (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
        }
    }

    public AudioManager provideAudioManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(AudioManager.class);
        } else {
            return (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
    }

    public BatteryManager provideBatteryManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(BatteryManager.class);
        } else {
            return (BatteryManager) mContext.getSystemService(Context.BATTERY_SERVICE);
        }
    }

    public BluetoothManager provideBluetoothManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(BluetoothManager.class);
        } else {
            return (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        }
    }

    public CameraManager provideCameraManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(CameraManager.class);
        } else {
            return (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        }
    }

    public CaptioningManager provideCaptioningManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(CaptioningManager.class);
        } else {
            return (CaptioningManager) mContext.getSystemService(Context.CAPTIONING_SERVICE);
        }
    }

    public CarrierConfigManager provideCarrierConfigManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(CarrierConfigManager.class);
        } else {
            return (CarrierConfigManager) mContext.getSystemService(Context.CARRIER_CONFIG_SERVICE);
        }
    }

    public ClipboardManager provideClipboardManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(ClipboardManager.class);
        } else {
            return (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public CompanionDeviceManager provideCompanionDeviceManager() {
        return mContext.getSystemService(CompanionDeviceManager.class);

    }

    public ConnectivityManager provideConnectivityManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(ConnectivityManager.class);
        } else {
            return (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
    }

    public ConsumerIrManager provideConsumerIrManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(ConsumerIrManager.class);
        } else {
            return (ConsumerIrManager) mContext.getSystemService(Context.CONSUMER_IR_SERVICE);
        }
    }

    public DownloadManager provideDownloadManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(DownloadManager.class);
        } else {
            return (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    public HardwarePropertiesManager provideHardwarePropertiesManager() {

        return mContext.getSystemService(HardwarePropertiesManager.class);

    }

    public InputManager provideInputManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(InputManager.class);
        } else {
            return (InputManager) mContext.getSystemService(Context.INPUT_SERVICE);
        }
    }

    public InputMethodManager provideInputMethodManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(InputMethodManager.class);
        } else {
            return (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
    }

    public KeyguardManager provideKeyguardManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(KeyguardManager.class);
        } else {
            return (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        }
    }

    public LocationManager provideLocationManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(LocationManager.class);
        } else {
            return (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public NetworkStatsManager provideNetworkStatsManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(NetworkStatsManager.class);
        } else {
            return (NetworkStatsManager) mContext.getSystemService(Context.NETWORK_STATS_SERVICE);
        }
    }

    public NfcManager provideNfcManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(NfcManager.class);
        } else {
            return (NfcManager) mContext.getSystemService(Context.NFC_SERVICE);
        }
    }

    public NotificationManager provideNotificationManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(NotificationManager.class);
        } else {
            return (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    public NsdManager provideNsdManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(NsdManager.class);
        } else {
            return (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
        }
    }

    public PowerManager providePowerManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(PowerManager.class);
        } else {
            return (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        }
    }

    public SharedPreferences providePreferences() {
        return PreferenceManager.getDefaultSharedPreferences(AndroidContext.instance().get());
    }

    public RestrictionsManager provideRestrictionsManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(RestrictionsManager.class);
        } else {
            return (RestrictionsManager) mContext.getSystemService(Context.RESTRICTIONS_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public RingtoneManager provideRingtoneManager() {
        return mContext.getSystemService(RingtoneManager.class);
    }

    public SearchManager provideSearchManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(SearchManager.class);
        } else {
            return (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public SecurityManager provideSecurityManager() {

        return mContext.getSystemService(SecurityManager.class);
    }

    public StorageManager provideStorageManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(StorageManager.class);
        } else {
            return (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public StorageStatsManager provideStorageStatsManager() {

        return mContext.getSystemService(StorageStatsManager.class);

    }

    @TargetApi(Build.VERSION_CODES.N)
    public SystemHealthManager provideSystemHealthManager() {

        return mContext.getSystemService(SystemHealthManager.class);

    }

    public TelecomManager provideTelecomManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(TelecomManager.class);
        } else {
            return (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
        }
    }

    public TelephonyManager provideTelephonyManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(TelephonyManager.class);
        } else {
            return (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    public TelephonyScanManager provideTelephonyScanManager() {
        return mContext.getSystemService(TelephonyScanManager.class);

    }

    @TargetApi(Build.VERSION_CODES.O)
    public TextClassificationManager provideTextClassificationManager() {
        return mContext.getSystemService(TextClassificationManager.class);

    }

    public UsbManager provideUsbManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(UsbManager.class);
        } else {
            return (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        }
    }

    public UserManager provideUserManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(UserManager.class);
        } else {
            return (UserManager) mContext.getSystemService(Context.USER_SERVICE);
        }
    }

    public WallpaperManager provideWallpaperManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(WallpaperManager.class);
        } else {
            return (WallpaperManager) mContext.getSystemService(Context.WALLPAPER_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public WifiAwareManager provideWifiAwareManager() {

        return mContext.getSystemService(WifiAwareManager.class);

    }

    public WifiManager provideWifiManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(WifiManager.class);
        } else {
            return (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        }
    }

    public WifiP2pManager provideWifiP2pManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(WifiP2pManager.class);
        } else {
            return (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    public WifiRttManager provideWifiRttManager() {

        return mContext.getSystemService(WifiRttManager.class);

    }

    public WindowManager provideWindowManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.getSystemService(WindowManager.class);
        } else {
            return (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        }
    }

    public static AndroidServices instance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final AndroidServices INSTANCE =
                new AndroidServices(AndroidContext.instance().get());
    }


//    public {0} provide{0}() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            return mContext.getSystemService({0}.class);
//        } else {
//            return ({0}) mContext.getSystemService(Context.{1}_SERVICE);
//        }
//    }

}
