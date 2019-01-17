package euphoria.psycho.funny.server;

import android.graphics.Bitmap;
import android.text.Html;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import euphoria.psycho.funny.util.AndroidContext;
import euphoria.psycho.funny.util.BitmapUtils;
import euphoria.psycho.funny.util.debug.Log;

import static euphoria.psycho.funny.server.Utils.addAll;
import static euphoria.psycho.funny.server.Utils.closeQuietly;
import static euphoria.psycho.funny.server.Utils.findVideoFile;
import static euphoria.psycho.funny.server.Utils.getDefaultReason;
import static euphoria.psycho.funny.server.Utils.getMimeTypeTable;
import static euphoria.psycho.funny.server.Utils.getVideoFiles;
import static euphoria.psycho.funny.server.Utils.isVideo;
import static euphoria.psycho.funny.server.Utils.lookup;
import static euphoria.psycho.funny.server.Utils.parseHeaders;
import static euphoria.psycho.funny.server.Utils.parseQuery;
import static euphoria.psycho.funny.server.Utils.parseURL;
import static euphoria.psycho.funny.server.Utils.sliceHeader;
import static euphoria.psycho.funny.server.Utils.sliceURL;
import static euphoria.psycho.funny.server.Utils.substringAfter;
import static euphoria.psycho.funny.server.Utils.substringAfterLast;
import static euphoria.psycho.funny.server.Utils.substringBefore;
import static euphoria.psycho.funny.server.Utils.substringBeforeLast;
import static euphoria.psycho.funny.server.Utils.trim;
import static euphoria.psycho.funny.util.FileUtils.closeSilently;

public class SimpleServer {

    private static final byte[] BYTES_DOUBLE_LINE_FEED = new byte[]{'\r', '\n', '\r', '\n'};
    private static final byte[] BYTES_LINE_FEED = new byte[]{'\r', '\n'};
    private static final String DATE_FORMAT_GMT = " EEE, dd MMM yyyy hh:mm:ss 'GMT'";
    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    private static final String HEADER_VALUE_NO_CACHE = "no-cache";
    static final String HTTP_ACCEPT_RANGES = "Accept-Ranges";
    private static final String HTTP_CACHE_CONTROL = "Cache-Control";
    static final String HTTP_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String HTTP_CONTENT_RANGE = "Content-Range";
    private static final String HTTP_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_DATE = "Date";
    private static final String HTTP_RANGE = "Range";
    private static final int MILLIS_PER_SECOND = 1000;
    private static final int MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60; //     60,000
    private static final int MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60;   //  3,600,000
    static final int MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;      // 86,400,000
    private static final int STATUS_CODE_BAD_REQUEST = 400;
    private static final int STATUS_CODE_INTERNAL_SERVER_ERROR = 500;
    private static final int STATUS_CODE_NOT_FOUND = 404;
    private static final int STATUS_CODE_OK = 200;
    private static final int STATUS_CODE_PARTIAL_CONTENT = 206;
    private static final String TAG = "Funny/SimpleServer";
    private static final String UTF_8 = "UTF-8";
    private static final byte[][] getmBytesMarkdown = new byte[][]{
/* 0 notelist */new byte[]{60, 33, 68, 79, 67, 84, 89, 80, 69, 32, 104, 116, 109, 108, 62, 60, 104, 116, 109, 108, 32, 108, 97, 110, 103, 61, 34, 101, 110, 34, 62, 60, 104, 101, 97, 100, 62, 60, 109, 101, 116, 97, 32, 99, 104, 97, 114, 115, 101, 116, 61, 34, 85, 84, 70, 45, 56, 34, 47, 62, 60, 109, 101, 116, 97, 32, 110, 97, 109, 101, 61, 34, 118, 105, 101, 119, 112, 111, 114, 116, 34, 32, 99, 111, 110, 116, 101, 110, 116, 61, 34, 119, 105, 100, 116, 104, 61, 100, 101, 118, 105, 99, 101, 45, 119, 105, 100, 116, 104, 44, 32, 105, 110, 105, 116, 105, 97, 108, 45, 115, 99, 97, 108, 101, 61, 49, 46, 48, 34, 47, 62, 60, 109, 101, 116, 97, 32, 104, 116, 116, 112, 45, 101, 113, 117, 105, 118, 61, 34, 88, 45, 85, 65, 45, 67, 111, 109, 112, 97, 116, 105, 98, 108, 101, 34, 32, 99, 111, 110, 116, 101, 110, 116, 61, 34, 105, 101, 61, 101, 100, 103, 101, 34, 47, 62, 60, 116, 105, 116, 108, 101, 62, 68, 111, 99, 117, 109, 101, 110, 116, 60, 47, 116, 105, 116, 108, 101, 62, 60, 108, 105, 110, 107, 32, 114, 101, 108, 61, 34, 115, 116, 121, 108, 101, 115, 104, 101, 101, 116, 34, 32, 104, 114, 101, 102, 61, 34, 104, 116, 116, 112, 115, 58, 47, 47, 99, 100, 110, 106, 115, 46, 99, 108, 111, 117, 100, 102, 108, 97, 114, 101, 46, 99, 111, 109, 47, 97, 106, 97, 120, 47, 108, 105, 98, 115, 47, 115, 105, 109, 112, 108, 101, 109, 100, 101, 47, 49, 46, 49, 49, 46, 50, 47, 115, 105, 109, 112, 108, 101, 109, 100, 101, 46, 109, 105, 110, 46, 99, 115, 115, 34, 47, 62, 60, 115, 99, 114, 105, 112, 116, 32, 115, 114, 99, 61, 34, 104, 116, 116, 112, 115, 58, 47, 47, 99, 100, 110, 106, 115, 46, 99, 108, 111, 117, 100, 102, 108, 97, 114, 101, 46, 99, 111, 109, 47, 97, 106, 97, 120, 47, 108, 105, 98, 115, 47, 115, 105, 109, 112, 108, 101, 109, 100, 101, 47, 49, 46, 49, 49, 46, 50, 47, 115, 105, 109, 112, 108, 101, 109, 100, 101, 46, 109, 105, 110, 46, 106, 115, 34, 62, 60, 47, 115, 99, 114, 105, 112, 116, 62, 60, 108, 105, 110, 107, 32, 114, 101, 108, 61, 34, 115, 116, 121, 108, 101, 115, 104, 101, 101, 116, 34, 32, 104, 114, 101, 102, 61, 34, 101, 100, 105, 116, 111, 114, 46, 99, 115, 115, 34, 47, 62, 60, 98, 111, 100, 121, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 99, 111, 110, 116, 97, 105, 110, 101, 114, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 108, 105, 115, 116, 34, 62},
/* 1  */new byte[]{60, 47, 100, 105, 118, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 101, 100, 105, 116, 111, 114, 34, 62, 60, 116, 101, 120, 116, 97, 114, 101, 97, 32, 105, 100, 61, 34, 101, 100, 105, 116, 45, 116, 101, 120, 116, 34, 62, 60, 47, 116, 101, 120, 116, 97, 114, 101, 97, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62, 60, 115, 99, 114, 105, 112, 116, 32, 115, 114, 99, 61, 34, 101, 100, 105, 116, 111, 114, 46, 106, 115, 34, 62, 60, 47, 115, 99, 114, 105, 112, 116, 62},
    };
    private static final byte[] mBytesDropZone = new byte[]{60, 100, 105, 118, 32, 105, 100, 61, 34, 100, 114, 111, 112, 122, 111, 110, 101, 34, 62, 60, 102, 111, 114, 109, 32, 99, 108, 97, 115, 115, 61, 34, 100, 114, 111, 112, 122, 111, 110, 101, 34, 32, 97, 99, 116, 105, 111, 110, 61, 34, 47, 117, 112, 108, 111, 97, 100, 34, 62, 60, 47, 102, 111, 114, 109, 62, 60, 47, 100, 105, 118, 62};
    private static final byte[][] mBytesIndex = new byte[][]{
/* 0  */new byte[]{60, 33, 68, 79, 67, 84, 89, 80, 69, 32, 104, 116, 109, 108, 62, 60, 104, 116, 109, 108, 62, 60, 104, 101, 97, 100, 62, 60, 109, 101, 116, 97, 32, 104, 116, 116, 112, 45, 101, 113, 117, 105, 118, 61, 34, 88, 45, 85, 65, 45, 67, 111, 109, 112, 97, 116, 105, 98, 108, 101, 34, 32, 99, 111, 110, 116, 101, 110, 116, 61, 34, 73, 69, 61, 101, 100, 103, 101, 34, 47, 62, 60, 109, 101, 116, 97, 32, 99, 104, 97, 114, 115, 101, 116, 61, 34, 117, 116, 102, 45, 56, 34, 47, 62, 60, 116, 105, 116, 108, 101, 62, 60, 47, 116, 105, 116, 108, 101, 62, 60, 109, 101, 116, 97, 32, 110, 97, 109, 101, 61, 34, 100, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, 34, 32, 99, 111, 110, 116, 101, 110, 116, 61, 34, 34, 47, 62, 60, 109, 101, 116, 97, 32, 110, 97, 109, 101, 61, 34, 97, 117, 116, 104, 111, 114, 34, 32, 99, 111, 110, 116, 101, 110, 116, 61, 34, 34, 47, 62, 60, 109, 101, 116, 97, 32, 110, 97, 109, 101, 61, 34, 118, 105, 101, 119, 112, 111, 114, 116, 34, 32, 99, 111, 110, 116, 101, 110, 116, 61, 34, 119, 105, 100, 116, 104, 61, 100, 101, 118, 105, 99, 101, 45, 119, 105, 100, 116, 104, 44, 32, 105, 110, 105, 116, 105, 97, 108, 45, 115, 99, 97, 108, 101, 61, 49, 34, 47, 62, 60, 108, 105, 110, 107, 32, 114, 101, 108, 61, 34, 115, 116, 121, 108, 101, 115, 104, 101, 101, 116, 34, 32, 104, 114, 101, 102, 61, 34, 100, 114, 111, 112, 122, 111, 110, 101, 46, 109, 105, 110, 46, 99, 115, 115, 34, 47, 62, 60, 108, 105, 110, 107, 32, 114, 101, 108, 61, 34, 115, 116, 121, 108, 101, 115, 104, 101, 101, 116, 34, 32, 104, 114, 101, 102, 61, 34, 109, 97, 105, 110, 46, 109, 105, 110, 46, 99, 115, 115, 34, 47, 62, 60, 33, 45, 45, 91, 105, 102, 32, 108, 116, 32, 73, 69, 32, 57, 93, 62, 60, 115, 99, 114, 105, 112, 116, 32, 115, 114, 99, 61, 34, 47, 47, 99, 100, 110, 106, 115, 46, 99, 108, 111, 117, 100, 102, 108, 97, 114, 101, 46, 99, 111, 109, 47, 97, 106, 97, 120, 47, 108, 105, 98, 115, 47, 104, 116, 109, 108, 53, 115, 104, 105, 118, 47, 51, 46, 55, 46, 50, 47, 104, 116, 109, 108, 53, 115, 104, 105, 118, 46, 109, 105, 110, 46, 106, 115, 34, 62, 60, 47, 115, 99, 114, 105, 112, 116, 62, 60, 115, 99, 114, 105, 112, 116, 32, 115, 114, 99, 61, 34, 47, 47, 99, 100, 110, 106, 115, 46, 99, 108, 111, 117, 100, 102, 108, 97, 114, 101, 46, 99, 111, 109, 47, 97, 106, 97, 120, 47, 108, 105, 98, 115, 47, 114, 101, 115, 112, 111, 110, 100, 46, 106, 115, 47, 49, 46, 52, 46, 50, 47, 114, 101, 115, 112, 111, 110, 100, 46, 109, 105, 110, 46, 106, 115, 34, 62, 60, 47, 115, 99, 114, 105, 112, 116, 62, 60, 33, 91, 101, 110, 100, 105, 102, 93, 45, 45, 62, 60, 115, 99, 114, 105, 112, 116, 32, 115, 114, 99, 61, 34, 100, 114, 111, 112, 122, 111, 110, 101, 46, 109, 105, 110, 46, 106, 115, 34, 62, 60, 47, 115, 99, 114, 105, 112, 116, 62, 60, 98, 111, 100, 121, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 99, 111, 110, 116, 97, 105, 110, 101, 114, 34, 62},
/* 1  */new byte[]{60, 47, 100, 105, 118, 62},
    };
    private static final byte[][] mBytesVideo = new byte[][]{
/* 0 src */new byte[]{60, 100, 105, 118, 32, 105, 100, 61, 34, 112, 108, 97, 121, 101, 114, 34, 32, 99, 108, 97, 115, 115, 61, 34, 112, 108, 97, 121, 101, 114, 45, 97, 112, 105, 32, 112, 108, 97, 121, 101, 114, 45, 115, 105, 122, 101, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 104, 116, 109, 108, 53, 45, 118, 105, 100, 101, 111, 45, 99, 111, 110, 116, 97, 105, 110, 101, 114, 34, 62, 60, 118, 105, 100, 101, 111, 32, 99, 108, 97, 115, 115, 61, 34, 104, 116, 109, 108, 53, 45, 118, 105, 100, 101, 111, 45, 112, 108, 97, 121, 101, 114, 34, 32, 99, 111, 110, 116, 114, 111, 108, 115, 32, 97, 117, 116, 111, 112, 108, 97, 121, 62, 60, 115, 111, 117, 114, 99, 101, 32, 115, 114, 99, 61, 34},
/* 1  */new byte[]{34, 32, 116, 121, 112, 101, 61, 34, 118, 105, 100, 101, 111, 47, 109, 112, 52, 34, 47, 62, 60, 47, 118, 105, 100, 101, 111, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62},
    };
    private final String HTTP_CONTENT_LENGTH = "Content-Length";
    private final ExecutorService mExecutorService;
    private final Hashtable<String, String> mMimeTypes = getMimeTypeTable();
    private final ServerSocket mServerSocket;
    private final String mStaticDirectory;
    private final String mURL;
    private final String mUploadDirectory;
    private final String[] mVideoDirectory;
    private byte[] mBytesTemplate;
    private int mPort;
    private Thread mThread;
    private List<File> mVideoFiles;


    private SimpleServer(Builder builder) throws IOException {

        mPort = builder.mPort;
        mStaticDirectory = builder.mStaticDirectory;
        mVideoDirectory = builder.mVideoDirectory;
        mUploadDirectory = builder.mUploadDirectory;
        InetAddress address = InetAddress.getByName(builder.mHost);
        byte[] bytes = address.getAddress();

        mServerSocket = new ServerSocket(mPort, 0, InetAddress.getByAddress(bytes));
        mServerSocket.setSoTimeout(MILLIS_PER_SECOND * 20);
        mPort = mServerSocket.getLocalPort();
        mURL = "http://" + mServerSocket.getInetAddress().getHostAddress() + ":" + mPort;

        Log.d(TAG, "[SimpleServer] ---> " + mURL);
        mExecutorService = Executors.newFixedThreadPool(4);

        mVideoFiles = getVideoFiles(mVideoDirectory);

        startServer();
    }


    private void file(Socket socket, String fileName) {
        InputStream is = null;
        try {
            OutputStream os = socket.getOutputStream();
            List<String> headers = new ArrayList<>();
            String extension = substringAfterLast(fileName, '.');
            headers.add(HTTP_CONTENT_TYPE);
            headers.add(mMimeTypes.get(extension));//  "; charset=UTF-8"

            if (extension != null) {
                switch (extension.toLowerCase()) {
                    case ".html":

                        headers.add(HTTP_CACHE_CONTROL);
                        headers.add("no-cache");
                        break;

                    case ".js":
                    case ".css": {
                        headers.add(HTTP_CACHE_CONTROL);
                        headers.add(HEADER_VALUE_NO_CACHE);
                        break;
                    }
                    case ".png":

                        headers.add(HTTP_CACHE_CONTROL);

                        headers.add("public, max-age=31536000, stale-while-revalidate=2592000");
                        break;
                }
            }
            if (extension != null && extension.equals(".png")) {
                File bitmap = png(fileName);
                if (bitmap == null) {
                    notFound(socket);
                    return;
                }
                is = new FileInputStream(bitmap);
                headers.add(HTTP_DATE);
                headers.add(new SimpleDateFormat(DATE_FORMAT_GMT, Locale.US).format(bitmap.lastModified()));
                headers.add(HTTP_CONTENT_LENGTH);
                headers.add(Long.toString(bitmap.length()));

            } else {
                try {
                    is = AndroidContext.instance().get().getAssets().open("server/" + fileName);

                } catch (Exception e) {
                    notFound(socket);
                    return;
                }

                headers.add(HTTP_DATE);
                headers.add(new SimpleDateFormat(DATE_FORMAT_GMT, Locale.US).format(new Date()));

            }


            writeHeaders(socket, STATUS_CODE_OK, headers);

            writeInputStream(socket, is, 0L);
            os.flush();
        } catch (Exception e) {
            e(e);
            send(socket, 500);
        } finally {
            closeQuietly(is);
            closeQuietly(socket);
        }
    }


    private List<String> generateGenericHeader(String mimeType, String cache) {
        List<String> headers = new ArrayList<>();

        headers.add(HTTP_CONTENT_TYPE);
        headers.add(mimeType);//  "; charset=UTF-8"
        headers.add(HTTP_CACHE_CONTROL);
        headers.add(cache);
        headers.add(HTTP_DATE);
        headers.add(new SimpleDateFormat(DATE_FORMAT_GMT, Locale.US).format(new Date()));
        return headers;
    }

    private byte[] generateItems() throws IOException {
        byte[][] buffer = new byte[][]{
/* 0 href */new byte[]{60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 109, 101, 100, 105, 97, 45, 105, 116, 101, 109, 34, 62, 60, 97, 32, 104, 114, 101, 102, 61, 34, 63, 113, 61},
/* 1 src */new byte[]{34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 118, 105, 100, 101, 111, 45, 116, 104, 117, 109, 98, 110, 97, 105, 108, 45, 99, 111, 110, 116, 97, 105, 110, 101, 114, 45, 108, 97, 114, 103, 101, 32, 99, 101, 110, 116, 101, 114, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 99, 111, 118, 101, 114, 32, 118, 105, 100, 101, 111, 45, 116, 104, 117, 109, 98, 110, 97, 105, 108, 45, 105, 109, 103, 32, 118, 105, 100, 101, 111, 45, 116, 104, 117, 109, 98, 110, 97, 105, 108, 45, 98, 103, 34, 62, 60, 47, 100, 105, 118, 62, 60, 105, 109, 103, 32, 99, 108, 97, 115, 115, 61, 34, 99, 111, 118, 101, 114, 32, 118, 105, 100, 101, 111, 45, 116, 104, 117, 109, 98, 110, 97, 105, 108, 45, 105, 109, 103, 34, 32, 115, 114, 99, 61, 34},
/* 2 href */new byte[]{34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 118, 105, 100, 101, 111, 45, 116, 104, 117, 109, 98, 110, 97, 105, 108, 45, 111, 118, 101, 114, 108, 97, 121, 45, 98, 111, 116, 116, 111, 109, 45, 103, 114, 111, 117, 112, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 46, 116, 105, 109, 101, 45, 108, 97, 98, 101, 108, 34, 62, 60, 115, 112, 97, 110, 62, 60, 47, 115, 112, 97, 110, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62, 60, 47, 97, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 100, 101, 116, 97, 105, 108, 115, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 108, 97, 114, 103, 101, 45, 109, 101, 100, 105, 97, 45, 105, 116, 101, 109, 45, 105, 110, 102, 111, 32, 99, 98, 111, 120, 34, 62, 60, 97, 32, 99, 108, 97, 115, 115, 61, 34, 108, 97, 114, 103, 101, 45, 109, 101, 100, 105, 97, 45, 105, 116, 101, 109, 45, 109, 101, 116, 97, 100, 97, 116, 97, 34, 32, 104, 114, 101, 102, 61, 34, 63, 113, 61},
/* 3 titles */new byte[]{34, 62, 60, 104, 51, 62},
/* 4  */new byte[]{60, 47, 104, 51, 62, 60, 47, 97, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62},
        };


        ByteArrayOutputStream os = new ByteArrayOutputStream();

        if (mVideoFiles != null) {
            for (File f : mVideoFiles) {
                String name = f.getName();
                byte[] href = name.getBytes(UTF_8);//URLEncoder.encode(name, UTF_8).getBytes(UTF_8);
                os.write(buffer[0]);
                os.write(href);
                os.write(buffer[1]);
                os.write(URLEncoder.encode(name + ".png", UTF_8).getBytes(UTF_8));
                os.write(buffer[2]);
                os.write(href);
                os.write(buffer[3]);
                os.write(f.getName().getBytes(UTF_8));
                os.write(buffer[4]);

            }
        }
        return os.toByteArray();

    }

    private FormHeader getFormHeader(byte[] data, int offset) {
        int index = lookup(data, BYTES_LINE_FEED, offset);
        if (index == -1) return null;
        FormHeader formHeader = new FormHeader();
        String contentDisposition = Utils.getString(Arrays.copyOfRange(data, offset, index));
        contentDisposition = substringAfter(contentDisposition, "filename=");
        contentDisposition = trim(contentDisposition, new char[]{'"'});
        formHeader.fileName = contentDisposition;
        index = index + BYTES_LINE_FEED.length;

        index = lookup(data, BYTES_DOUBLE_LINE_FEED, index);
        if (index == -1) return null;

        formHeader.start = index + BYTES_DOUBLE_LINE_FEED.length;

        return formHeader;
    }

    public String getURL() {
        return mURL;
    }

    private void index(Socket socket) {
        try {

            OutputStream os = socket.getOutputStream();
            List<String> headers = new ArrayList<>();

            headers.add(HTTP_CONTENT_TYPE);
            headers.add(mMimeTypes.get(".html") + "; charset=UTF-8");//  "; charset=UTF-8"
            headers.add(HTTP_CACHE_CONTROL);
            headers.add("no-cache");
            headers.add(HTTP_DATE);
            headers.add(new SimpleDateFormat(DATE_FORMAT_GMT, Locale.US).format(new Date()));
            // 如果设置 HTTP_CONTENT_LENGTH
            // 客户端只会接收相应长度的字符
            // 其他的数据将被忽略

            byte[] header = responseHeader(200, headers).getBytes(UTF_8);
            os.write(header);
            os.write(mBytesIndex[0]);

            if (mBytesTemplate == null) {

                mBytesTemplate = generateItems();
            }
            os.write(mBytesTemplate);

            os.write(mBytesDropZone);
            os.write(mBytesIndex[1]);
            os.flush();

        } catch (Exception e) {
            e(e);
        } finally {
            closeQuietly(socket);
        }
    }

    private void jsonGet(Socket socket, String url) {
        Log.d(TAG, "[jsonGet] ---> ");
        try {

            long hash = Utils.safeParseLong(Utils.substringAfterLast(url, '/'));
            if (hash == -1) {
                send(socket, STATUS_CODE_BAD_REQUEST);
                return;
            }
            List<String> headers = generateGenericHeader("application/json; charset=utf-8", HEADER_VALUE_NO_CACHE);
            writeHeaders(socket, STATUS_CODE_OK, headers);
            Log.d(TAG, "[jsonGet] ---> " + hash);
            Note note = DatabaseHelper.getInstance(AndroidContext.instance().get()).fetchNote(hash);
            if (note == null) {
                notFound(socket);
                return;
            }
            Gson gson = new Gson();

            socket.getOutputStream().write(Utils.getBytes(gson.toJson(note, Note.class)));


        } catch (Exception e) {

        } finally {
            closeQuietly(socket);
        }
    }

    private void markdown(Socket socket) throws UnsupportedEncodingException {
        List<Note> titles = DatabaseHelper.getInstance(AndroidContext.instance().get()).fetchTitles();
        StringBuilder sb = new StringBuilder();
        Log.d(TAG, "[markdown] ---> " + titles.size());
        for (Note note : titles) {
            sb.append("<a class=\"list-item\" href=\"/md#" + note.ID + "\">\n" +
                    "                <h3 class=\"list-heading  clearfix\">\n" +
                    "                    <span class=\"list-title\">" + Html.escapeHtml(note.Title) + "</span>\n" +
                    "                </h3>\n" +
                    "            </a>");
        }
        List<String> headers = generateGenericHeader("text/html", "no-cache");

        try {
            writeHeaders(socket, STATUS_CODE_OK, headers);
            socket.getOutputStream().write(getmBytesMarkdown[0]);
            socket.getOutputStream().write(sb.toString().getBytes(Charset.forName(UTF_8)));
            socket.getOutputStream().write(getmBytesMarkdown[1]);

        } catch (IOException e) {
            Log.e(TAG, "[markdown] ---> ", e);
        } finally {
            closeQuietly(socket);
        }

    }

    private Position nextLine(byte[] data, byte[] boundary, int offset) {
        int length = data.length;
        int position = -1;
        int len = boundary.length;


        for (int i = offset; i < length; i++) {
            if (data[i] == 10) {
                position = i + 1;

                Position p = new Position();
                p.position = position;

                int lineLength = i - offset - 1;

                if (lineLength == len || lineLength == len + 2) {
                    for (int j = 0; j < len; j++) {
                        if (data[offset + j] != boundary[j]) {

                            p.isBoundaryLine = false;
                            p.isLastLine = false;
                            return p;
                        }
                    }

                    if (lineLength == len + 2 && data[offset + lineLength - 1] == 45 && data[offset + lineLength - 2] == 45) {
                        p.isBoundaryLine = true;
                        p.isLastLine = true;
                        return p;
                    } else {
                        p.isBoundaryLine = true;
                        p.isLastLine = false;
                        return p;
                    }
                }

                return p;
            }
        }
        return null;
    }

    private void notFound(Socket socket) {
        send(socket, STATUS_CODE_NOT_FOUND);
    }

    private File png(String fileName) {
        File bitmap = new File(mStaticDirectory, fileName);
        if (bitmap.isFile()) {
            return bitmap;
        }
        File video = findVideoFile(substringBeforeLast(fileName, '.'), mVideoFiles);
        if (video == null) {
            d(fileName);
            return null;
        }
        try {
            Bitmap b = BitmapUtils.createVideoThumbnail(video.getAbsolutePath());
            FileOutputStream outputStream = new FileOutputStream(bitmap);
            b.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            closeSilently(outputStream);
            return bitmap;
        } catch (Exception e) {
            e(e);
        }
        return null;
    }

    private void processRequest(Socket socket) {

        try {
            InputStream is = new BufferedInputStream(socket.getInputStream());
            byte[][] status = sliceURL(is);
            if (status == null || status.length < 1) {
                notFound(socket);
                return;
            }

            String[] u = parseURL(status[0]);
            Log.d(TAG, "[processRequest] ---> " + u[1]);
            if (u[1].length() == 0/* / */) {
                if (u[2] == null) {
                    index(socket);
                } else {

                    List<String> parameters = parseQuery(u[2]);
                    if (parameters.size() != 2 || !parameters.get(0).equals("q")) {
                        send(socket, STATUS_CODE_BAD_REQUEST);
                        return;
                    }
                    processVideoPage(socket, parameters.get(1));
                }
                return;
            } else if (u[1].equals("md")) {
                markdown(socket);
            } else if (u[1].lastIndexOf('.') != -1) {
                if (isVideo(u[1])) {
                    d(URLDecoder.decode(u[1], UTF_8));
                    video(socket, is, URLDecoder.decode(u[1], UTF_8), status[1]);
                    return;
                } else {
                    file(socket, URLDecoder.decode(u[1], UTF_8));
                }

            } else if (u[1].equals("upload")) {
                processUploadFile(socket, is, status[1]);

            } else if (u[1].startsWith("api/get/")) {
                jsonGet(socket, u[1]);
            } else {
                notFound(socket);
            }


            //d(toString(status[1]));

            //parseHeader(socket);
        } catch (Exception e) {
            e(e);


        }

        closeQuietly(socket);
    }

    private void processUploadFile(Socket socket, InputStream is, byte[] bytes) throws IOException {

        byte[][] header = sliceHeader(is, bytes);
        List<String> headers = parseHeaders(header[0]);

        String boundary = null;
        for (int i = 0; i < headers.size(); i += 2) {
            String key = headers.get(i);
            if (key.equalsIgnoreCase(HTTP_CONTENT_TYPE)) {
                if (headers.get(i + 1).startsWith("multipart/form-data;")) {
                    boundary = substringAfter(headers.get(i + 1), "boundary=");

                } else {

                    send(socket, STATUS_CODE_BAD_REQUEST);
                    return;
                }
            } else if (key.equalsIgnoreCase(HTTP_CONTENT_LENGTH)) {

            }
        }
        if (boundary == null) {

            send(socket, STATUS_CODE_BAD_REQUEST);
            return;
        } else {
            // Add two dashes in start according to the convention
            boundary = "--" + boundary;
        }

        if (header[1] == null) {

            send(socket, STATUS_CODE_BAD_REQUEST);
            return;
        }

        // handleLargeFile(socket, is, boundary, header[1]);

        byte[] boundaryBytes = boundary.getBytes(UTF_8);
        byte[] data = header[1];
        int offset = 0;
        OutputStream os = null;
        boolean exit = false;
        while (true) {

            if (exit) {


                // do not forget invoke outputStream.flush
                // otherwise maybe loss some data
                closeQuietly(os);
                send(socket, STATUS_CODE_OK);
                return;
            }
            Position position = nextLine(data, boundaryBytes, offset);
            if (position == null) {
                os.write(data, offset, data.length - offset);
                os.flush();

                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int len = is.read(buffer, 0, DEFAULT_BUFFER_SIZE);

                data = buffer;
                offset = 0;


            } else if (position.isBoundaryLine) {

                if (position.isLastLine) {


                    exit = true;
                } else {

                    FormHeader formHeader = getFormHeader(data, position.position);
                    if (formHeader == null) {
                        send(socket, STATUS_CODE_BAD_REQUEST);
                        return;
                    }

                    offset = formHeader.start;

                    os = new BufferedOutputStream(new FileOutputStream(new File(mUploadDirectory, formHeader.fileName)));

                }

            } else {
                if (data.length - position.position > boundaryBytes.length && Utils.byteArrayHasPrefix(boundaryBytes, data, position.position)) {

                    os.write(data, offset, position.position - offset - 2);
                    os.flush();

                } else {
                    os.write(data, offset, position.position - offset);
                    os.flush();
                }
                offset = position.position;
            }
        }


//        closeQuietly(os);
//        send(socket, STATUS_CODE_OK);
    }

    private void processVideoPage(Socket socket, String videoName) {
        try {
            OutputStream os = socket.getOutputStream();
            List<String> headers = generateGenericHeader(mMimeTypes.get(".html"),
                    "public, max-age=31536000");

            byte[] header = responseHeader(STATUS_CODE_OK, headers).getBytes(UTF_8);
            os.write(header);
            os.write(mBytesIndex[0]);

            os.write(mBytesVideo[0]);
            os.write(URLEncoder.encode(videoName, UTF_8).getBytes(UTF_8));
            os.write(mBytesVideo[1]);

            os.write(mBytesIndex[1]);
            os.flush();

        } catch (Exception e) {
            e(e);
        } finally {
            closeQuietly(socket);
        }
    }

    private String responseHeader(int statusCode, List<String> headers) {

        StringBuilder sb = new StringBuilder();

        sb.append("HTTP/1.1 ")
                .append(statusCode)
                .append(' ')
                .append(getDefaultReason(statusCode))
                .append("\r\n");

        if (headers != null) {
            int len = headers.size();
            if (len != 0) {
                assert (len % 2 == 0);
                len = len - 1;
                for (int i = 0; i < len; i++) {
                    sb.append(headers.get(i)).append(": ").append(headers.get(++i)).append("\r\n");
                }
            }
        }

        sb.append("\r\n");
        return sb.toString();
    }

    private void send(Socket socket, int statusCode) {
        send(socket, statusCode, null, new byte[0]);
    }

    private void send(Socket socket, int statusCode, List<String> headers, byte[]... bytes) {

        try {
            OutputStream os = socket.getOutputStream();

            byte[] header = responseHeader(statusCode, headers).getBytes(UTF_8);
            os.write(header, 0, header.length);
            if (bytes != null) {
                for (byte[] aByte : bytes) {
                    os.write(aByte, 0, aByte.length);
                }
            }
            os.flush();
        } catch (Exception e) {
            e(e);
        } finally {
            closeQuietly(socket);
        }
    }


    private void startServer() {
        d("Start Server");
        mThread = new Thread(() -> {
            while (true) {

                try {
                    Socket socket = mServerSocket.accept();

                    mExecutorService.submit(() -> processRequest(socket));

                } catch (SocketTimeoutException ignore) {
                } catch (IOException e) {
                    e(e);
                }
            }
        });
        mThread.start();
    }

    private void video(Socket socket, InputStream is, String videoFileName, byte[] remainingBytes) {
        File videoFile = null;
        FileInputStream fis = null;
        videoFile = findVideoFile(videoFileName, mVideoFiles);
        if (videoFile == null) {
            d(videoFileName);
            notFound(socket);
            return;
        }


        try {
            d("video = " + videoFileName);
            byte[][] header = sliceHeader(is, remainingBytes);
            List<String> headers = parseHeaders(header[0]);
            long skip = 0L;
            for (int i = 0; i < headers.size(); i++) {
                if (headers.get(i).equalsIgnoreCase(HTTP_RANGE)) {
                    String bytes = substringAfter(headers.get(i + 1), "bytes=");
                    if (bytes != null) {
                        bytes = substringBefore(bytes, '-');
                        try {
                            skip = Long.parseLong(bytes);
                            d("skip: " + skip);
                        } catch (Exception e) {
                            d(e.getMessage());
                        }
                    }

                    break;
                }
            }

            List<String> responseHeaders = generateGenericHeader(
                    "video/mp4",
                    "public,max-age=31536000,public");// public, max-age=31536000

            responseHeaders.add(HTTP_ACCEPT_RANGES);
            responseHeaders.add("bytes");
            responseHeaders.add("Keep-Alive");
            responseHeaders.add("bytes");


            if (skip == 0L) {
                responseHeaders.add(HTTP_CONTENT_LENGTH);
                responseHeaders.add(Long.toString(videoFile.length()));
                writeHeaders(socket, STATUS_CODE_OK, responseHeaders);


            } else {

                responseHeaders.add(HTTP_CONTENT_LENGTH);
                responseHeaders.add(Long.toString(videoFile.length() - skip));
                responseHeaders.add(HTTP_CONTENT_RANGE);
                responseHeaders.add("bytes " + skip + "-" + (videoFile.length() - 1) + "/" + videoFile.length());
                writeHeaders(socket, STATUS_CODE_PARTIAL_CONTENT, responseHeaders);

            }
            fis = new FileInputStream(videoFile);
            fis.skip(skip);
            int len;
            OutputStream os = socket.getOutputStream();
            byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
            while ((len = fis.read(buf, 0, DEFAULT_BUFFER_SIZE)) != -1) {
                os.write(buf, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            e(e);
            // send(socket, STATUS_CODE_INTERNAL_SERVER_ERROR);
        } finally {
            closeQuietly(fis);
            closeQuietly(socket);
        }

    }

    private void writeHeaders(Socket socket, int statusCode, List<String> headers) throws IOException {
        byte[] header = responseHeader(statusCode, headers).getBytes(UTF_8);
        socket.getOutputStream().write(header);
    }

    private void writeInputStream(Socket socket, InputStream is, long skip) throws IOException {
        if (skip > 0) {
            is.skip(skip);
        }
        OutputStream os = socket.getOutputStream();

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int len;
        while ((len = is.read(buffer, 0, DEFAULT_BUFFER_SIZE)) != -1) {
            os.write(buffer, 0, len);
        }
    }


    private static void d(String message) {
        // Log.e(TAG, message);
    }

    private static void e(Exception e) {

    }

    private static void e(String e) {
        // Log.e(TAG, e);
    }


    public static class Builder {
        private final String mHost;
        private final int mPort;
        private String mStaticDirectory;
        private String mUploadDirectory;
        private String[] mVideoDirectory;

        public Builder(String host, int port) {

            mHost = host;
            mPort = port;
        }

        public SimpleServer build() {
            try {
                return new SimpleServer(this);
            } catch (IOException e) {
                return null;
            }
        }

        public Builder setStaticDirectory(String staticDirectory) {
            mStaticDirectory = staticDirectory;
            return this;
        }

        Builder setUploadDirectory(String uploadDirectory) {
            mUploadDirectory = uploadDirectory;
            return this;
        }

        public Builder setVideoDirectory(String[] videoDirectory) {
            mVideoDirectory = videoDirectory;
            return this;
        }
    }

    private static class FormHeader {
        String fileName;
        int start;
    }

    private static class Position {
        boolean isBoundaryLine;
        boolean isLastLine;
        int position;
    }

    private static class FormData {
        byte[] buffer;
        boolean hasNext;
    }
}