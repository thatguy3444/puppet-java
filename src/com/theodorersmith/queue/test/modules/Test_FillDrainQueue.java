package com.theodorersmith.queue.test.modules;

import com.theodorersmith.queue.test.TestableQueue;
import com.theodorersmith.queue.test.PCQueueTestHelpers;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Test_FillDrainQueue {
    public static boolean run(ExecutorService threadPool, TestableQueue<Object> testQueue) {
        System.out.print("Running test: Filling then emptying queue... ");

        Future<List<Object>> testFuture = null;
        try {
            testFuture = threadPool.submit(() ->
            {
                testQueue.resetQueue();
                PCQueueTestHelpers.enqueueToCapacity(testQueue);
                return PCQueueTestHelpers.dequeueAll(testQueue);
            });

            // Check if thread timed out
            if (!PCQueueTestHelpers.checkWaitTimeout(testFuture, PCQueueTestHelpers.CONST_TIMEOUT_SHORT)) {
                // If we failed because of timeout, just return false and let finally shut down the threads.
                return false;
            }

            // If we are here, we know the future is complete, so we can check if we have a full set of objects (no duplicates) returned from the queue
            List<Object> queueContentList = testFuture.get();
            HashSet<Object> queueContentSet = new HashSet<>(queueContentList);

            // Check if we dequeued everything we were expecting
            if (queueContentSet.size() != testQueue.getCapacity() || testQueue.getLength() != 0) {
                PCQueueTestHelpers.printFail("Expected to dequeue " + testQueue.getCapacity() + " Objects; Observed dequeue of " + queueContentSet.size() + " Objects");
                return false;
            }

            PCQueueTestHelpers.printSuccess();

        } catch (Exception ex) {
            // If we encounter an exception, log that we failed the test and let finally shut down threads
            PCQueueTestHelpers.printFail("Exception Encountered: " + ex.getMessage());
            return false;
        } finally {
            // Cancel any threads that are still working
            if (testFuture != null && !testFuture.isDone()) testFuture.cancel(true);
        }

        return true;
    }
}
