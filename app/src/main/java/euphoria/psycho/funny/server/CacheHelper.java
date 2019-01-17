package euphoria.psycho.funny.server;

public class CacheHelper {

    public static CacheHelper instance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final CacheHelper INSTANCE =
                new CacheHelper();
    }
}
