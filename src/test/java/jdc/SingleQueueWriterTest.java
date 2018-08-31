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
public class SingleQueueWriterTest {
    private File file;
    private FileChannel fileChannel;
    private MappedByteBuffer mappedByteBuffer;
    private SingleQueueWriter<Integer> underTest;

    @Before
    public void setUp() throws IOException {
        file = File.createTempFile("test_wqueue_", ".tmp");
        fileChannel = new RandomAccessFile(file, "rw").getChannel();
        mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 16); // Capacity for 3 items + cursor

        underTest = new SingleQueueWriter<>(
                "tests_writer_queue_",
                mappedByteBuffer,
                (buffer, item) -> buffer.asIntBuffer().put(item),
                4
        );
    }

    @Test
    public void nothingAddedNothingStored() {

        Assert.assertEquals(0, underTest.size());
    }

    @Test
    public void singleItemAdded() {
        underTest.add(753);

        Assert.assertEquals(1, underTest.size());
    }

    @Test
    public void multipleItemsAdded() {
        underTest.add(753);
        underTest.add(32);

        Assert.assertEquals(2, underTest.size());
    }

    @Test
    public void queueIsNotFull() {
        underTest.add(753);
        underTest.add(32);

        Assert.assertEquals(false, underTest.isFull());
    }

    @Test
    public void queueIsFull() {
        underTest.add(753);
        underTest.add(32);
        underTest.add(123534);

        Assert.assertEquals(true, underTest.isFull());
    }

    @After
    public void tearDown() throws IOException {
        mappedByteBuffer.clear();
        fileChannel.close();
        file.deleteOnExit();
    }
}