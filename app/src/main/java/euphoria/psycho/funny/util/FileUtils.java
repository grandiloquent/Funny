package euphoria.psycho.funny.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.WindowManager;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

import androidx.annotation.RequiresApi;
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

    public static boolean deleteFile(Context context, String treeUri, String fullPath) {
        if (fullPath.startsWith(getRemovableStoragePath())) {
            DocumentFile documentFile = getDocumentFile(context, fullPath, Uri.parse(treeUri));
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

    public static boolean isAudio(File file) {
        String ext = Simple.getExtension(file.getName());
        if (ext == null) return false;
        return Simple.linearSearch(sAudioExtensions, ext) != -1;
    }

    public static boolean isSubTitle(File file) {
        String ext = Simple.getExtension(file.getName());
        if (ext == null) return false;
        return Simple.linearSearch(sSubTitleExtensions, ext) != -1;
    }

    public static boolean isVideo(File file) {
        String ext = Simple.getExtension(file.getName());
        if (ext == null) return false;
        return Simple.linearSearch(sVideoExtensions, ext) != -1;
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

    public interface DialogListener {
        void onConfirmed(CharSequence value);

        void onDismiss();
    }

    public interface OperationResultCallback {
        void onCancel();

        void onFinished(boolean result);
    }
}
