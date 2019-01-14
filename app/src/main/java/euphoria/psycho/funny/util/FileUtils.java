package euphoria.psycho.funny.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;

public class FileUtils {
    private static final int BUFFER_SIZE = 8192;//8k
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
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
    private static String sRemovablePath = null;
    private static final String[] sSubTitleExtensions = new String[]{
            ".srt",
            ".vtt"
    };
    private static String sTreeUri;
    private static final String[] sVideoExtensions = new String[]{
            ".3gp",
            ".mp4",
            ".ts",
            ".webm",
            ".mkv",
    };

    public static String changeExtension(String path, String extension) {
        if (path != null) {
            String s = path;
            int length = path.length();
            for (int i = length; --i >= 0; ) {
                char ch = path.charAt(i);
                if (ch == '.') {
                    s = path.substring(0, i);
                    break;
                }
                if (ch == File.separatorChar)
                    break;
            }
            if (extension != null && path.length() != 0) {
                if (extension.length() == 0 || extension.charAt(0) != '.') {
                    s = s + ".";
                }
                s = s + extension;
            }
            return s;
        }
        return null;
    }

    public static String changeFileName(String path, String fileName) {
        return getDirectoryName(path) + File.separatorChar + fileName;
    }

    public static boolean checkExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

    }

    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException t) {
        }
    }

    public static boolean createDirectory(File dir) {
        if (dir.getAbsolutePath().startsWith(getRemovableStoragePath())) {
            if (sTreeUri == null) {
                throw new IllegalArgumentException("treeuri can not been null");
            }
            String[] paths = dir.getAbsolutePath().substring(getRemovableStoragePath().length() + 1).split("/");
            DocumentFile baseDirectory = DocumentFile.fromTreeUri(AndroidContext.instance().get(), Uri.parse(sTreeUri));

            for (String p : paths) {
                DocumentFile r = baseDirectory.findFile(p);
                if (r == null) {
                    r = baseDirectory.createDirectory(p);
                    if (!r.isDirectory()) return false;
                }
            }

            return true;
        } else {
            return dir.mkdirs();
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

    public static boolean deleteFile(Context context, String fullPath) {
        if (fullPath.startsWith(getRemovableStoragePath())) {
            DocumentFile documentFile = getDocumentFile(context, fullPath, Uri.parse(sTreeUri));
            return documentFile.delete();

        } else {
            return new File(fullPath).delete();
        }
    }

    public static String formatSize(long size) {
        if (size <= 0)
            return "0 B";
        String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        double digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups))
                + " " + units[(int) digitGroups];
    }

    public static String formatSizeNative(long number) {
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

    public static String getDirectoryName(String path) {
        int length = path.length();
        char ch = File.separatorChar;
        for (int i = length; --i >= 0; ) {
            if (path.charAt(i) == ch) {
                return path.substring(0, i);
            }
        }
        return "";
    }

    public static DocumentFile getDocumentFile(Context context, String pathName, Uri treeUri) {
        if (treeUri != null) {
            File[] sdcardPath = new File("/storage").listFiles();
            if (sdcardPath != null && sdcardPath.length >= 2) {
                String extSdcardPath = sdcardPath[1].getAbsolutePath();
                String truePath = pathName.substring(extSdcardPath.length() + 1, pathName.length());//除去root之外的
                String rootDocumentId = getRootDocumentId(context, treeUri);
                String documentId = rootDocumentId + truePath;
                //这里存在两种方式 一种会生成singleDocumentFile 另一种生成TreeDocumentFile
//            DocumentFile documentFile = DocumentFile.fromTreeUri(context, DocumentsContract.buildTreeDocumentUri(treeUri.getAuthority(), documentId));
                DocumentFile documentFile = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    documentFile = DocumentFile.fromSingleUri(context, DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId));
                }
                return documentFile;
            }
        }
        return null;
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

    public static String getFileName(String path) {
        if (path != null) {
            int length = path.length();
            for (int i = length; --i >= 0; ) {
                char ch = path.charAt(i);
                if (ch == File.separatorChar)
                    return path.substring(i + 1);
            }
        }
        return path;
    }

    public static String getFileNameWithoutExtension(String path) {
        path = getFileName(path);
        if (path != null) {
            int i;
            if ((i = path.lastIndexOf('.')) == -1)
                return path; // No path extension found
            else
                return path.substring(0, i);
        }
        return null;
    }

    public static String getRemovableStoragePath() {
        if (sRemovablePath != null) return sRemovablePath;
        File fileList[] = new File("/storage/").listFiles();
        for (File file : fileList) {
            if (!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead())
                sRemovablePath = file.getAbsolutePath();
        }
        return sRemovablePath;
    }

    public static String getRootDocumentId(Context context, Uri treeUri) {
        if (treeUri != null) {
            DocumentFile documentFile = DocumentFile.fromTreeUri(context, treeUri);
            return DocumentsContract.getDocumentId(documentFile.getUri());
        }
        return null;
    }

    public static void initialize(String treeUri) {
        sTreeUri = treeUri;
    }

    public static boolean isAudio(File file) {
        String ext = getExtension(file.getName());
        if (ext == null) return false;
        return Simple.linearSearch(sAudioExtensions, ext) != -1;
    }

    public static boolean isSubTitle(File file) {
        String ext = getExtension(file.getName());
        if (ext == null) return false;
        return Simple.linearSearch(sSubTitleExtensions, ext) != -1;
    }

    public static boolean isVideo(File file) {
        String ext = getExtension(file.getName());
        if (ext == null) return false;
        return Simple.linearSearch(sVideoExtensions, ext) != -1;
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

    public static boolean renameFile(Context context,  String source, String destination) {
        if (source.equals(destination)) return false;
        File sourceFile = new File(source);
        if (!sourceFile.isFile()) return false;
        File dstFile = new File(destination);
        if (dstFile.isFile()) return false;

        if (source.startsWith(getRemovableStoragePath())) {
            DocumentFile documentFile = getDocumentFile(context, sourceFile.getAbsolutePath(), Uri.parse(sTreeUri));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    return DocumentsContract.renameDocument(context.getContentResolver(),
                            documentFile.getUri(), dstFile.getName()) == null ? false : true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } else {
            return sourceFile.renameTo(dstFile);
        }
        return false;
    }

    public static void showFileDialog(Context context,
                                      String content,
                                      String title,
                                      DialogListener listener) {
        EditText editText = new EditText(context);
        editText.setMaxLines(1);
        editText.setText(content);
        if (content != null) {
            int pos = content.lastIndexOf('.');
            if (pos > -1) {
                editText.setSelection(0, pos);
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(editText)
                .setTitle(title)
                .setNegativeButton(android.R.string.cancel, (dialog1, which) -> {
                    dialog1.dismiss();
                    listener.onDismiss();
                })
                .setPositiveButton(android.R.string.ok, (dialog1, which) -> {
                    listener.onConfirmed(editText.getText());
                    dialog1.dismiss();
                }).create();
        //  Show the input keyboard for user
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
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

    public interface DialogListener {
        void onConfirmed(CharSequence value);

        void onDismiss();
    }

    public interface OperationResultCallback {
        void onCancel();

        void onFinished(boolean result);
    }
}
