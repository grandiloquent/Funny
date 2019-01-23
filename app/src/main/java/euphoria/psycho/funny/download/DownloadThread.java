package euphoria.psycho.funny.download;

import android.content.Context;
import android.os.SystemClock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import euphoria.psycho.funny.DownloadInfoDatabase;
import euphoria.psycho.funny.RequestException;
import euphoria.psycho.funny.util.AndroidContext;
import euphoria.psycho.funny.util.FileUtils;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_GONE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;


public class DownloadThread extends Thread {
    private static final boolean DEBUG = true;
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int DEFAULT_DELAY_TIME = 5 * 1000;
    private static final int DEFAULT_RETRIES = 20;
    private static final String HTTP_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String HTTP_CONNECTION = "Connection";
    private static final String HTTP_CONTENT_LENGTH = "Content-Length";
    private static final String HTTP_RANGE = "Range";
    private static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    private static final String HTTP_TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String HTTP_USER_AGENT = "User-Agent";
    private static final int MIN_PROGRESS_STEP = 65536;
    private static final long MIN_PROGRESS_TIME = 2000;
    private static final int STATUS_FATAL = 0;
    private static final int STATUS_FILE_ERROR = 1;
    private static final int STATUS_HTTP_DATA_ERROR = 2;
    private static final int STATUS_TIMEOUT = 3;
    private static final int STATUS_UNCERTAIN = 4;
    private final Context mContext;
    private final DownloadInfo mInfo;
    private final long mTaskId;
    private volatile boolean mIsStop = false;
    private long mLastUpdateBytes = 0;
    private long mLastUpdateTime = 0;
    private long mSpeed;
    private long mSpeedSampleBytes = 0L;
    private long mSpeedSampleStart = 0L;
    private int mTimeout = 20 * 1000;


    public DownloadThread(DownloadInfo info, Context context) {
        mInfo = info;
        mTaskId = info.id;
        mContext = context;
    }

    private void addRequestHeaders(HttpsURLConnection c) {
        // https://docs.microsoft.com/en-us/windows/desktop/wininet/about-wininet
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers

        // Accept
        // Accept-Charset
        // Accept-Encoding
        // Accept-Language
        // Accept-Ranges
        // Access-Control-Allow-Credentials
        // Access-Control-Allow-Headers
        // Access-Control-Allow-Methods
        // Access-Control-Allow-Origin
        // Access-Control-Expose-Headers
        // Access-Control-Max-Age
        // Access-Control-Request-Headers
        // Access-Control-Request-Method
        // Age
        // Allow
        // Alt-Svc
        // Authorization
        // Cache-Control
        // Clear-Site-Data
        // Connection
        // Content-Disposition
        // Content-Encoding
        // Content-Language
        // Content-Length
        // Content-Location
        // Content-Range
        // Content-Security-Policy
        // Content-Security-Policy-Report-Only
        // Content-Type
        // Cookie
        // Cookie2
        // DNT
        // Date
        // ETag
        // Early-Data
        // Expect
        // Expect-CT
        // Expires
        // Feature-Policy
        // Forwarded
        // From
        // Host
        // If-Match
        // If-Modified-Since
        // If-None-Match
        // If-Range
        // If-Unmodified-Since
        // Index
        // Keep-Alive
        // Large-Allocation
        // Last-Modified
        // Location
        // Origin
        // Pragma
        // Proxy-Authenticate
        // Proxy-Authorization
        // Public-Key-Pins
        // Public-Key-Pins-Report-Only
        // Range
        // Referer
        // Referrer-Policy
        // Retry-After
        // Sec-WebSocket-Accept
        // Server
        // Server-Timing
        // Set-Cookie
        // Set-Cookie2
        // SourceMap
        // Strict-Transport-Security
        // TE
        // Timing-Allow-Origin
        // Tk
        // Trailer
        // Transfer-Encoding
        // Upgrade-Insecure-Requests
        // User-Agent
        // Vary
        // Via
        // WWW-Authenticate
        // Warning
        // X-Content-Type-Options
        // X-DNS-Prefetch-Control
        // X-Forwarded-For
        // X-Forwarded-Host
        // X-Forwarded-Proto
        // X-Frame-Options
        // X-XSS-Protection

        // identity
        // Indicates the identity function (i.e. no compression,
        // nor modification). This value is always considered
        // as acceptable, even if not present.

        c.addRequestProperty(HTTP_ACCEPT_ENCODING, "identity");
        // close
        // Indicates that either the client or the server would
        // like to close the connection. This is the default on
        // HTTP/1.0 requests.

        c.addRequestProperty(HTTP_CONNECTION, "close");
        c.addRequestProperty(HTTP_USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.92 Safari/537.36");
        File file = new File(mInfo.fileName);
        if (file.exists()) {
            mInfo.currentBytes = file.length();
            c.addRequestProperty(HTTP_RANGE, "bytes=" + mInfo.currentBytes + "-");
        } else {
            mInfo.currentBytes = 0L;
        }
    }

    private void executeDownload() throws RequestException {

        URL url;

        try {
            url = new URL(mInfo.url);
        } catch (MalformedURLException e) {
            throw new RequestException(STATUS_FATAL, e);
        }
        SSLContext appContext = null;
        try {
            appContext = SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // https://developer.android.com/reference/java/net/HttpURLConnection.html


        int tries = 0;
        while (tries++ < DEFAULT_RETRIES) {
            mInfo.listener.onStatusChanged(mTaskId, "第 " + tries + " 尝试下载 " + FileUtils.getFileName(mInfo.fileName));
            HttpsURLConnection c = null;

            try {
                if (mIsStop) {
                    throw new RequestException(STATUS_FATAL, "User terminates current task");
                }
                c = (HttpsURLConnection) url.openConnection();


                c.setInstanceFollowRedirects(false);


                c.setConnectTimeout(mTimeout);


                c.setReadTimeout(mTimeout);
                addRequestHeaders(c);
                if (c instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) c).setSSLSocketFactory(appContext.getSocketFactory());
                }
                int rc = c.getResponseCode();

                switch (rc) {
                    case HTTP_OK:
                    case HTTP_PARTIAL: {
                        parseOkHeaders(c);
                        transferData(c);
                        mInfo.finished = true;
                        writeToDatabase();
                        mInfo.listener.onFinished(mInfo.id);
                        return;
                    }
                    case HTTP_FORBIDDEN: {
                        SystemClock.sleep(DEFAULT_DELAY_TIME);
                        continue;
                    }
                    case HTTP_GONE: {
                        throw new RequestException(STATUS_FATAL, "The server rejected the current request");
                    }
                    case HTTP_REQUESTED_RANGE_NOT_SATISFIABLE: {
                        throw new RequestException(STATUS_FATAL, "Range Not Satisfiable. It is possible that the file has been downloaded but the task is not marked as completed.");
                    }
                    default: {
                        mInfo.listener.onStatusChanged(mTaskId, "发生未捕获错误。 状态码: " + rc);
                        throw new RequestException(STATUS_HTTP_DATA_ERROR, "ResponseCode: " + rc);
                    }

                }
            } catch (RequestException e) {
                if (e.getFinalStatus() == STATUS_FATAL) {
                    throw e;
                }
            } catch (SocketTimeoutException e) {
                SystemClock.sleep(DEFAULT_DELAY_TIME);

            } catch (IOException e) {
                SystemClock.sleep(DEFAULT_DELAY_TIME);

            } finally {
                if (c != null) c.disconnect();
            }
        }

        throw new RequestException(STATUS_UNCERTAIN, "Too many retries");
    }

    public long getTaskId() {
        return mTaskId;
    }

    private void parseOkHeaders(HttpsURLConnection c) {

        String e = c.getHeaderField(HTTP_TRANSFER_ENCODING);

        if (e == null) {
            try {

                // 如果目标文件已存在
                // 累加其大小

                mInfo.totalBytes = Long.parseLong(c.getHeaderField(HTTP_CONTENT_LENGTH));
                File file = new File(mInfo.fileName);
                if (file.exists()) {
                    mInfo.totalBytes += file.length();
                }


            } catch (Exception error) {
                mInfo.totalBytes = -1L;
            }
        }

    }

    public void stopDownload() {
        mIsStop = true;
    }

    private void transferData(HttpURLConnection c) throws RequestException {
        InputStream in = null;
        RandomAccessFile out = null;
        try {
            try {
                in = c.getInputStream();
            } catch (IOException e) {
                throw new RequestException(STATUS_HTTP_DATA_ERROR, e);
            }

            try {
                out = new RandomAccessFile(mInfo.fileName, "rwd");
                if (mInfo.currentBytes > 0)
                    out.seek(mInfo.currentBytes);
            } catch (IOException e) {
                throw new RequestException(STATUS_FILE_ERROR, e);
            }
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE >> 1];

            while (true) {
                if (mIsStop) {
                    throw new RequestException(STATUS_HTTP_DATA_ERROR, "Local halt requested; job probably timed out");
                }
                int len = -1;
                try {
                    len = in.read(buffer);
                } catch (IOException e) {
                    throw new RequestException(STATUS_HTTP_DATA_ERROR, e);
                }
                if (len == -1) {
                    break;
                }

                try {
                    out.write(buffer, 0, len);
                    mInfo.currentBytes += len;
                    updateProgress();
                } catch (IOException e) {
                    throw new RequestException(STATUS_UNCERTAIN, e);
                }
            }

        } catch (Exception e) {
            throw new RequestException(STATUS_HTTP_DATA_ERROR, e);
        } finally {
            if (in != null) FileUtils.closeSilently(in);
            if (out != null) FileUtils.closeSilently(out);
        }
    }

    private void updateProgress() {
        final long now = SystemClock.elapsedRealtime();
        final long currentBytes = mInfo.currentBytes;
        final long sampleDelta = now - mSpeedSampleStart;
        if (sampleDelta > 500) {
            final long sampleSpeed = ((currentBytes - mSpeedSampleBytes) * 1000)
                    / sampleDelta;
            if (mSpeed == 0) {
                mSpeed = sampleSpeed;
            } else {
                mSpeed = ((mSpeed * 3) + sampleSpeed) / 4;
            }

            if (mSpeedSampleStart != 0) {
                mInfo.listener.notifySpeed(mTaskId, mSpeed);
            }
            mSpeedSampleStart = now;
            mSpeedSampleBytes = currentBytes;
        }
        final long bytesDelta = currentBytes - mLastUpdateBytes;
        final long timeDelta = now - mLastUpdateTime;
        if (bytesDelta > MIN_PROGRESS_STEP && timeDelta > MIN_PROGRESS_TIME) {
            writeToDatabase();
            mLastUpdateBytes = currentBytes;
            mLastUpdateTime = now;
        }
    }

    private void writeToDatabase() {
        DownloadInfoDatabase.getInstance(AndroidContext.instance().get()).updateTask(mInfo);
    }

    @Override
    public void run() {
        try {
            mInfo.listener.onStatusChanged(mTaskId, "开始下载: " + FileUtils.getFileName(mInfo.fileName));
            executeDownload();
        } catch (RequestException e) {


            int s = e.getFinalStatus();

            if (s == STATUS_FATAL) {
                mInfo.finished = true;

            }
            mInfo.status = s;
            mInfo.message = e.getMessage();
            writeToDatabase();
            mInfo.listener.onError(mTaskId, e.getMessage());
        }
    }
}