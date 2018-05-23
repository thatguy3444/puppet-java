package com.theodorersmith.queue.test.modules;

import com.theodorersmith.queue.test.TestableQueue;
import com.theodorersmith.queue.test.PCQueueTestHelpers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Test_FillQueue {
    public static boolean run(ExecutorService threadPool, TestableQueue<Object> testQueue) {
        System.out.print("Running test: Fill Queue to Capacity... ");

        Future<?> testFuture = null;
        try {

            testFuture = threadPool.submit(() ->
            {
                testQueue.resetQueue();
                PCQueueTestHelpers.enqueueToCapacity(testQueue);
            });

            // Check if thread times out
            if (!PCQueueTestHelpers.checkWaitTimeout(testFuture, PCQueueTestHelpers.CONST_TIMEOUT_SHORT)) {
                // If we failed because of timeout, just return false and let finally shut down the threads.
                return false;
            }

            // Check if queue is full
            if (testQueue.getLength() != testQueue.getCapacity()) {
                PCQueueTestHelpers.printFail("Expected length " + testQueue.getCapacity() + "; Observed length " + testQueue.getLength());
                return false;
            }

            PCQueueTestHelpers.printSuccess();

        } catch (Exception ex) {
            // If we encounter an exception, log that we failed the test.
            PCQueueTestHelpers.printFail("Exception Encountered: " + ex.getMessage());
            return false;
        } finally {
            // Cancel any threads that are still working
            if (testFuture != null && !testFuture.isDone()) testFuture.cancel(true);
        }

        return true;
    }
}
