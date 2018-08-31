package jdc;

import org.jetbrains.annotations.NotNull;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.function.Function;

/**
 * @author Jorge De Castro
 */
public final class SingleQueueReader<T> implements IpcQueueReader<T> {
    private static final int DEFAULT_MEMORY_MAP_SIZE = (int) Math.pow(2, 31) - 1;
    private final MappedByteBuffer readBuffer;
    private final Function<ByteBuffer, T> unmarshaller;
    private final String name;
    private final int bufferSize;

    private final long address;

    public SingleQueueReader(@NotNull final String name,
                             @NotNull final MappedByteBuffer readBuffer,
                             @NotNull final Function<ByteBuffer, T> unmarshaller,
                             final int bufferSize) {

        this.name = name;
        this.readBuffer = readBuffer;
        this.unmarshaller = unmarshaller;
        this.readBuffer.getInt();
        this.bufferSize = bufferSize;
        this.address = ((DirectBuffer) readBuffer).address();
    }

    @Override
    public T poll() {
        if (!hasItemsLeft()) {
            return null;
        }

        int writerCursor = Util.unsafe.getIntVolatile(null, address);
        if (writerCursor - readBuffer.position() < bufferSize) {
            return null;
        }

        return unmarshaller.apply(readBuffer);
    }

    @Override
    public boolean hasItemsLeft() {
        return readBuffer.limit() - readBuffer.position() >= bufferSize;
    }

    @Override
    public void close() throws IOException {
        readBuffer.clear();
    }

    public static SingleQueueReader<Answer> answersReader(@NotNull final String name) throws IOException {
        int bufferSize = 8;
        File file = new File("answers_queue_.dat");
        FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, DEFAULT_MEMORY_MAP_SIZE);

        return new SingleQueueReader<>(
                name,
                mappedByteBuffer,
                (buffer) -> {
                    int number = buffer.getInt();
                    int isPrime = buffer.getInt();
                    return new Answer(number, isPrime);
                },
                bufferSize
        );
    }

    public static SingleQueueReader<Integer> intsReader(@NotNull final String name) throws IOException {
        int bufferSize = 8;
        File file = new File("ints_queue_.dat");
        FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
        MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, DEFAULT_MEMORY_MAP_SIZE);

        return new SingleQueueReader<>(
                name,
                mappedByteBuffer,
                ByteBuffer::getInt,
                bufferSize
        );
    }
}
