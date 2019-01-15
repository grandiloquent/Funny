package euphoria.psycho.funny.offline;

import java.io.DataInputStream;

public class DownloadAction {
    public abstract static class Deserializer {
        public final String type;
        public final int version;

        public Deserializer(String type, int version) {
            this.type = type;
            this.version = version;
        }

        public abstract DownloadAction readFromStream(int version, DataInputStream input);
    }
}
