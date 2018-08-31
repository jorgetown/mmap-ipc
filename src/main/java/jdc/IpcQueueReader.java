package jdc;

import java.io.Closeable;

/**
 * @author Jorge De Castro
 */
public interface IpcQueueReader<T> extends Closeable {
    T poll();

    boolean hasItemsLeft();
}
