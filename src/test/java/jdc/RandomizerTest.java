package jdc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Jorge De Castro
 */
public class RandomizerTest {
    private QueueWriterStub<Integer> queueWriter;
    private QueueReaderStub<Answer> queueReader;
    private Randomizer underTest;

    @Before
    public void setUp() {
        queueWriter = new QueueWriterStub<>();
        queueReader = new QueueReaderStub<>();
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeIntegerSuppliedThrows() {
        Iterator<Integer> ints = Arrays.asList(-1, 3).iterator();

        underTest = new Randomizer(
                () -> ints.next(),
                answer -> {
                },
                queueWriter,
                queueReader
        );

        underTest.enqueueInt();
    }

    @Test
    public void singleIntegerSuppliedSingleIntegerEnqueued() {
        Iterator<Integer> ints = Arrays.asList(13).iterator();

        underTest = new Randomizer(
                () -> ints.next(),
                answer -> {
                },
                queueWriter,
                queueReader
        );

        underTest.enqueueInt();

        Assert.assertEquals(1, queueWriter.backingStore.size());
        Assert.assertEquals(13, (int) queueWriter.backingStore.poll());
    }

    @Test
    public void multipleIntegersSuppliedMultipleIntegersEnqueued() {
        Iterator<Integer> ints = Arrays.asList(13, 1, 8782).iterator();

        underTest = new Randomizer(
                () -> ints.next(),
                answer -> {
                },
                queueWriter,
                queueReader
        );

        underTest.enqueueInt();
        underTest.enqueueInt();
        underTest.enqueueInt();

        Assert.assertEquals(3, queueWriter.backingStore.size());
        Assert.assertEquals(13, (int) queueWriter.backingStore.poll());
        Assert.assertEquals(1, (int) queueWriter.backingStore.poll());
        Assert.assertEquals(8782, (int) queueWriter.backingStore.poll());
    }
}