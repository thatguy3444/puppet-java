package com.theodorersmith.queue.test.modules;

import com.theodorersmith.queue.test.TestableQueue;
import com.theodorersmith.queue.test.PCQueueTestHelpers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Test_QueueBlocking {
    public static boolean run(ExecutorService threadPool, TestableQueue<Object> testQueue) {
        System.out.print("Running test: Blocking enqueue and dequeue... ");

        Future<Object> testFutureDequeueBlock = null;
        Future<?> testFutureEnqueueUnblock = null;
        Future<?> testFutureEnqueueBlock = null;
        Future<Object> testFutureDequeueUnblock = null;
        try {
            // Test dequeue blocking
            testFutureDequeueBlock = threadPool.submit(() ->
            {
                testQueue.resetQueue();
                return testQueue.dequeue();
            });
            Thread.sleep(PCQueueTestHelpers.CONST_TIMEOUT_SHORT);
            if (testFutureDequeueBlock.isDone()) {
                PCQueueTestHelpers.printFail( "Expected to block after empty dequeue; Observed did not block");
                return false;
            }

            // Test enqueue unblocking
            testFutureEnqueueUnblock = threadPool.submit(() ->
            {
                testQueue.enqueue(new Object()); // This should unblock the earlier thread
            });
            Thread.sleep(PCQueueTestHelpers.CONST_TIMEOUT_SHORT);
            if (!testFutureDequeueBlock.isDone()) {
                PCQueueTestHelpers.printFail("Expected to unblock dequeue after enqueuing new object; Observed did not unblock");
                return false;
            }

            // Test enqueue blocking
            testFutureEnqueueBlock = threadPool.submit(() ->
            {
                PCQueueTestHelpers.enqueueToCapacity(testQueue);
                testQueue.enqueue(new Object()); // This should block
            });
            Thread.sleep(PCQueueTestHelpers.CONST_TIMEOUT_SHORT);
            if (testFutureEnqueueBlock.isDone()) {
                PCQueueTestHelpers.printFail("Expected to block after full enqueue; Observed did not block");
                return false;
            }

            // Test dequeue unblocking
            testFutureDequeueUnblock = threadPool.submit(() ->
            {
                return testQueue.dequeue(); // This should unblock the testFutureEnqueueBlock thread
            });
            Thread.sleep(PCQueueTestHelpers.CONST_TIMEOUT_SHORT);
            if (!testFutureEnqueueBlock.isDone()) {
                PCQueueTestHelpers.printFail("Expected to unblock enqueue after dequeuing; Observed did not unblock");
                return false;
            }

            PCQueueTestHelpers.printSuccess();

        } catch (Exception ex) {
            // If we encounter an exception, log that we failed the test and let finally shut down threads
            PCQueueTestHelpers.printFail("Exception Encountered: " + ex.getMessage());
            return false;
        } finally {
            // Cancel any threads that are still working
            if (testFutureDequeueBlock != null && !testFutureDequeueBlock.isDone()) testFutureDequeueBlock.cancel(true);
            if (testFutureEnqueueUnblock != null && !testFutureEnqueueUnblock.isDone()) testFutureEnqueueUnblock.cancel(true);
            if (testFutureEnqueueBlock != null && !testFutureEnqueueBlock.isDone()) testFutureEnqueueBlock.cancel(true);
            if (testFutureDequeueUnblock != null && !testFutureDequeueUnblock.isDone()) testFutureDequeueUnblock.cancel(true);
        }

        return true;
    }
}
