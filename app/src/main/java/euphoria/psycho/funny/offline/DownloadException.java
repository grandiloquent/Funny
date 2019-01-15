package euphoria.psycho.funny.offline;

import java.io.IOException;

public class DownloadException extends IOException {
    public DownloadException(String message) {
        super(message);
    }

    public DownloadException(Throwable cause) {
        super(cause);
    }
}
