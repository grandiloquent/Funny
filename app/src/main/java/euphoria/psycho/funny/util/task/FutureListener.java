package euphoria.psycho.funny.util.task;
public interface FutureListener<T> {
    public void onFutureDone(Future<T> future);
}
