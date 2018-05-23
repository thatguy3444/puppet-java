package com.theodorersmith.queue.test;

import com.theodorersmith.queue.ProducerConsumerConcurrentQueue;
import com.theodorersmith.queue.ProducerConsumerQueue;
import com.theodorersmith.queue.test.implementations.*;
import com.theodorersmith.queue.test.modules.*;

import java.util.concurrent.*;

public class PCQueueTestManager {

    public static void runDefaultTest() {
        System.out.println(" --- Running the Default Test Exercise  ---");
        System.out.println("Capacity: 5; Producers: 5; Consumers: 5; 1000 Objects)");


        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        ProducerConsumerQueue<Object> defaultQueue = new ProducerConsumerConcurrentQueue<>(5);
        BenchTest_NThreadContention.run(threadPool, defaultQueue, 5, 0,5, 0, 1000, PCQueueTestHelpers.CONST_TIMEOUT_LONG, false);
        System.out.println(" --- Default Exercise Complete  ---");
    }

    // Runs suite of tests on all queue implementations
    public static void testAllQueues() {
        TestableQueue<Object> notifyAllArrayQueue = new ConcurrentNotifyAllArrayPCQueue<Object>(10);
        TestableQueue<Object> twoConditionArrayQueue = new ConcurrentTwoConditionLockArrayPCQueue<>(10);
        TestableQueue<Object> doubleSyncArrayQueue = new ConcurrentDoubleSyncArrayPCQueue<>(10);
        TestableQueue<Object> javaReferenceArrayQueue = new JavaReferenceArrayPCQueue<>(10);
        TestableQueue<Object> javaReferenceLinkQueue = new JavaReferenceLinkedPCQueue<>(10);

        PCQueueTestManager.runAllTestsOnQueue(notifyAllArrayQueue);
        PCQueueTestManager.runAllTestsOnQueue(twoConditionArrayQueue);
        PCQueueTestManager.runAllTestsOnQueue(doubleSyncArrayQueue);
        PCQueueTestManager.runAllTestsOnQueue(javaReferenceArrayQueue);
        PCQueueTestManager.runAllTestsOnQueue(javaReferenceLinkQueue);
    }

    // Runs a basic stopwatch benchmark on all queue implementations
    public static void benchAllQueues() {
        System.out.println(" --- Running Benchmarks for All PC Queue Implementations ---");

        runBenchmarks(1, 5,5, 100000);
        runBenchmarks(10, 10,10, 100000);
        runBenchmarks(10, 100, 100, 20000);
        runBenchmarks(100, 100, 100, 100000);

        System.out.println(" --- Benchmarks Complete ---");
    }

    // Runs two different capacity tests on the notifyAll implementation
    public static void testNotifyAllQueue() {
        TestableQueue<Object> notifyAllArrayQueueTwo = new ConcurrentNotifyAllArrayPCQueue<>(2);
        TestableQueue<Object> notifyAllArrayQueueTen = new ConcurrentNotifyAllArrayPCQueue<>(10);

        PCQueueTestManager.runAllTestsOnQueue(notifyAllArrayQueueTwo);
        PCQueueTestManager.runAllTestsOnQueue(notifyAllArrayQueueTen);
    }

    // Runs two different capacity tests on the two condition implementation
    public static void testTwoConditionQueue() {
        TestableQueue<Object> twoConditionQueueTwo = new ConcurrentTwoConditionLockArrayPCQueue<>(2);
        TestableQueue<Object> twoConditionQueueTen = new ConcurrentTwoConditionLockArrayPCQueue<>(10);

        PCQueueTestManager.runAllTestsOnQueue(twoConditionQueueTwo);
        PCQueueTestManager.runAllTestsOnQueue(twoConditionQueueTen);
    }

    // Runs two different capacity tests on the double sync implementation
    public static void testDoubleSyncQueue() {
        TestableQueue<Object> doubleSyncQueueTwo = new ConcurrentDoubleSyncArrayPCQueue<>(2);
        TestableQueue<Object> doubleSyncQueueTen = new ConcurrentDoubleSyncArrayPCQueue<>(10);

        PCQueueTestManager.runAllTestsOnQueue(doubleSyncQueueTwo);
        PCQueueTestManager.runAllTestsOnQueue(doubleSyncQueueTen);
    }


    public static void runBenchmarks(int capacity, int producers, int consumers, int numObjects) {
        TestableQueue<Object> notifyAllArrayQueue = new ConcurrentNotifyAllArrayPCQueue<>(capacity);
        TestableQueue<Object> twoConditionArrayQueue = new ConcurrentTwoConditionLockArrayPCQueue<>(capacity);
        TestableQueue<Object> doubleSyncArrayQueue = new ConcurrentDoubleSyncArrayPCQueue<>(capacity);
        TestableQueue<Object> javaReferenceArrayQueue = new JavaReferenceArrayPCQueue<>(capacity);
        TestableQueue<Object> javaReferenceLinkQueue = new JavaReferenceLinkedPCQueue<>(capacity);

        System.out.println(" --- Benchmarking (Capacity: " + capacity + "; Producers: " + producers + "; Consumers: " + consumers + "; " + numObjects + " Objects)");

        runBenchmark(doubleSyncArrayQueue, producers, consumers, numObjects, true);
        runBenchmark(notifyAllArrayQueue, producers, consumers, numObjects, false);
        runBenchmark(twoConditionArrayQueue, producers, consumers, numObjects, false);
        runBenchmark(javaReferenceArrayQueue, producers, consumers, numObjects, false);
        runBenchmark(javaReferenceLinkQueue, producers, consumers, numObjects, false);
    }

    private static void runBenchmark(TestableQueue<Object> testQueue, int producers, int consumers, int numObjects, boolean isHighlighted) {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        if (isHighlighted)
            PCQueueTestHelpers.printClassNamePurple(testQueue);
        else
            PCQueueTestHelpers.printClassNameBlue(testQueue);
        System.out.print(" - ");
        BenchTest_NThreadContention.run(threadPool, testQueue, producers, 0,consumers, 0, numObjects, PCQueueTestHelpers.CONST_TIMEOUT_XLONG, true);
    }

    /////
    ///// Test Helpers
    /////

    // Runs all the tests (Requires that the queue implement ITestableQueue)
    public static void runAllTestsOnQueue(TestableQueue testQueue) {
        String queueClassName = testQueue.getClass().getSimpleName();
        PCQueueTestHelpers.printTestHeader(queueClassName, "Running All Tests with Capacity " + testQueue.getCapacity());

        boolean success = runBasicFunctionalTestsOnQueue(testQueue);
        success = success && runHighConcurrencyTestsOnQueue(testQueue);

        if (success)
            PCQueueTestHelpers.printTestHeader(queueClassName, "All Tests Successful");
        else
            PCQueueTestHelpers.printTestHeader(queueClassName, "TEST FAILED");
    }

    /// This runs a set of basic functionality tests (Requires that the queue implement ITestableQueue)
    public static boolean runBasicFunctionalTestsOnQueue(TestableQueue testQueue) {
        boolean success = true;
        ExecutorService threadPool = null;

        try {
            String queueClassName = testQueue.getClass().getSimpleName();
            threadPool = Executors.newCachedThreadPool();

            PCQueueTestHelpers.printTestHeader(queueClassName, "Running Basic Functional Tests");

            success = Test_FillQueue.run(threadPool, testQueue);
            success = success && Test_FillDrainQueue.run(threadPool, testQueue);
            success = success && Test_QueueBlocking.run(threadPool, testQueue);
            success = success && Test_TwoThreadContention.run(threadPool, testQueue, false);
            success = success && Test_TwoThreadContention.run(threadPool, testQueue, true);

            if (success)
                PCQueueTestHelpers.printTestHeader(queueClassName, "Basic Functionality Successful");
            else
                PCQueueTestHelpers.printTestHeader(queueClassName, "Basic Functionality Tests FAILED");

        } catch (Exception ex) {
            // Complain if we encountered an exception
            System.out.println("PCQueueTestManager->runBasicFunctionalTests: Encountered exception - " + ex.getMessage());
        } finally {
            // Our test methods should have killed any remaining threads before they exited, but just in case,
            // interrupt and shut down any remaining threads.
            if (threadPool != null) {
                threadPool.shutdownNow();
            }
        }

        return success;
    }

    /// This runs a set of high concurrency multithreading tests with rough benchmarks
    public static  boolean runHighConcurrencyTestsOnQueue(ProducerConsumerQueue testQueue) {
        boolean success = true;
        ExecutorService threadPool = null;

        try {
        String queueClassName = testQueue.getClass().getSimpleName();
        threadPool = Executors.newCachedThreadPool();

        PCQueueTestHelpers.printTestHeader(queueClassName, "Running High Concurrency Tests");

        success = BenchTest_NThreadContention.run(threadPool, testQueue, 10, 0,10, 0, PCQueueTestHelpers.CONST_DEFAULT_MEDIUM_CONTENTIONOBJECTCOUNT, PCQueueTestHelpers.CONST_TIMEOUT_MEDIUM, false);
        success = success && BenchTest_NThreadContention.run(threadPool, testQueue, 100, 0,100, 0, PCQueueTestHelpers.CONST_DEFAULT_MEDIUM_CONTENTIONOBJECTCOUNT, PCQueueTestHelpers.CONST_TIMEOUT_MEDIUM, false);
        success = success && BenchTest_NThreadContention.run(threadPool, testQueue, 200, 0,200, 0, PCQueueTestHelpers.CONST_DEFAULT_MEDIUM_CONTENTIONOBJECTCOUNT, PCQueueTestHelpers.CONST_TIMEOUT_MEDIUM, false);
        success = success && BenchTest_NThreadContention.run(threadPool, testQueue, 50, 0,50, 0, PCQueueTestHelpers.CONST_DEFAULT_LARGE_CONTENTIONOBJECTCOUNT, PCQueueTestHelpers.CONST_TIMEOUT_XLONG, false);
        success = success && BenchTest_NThreadContention.run(threadPool, testQueue, 40, 0,3, 0, PCQueueTestHelpers.CONST_DEFAULT_MEDIUM_CONTENTIONOBJECTCOUNT, PCQueueTestHelpers.CONST_TIMEOUT_LONG, false);
        success = success && BenchTest_NThreadContention.run(threadPool, testQueue, 3, 0,40, 0, PCQueueTestHelpers.CONST_DEFAULT_MEDIUM_CONTENTIONOBJECTCOUNT, PCQueueTestHelpers.CONST_TIMEOUT_LONG, false);
        success = success && BenchTest_NThreadContention.run(threadPool, testQueue, 5, 0,2, 2, PCQueueTestHelpers.CONST_DEFAULT_MEDIUM_CONTENTIONOBJECTCOUNT, PCQueueTestHelpers.CONST_TIMEOUT_XLONG, false);
        success = success && BenchTest_NThreadContention.run(threadPool, testQueue, 2, 2,5, 0, PCQueueTestHelpers.CONST_DEFAULT_MEDIUM_CONTENTIONOBJECTCOUNT, PCQueueTestHelpers.CONST_TIMEOUT_XLONG, false);
        success = success && BenchTest_NThreadContention.run(threadPool, testQueue, 2, 5,2, 5, PCQueueTestHelpers.CONST_DEFAULT_SMALL_CONTENTIONOBJECTCOUNT, PCQueueTestHelpers.CONST_TIMEOUT_XLONG, false);
        success = success && BenchTest_NThreadContention.run(threadPool, testQueue, 1, 0,1, 0, PCQueueTestHelpers.CONST_DEFAULT_XLARGE__CONTENTIONOBJECTCOUNT, PCQueueTestHelpers.CONST_TIMEOUT_XLONG, false);


        if (success)
            PCQueueTestHelpers.printTestHeader(queueClassName, "High Concurrency Successful");
        else
            PCQueueTestHelpers.printTestHeader(queueClassName, "High Concurrency Tests FAILED");

        } catch (Exception ex) {
            // Complain if we encountered an exception
            System.out.println("PCQueueTestManager->runHighConcurrencyTests: Encountered exception - " + ex.getMessage());
        } finally {
            // Our test methods should have killed any remaining threads before they exited, but just in case,
            // interrupt and shut down any remaining threads.
            if (threadPool != null) {
                threadPool.shutdownNow();
            }
        }

        return success;
    }


}
