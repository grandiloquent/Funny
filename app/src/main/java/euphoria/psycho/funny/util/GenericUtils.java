package euphoria.psycho.funny.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GenericUtils {
    /**
     * Returns an array with the arrays concatenated together.
     *
     * @see <a href="http://stackoverflow.com/a/784842/1122089">Stackoverflow answer</a> by <a
     * href="http://stackoverflow.com/users/40342/joachim-sauer">Joachim Sauer</a>
     */
    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * Returns {@code true} if the two specified collections have common elements.
     */
    public static <T> boolean containsAny(
            Collection<T> c1, Collection<T> c2, Comparator<T> comparator) {
        List<T> contains = new ArrayList<>(c1);
        Collections.sort(contains, comparator);
        for (T iterate : c2) {
            if (Collections.binarySearch(contains, iterate, comparator) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Subtracts the elements from the original collection.
     */
    public static <T> List<T> subtract(
            Collection<T> originals, T[] toSubtracts, Comparator<T> comparator) {
        List<T> result = new ArrayList<>(originals);
        Collections.sort(result, comparator);
        for (T toSubtract : toSubtracts) {
            int index = Collections.binarySearch(result, toSubtract, comparator);
            if (index >= 0) {
                result.remove(index);
            }
        }
        return result;
    }

    /**
     * Unions the two collections and returns the unified list.
     *
     * <p>The elements is not compared with hashcode() or equals(). Comparator is used for the
     * equality check.
     */
    public static <T> List<T> union(
            Collection<T> originals, Collection<T> toAdds, Comparator<T> comparator) {
        List<T> result = new ArrayList<>(originals);
        Collections.sort(result, comparator);
        List<T> resultToAdd = new ArrayList<>();
        for (T toAdd : toAdds) {
            if (Collections.binarySearch(result, toAdd, comparator) < 0) {
                resultToAdd.add(toAdd);
            }
        }
        result.addAll(resultToAdd);
        return result;
    }
}
