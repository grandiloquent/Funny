package euphoria.psycho.funny;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

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
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import euphoria.psycho.funny.util.AndroidContext;
import euphoria.psycho.funny.util.BitmapUtils;
import euphoria.psycho.funny.util.FileUtils;

public class SimpleServer {

    private static final byte[] BYTES_DOUBLE_LINE_FEED = new byte[]{'\r', '\n', '\r', '\n'};
    private static final byte[] BYTES_LINE_FEED = new byte[]{'\r', '\n'};
    private static final String DATE_FORMAT_GMT = " EEE, dd MMM yyyy hh:mm:ss 'GMT'";
    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
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
    private static final byte[] mBytesDropZone = new byte[]{60, 100, 105, 118, 32, 105, 100, 61, 34, 100, 114, 111, 112, 122, 111, 110, 101, 34, 62, 60, 102, 111, 114, 109, 32, 99, 108, 97, 115, 115, 61, 34, 100, 114, 111, 112, 122, 111, 110, 101, 34, 32, 97, 99, 116, 105, 111, 110, 61, 34, 47, 117, 112, 108, 111, 97, 100, 34, 62, 60, 47, 102, 111, 114, 109, 62, 60, 47, 100, 105, 118, 62};
    private static final byte[][] mBytesIndex = new byte[][]{
/* 0  */new byte[]{60, 33, 68, 79, 67, 84, 89, 80, 69, 32, 104, 116, 109, 108, 62, 60, 104, 116, 109, 108, 62, 60, 104, 101, 97, 100, 62, 60, 109, 101, 116, 97, 32, 104, 116, 116, 112, 45, 101, 113, 117, 105, 118, 61, 34, 88, 45, 85, 65, 45, 67, 111, 109, 112, 97, 116, 105, 98, 108, 101, 34, 32, 99, 111, 110, 116, 101, 110, 116, 61, 34, 73, 69, 61, 101, 100, 103, 101, 34, 47, 62, 60, 109, 101, 116, 97, 32, 99, 104, 97, 114, 115, 101, 116, 61, 34, 117, 116, 102, 45, 56, 34, 47, 62, 60, 116, 105, 116, 108, 101, 62, 60, 47, 116, 105, 116, 108, 101, 62, 60, 109, 101, 116, 97, 32, 110, 97, 109, 101, 61, 34, 100, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, 34, 32, 99, 111, 110, 116, 101, 110, 116, 61, 34, 34, 47, 62, 60, 109, 101, 116, 97, 32, 110, 97, 109, 101, 61, 34, 97, 117, 116, 104, 111, 114, 34, 32, 99, 111, 110, 116, 101, 110, 116, 61, 34, 34, 47, 62, 60, 109, 101, 116, 97, 32, 110, 97, 109, 101, 61, 34, 118, 105, 101, 119, 112, 111, 114, 116, 34, 32, 99, 111, 110, 116, 101, 110, 116, 61, 34, 119, 105, 100, 116, 104, 61, 100, 101, 118, 105, 99, 101, 45, 119, 105, 100, 116, 104, 44, 32, 105, 110, 105, 116, 105, 97, 108, 45, 115, 99, 97, 108, 101, 61, 49, 34, 47, 62, 60, 108, 105, 110, 107, 32, 114, 101, 108, 61, 34, 115, 116, 121, 108, 101, 115, 104, 101, 101, 116, 34, 32, 104, 114, 101, 102, 61, 34, 100, 114, 111, 112, 122, 111, 110, 101, 46, 109, 105, 110, 46, 99, 115, 115, 34, 47, 62, 60, 108, 105, 110, 107, 32, 114, 101, 108, 61, 34, 115, 116, 121, 108, 101, 115, 104, 101, 101, 116, 34, 32, 104, 114, 101, 102, 61, 34, 109, 97, 105, 110, 46, 109, 105, 110, 46, 99, 115, 115, 34, 47, 62, 60, 33, 45, 45, 91, 105, 102, 32, 108, 116, 32, 73, 69, 32, 57, 93, 62, 60, 115, 99, 114, 105, 112, 116, 32, 115, 114, 99, 61, 34, 47, 47, 99, 100, 110, 106, 115, 46, 99, 108, 111, 117, 100, 102, 108, 97, 114, 101, 46, 99, 111, 109, 47, 97, 106, 97, 120, 47, 108, 105, 98, 115, 47, 104, 116, 109, 108, 53, 115, 104, 105, 118, 47, 51, 46, 55, 46, 50, 47, 104, 116, 109, 108, 53, 115, 104, 105, 118, 46, 109, 105, 110, 46, 106, 115, 34, 62, 60, 47, 115, 99, 114, 105, 112, 116, 62, 60, 115, 99, 114, 105, 112, 116, 32, 115, 114, 99, 61, 34, 47, 47, 99, 100, 110, 106, 115, 46, 99, 108, 111, 117, 100, 102, 108, 97, 114, 101, 46, 99, 111, 109, 47, 97, 106, 97, 120, 47, 108, 105, 98, 115, 47, 114, 101, 115, 112, 111, 110, 100, 46, 106, 115, 47, 49, 46, 52, 46, 50, 47, 114, 101, 115, 112, 111, 110, 100, 46, 109, 105, 110, 46, 106, 115, 34, 62, 60, 47, 115, 99, 114, 105, 112, 116, 62, 60, 33, 91, 101, 110, 100, 105, 102, 93, 45, 45, 62, 60, 115, 99, 114, 105, 112, 116, 32, 115, 114, 99, 61, 34, 100, 114, 111, 112, 122, 111, 110, 101, 46, 109, 105, 110, 46, 106, 115, 34, 62, 60, 47, 115, 99, 114, 105, 112, 116, 62, 60, 98, 111, 100, 121, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 99, 111, 110, 116, 97, 105, 110, 101, 114, 34, 62},
/* 1  */new byte[]{60, 47, 100, 105, 118, 62},
    };
    private static final byte[][] mBytesVideo = new byte[][]{
/* 0 src */new byte[]{60, 100, 105, 118, 32, 105, 100, 61, 34, 112, 108, 97, 121, 101, 114, 34, 32, 99, 108, 97, 115, 115, 61, 34, 112, 108, 97, 121, 101, 114, 45, 97, 112, 105, 32, 112, 108, 97, 121, 101, 114, 45, 115, 105, 122, 101, 34, 62, 60, 100, 105, 118, 32, 99, 108, 97, 115, 115, 61, 34, 104, 116, 109, 108, 53, 45, 118, 105, 100, 101, 111, 45, 99, 111, 110, 116, 97, 105, 110, 101, 114, 34, 62, 60, 118, 105, 100, 101, 111, 32, 99, 108, 97, 115, 115, 61, 34, 104, 116, 109, 108, 53, 45, 118, 105, 100, 101, 111, 45, 112, 108, 97, 121, 101, 114, 34, 32, 99, 111, 110, 116, 114, 111, 108, 115, 32, 97, 117, 116, 111, 112, 108, 97, 121, 62, 60, 115, 111, 117, 114, 99, 101, 32, 115, 114, 99, 61, 34},
/* 1  */new byte[]{34, 32, 116, 121, 112, 101, 61, 34, 118, 105, 100, 101, 111, 47, 109, 112, 52, 34, 47, 62, 60, 47, 118, 105, 100, 101, 111, 62, 60, 47, 100, 105, 118, 62, 60, 47, 100, 105, 118, 62},
    };


    private static final String[] mVideoExtensions = new String[]{
            ".mp4", ".webm"};
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

        d(mURL);
        mExecutorService = Executors.newFixedThreadPool(4);

        setupVideoFiles();
        startServer();
    }

    private boolean byteArrayHasPrefix(byte[] prefix, byte[] byteArray, int offset) {
        if (prefix == null || byteArray == null || prefix.length > byteArray.length)
            return false;
        for (int i = 0; i < prefix.length; i++) {
            if (prefix[i] != byteArray[i + offset])
                return false;
        }
        return true;
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
                    case ".css":
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

    private File findVideoFile(String videoFileName) {
        File videoFile = null;
        for (File file : mVideoFiles) {
            if (file.getName().endsWith(videoFileName)) {
                videoFile = file;
                break;
            }
        }
        return videoFile;
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
        String contentDisposition = toString(Arrays.copyOfRange(data, offset, index));
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

    private int lookup(byte[] content, byte[] pattern, int startIndex) {

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

    private List<String> parseHeaders(byte[] buffer) {
        List<String> headers = new ArrayList<>();
        int len = buffer.length;
        int offset = 0;
        boolean skip = false;
        for (int i = 0; i < len; i++) {
            if (!skip && buffer[i] == ':') {
                headers.add(toString(Arrays.copyOfRange(buffer, offset, i)));
                offset = i + 1;
                skip = true;
            }
            if (buffer[i] == '\r') {
                while (buffer[offset] == ' ') {
                    offset++;
                }
                headers.add(toString(Arrays.copyOfRange(buffer, offset, i)));
                offset = i + 2;
                skip = false;
            }
        }
        if (offset < len) {
            headers.add(toString(Arrays.copyOfRange(buffer, offset, len)));
        }
        return headers;
    }

    private List<String> parseQuery(String parameter) throws UnsupportedEncodingException {
        List<String> parameters = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean decode = false;

        // set i = 1 to skip the start character ' ?'
        for (int i = 1; i < parameter.length(); i++) {
            char ch = parameter.charAt(i);
            if (ch == '=' || ch == '&') {
                if (decode) {
                    parameters.add(URLDecoder.decode(sb.toString(), UTF_8));
                } else {
                    parameters.add(sb.toString());
                }
                decode = false;
                sb.setLength(0);
            } else {
                if (ch == '%') {
                    decode = true;
                }
                sb.append(ch);
            }
        }
        if (sb.length() > 0) {
            if (decode) {
                parameters.add(URLDecoder.decode(sb.toString(), UTF_8));
            } else {
                parameters.add(sb.toString());
            }
        }
        return parameters;
    }

    private String[] parseURL(byte[] buffer) {


        int p1 = 0, p2 = 0, p3 = 0;

        for (int i = 0; i < buffer.length; i++) {
            if (p1 == 0) {
                if (buffer[i] == '/')
                    p1 = i + 1;
            } else if (p3 == 0 && buffer[i] == ' ') {
                p3 = i;
            }
            if (p2 == 0 && buffer[i] == '?') {
                p2 = i;
            }
        }
        String method = null;
        if (p1 > 0) {
            method = toString(Arrays.copyOfRange(buffer, 0, p1 - 1)).trim();
        }
        String url = null;
        String parameter = null;
        if (p2 != 0) {

            url = toString(Arrays.copyOfRange(buffer, p1, p2));
            parameter = toString(Arrays.copyOfRange(buffer, p2, p3));
        } else {

            url = toString(Arrays.copyOfRange(buffer, p1, p3));

        }

        return new String[]{
                method,
                url,
                parameter
        };
    }

    private File png(String fileName) {
        File bitmap = new File(mStaticDirectory, fileName);
        if (bitmap.isFile()) {
            return bitmap;
        }
        File video = findVideoFile(substringBeforeLast(fileName, '.'));
        if (video == null) {
            d(fileName);
            return null;
        }
        try {
            Bitmap b = BitmapUtils.createVideoThumbnail(video.getAbsolutePath());
            FileOutputStream outputStream = new FileOutputStream(bitmap);
            b.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            FileUtils.closeSilently(outputStream);
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
            // Log.d(TAG, "[processRequest] ---> uri = " + u[1]);
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
            } else if (u[1].lastIndexOf('.') != -1) {
                if (isVideoFileName(u[1])) {
                    d(URLDecoder.decode(u[1], UTF_8));
                    video(socket, is, URLDecoder.decode(u[1], UTF_8), status[1]);
                    return;
                } else {
                    file(socket, URLDecoder.decode(u[1], UTF_8));
                }

            } else if (u[1].equals("upload")) {
                processUploadFile(socket, is, status[1]);

            } else {
                d("URI: " + u[1]);
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
                if (data.length - position.position > boundaryBytes.length && byteArrayHasPrefix(boundaryBytes, data, position.position)) {

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

    private void setupVideoFiles() {
        mVideoFiles = listFilesRecursivelyInDirectories(mVideoDirectory,
                mVideoExtensions);
    }

    private byte[][] sliceHeader(InputStream is, byte[] bytes) throws IOException {
        int bufferSize = 1024 * 8;
        int len;
        byte[] buffer = new byte[bufferSize];
        if ((len = is.read(buffer, 0, bufferSize)) != -1) {

            if (bytes != null && bytes.length > 0)
                buffer = addAll(bytes, buffer);

            int index = lookup(buffer, BYTES_DOUBLE_LINE_FEED, 0);
            if (index == -1) {
                return null;
            }
            byte[] buf1 = Arrays.copyOfRange(buffer, 0, index);
            byte[] buf2 = null;
            if (len - index > 4) {
                buf2 = Arrays.copyOfRange(buffer, index + 4, buffer.length);
            }


            return new byte[][]{
                    buf1,
                    buf2,
                    len < bufferSize ? new byte[]{0} : new byte[]{1}
            };
        }
        return null;
    }

    private byte[][] sliceURL(InputStream is) throws IOException {


        int bufferSize = 256;
        byte[] buffer = new byte[bufferSize];
        int len;
        if ((len = is.read(buffer, 0, bufferSize)) != -1) {

            int index = lookup(buffer, BYTES_LINE_FEED, 0);
            if (index == -1) {
                return null;
            }
            String x = toString(buffer);

            byte[] buf1 = Arrays.copyOfRange(buffer, 0, index);
            byte[] buf2 = null;
            if (len - index > 2) {
                buf2 = Arrays.copyOfRange(buffer, index + 2, len);
            }
            return new byte[][]{
                    buf1,
                    buf2
            };
        }
        return null;
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
        videoFile = findVideoFile(videoFileName);
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

    private static byte[] addAll(final byte[] array1, final byte... array2) {
        if (array1 == null) {
            return array2.clone();
        } else if (array2 == null) {
            return array1.clone();
        }
        final byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {

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

    private static String getDefaultReason(int statusCode) {
        switch (statusCode) {
            case 100:
                return "Continue";
            case 101:
                return "Switching Protocols";
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 202:
                return "Accepted";
            case 203:
                return "Non-Authoritative Information";
            case 204:
                return "No Content";
            case 205:
                return "Reset Content";
            case 206:
                return "Partial Content";
            case 300:
                return "Multiple Choices";
            case 301:
                return "Moved Permanently";
            case 302:
                return "Found";
            case 303:
                return "See Other";
            case 304:
                return "Not Modified";
            case 305:
                return "Use Proxy";
            case 307:
                return "Temporary Redirect";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 402:
                return "Payment Required";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 405:
                return "Method Not Allowed";
            case 406:
                return "Not Acceptable";
            case 407:
                return "Proxy Authentication Required";
            case 408:
                return "Request Time-out";
            case 409:
                return "Conflict";
            case 410:
                return "Gone";
            case 411:
                return "Length Required";
            case 412:
                return "Precondition Failed";
            case 413:
                return "Request Entity Too Large";
            case 414:
                return "Request-URI Too Large";
            case 415:
                return "Unsupported Media Type";
            case 416:
                return "Requested range not satisfiable";
            case 417:
                return "Expectation Failed";
            case 500:
                return "Internal Server Error";
            case 501:
                return "Not Implemented";
            case 502:
                return "Bad Gateway";
            case 503:
                return "Service Unavailable";
            case 504:
                return "Gateway Time-out";
            case 505:
                return "HTTP Version not supported";
            default: {
                int errorClass = statusCode / 100;
                switch (errorClass) {
                    case 1:
                        return "Informational";
                    case 2:
                        return "Success";
                    case 3:
                        return "Redirection";
                    case 4:
                        return "Client Error";
                    case 5:
                        return "Server Error";
                    default:
                        return null;
                }
            }
        }
    }

    private static String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index != -1)
            return fileName.substring(index);
        else
            return null;
    }

    private static Hashtable<String, String> getMimeTypeTable() {
        Hashtable<String, String> hashtable = new Hashtable<>();

        hashtable.put(".323", "text/h323");
        hashtable.put(".aaf", "application/octet-stream");
        hashtable.put(".aca", "application/octet-stream");
        hashtable.put(".accdb", "application/msaccess");
        hashtable.put(".accde", "application/msaccess");
        hashtable.put(".accdt", "application/msaccess");
        hashtable.put(".acx", "application/internet-property-stream");
        hashtable.put(".afm", "application/octet-stream");
        hashtable.put(".ai", "application/postscript");
        hashtable.put(".aif", "audio/x-aiff");
        hashtable.put(".aifc", "audio/aiff");
        hashtable.put(".aiff", "audio/aiff");
        hashtable.put(".application", "application/x-ms-application");
        hashtable.put(".art", "image/x-jg");
        hashtable.put(".asd", "application/octet-stream");
        hashtable.put(".asf", "video/x-ms-asf");
        hashtable.put(".asi", "application/octet-stream");
        hashtable.put(".asm", "text/plain");
        hashtable.put(".asr", "video/x-ms-asf");
        hashtable.put(".asx", "video/x-ms-asf");
        hashtable.put(".atom", "application/atom+xml");
        hashtable.put(".au", "audio/basic");
        hashtable.put(".avi", "video/x-msvideo");
        hashtable.put(".axs", "application/olescript");
        hashtable.put(".bas", "text/plain");
        hashtable.put(".bcpio", "application/x-bcpio");
        hashtable.put(".bin", "application/octet-stream");
        hashtable.put(".bmp", "image/bmp");
        hashtable.put(".c", "text/plain");
        hashtable.put(".cab", "application/octet-stream");
        hashtable.put(".calx", "application/vnd.ms-office.calx");
        hashtable.put(".cat", "application/vnd.ms-pki.seccat");
        hashtable.put(".cdf", "application/x-cdf");
        hashtable.put(".chm", "application/octet-stream");
        hashtable.put(".class", "application/x-java-applet");
        hashtable.put(".clp", "application/x-msclip");
        hashtable.put(".cmx", "image/x-cmx");
        hashtable.put(".cnf", "text/plain");
        hashtable.put(".cod", "image/cis-cod");
        hashtable.put(".cpio", "application/x-cpio");
        hashtable.put(".cpp", "text/plain");
        hashtable.put(".crd", "application/x-mscardfile");
        hashtable.put(".crl", "application/pkix-crl");
        hashtable.put(".crt", "application/x-x509-ca-cert");
        hashtable.put(".csh", "application/x-csh");
        hashtable.put(".css", "text/css");
        hashtable.put(".csv", "application/octet-stream");
        hashtable.put(".cur", "application/octet-stream");
        hashtable.put(".dcr", "application/x-director");
        hashtable.put(".deploy", "application/octet-stream");
        hashtable.put(".der", "application/x-x509-ca-cert");
        hashtable.put(".dib", "image/bmp");
        hashtable.put(".dir", "application/x-director");
        hashtable.put(".disco", "text/xml");
        hashtable.put(".dll", "application/x-msdownload");
        hashtable.put(".dll.config", "text/xml");
        hashtable.put(".dlm", "text/dlm");
        hashtable.put(".doc", "application/msword");
        hashtable.put(".docm", "application/vnd.ms-word.document.macroEnabled.12");
        hashtable.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        hashtable.put(".dot", "application/msword");
        hashtable.put(".dotm", "application/vnd.ms-word.template.macroEnabled.12");
        hashtable.put(".dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        hashtable.put(".dsp", "application/octet-stream");
        hashtable.put(".dtd", "text/xml");
        hashtable.put(".dvi", "application/x-dvi");
        hashtable.put(".dwf", "drawing/x-dwf");
        hashtable.put(".dwp", "application/octet-stream");
        hashtable.put(".dxr", "application/x-director");
        hashtable.put(".eml", "message/rfc822");
        hashtable.put(".emz", "application/octet-stream");
        hashtable.put(".eot", "application/octet-stream");
        hashtable.put(".eps", "application/postscript");
        hashtable.put(".etx", "text/x-setext");
        hashtable.put(".evy", "application/envoy");
        hashtable.put(".exe", "application/octet-stream");
        hashtable.put(".exe.config", "text/xml");
        hashtable.put(".fdf", "application/vnd.fdf");
        hashtable.put(".fif", "application/fractals");
        hashtable.put(".fla", "application/octet-stream");
        hashtable.put(".flr", "x-world/x-vrml");
        hashtable.put(".flv", "video/x-flv");
        hashtable.put(".gif", "image/gif");
        hashtable.put(".gtar", "application/x-gtar");
        hashtable.put(".gz", "application/x-gzip");
        hashtable.put(".h", "text/plain");
        hashtable.put(".hdf", "application/x-hdf");
        hashtable.put(".hdml", "text/x-hdml");
        hashtable.put(".hhc", "application/x-oleobject");
        hashtable.put(".hhk", "application/octet-stream");
        hashtable.put(".hhp", "application/octet-stream");
        hashtable.put(".hlp", "application/winhlp");
        hashtable.put(".hqx", "application/mac-binhex40");
        hashtable.put(".hta", "application/hta");
        hashtable.put(".htc", "text/x-component");
        hashtable.put(".htm", "text/html");
        hashtable.put(".html", "text/html");
        hashtable.put(".htt", "text/webviewhtml");
        hashtable.put(".hxt", "text/html");
        hashtable.put(".ico", "image/x-icon");
        hashtable.put(".ics", "application/octet-stream");
        hashtable.put(".ief", "image/ief");
        hashtable.put(".iii", "application/x-iphone");
        hashtable.put(".inf", "application/octet-stream");
        hashtable.put(".ins", "application/x-internet-signup");
        hashtable.put(".isp", "application/x-internet-signup");
        hashtable.put(".IVF", "video/x-ivf");
        hashtable.put(".jar", "application/java-archive");
        hashtable.put(".java", "application/octet-stream");
        hashtable.put(".jck", "application/liquidmotion");
        hashtable.put(".jcz", "application/liquidmotion");
        hashtable.put(".jfif", "image/pjpeg");
        hashtable.put(".jpb", "application/octet-stream");
        hashtable.put(".jpe", "image/jpeg");
        hashtable.put(".jpeg", "image/jpeg");
        hashtable.put(".jpg", "image/jpeg");
        hashtable.put(".js", "application/x-javascript");
        hashtable.put(".jsx", "text/jscript");
        hashtable.put(".latex", "application/x-latex");
        hashtable.put(".lit", "application/x-ms-reader");
        hashtable.put(".lpk", "application/octet-stream");
        hashtable.put(".lsf", "video/x-la-asf");
        hashtable.put(".lsx", "video/x-la-asf");
        hashtable.put(".lzh", "application/octet-stream");
        hashtable.put(".m13", "application/x-msmediaview");
        hashtable.put(".m14", "application/x-msmediaview");
        hashtable.put(".m1v", "video/mpeg");
        hashtable.put(".m3u", "audio/x-mpegurl");
        hashtable.put(".man", "application/x-troff-man");
        hashtable.put(".manifest", "application/x-ms-manifest");
        hashtable.put(".map", "text/plain");
        hashtable.put(".mdb", "application/x-msaccess");
        hashtable.put(".mdp", "application/octet-stream");
        hashtable.put(".me", "application/x-troff-me");
        hashtable.put(".mht", "message/rfc822");
        hashtable.put(".mhtml", "message/rfc822");
        hashtable.put(".mid", "audio/mid");
        hashtable.put(".midi", "audio/mid");
        hashtable.put(".mix", "application/octet-stream");
        hashtable.put(".mmf", "application/x-smaf");
        hashtable.put(".mno", "text/xml");
        hashtable.put(".mny", "application/x-msmoney");
        hashtable.put(".mov", "video/quicktime");
        hashtable.put(".movie", "video/x-sgi-movie");
        hashtable.put(".mp2", "video/mpeg");
        hashtable.put(".mp3", "audio/mpeg");
        hashtable.put(".mp4", "video/mp4");

        hashtable.put(".mpa", "video/mpeg");
        hashtable.put(".mpe", "video/mpeg");
        hashtable.put(".mpeg", "video/mpeg");
        hashtable.put(".mpg", "video/mpeg");
        hashtable.put(".mpp", "application/vnd.ms-project");
        hashtable.put(".mpv2", "video/mpeg");
        hashtable.put(".ms", "application/x-troff-ms");
        hashtable.put(".msi", "application/octet-stream");
        hashtable.put(".mso", "application/octet-stream");
        hashtable.put(".mvb", "application/x-msmediaview");
        hashtable.put(".mvc", "application/x-miva-compiled");
        hashtable.put(".nc", "application/x-netcdf");
        hashtable.put(".nsc", "video/x-ms-asf");
        hashtable.put(".nws", "message/rfc822");
        hashtable.put(".ocx", "application/octet-stream");
        hashtable.put(".oda", "application/oda");
        hashtable.put(".odc", "text/x-ms-odc");
        hashtable.put(".ods", "application/oleobject");
        hashtable.put(".one", "application/onenote");
        hashtable.put(".onea", "application/onenote");
        hashtable.put(".onetoc", "application/onenote");
        hashtable.put(".onetoc2", "application/onenote");
        hashtable.put(".onetmp", "application/onenote");
        hashtable.put(".onepkg", "application/onenote");
        hashtable.put(".osdx", "application/opensearchdescription+xml");
        hashtable.put(".p10", "application/pkcs10");
        hashtable.put(".p12", "application/x-pkcs12");
        hashtable.put(".p7b", "application/x-pkcs7-certificates");
        hashtable.put(".p7c", "application/pkcs7-mime");
        hashtable.put(".p7m", "application/pkcs7-mime");
        hashtable.put(".p7r", "application/x-pkcs7-certreqresp");
        hashtable.put(".p7s", "application/pkcs7-signature");
        hashtable.put(".pbm", "image/x-portable-bitmap");
        hashtable.put(".pcx", "application/octet-stream");
        hashtable.put(".pcz", "application/octet-stream");
        hashtable.put(".pdf", "application/pdf");
        hashtable.put(".pfb", "application/octet-stream");
        hashtable.put(".pfm", "application/octet-stream");
        hashtable.put(".pfx", "application/x-pkcs12");
        hashtable.put(".pgm", "image/x-portable-graymap");
        hashtable.put(".pko", "application/vnd.ms-pki.pko");
        hashtable.put(".pma", "application/x-perfmon");
        hashtable.put(".pmc", "application/x-perfmon");
        hashtable.put(".pml", "application/x-perfmon");
        hashtable.put(".pmr", "application/x-perfmon");
        hashtable.put(".pmw", "application/x-perfmon");
        hashtable.put(".png", "image/png");
        hashtable.put(".pnm", "image/x-portable-anymap");
        hashtable.put(".pnz", "image/png");
        hashtable.put(".pot", "application/vnd.ms-powerpoint");
        hashtable.put(".potm", "application/vnd.ms-powerpoint.template.macroEnabled.12");
        hashtable.put(".potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        hashtable.put(".ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12");
        hashtable.put(".ppm", "image/x-portable-pixmap");
        hashtable.put(".pps", "application/vnd.ms-powerpoint");
        hashtable.put(".ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
        hashtable.put(".ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        hashtable.put(".ppt", "application/vnd.ms-powerpoint");
        hashtable.put(".pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
        hashtable.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        hashtable.put(".prf", "application/pics-rules");
        hashtable.put(".prm", "application/octet-stream");
        hashtable.put(".prx", "application/octet-stream");
        hashtable.put(".ps", "application/postscript");
        hashtable.put(".psd", "application/octet-stream");
        hashtable.put(".psm", "application/octet-stream");
        hashtable.put(".psp", "application/octet-stream");
        hashtable.put(".pub", "application/x-mspublisher");
        hashtable.put(".qt", "video/quicktime");
        hashtable.put(".qtl", "application/x-quicktimeplayer");
        hashtable.put(".qxd", "application/octet-stream");
        hashtable.put(".ra", "audio/x-pn-realaudio");
        hashtable.put(".ram", "audio/x-pn-realaudio");
        hashtable.put(".rar", "application/octet-stream");
        hashtable.put(".ras", "image/x-cmu-raster");
        hashtable.put(".rf", "image/vnd.rn-realflash");
        hashtable.put(".rgb", "image/x-rgb");
        hashtable.put(".rm", "application/vnd.rn-realmedia");
        hashtable.put(".rmi", "audio/mid");
        hashtable.put(".roff", "application/x-troff");
        hashtable.put(".rpm", "audio/x-pn-realaudio-plugin");
        hashtable.put(".rtf", "application/rtf");
        hashtable.put(".rtx", "text/richtext");
        hashtable.put(".scd", "application/x-msschedule");
        hashtable.put(".sct", "text/scriptlet");
        hashtable.put(".sea", "application/octet-stream");
        hashtable.put(".setpay", "application/set-payment-initiation");
        hashtable.put(".setreg", "application/set-registration-initiation");
        hashtable.put(".sgml", "text/sgml");
        hashtable.put(".sh", "application/x-sh");
        hashtable.put(".shar", "application/x-shar");
        hashtable.put(".sit", "application/x-stuffit");
        hashtable.put(".sldm", "application/vnd.ms-powerpoint.slide.macroEnabled.12");
        hashtable.put(".sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
        hashtable.put(".smd", "audio/x-smd");
        hashtable.put(".smi", "application/octet-stream");
        hashtable.put(".smx", "audio/x-smd");
        hashtable.put(".smz", "audio/x-smd");
        hashtable.put(".snd", "audio/basic");
        hashtable.put(".snp", "application/octet-stream");
        hashtable.put(".spc", "application/x-pkcs7-certificates");
        hashtable.put(".spl", "application/futuresplash");
        hashtable.put(".src", "application/x-wais-source");
        hashtable.put(".ssm", "application/streamingmedia");
        hashtable.put(".sst", "application/vnd.ms-pki.certstore");
        hashtable.put(".stl", "application/vnd.ms-pki.stl");
        hashtable.put(".sv4cpio", "application/x-sv4cpio");
        hashtable.put(".sv4crc", "application/x-sv4crc");
        hashtable.put(".swf", "application/x-shockwave-flash");
        hashtable.put(".t", "application/x-troff");
        hashtable.put(".tar", "application/x-tar");
        hashtable.put(".tcl", "application/x-tcl");
        hashtable.put(".tex", "application/x-tex");
        hashtable.put(".texi", "application/x-texinfo");
        hashtable.put(".texinfo", "application/x-texinfo");
        hashtable.put(".tgz", "application/x-compressed");
        hashtable.put(".thmx", "application/vnd.ms-officetheme");
        hashtable.put(".thn", "application/octet-stream");
        hashtable.put(".tif", "image/tiff");
        hashtable.put(".tiff", "image/tiff");
        hashtable.put(".toc", "application/octet-stream");
        hashtable.put(".tr", "application/x-troff");
        hashtable.put(".trm", "application/x-msterminal");
        hashtable.put(".ts", "video/MP2T");

        hashtable.put(".tsv", "text/tab-separated-values");
        hashtable.put(".ttf", "application/octet-stream");
        hashtable.put(".txt", "text/plain");
        hashtable.put(".u32", "application/octet-stream");
        hashtable.put(".uls", "text/iuls");
        hashtable.put(".ustar", "application/x-ustar");
        hashtable.put(".vbs", "text/vbscript");
        hashtable.put(".vcf", "text/x-vcard");
        hashtable.put(".vcs", "text/plain");
        hashtable.put(".vdx", "application/vnd.ms-visio.viewer");
        hashtable.put(".vml", "text/xml");
        hashtable.put(".vsd", "application/vnd.visio");
        hashtable.put(".vss", "application/vnd.visio");
        hashtable.put(".vst", "application/vnd.visio");
        hashtable.put(".vsto", "application/x-ms-vsto");
        hashtable.put(".vsw", "application/vnd.visio");
        hashtable.put(".vsx", "application/vnd.visio");
        hashtable.put(".vtx", "application/vnd.visio");
        hashtable.put(".wav", "audio/wav");
        hashtable.put(".wax", "audio/x-ms-wax");
        hashtable.put(".wbmp", "image/vnd.wap.wbmp");
        hashtable.put(".wcm", "application/vnd.ms-works");
        hashtable.put(".wdb", "application/vnd.ms-works");
        hashtable.put(".wks", "application/vnd.ms-works");
        hashtable.put(".wm", "video/x-ms-wm");
        hashtable.put(".wma", "audio/x-ms-wma");
        hashtable.put(".wmd", "application/x-ms-wmd");
        hashtable.put(".wmf", "application/x-msmetafile");
        hashtable.put(".wml", "text/vnd.wap.wml");
        hashtable.put(".wmlc", "application/vnd.wap.wmlc");
        hashtable.put(".wmls", "text/vnd.wap.wmlscript");
        hashtable.put(".wmlsc", "application/vnd.wap.wmlscriptc");
        hashtable.put(".wmp", "video/x-ms-wmp");
        hashtable.put(".wmv", "video/x-ms-wmv");
        hashtable.put(".wmx", "video/x-ms-wmx");
        hashtable.put(".wmz", "application/x-ms-wmz");
        hashtable.put(".wps", "application/vnd.ms-works");
        hashtable.put(".wri", "application/x-mswrite");
        hashtable.put(".wrl", "x-world/x-vrml");
        hashtable.put(".wrz", "x-world/x-vrml");
        hashtable.put(".wsdl", "text/xml");
        hashtable.put(".wvx", "video/x-ms-wvx");
        hashtable.put(".x", "application/directx");
        hashtable.put(".xaf", "x-world/x-vrml");
        hashtable.put(".xaml", "application/xaml+xml");
        hashtable.put(".xap", "application/x-silverlight-app");
        hashtable.put(".xbap", "application/x-ms-xbap");
        hashtable.put(".xbm", "image/x-xbitmap");
        hashtable.put(".xdr", "text/plain");
        hashtable.put(".xla", "application/vnd.ms-excel");
        hashtable.put(".xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
        hashtable.put(".xlc", "application/vnd.ms-excel");
        hashtable.put(".xlm", "application/vnd.ms-excel");
        hashtable.put(".xls", "application/vnd.ms-excel");
        hashtable.put(".xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        hashtable.put(".xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12");
        hashtable.put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        hashtable.put(".xlt", "application/vnd.ms-excel");
        hashtable.put(".xltm", "application/vnd.ms-excel.template.macroEnabled.12");
        hashtable.put(".xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        hashtable.put(".xlw", "application/vnd.ms-excel");
        hashtable.put(".xml", "text/xml");
        hashtable.put(".xof", "x-world/x-vrml");
        hashtable.put(".xpm", "image/x-xpixmap");
        hashtable.put(".xps", "application/vnd.ms-xpsdocument");
        hashtable.put(".xsd", "text/xml");
        hashtable.put(".xsf", "text/xml");
        hashtable.put(".xsl", "text/xml");
        hashtable.put(".xslt", "text/xml");
        hashtable.put(".xsn", "application/octet-stream");
        hashtable.put(".xtp", "application/octet-stream");
        hashtable.put(".xwd", "image/x-xwindowdump");
        hashtable.put(".z", "application/x-compress");
        hashtable.put(".zip", "application/x-zip-compressed");

        return hashtable;
    }

    private static boolean isVideoFileName(String name) {
        int index = name.lastIndexOf('.');
        if (index == -1) return false;
        String extension = name.substring(index);
        for (String e : mVideoExtensions) {
            if (e.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }


    private static List<File> listFilesRecursively(String dir, String[] extensions) {
        List<File> allFiles = new ArrayList<File>();
        Queue<File> dirs = new LinkedList<File>();
        dirs.add(new File(dir));
        while (!dirs.isEmpty()) {
            for (File f : dirs.poll().listFiles()) {
                if (f.isDirectory()) {
                    dirs.add(f);
                } else if (f.isFile()) {
                    int index = f.getName().lastIndexOf('.');
                    if (index != -1) {
                        String ext = f.getName().substring(index);
                        for (String e : extensions) {
                            if (e.equalsIgnoreCase(ext)) {
                                allFiles.add(f);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return allFiles;
    }

    private static List<File> listFilesRecursivelyInDirectories(String[] directories, String[] extensions) {
        List<File> files = null;
        for (String dir : directories) {
            if (files == null) {
                files = listFilesRecursively(dir, extensions);
            } else {
                files.addAll(listFilesRecursively(dir, extensions));
            }
        }
        return files;
    }

    private static String substringAfter(String s, String delimiter) {
        int index = s.indexOf(delimiter);
        if (index == -1) return null;
        else return s.substring(index + delimiter.length());
    }

    private static String substringAfterLast(String s, char delimiter) {
        int index = s.lastIndexOf(delimiter);
        if (index == -1) return null;
        else return s.substring(index);
    }

    private static String substringBefore(String s, char delimiter) {
        int index = s.indexOf(delimiter);
        if (index == -1) return null;
        else return s.substring(0, index);
    }

    private static String substringBeforeLast(String s, char delimiter) {
        int index = s.lastIndexOf(delimiter);
        if (index == -1) return null;
        else return s.substring(0, index);
    }

    private static String toString(byte[] buffer) {
        try {
            return new String(buffer, UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String trim(String s, char[] chars) {
        int startIndex = 0;
        int endIndex = s.length() - 1;
        boolean startFound = false;

        while (startIndex <= endIndex) {
            int index = (!startFound) ? startIndex : endIndex;
            boolean match = false;
            for (char c : chars) {
                if (c == s.charAt(index)) {
                    match = true;
                    break;
                }
            }

            if (!startFound) {
                if (!match)
                    startFound = true;
                else
                    startIndex += 1;
            } else {
                if (!match)
                    break;
                else
                    endIndex -= 1;
            }
        }

        return s.substring(startIndex, endIndex + 1);
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