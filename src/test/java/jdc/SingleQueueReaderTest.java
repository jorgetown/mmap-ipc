package jdc;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Jorge De Castro
 */
public class SingleQueueReaderTest {
    private File file;
    private FileChannel fileChannel;
    private MappedByteBuffer writeBuffer;
    private MappedByteBuffer readBuffer;
    private SingleQueueWriter<Answer> queueWriter;
    private SingleQueueReader<Answer> underTest;

    @Before
    public void setUp() throws IOException {
        file = File.createTempFile("test_queue_", ".tmp");
        fileChannel = new RandomAccessFile(file, "rw").getChannel();
        writeBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 28); // Capacity for 3 items + cursor
        readBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, 28); // Capacity for 3 items + cursor

        queueWriter = new SingleQueueWriter<>(
                writeBuffer,
                (buffer, answer) -> buffer.asIntBuffer().put(answer.number).put(answer.isPrime ? 1 : 0),
                8
        );

        underTest = new SingleQueueReader<>(
                readBuffer,
                (buffer) -> {
                    int number = buffer.getInt();
                    int isPrime = buffer.getInt();
                    return new Answer(number, isPrime);
                },
                8
        );
    }

    @Test
    public void nothingWrittenNothingRead() {

        Assert.assertEquals(0, queueWriter.size());
        Assert.assertEquals(null, underTest.poll());
    }

    @Test
    public void singleItemRead() {
        Answer expected = new Answer(753, false);

        queueWriter.add(expected);

        Assert.assertEquals(expected, underTest.poll());
    }

    @Test
    public void multipleItemsRead() {
        Answer a1 = new Answer(753, false);
        Answer a2 = new Answer(32, false);

        queueWriter.add(a1);
        queueWriter.add(a2);

        Assert.assertEquals(a1, underTest.poll());
        Assert.assertEquals(a2, underTest.poll());
    }

    @Test
    public void interleavedWritingAndReading() {
        Answer a1 = new Answer(753, false);
        Answer a2 = new Answer(32, false);
        Answer a3 = new Answer(764432, false);

        queueWriter.add(a1);
        queueWriter.add(a2);

        Assert.assertEquals(a1, underTest.poll());
        Assert.assertEquals(a2, underTest.poll());
        Assert.assertEquals(null, underTest.poll());

        queueWriter.add(a3);
        Assert.assertEquals(a3, underTest.poll());
    }

    @Test
    public void queueHasItemsLeft() {
        Answer a1 = new Answer(753, false);
        Answer a2 = new Answer(32, false);
        Answer a3 = new Answer(764432, false);

        queueWriter.add(a1);
        queueWriter.add(a2);
        queueWriter.add(a3);

        underTest.poll();
        underTest.poll();

        Assert.assertEquals(true, underTest.hasItemsLeft());
    }

    @Test
    public void queueHasNoItemsLeft() {
        Answer a1 = new Answer(753, false);
        Answer a2 = new Answer(32, false);
        Answer a3 = new Answer(764432, false);

        queueWriter.add(a1);
        queueWriter.add(a2);
        queueWriter.add(a3);

        underTest.poll();
        underTest.poll();
        underTest.poll();

        Assert.assertEquals(false, underTest.hasItemsLeft());
    }

    @After
    public void tearDown() throws IOException {
        writeBuffer.clear();
        readBuffer.clear();
        fileChannel.close();
        file.deleteOnExit();
    }
}