package jdc;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Jorge De Castro
 */
public final class QueueReaderStub<T> implements IpcQueueReader<T> {
    private final Queue<T> backingStore;

    public QueueReaderStub(@NotNull final Queue<T> backingStore) {
        this.backingStore = backingStore;
    }

    public QueueReaderStub() {
        this(new ArrayDeque<>());
    }

    @Override
    public T poll() {
        return backingStore.poll();
    }

    @Override
    public boolean hasItemsLeft() {
        return true;
    }

    @Override
    public void close() {
        backingStore.clear();
    }
}
