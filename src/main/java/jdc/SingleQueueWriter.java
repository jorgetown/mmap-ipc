package jdc;

import org.jetbrains.annotations.NotNull;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.BiConsumer;

/**
 * @author Jorge De Castro
 */
public final class SingleQueueWriter<T> implements IpcQueueWriter<T> {
    private static final int DEFAULT_MEMORY_MAP_SIZE = (int) Math.pow(2, 31) - 1;
    private final MappedByteBuffer writeBuffer;
    private final BiConsumer<ByteBuffer, T> marshaller;
    private final ByteBuffer buffer;

    private final long address;

    public SingleQueueWriter(@NotNull final MappedByteBuffer writeBuffer,
                             @NotNull final BiConsumer<ByteBuffer, T> marshaller,
                             final int bufferSize) {

        this.writeBuffer = writeBuffer;
        this.marshaller = marshaller;
        this.writeBuffer.putInt(0);
        this.buffer = ByteBuffer.allocate(bufferSize);
        this.address = ((DirectBuffer) writeBuffer).address();
    }

    public static SingleQueueWriter<Integer> intsWriter() throws IOException {
        int bufferSize = 4;
        File file = new File("ints_queue_.dat");

        try (FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel()) {

            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, DEFAULT_MEMORY_MAP_SIZE);

            return new SingleQueueWriter<>(
                    mappedByteBuffer,
                    (buffer, item) -> buffer.asIntBuffer().put(item),
                    bufferSize
            );
        }
    }

    public static SingleQueueWriter<Answer> answersWriter() throws IOException {
        int bufferSize = 8;
        File file = new File("answers_queue_.dat");

        try (FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel()) {

            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, DEFAULT_MEMORY_MAP_SIZE);

            return new SingleQueueWriter<>(
                    mappedByteBuffer,
                    (buffer, item) -> buffer.asIntBuffer().put(item.number).put(item.isPrime ? 1 : 0),
                    bufferSize
            );
        }
    }

    @Override
    public void add(@NotNull final T item) {
        if (isFull()) {
            return;
        }

        buffer.clear();
        marshaller.accept(buffer, item);
        writeBuffer.put(buffer);
        Util.unsafe.putIntVolatile(null, address, writeBuffer.position());
    }

    @Override
    public boolean isFull() {
        return writeBuffer.limit() - writeBuffer.position() < buffer.capacity();
    }

    public int size() {
        return writeBuffer.position() <= 4 ? 0 : (writeBuffer.position() / buffer.capacity()) - 1;
    }

    @Override
    public void close() {
        writeBuffer.clear();
    }
}
