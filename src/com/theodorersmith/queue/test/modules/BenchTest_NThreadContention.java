package com.theodorersmith.queue.test.modules;

import com.theodorersmith.queue.ProducerConsumerQueue;
import com.theodorersmith.queue.test.PCQueueTestHelpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

// Benchmarked test - tests multiple thread contention for the provided number of producer and consumer threads and number
// of objects, where each thread has between 0 and max delay between each queue/dequeue.
// Uses very simple and inexact stopwatch benchmark, giving a timeout failure if we don't finish before timeout.
public class BenchTest_NThreadContention {
    public static boolean run(ExecutorService threadPool,
                              ProducerConsumerQueue<Object> testQueue,
                              int numProducerThreads,
                              int maxProducerDelay,
                              int numConsumerThreads,
                              int maxConsumerDelay,
                              int numObjects,
                              int timeout,
                              boolean runQuiet) {
        if (!runQuiet) {
            System.out.print("Running test: Multiple Thread Contention with " +
                    +numObjects + " Objects; " +
                    +numProducerThreads + " Producers (" + maxProducerDelay + " ms delay); " +
                    +numConsumerThreads + " Consumers (" + maxConsumerDelay + " ms delay)... ");
        }

        List<Future<Set<Object>>> testFutureResults = null;
        try {
            // Figure out our number of test objects for each thread
            int numObjectsPerProducer = numObjects / numProducerThreads;
            int remProducerObjects = numObjects % numProducerThreads;
            int numObjectsPerConsumer = numObjects / numConsumerThreads;
            int remConsumerObjects = numObjects % numConsumerThreads;

            // Create our callable lists
            // Note: Making the producer callable return an object set is a little hacky, but I want to use invokeAll, which
            // is blocking with a timeout, which means all the Callable objects have to have the same generic type.
            List<Callable<Set<Object>>> taskList = new ArrayList<>();

            // Add our producers to the list
            for (int i = 0; i < numProducerThreads; i++) {
                int tempNumObjects = numObjectsPerProducer + (remProducerObjects > 0 ? 1 : 0);
                remProducerObjects -= remProducerObjects > 0 ? 1 : 0;
                taskList.add(() -> {
                    // Enqueue for a number of objects calculated above
                    for (int j = 0; j < tempNumObjects; j++) {
                        if (Thread.currentThread().isInterrupted()) {
                            // Our queue doesn't throw InterruptedException, so we need to handle interruption.
                            System.out.print("Breaking out of enqueue thread.");
                            return null;
                        }

                        testQueue.enqueue(new Object());

                        // Random sleep if we are using a delay
                        if (maxProducerDelay > 0) Thread.sleep(ThreadLocalRandom.current().nextInt(maxProducerDelay));
                    }
                    return null;
                });
            }

            // Add our consumers to the list
            for (int i = 0; i < numConsumerThreads; i++) {
                int tempNumObjects = numObjectsPerConsumer + (remConsumerObjects > 0 ? 1 : 0);
                remConsumerObjects -= remConsumerObjects > 0 ? 1 : 0;
                taskList.add(() -> {
                    // Dequeue and add to result list for a number of objects calculated above
                    Set<Object> resultSet = new HashSet<>();
                    for (int j = 0; j < tempNumObjects; j++) {
                        if (Thread.currentThread().isInterrupted()) {
                            // Our queue doesn't throw InterruptedException, so we need to handle interruption.
                            System.out.print("Breaking out of dequeue thread.");
                            return resultSet;
                        }

                        resultSet.add(testQueue.dequeue());

                        // Random sleep if we are using a delay
                        if (maxConsumerDelay > 0) Thread.sleep(ThreadLocalRandom.current().nextInt(maxConsumerDelay));
                    }
                    return resultSet;
                });
            }

            // Run Threads using a little rough stopwatch benchmarking to get some idea of performance.
            // Note: We are adding a small value to the timeout to ensure that if we do timeout, the stopwatch timestamp
            // will actually be greater than the timeout.
            long startStamp = System.currentTimeMillis();
            testFutureResults = threadPool.invokeAll(taskList, timeout + PCQueueTestHelpers.CONST_TIMEOUT_SHORT, TimeUnit.MILLISECONDS);
            long endStamp = System.currentTimeMillis();

            // If we are over time, return timeout fail
            long timeTaken = endStamp - startStamp;
            if (timeTaken > timeout) {
                PCQueueTestHelpers.printFail("Timed out");
                return false;
            }

            // Combine our results and verify that we dequeued as many objects as we were expecting.
            Set<Object> combinedResults = new HashSet<>();
            for (Future<Set<Object>> fut : testFutureResults) {
                Set<Object> tempResult = fut.get();
                if (tempResult != null) {
                    combinedResults.addAll(tempResult);
                }
            }
            if (combinedResults.size() != numObjects) {
                PCQueueTestHelpers.printFail("Expected to dequeue " + numObjects + " objects; Observed " + combinedResults.size() + " objects");
                return false;
            }

            PCQueueTestHelpers.printSuccess(timeTaken);

        } catch (Exception ex) {
            // If we encounter an exception, log that we failed the test and let finally shut down threads
            PCQueueTestHelpers.printFail("Exception Encountered: " + ex.getMessage());
            return false;
        } finally {
            // Cancel any threads that are still running
            if (testFutureResults != null) {
                for (Future<Set<Object>> fut : testFutureResults) {
                    if (fut != null && !fut.isDone()) fut.cancel(true);
                }
            }
        }

        return true;
    }
}
