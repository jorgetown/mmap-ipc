package jdc;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author Jorge De Castro
 */
public final class Prime {
    private final Function<Integer, Answer> primeFunction;
    private final IpcQueueReader<Integer> intsQueue;
    private final IpcQueueWriter<Answer> answersQueue;
    private final ExecutorService executorService;

    public Prime(@NotNull final Function<Integer, Answer> primeFunction,
                 @NotNull final IpcQueueReader<Integer> intsQueue,
                 @NotNull final IpcQueueWriter<Answer> answersQueue) {

        this.primeFunction = primeFunction;
        this.intsQueue = intsQueue;
        this.answersQueue = answersQueue;
        this.executorService = Executors.newCachedThreadPool();
    }

    public void start() {
        executorService.execute(() -> {
                    System.out.println("Prime ints consumer started...\n");

                    while (!Thread.currentThread().isInterrupted() && !answersQueue.isFull()) {
                        Integer i = intsQueue.poll();

                        if (i == null) continue;

                        Answer p = primeFunction.apply(i);
                        answersQueue.add(p);
                    }

                    System.out.println("Prime task stopped after publishing 2GBs of answers...\n");
                }
        );
    }

    public void stop() {
        executorService.shutdown();
    }

    public static void main(String[] args) throws IOException {
        SingleQueueReader<Integer> intsQueue = SingleQueueReader.intsReader("PrimeIntsQueueReader");

        SingleQueueWriter<Answer> answersQueue = SingleQueueWriter.answersWriter("PrimeAnswersQueueWriter");

        Primality primality = new Primality();

        Prime prime = new Prime(
                primality,
                intsQueue,
                answersQueue
        );

        prime.start();

        prime.stop();
    }
}
