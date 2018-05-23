package com.theodorersmith.queue;

import com.theodorersmith.queue.test.PCQueueTestManager;

public class ProducerConsumerTestInterface {

    public static void main(String[] args) {
        run();
    }

    // Runs a super simple input loop around a switch statement. Reprints the instructions if it doesn't get valid input
    public static void run() {
        try {
            printInstructions();
            while (processInput((char) System.in.read())) {
            }
        } catch (Exception ex) {
            System.out.println("Exception Encountered: " + ex.getMessage());
        } finally {
            System.exit(0);
        }
    }

    public static void printInstructions() {
        System.out.println("");
        System.out.println("--------------------------------------------------------");
        System.out.println("--- Puppet Java Exercise -------------------------------");
        System.out.println("");
        System.out.println(" A) Run Basic Exercise Test Case (5 x 5 for 1000 elements)");
        System.out.println("    (This uses the DoubleSyncArray implementation below)");
        System.out.println("");
        System.out.println("--- Other Tests and Implementations... ---");
        System.out.println("");
        System.out.println(" B) Run Benchmarks On All Implementations of Blocking PC Queue");
        System.out.println(" F) Run Full Tests On All Implementations of Blocking PC Queue");
        System.out.println("");
        System.out.println(" 1) Test ConcurrentNotifyAllArrayPCQueue");
        System.out.println(" 2) Test ConcurrentTwoConditionLockArrayPCQueue");
        System.out.println(" 3) Test ConcurrentDoubleSyncArrayPCQueue (* default implementation)");
        System.out.println("");
        System.out.println("--------------------------------------------------------");
        System.out.println(" Q) Quit");
        System.out.println("--------------------------------------------------------");
        System.out.println("");
    }

    public static boolean processInput(char c) {
        switch (c) {
            case 'a':
            case 'A':
                PCQueueTestManager.runDefaultTest();
                return true;
            case 'f':
            case 'F':
                PCQueueTestManager.testAllQueues();
                return true;
            case 'b':
            case 'B':
                PCQueueTestManager.benchAllQueues();
                return true;
            case '1':
                PCQueueTestManager.testNotifyAllQueue();
                return true;
            case '2':
                PCQueueTestManager.testTwoConditionQueue();
                return true;
            case '3':
                PCQueueTestManager.testDoubleSyncQueue();
                return true;
            case 'q':
            case 'Q':
                return false;
            default:
                printInstructions();
                return true;
        }
    }
}
