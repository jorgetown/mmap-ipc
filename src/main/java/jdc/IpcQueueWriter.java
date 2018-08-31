package jdc;

import java.io.Closeable;

/**
 * @author Jorge De Castro
 */
public interface IpcQueueWriter<T> extends Closeable {
    void add(T item);

    boolean isFull();
}
