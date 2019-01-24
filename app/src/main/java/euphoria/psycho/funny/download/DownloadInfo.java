package euphoria.psycho.funny.download;


public class DownloadInfo {
    public long currentBytes;

    public String fileName;
    public boolean finished;

    public long id;
    public Listener listener;
    public String message;

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "currentBytes=" + currentBytes +
                ", fileName='" + fileName + '\'' +
                ", finished=" + finished +
                ", id=" + id +
                ", listener=" + listener +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", totalBytes=" + totalBytes +
                ", url='" + url + '\'' +
                ", speed=" + speed +
                '}';
    }

    public int status;
    public long totalBytes;
    public String url;
    public long speed;


    public interface Listener {
        void notifySpeed(long id, long speed);

        void onError(long id, String message);

        void onFinished(long id);

        void onStatusChanged(long id, String message);
    }
}
