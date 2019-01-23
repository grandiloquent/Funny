package euphoria.psycho.funny.downloader;

public class DownloaderItem {
    private String mTitle;
    private int mProgress;
    private int mSoFarBytes;
    private int mTotalBytes;
    private int mStatus;
    private int mSpeed;

    public int getSpeed() {
        return mSpeed;
    }

    public int getStatus() {
        return mStatus;
    }

    public int getTotalBytes() {
        return mTotalBytes;
    }

    public int getSoFarBytes() {
        return mSoFarBytes;
    }

    public int getProgress() {
        return mProgress;
    }

    public String getTitle() {
        return mTitle;
    }
}
