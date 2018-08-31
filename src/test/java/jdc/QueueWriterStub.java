package jdc;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Jorge De Castro
 */
public final class QueueWriterStub<T> implements IpcQueueWriter<T> {
    public final Queue<T> backingStore;

    public QueueWriterStub() {
        this.backingStore = new ArrayDeque<T>();
    }

    @Override
    public void add(@NotNull final T item) {
        backingStore.add(item);
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public void close() throws IOException {
        backingStore.clear();
    }
}
