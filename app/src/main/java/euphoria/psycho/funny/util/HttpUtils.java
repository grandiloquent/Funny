package euphoria.psycho.funny.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.RequiresPermission;
import euphoria.psycho.funny.util.debug.Log;

public class HttpUtils {
    private static final String TAG = "Funny/HttpUtils";

    public static String decodeURL(String url) throws UnsupportedEncodingException {
        return URLDecoder.decode(url, "UTF-8");
    }

    public static String dumpHeaders(HttpURLConnection conn) {
        StringBuilder sb = new StringBuilder();
        Map<String, List<String>> headers = conn.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            sb.append(entry.getKey())
                    .append(": ");
            List<String> value = entry.getValue();
            for (String v : value) {
                sb.append(v).append(';');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @RequiresPermission("android.permission.ACCESS_WIFI_STATE")
    public static String getDeviceIP(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();


            InetAddress inetAddress = intToInetAddress(wifiInfo.getIpAddress());

            return inetAddress.getHostAddress();
        } catch (Exception e) {
            Log.e(TAG, "[getDeviceIP] ---> ", e);
            return null;
        }
    }

    public static String getFileNameFromUrl(String url) {
        int p = url.indexOf('?');
        if (p != -1) {
            url = url.substring(0, p);
        }
        p = url.lastIndexOf('/');
        if (p != -1) {
            url = url.substring(p + 1);
        }
        return url;
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    public static boolean isValidUrl(String value) {

        URL url;


        try {
            url = new URL(value);
            url.openConnection();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
