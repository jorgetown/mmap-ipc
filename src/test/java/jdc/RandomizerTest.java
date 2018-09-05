package jdc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

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
                ints::next,
                answer -> {
                },
                queueWriter,
                queueReader
        );

        underTest.enqueueInt();
    }

    @Test
    public void singleIntegerSuppliedSingleIntegerEnqueued() {
        Iterator<Integer> ints = Collections.singletonList(13).iterator();

        underTest = new Randomizer(
                ints::next,
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
                ints::next,
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

    @Test
    public void noAnswerEnqueuedNoAnswerDequeued() {
        Iterator<Integer> ints = Arrays.asList(13, 1, 8782).iterator();
        List<Answer> expected = new ArrayList<>();

        underTest = new Randomizer(
                ints::next,
                expected::add,
                queueWriter,
                queueReader
        );

        underTest.dequeueAnswer();

        Assert.assertEquals(0, expected.size());
    }

    @Test
    public void singleAnswerEnqueued() {
        Iterator<Integer> ints = Arrays.asList(13, 1, 8782).iterator();
        List<Answer> answers = new ArrayList<>();
        Answer expected = new Answer(5432, true);
        Queue<Answer> store = new ArrayDeque<>();
        store.add(expected);
        queueReader = new QueueReaderStub<>(store);

        underTest = new Randomizer(
                ints::next,
                answers::add,
                queueWriter,
                queueReader
        );

        underTest.dequeueAnswer();

        Assert.assertEquals(1, answers.size());
        Assert.assertEquals(expected, answers.get(0));
    }

    @Test
    public void multipleAnswersEnqueued() {
        Iterator<Integer> ints = Arrays.asList(13, 1, 8782).iterator();
        List<Answer> answers = new ArrayList<>();
        Answer expected1 = new Answer(5432, true);
        Answer expected2 = new Answer(98, false);
        Queue<Answer> store = new ArrayDeque<>();
        store.add(expected1);
        store.add(expected2);
        queueReader = new QueueReaderStub<>(store);

        underTest = new Randomizer(
                ints::next,
                answers::add,
                queueWriter,
                queueReader
        );

        underTest.dequeueAnswer();
        underTest.dequeueAnswer();

        Assert.assertEquals(2, answers.size());
        Assert.assertEquals(expected1, answers.get(0));
        Assert.assertEquals(expected2, answers.get(1));
    }
}