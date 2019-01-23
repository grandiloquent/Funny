package euphoria.psycho.funny.natives;


public class NativeUtils {

    static {
        System.loadLibrary("main");
    }

    public static native void renameMp3File(String fileName);

    public static native void startServer(String ip, int port, String[] directories);
}
