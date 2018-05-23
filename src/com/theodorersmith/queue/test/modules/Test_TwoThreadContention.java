package com.theodorersmith.queue.test.modules;

import com.theodorersmith.queue.test.TestableQueue;
import com.theodorersmith.queue.test.PCQueueTestHelpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Test_TwoThreadContention {
    public static boolean run(ExecutorService threadPool, TestableQueue<Object> testQueue, boolean isStartQueueFilled) {
        System.out.print("Running test: Two Thread Contention with " + (isStartQueueFilled ? "Filled" : "Unfilled") + " Queue... ");

        Future<?> testFutureEnqueue = null;
        Future<Set<Object>> testFutureDequeue = null;
        try {
            // Test two threaded contentious enqueue/dequeue, starting with a full or empty queue

            // Reset the queue and fill if necessary
            testQueue.resetQueue();
            if (isStartQueueFilled) {
                PCQueueTestHelpers.enqueueToCapacity(testQueue);
            }

            // Build list of objects to enqueue
            List<Object> queueObjects = new ArrayList<>();
            for (int i = 0; i < PCQueueTestHelpers.CONST_DEFAULT_SMALL_CONTENTIONOBJECTCOUNT; i++) {
                queueObjects.add(new Object());
            }
            // Start enqueue thread
            testFutureEnqueue = threadPool.submit(() ->
            {
                for (Object queueObj : queueObjects) {
                    if (Thread.currentThread().isInterrupted()) {
                        // Our queue doesn't throw InterruptedException, so we need to handle interruption.
                        System.out.print("Breaking out of enqueue thread.");
                        return;
                    }

                    testQueue.enqueue(queueObj);
                }
            });
            // Start dequeue thread
            testFutureDequeue = threadPool.submit(() ->
            {
                Set<Object> dequeueResults = new HashSet<>();

                for (int i = 0; i < PCQueueTestHelpers.CONST_DEFAULT_SMALL_CONTENTIONOBJECTCOUNT; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        // Our queue doesn't throw InterruptedException, so we need to handle interruption.
                        System.out.print("Breaking out of dequeue thread.");
                        return dequeueResults;
                    }

                    dequeueResults.add(testQueue.dequeue());
                }
                return dequeueResults;
            });

            // Check if either thread timed out
            Thread.sleep(PCQueueTestHelpers.CONST_TIMEOUT_SHORT * 5);
            if (!testFutureEnqueue.isDone()) {
                // If we failed because of timeout, just return false and let finally shut down the threads.
                PCQueueTestHelpers.printFail("Timeout - Enqueue thread is blocking");
                return false;
            }
            if (!testFutureDequeue.isDone()) {
                // If we failed because of timeout, just return false and let finally shut down the threads.
                PCQueueTestHelpers.printFail("Timeout - Dequeue thread is blocking");
                return false;
            }

            // Verify that we dequeued as many objects as we were expecting
            Set<Object> resultSet = testFutureDequeue.get();
            if (resultSet.size() != queueObjects.size()) {
                PCQueueTestHelpers.printFail("Expected the same number of dequeues as enqueues; Observed different number of dequeues");
                return false;
            }

            PCQueueTestHelpers.printSuccess();

        }
        catch (Exception ex) {
            // If we encounter an exception, log that we failed the test and let finally shut down threads
            PCQueueTestHelpers.printFail("Exception Encountered: " + ex.getMessage());
            return false;
        } finally {
            // Cancel any threads that are still working
            if (testFutureEnqueue != null && !testFutureEnqueue.isDone()) testFutureEnqueue.cancel(true);
            if (testFutureDequeue != null && !testFutureDequeue.isDone()) testFutureDequeue.cancel(true);
        }

        return true;
    }
}
