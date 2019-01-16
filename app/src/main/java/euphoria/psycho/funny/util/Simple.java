package euphoria.psycho.funny.util;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import androidx.fragment.app.Fragment;


public class Simple {
    public static final String PATH_CPU = "/sys/devices/system/cpu/";

    private static float sPixelDensity = -1f;

    public static void assertTrue(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }
    public static float dpiFromPx(int size, DisplayMetrics metrics){
        float densityRatio = (float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return (size / densityRatio);
    }
    public static int pxFromDp(float size, DisplayMetrics metrics) {
        return (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                size, metrics));
    }
    public static int pxFromSp(float size, DisplayMetrics metrics) {
        return (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                size, metrics));
    }

    public static <T> T checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }

    public static <T> List<T> distinct(List<T> source) {
        List<T> r = new ArrayList<>();
        int length = source.size();
        for (int i = 0; i < length; i++) {

            while (i + 1 < length && source.get(i).equals(source.get(i + 1))) {
                i++;
            }
            r.add(source.get(i));
        }
        return r;
    }

    public static float dpToPixel(float dp) {
        return sPixelDensity * dp;
    }

    public static int dpToPixel(int dp) {
        return Math.round(dpToPixel((float) dp));
    }

    public static boolean endWiths(String value, String suffix, boolean ignoreCase) {
        int s = suffix.length();
        int v = value.length();
        if (v < s) return false;
        int i = 0;
        while (--s >= 0) {
            if (ignoreCase) {
                i++;
                int a = value.charAt(v - i);
                int b = suffix.charAt(s);
                a |= 0x20;
                b |= 0x20;
                if (a != b) return false;
            } else {
                if (value.charAt(v - s) != suffix.charAt(s)) return false;
            }
        }
        return true;
    }

    public static String getSelectedText(EditText editText) {
        CharSequence c = editText.getText();
        if (isNullOrWhiteSpace(c)) return null;
        String s = c.toString();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (end > start)
            return s.substring(editText.getSelectionStart(), editText.getSelectionEnd());

        return null;
    }

    public static String getString(CharSequence charSequence) {
        if (charSequence == null) return "";
        return charSequence.toString();
    }

    public static void initialize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        sPixelDensity = metrics.density;
    }

    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNullOrWhiteSpace(CharSequence charSequence) {
        if (charSequence == null) return true;
        int length = charSequence.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(charSequence.charAt(i))) return false;
        }
        return true;
    }

    public static boolean isNullOrWhiteSpace(String value) {
        if (value == null) return true;
        int len = value.length();

        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(value.charAt(i))) return false;
        }
        return true;
    }

    public static <T> boolean isSame(T[] t1, T[] t2) {
        if (t1 == null && t2 == null) return true;
        if ((t1 == null || t2 == null) || (t1.length != t2.length)) return false;
        for (int i = 0; i < t1.length; i++) {
            if (t1[i] != t2[i]) return false;
        }
        return true;
    }

    public static <T> String joining(List<T> list, String separator) {
        StringBuilder builder = new StringBuilder();
        for (T t : list) {
            builder.append(t).append(separator);
        }
        return builder.toString();
    }

    public static <T extends Comparable<T>> int linearSearch(T[] array, T value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].compareTo(value) == 0) {
                return i;
            }
        }
        return -1;
    }

    public static <T extends Comparable<T>> int linearSearch(List<T> list, T value) {
        int length = list.size();
        for (int i = 0; i < length; i++) {
            if (list.get(i).compareTo(value) == 0) {
                return i;
            }
        }
        return -1;
    }

    public static File[] listFiles(File dir, final String[] extensions) {
        File[] files;
        if (isEmpty(extensions)) {
            files = dir.listFiles();
        } else {
            files = dir.listFiles(file -> {
                if (file.isFile()) {
                    String extension = FileUtils.getExtension(file.getName());
                    for (String e : extensions) {
                        if (e.equals(extension)) return true;
                    }
                    return false;
                }
                return true;
            });
        }
        if (isEmpty(files)) return null;

        return files;
    }

    public static int lookup(byte[] content, byte[] pattern, int startIndex) {

        int l1 = content.length;
        int l2 = pattern.length;

        for (int i = startIndex; i < l1 - l2 + 1; ++i) {
            boolean found = true;
            for (int j = 0; j < l2; ++j) {
                if (content[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    public static int meterToPixel(float meter) {
        // 1 meter = 39.37 inches, 1 inch = 160 dp.
        return Math.round(dpToPixel(meter * 39.37f * 160));
    }

    public static int nextPowerOf2(int n) {
        if (n <= 0 || n > (1 << 30)) throw new IllegalArgumentException("n is invalid: " + n);
        n -= 1;
        n |= n >> 16;
        n |= n >> 8;
        n |= n >> 4;
        n |= n >> 2;
        n |= n >> 1;
        return n + 1;
    }

    public static void openSoftInput(AlertDialog dialog) {
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static float parseFloatSafely(String content, float defaultValue) {
        if (content == null) return defaultValue;
        try {
            return Float.parseFloat(content);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int parseIntSafely(String content, int defaultValue) {
        if (content == null) return defaultValue;
        try {
            return Integer.parseInt(content);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int prevPowerOf2(int n) {
        if (n <= 0) throw new IllegalArgumentException();
        return Integer.highestOneBit(n);
    }

    public static void replaceSelectedText(EditText editText, String str) {
        CharSequence c = editText.getText();
        if (isNullOrWhiteSpace(c)) return;
        String s = c.toString();
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (end > start) {
            String r1 = s.substring(0, start);
            String r2 = s.substring(end);

            editText.setText(r1 + str + r2);
        }


    }

    public static void setClipboardText(Context context, String s) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText("", s));
    }

    public static String sort(String s, Comparator<String> comparator) {
        if (s == null) return null;
        String[] lines = s.split("\n");
        List<String> sortLines = new ArrayList<>();
        for (String l : lines) {
            String s2 = l.trim();

            if (s2.length() == 0 || sortLines.indexOf(s2) != -1) continue;
            sortLines.add(s2);
        }
        Collections.sort(sortLines, comparator);
        return joining(sortLines, "\n");
    }

    public static String sort(String value) {
        String[] lines = value.split("\n");
        List<String> list = new ArrayList<>();
        for (String l : lines) {
            list.add(l.trim());
        }
        Collections.sort(list, String::compareToIgnoreCase);

        StringBuilder stringBuilder = new StringBuilder();
        for (String l : list) {
            stringBuilder.append(l).append('\n');
        }
        return stringBuilder.toString();

    }

    public static String substringBefore(String value, String delimiter) {
        int index = value.indexOf(delimiter);
        if (index == -1)
            return null;
        return value.substring(0, index);
    }

    public static String substringBefore(String value, char delimiter) {
        int index = value.indexOf(delimiter);
        if (index == -1)
            return null;
        return value.substring(0, index);
    }

    public static String substringBeforeLast(String value, char delimiter) {
        int index = value.lastIndexOf(delimiter);
        if (index == -1)
            return null;
        return value.substring(0, index);
    }

    public static String substringBeforeLast(String value, String delimiter) {
        int index = value.lastIndexOf(delimiter);
        if (index == -1)
            return null;
        return value.substring(0, index);
    }

    public static void toast(Fragment fragment, String message, boolean showLong) {
        if (showLong) {
            Toast.makeText(fragment.getContext(), message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(fragment.getContext(), message, Toast.LENGTH_SHORT).show();

        }
    }

    public static void toast(Fragment fragment, int resId, boolean showLong) {
        toast(fragment, fragment.getResources().getString(resId), showLong);
    }
}
