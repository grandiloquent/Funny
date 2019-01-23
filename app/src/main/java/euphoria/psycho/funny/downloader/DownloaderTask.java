package euphoria.psycho.funny.downloader;

public class DownloaderTask {
    private long mId;
    private int mSpeed;
    private int mStatus;
    private int mSmallFileSoFarBytes;
    private int mSmallFileTotalBytes;

    public long getId() {
        return mId;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public int getSmallFileSoFarBytes() {
        return mSmallFileSoFarBytes;
    }

    public int getSmallFileTotalBytes() {
        return mSmallFileTotalBytes;
    }

    public int getStatus() {
        return mStatus;
    }
}
