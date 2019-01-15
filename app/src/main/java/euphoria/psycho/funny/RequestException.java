package euphoria.psycho.funny;

public class RequestException extends Exception {
    private final int mFinalStatus;

    public RequestException(int finalStatus, String message) {
        super(message);
        mFinalStatus = finalStatus;
    }
    public RequestException(int finalStatus, Throwable t) {
        this(finalStatus, t.getMessage());
        initCause(t);
    }
    public int getFinalStatus() {
        return mFinalStatus;
    }
}
