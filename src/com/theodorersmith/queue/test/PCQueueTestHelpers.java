package com.theodorersmith.queue.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

// This class organize some static helper methods to assist with testing and output
public class PCQueueTestHelpers {

    /////
    ///// Testing Constants
    /////

    // The time test sets are given to complete.
    public static final int CONST_TIMEOUT_SHORT = 100;
    public static final int CONST_TIMEOUT_MEDIUM = 10000;
    public static final int CONST_TIMEOUT_LONG = 20000;
    public static final int CONST_TIMEOUT_XLONG = 30000;

    // Default number of objects we run contention tests for;
    public static final int CONST_DEFAULT_SMALL_CONTENTIONOBJECTCOUNT = 1000;
    public static final int CONST_DEFAULT_MEDIUM_CONTENTIONOBJECTCOUNT = 20000;
    public static final int CONST_DEFAULT_LARGE_CONTENTIONOBJECTCOUNT = 50000;
    public static final int CONST_DEFAULT_XLARGE__CONTENTIONOBJECTCOUNT = 500000;

    /////
    ///// ANSI Color Codes for Console Output
    /////

    // Console colors codes making things pretty for our test (Won't work in windows console)
    private static final String COLOR_DEFAULT = "\u001B[0m";
    private static final String COLOR_CYAN = "\u001B[36m";
    private static final String COLOR_RED = "\u001B[31m";
    private static final String COLOR_GREEN = "\u001B[32m";
    private static final String COLOR_PURPLE = "\u001B[35m";

    /////
    ///// Helpful queue testing methods
    /////

    // Dequeues all objects currently in the queue and returns as a list. Respects thread interrupt
    public static List<Object> dequeueAll(TestableQueue testQueue) {
        List<Object> dequeueList = new ArrayList<Object>();
        while (testQueue.getLength() > 0) {
            if (Thread.currentThread().isInterrupted()) {
                // Our dequeue method doesn't throw InterruptedException, so we need to handle interruption.
                System.out.print("Breaking out of dequeue all thread.");
                break;
            }

            dequeueList.add(testQueue.dequeue());
        }
        return dequeueList;
    }

    // Enqueues objects until the queue is at max capacity. Respects thread interrupt
    public static void enqueueToCapacity(TestableQueue testQueue) {
        while (testQueue.getLength() < testQueue.getCapacity()) {
            if (Thread.currentThread().isInterrupted()) {
                // Our enqueue method doesn't throw InterruptedException, so we need to handle interruption.
                System.out.print("Breaking out of enqueue to capacity thread.");
                break;
            }

            testQueue.enqueue(new Object());
        }
    }



    /////
    ///// General Testing and Printing Helpers
    /////

    // Waits for custom timeout then checks if the test thread has completed. Prints error if not.
    public static boolean checkWaitTimeout(Future testFuture, int timeoutLength) throws InterruptedException{
        // Wait for completion of fill task.
        Thread.sleep(timeoutLength);

        if (!testFuture.isDone()) {
            // If we aren't done after the timeout, something is likely blocking the thread, so print error cause and fail.
            printFail("Timeout");
            return false;
        }

        return true;
    }

    // Prints a fail error string with message in red, followed by newline.
    public static void printTestHeader(String testLabel, String headerMessage) {
        System.out.print(COLOR_CYAN + testLabel + " ---> " + headerMessage + COLOR_DEFAULT);
        System.out.println();
    }

    // Prints a fail error string with message in red, followed by newline.
    public static void printFail(String failString) {
        System.out.print(COLOR_RED + "FAIL (" + failString + ")" + COLOR_DEFAULT);
        System.out.println();
    }

    // Prints a success string with message in green, followed by newline.
    public static void printSuccess() {
        System.out.print(COLOR_GREEN + "Success" + COLOR_DEFAULT);
        System.out.println();
    }

    // Prints a success string with benchmark, followed by newline
    public static void printSuccess(long benchmark) {
        System.out.print(COLOR_GREEN + "Success (" + benchmark + " ms)" + COLOR_DEFAULT);
        System.out.println();
    }

    // Prints the name of the passed object runtime class in blue
    public static void printClassNameBlue(Object nameObj) {
        System.out.print(COLOR_CYAN + nameObj.getClass().getSimpleName() + COLOR_DEFAULT);
    }
    public static void printClassNamePurple(Object nameObj) {
        System.out.print(COLOR_PURPLE + nameObj.getClass().getSimpleName() + COLOR_DEFAULT);
    }
}
