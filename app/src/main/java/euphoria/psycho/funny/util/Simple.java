package euphoria.psycho.funny.util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class Simple {
    public static final String PATH_CPU = "/sys/devices/system/cpu/";
    private static final String[] sAudioExtensions = new String[]{
            ".m4a",
            ".aac",
            ".flac",
            ".gsm",
            ".mid",
            ".xmf",
            ".mxmf",
            ".rtttl",
            ".rtx",
            ".ota",
            ".imy",
            ".mp3",
            ".wav",
            ".ogg"
    };
    private static float sPixelDensity = -1f;
    private static final String[] sSubTitleExtensions = new String[]{
            ".srt",
            ".txt",
            ".htm"
    };
    private static final String[] sVideoExtensions = new String[]{
            ".3gp",
            ".mp4",
            ".ts",
            ".webm",
            ".mkv",
    };

    public static boolean checkExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

    }

    public static <T> T checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }

    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException t) {
        }
    }
    public static void assertTrue(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }
    public static boolean deleteDirectoryRecursively(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }

        for (File entry : directory.listFiles()) {
            if (entry.isDirectory()) {
                deleteDirectoryRecursively(entry);
            }
            if (!entry.delete()) {
                return false;
            }
        }
        return directory.delete();
    }

    public static float dpToPixel(float dp) {
        return sPixelDensity * dp;
    }

    public static int dpToPixel(int dp) {
        return Math.round(dpToPixel((float) dp));
    }

    public static String formatSize(long number) {
        float result = number;
        String suffix = "";
        if (result > 900) {
            suffix = " KB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " MB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " GB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " TB";
            result = result / 1024;
        }
        if (result > 900) {
            suffix = " PB";
            result = result / 1024;
        }
        String value;
        if (result < 1) {
            value = String.format("%.2f", result);
        } else if (result < 10) {
            value = String.format("%.1f", result);
        } else if (result < 100) {
            value = String.format("%.0f", result);
        } else {
            value = String.format("%.0f", result);
        }
        return value + suffix;
    }

    public static int getDirectoryChildCount(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return 0;
        else return files.length;
    }

    public static String getExtension(String fileName) {
        if (fileName == null) return null;
        int length = fileName.length();
        for (int i = length; --i >= 0; ) {
            char ch = fileName.charAt(i);
            if (ch == '.') {
                if (i != length - 1)
                    return fileName.substring(i);
                else
                    return null;
            }
            if (ch == File.separatorChar)
                break;
        }
        return null;
    }

    public static String getExternalStorageDirectoryPath() {
        return Environment.getExternalStorageDirectory().getPath();
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

    public static void initialize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        sPixelDensity = metrics.density;
    }

    public static boolean isAudio(File file) {
        String ext = getExtension(file.getName());
        if (ext == null) return false;
        return linearSearch(sAudioExtensions, ext) != -1;
    }

    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNullOrWhiteSpace(CharSequence charSequence) {
        if (charSequence == null) return true;
        int length = charSequence.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(charSequence.charAt(i))) return false;
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

    public static boolean isSubTitle(File file) {
        String ext = getExtension(file.getName());
        if (ext == null) return false;
        return linearSearch(sSubTitleExtensions, ext) != -1;
    }

    public static boolean isVideo(File file) {
        String ext = getExtension(file.getName());
        if (ext == null) return false;
        return linearSearch(sVideoExtensions, ext) != -1;
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
                    String extension = getExtension(file.getName());
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

    public static byte[] readAllBytes(File file) throws IOException {
        int length = (int) file.length();
        byte[] data = new byte[length];
        FileInputStream stream = new FileInputStream(file);
        try {
            int offset = 0;
            while (offset < length) {
                offset += stream.read(data, offset, length - offset);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            stream.close();
        }
        return data;
    }

    public static List<String> readAllLines(String path, String charset) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), Charset.forName(charset)));

        List<String> arrayList = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            arrayList.add(line);
        }
        reader.close();
        return arrayList;
    }

    public static String readAllText(Reader reader) throws IOException {
        char[] buffer = new char[8192];//8k
        StringBuilder builder = new StringBuilder();
        while (true) {
            int read = reader.read(buffer);
            if (read == -1) {
                break;
            }
            builder.append(buffer, 0, read);
        }
        return builder.toString();
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

    public static void writeAllLines(String path, String charset, List<String> lines) throws IOException {

        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path), Charset.forName(charset));

        for (String line : lines) {
            writer.write(line);
            writer.write('\n');
        }
        writer.close();
    }

    public static void writeAllText(String path, String charset, String text) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path), Charset.forName(charset));
        writer.write(text);
        writer.close();
    }

    public static void writeAllText(File file, String charset, String text) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), Charset.forName(charset));
        writer.write(text);
        writer.close();
    }
}
