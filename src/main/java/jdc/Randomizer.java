package jdc;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Jorge De Castro
 */
public final class Randomizer {
    private final Supplier<Integer> intsSupplier;
    private final Consumer<Answer> answersConsumer;
    private final IpcQueueWriter<Integer> intsQueue;
    private final IpcQueueReader<Answer> answersQueue;

    public Randomizer(
            @NotNull final Supplier<Integer> intsSupplier,
            @NotNull final Consumer<Answer> answersConsumer,
            @NotNull final IpcQueueWriter<Integer> intsQueue,
            @NotNull final IpcQueueReader<Answer> answersQueue) {

        this.intsSupplier = intsSupplier;
        this.answersConsumer = answersConsumer;
        this.intsQueue = intsQueue;
        this.answersQueue = answersQueue;
    }

    public static void main(String[] args) throws IOException {

        IpcQueueWriter<Integer> intsQueue = SingleQueueWriter.intsWriter();

        IpcQueueReader<Answer> answersQueue = SingleQueueReader.answersReader();

        Random random = new Random();

        Randomizer randomizer = new Randomizer(
                () -> random.nextInt(Integer.MAX_VALUE - 1) + 1, // {@link Supplier} of random positive integers
                answer -> {
                    if (answer.isPrime) {
                        System.out.println(answer);
                    }
                },
                intsQueue,
                answersQueue
        );

        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.execute(() -> {
            System.out.println("Randomizer ints producer started...\n");

            while (!Thread.currentThread().isInterrupted() && !intsQueue.isFull()) {
                randomizer.enqueueInt();
            }
        });

        executorService.execute(() -> {
            System.out.println("Randomizer answers consumer started...\n");

            while (!Thread.currentThread().isInterrupted()) {
                if (!answersQueue.hasItemsLeft()) {
                    System.out.println("Randomizer stopped after reading 2GBs of answers");
                    try {
                        intsQueue.close();
                        answersQueue.close();
                    } catch (IOException ignored) {
                    }

                    break;
                }

                randomizer.dequeueAnswer();
            }
        });

        executorService.shutdown();
    }

    public void dequeueAnswer() {
        Answer answer = answersQueue.poll();

        if (answer != null) {
            answersConsumer.accept(answer);
        }
    }

    public void enqueueInt() {
        int i = intsSupplier.get();
        if (i < 1) {
            throw new IllegalArgumentException("Must supply positive integers only");
        }

        intsQueue.add(i);
    }
}
