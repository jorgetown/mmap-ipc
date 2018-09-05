package jdc;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jorge De Castro
 */
public final class Prime {

    public static void main(String[] args) throws IOException {
        SingleQueueReader<Integer> intsQueue = SingleQueueReader.intsReader();

        SingleQueueWriter<Answer> answersQueue = SingleQueueWriter.answersWriter();

        Primality primality = new Primality();

        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.execute(() -> {
                    System.out.println("Prime ints consumer started...\n");

                    while (!Thread.currentThread().isInterrupted() && !answersQueue.isFull()) {
                        Integer i = intsQueue.poll();

                        if (i == null) continue;

                        Answer p = primality.apply(i);
                        answersQueue.add(p);
                    }

                    System.out.println("Prime task stopped after publishing 2GBs of answers...\n");
                }
        );

        executorService.shutdown();
    }
}
