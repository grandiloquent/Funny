package euphoria.psycho.funny.natives;


public class NativeUtils {

    static {
        System.loadLibrary("main");
    }

    public static native void renameMp3File(String fileName);
}
