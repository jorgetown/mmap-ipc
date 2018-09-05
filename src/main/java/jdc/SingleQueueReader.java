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
    private final ByteBuffer readBuffer;
    private final Function<ByteBuffer, T> unmarshaller;
    private final int bufferSize;

    private final long address;

    public SingleQueueReader(@NotNull final ByteBuffer readBuffer,
                             @NotNull final Function<ByteBuffer, T> unmarshaller,
                             final int bufferSize) {

        this.readBuffer = readBuffer;
        this.unmarshaller = unmarshaller;
        this.readBuffer.getInt();
        this.bufferSize = bufferSize;
        this.address = ((DirectBuffer) readBuffer).address();
    }

    public static SingleQueueReader<Answer> answersReader() throws IOException {
        int bufferSize = 8;
        File file = new File(".", "answers_queue_.dat");

        try (FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel()) {

            ByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, DEFAULT_MEMORY_MAP_SIZE).asReadOnlyBuffer();

            return new SingleQueueReader<>(
                    mappedByteBuffer,
                    (buffer) -> {
                        int number = buffer.getInt();
                        int isPrime = buffer.getInt();
                        return new Answer(number, isPrime);
                    },
                    bufferSize
            );
        }
    }

    public static SingleQueueReader<Integer> intsReader() throws IOException {
        int bufferSize = 8;
        File file = new File("ints_queue_.dat");

        try (FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel()) {

            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, DEFAULT_MEMORY_MAP_SIZE);

            return new SingleQueueReader<>(
                    mappedByteBuffer,
                    ByteBuffer::getInt,
                    bufferSize
            );
        }
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
    public void close() {
        readBuffer.clear();
    }
}
