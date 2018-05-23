package com.theodorersmith.queue.test.implementations;

// Copyright Theodore Smith, 2018 - All Rights Reserved

import com.theodorersmith.queue.test.TestableQueue;

// Very basic implementation of a producer consumer queue. It uses synchronized methods and notifies all waiting threeads
// when the queue becomes not-full or not-empty. Works fine, but there are likely more efficient implementations.
public final class ConcurrentNotifyAllArrayPCQueue<T> implements TestableQueue<T> {

    /// Member Variables
    private int headIdx;  // The index of the first item in the queue
    private int length;  // The current length of the queue
    private final T[] queueArray; // The internal array backing the queue. (Treated as circular array around headIdx)

    /// Construction and Initialization
    @SuppressWarnings("unchecked")
    public ConcurrentNotifyAllArrayPCQueue(int capacity) {
        if (capacity < 1) {
            // If the capacity is less than 1, throw an Illegal Argument Exception
            // Note: This is an ApplicationException, so it won't be checked at compile time
            throw new IllegalArgumentException("Queue capacity must be greater than zero");
        }

        this.queueArray = (T[])new Object[capacity];
        resetQueue();
    }

    /// ITestableQueue Implementation (Just used for testing)
    @Override
    // Gets the maximum capacity of the queue
    public synchronized int getCapacity() {
        return this.queueArray.length;
    }
    @Override
    // Gets the current length of the queue
    public synchronized int getLength() {
        return this.length;
    }
    @Override
    // Resets queue to a default empty state
    public synchronized void resetQueue() {
        this.headIdx = 0;
        this.length = 0;
    }

    // IProducerConsumerQueue Implementation
    @Override
    /// Threadsafe add an item to the end of the queue. Blocks if queue is full.
    public synchronized void enqueue(T item) {
        // Check if we have space to add new items
        while (length == queueArray.length) {
            // Block until we have free space
            try {
                wait();
            } catch (InterruptedException iEx) {
                // NOTE: Would normally rethrow an InterruptedException in a place like this; however the interface doesn't
                // throw InterruptedException, so we can't rethrow. Instead print message to system.out, reset the interrupt
                // flag, and exit. Note that this means a thread interrupt while this method is blocking will unblock as
                // soon as possible WITHOUT enqueuing the item. (And future enqueues will be interrupted out of wait() until
                // the interrupt is cleared or otherwise handled.)
                System.out.println("com.theodorersmith.queue.test.implementations.ConcurrentNotifyAllArrayPCQueue->enqueue: Thread " + Thread.currentThread().getName() + " received an interrupt. Exiting without enqueuing item.");
                Thread.currentThread().interrupt();
                return;
            }
        }

        // If we are adding the first item in the queue, we want to notify all waiting threads (they may be waiting to dequeue)
        if (length == 0) {
            notifyAll();
        }

        // Enqueue the item at the end of the (circular) array and update the queue length
        int nextQueueIdx = (headIdx + length) % queueArray.length;
        this.queueArray[nextQueueIdx] = item;
        this.length++;
    }

    @Override
    /// Threadsafe pops an item off the front of the queue. Blocks if queue is full
    public synchronized T dequeue() {
        // Check if we have space to remove new items
        while (length == 0) {
            // Block until we have at least one item in the queue
            try {
                wait();
            } catch (InterruptedException iEx) {
                // NOTE: A thread interrupt while this method is blocking will unblock as soon as possible, reset the interrupt flag, and return null;
                System.out.println("com.theodorersmith.queue.test.implementations.ConcurrentNotifyAllArrayPCQueue->dequeue: Thread " + Thread.currentThread().getName() + " received an interrupt. Exiting returning null.");
                Thread.currentThread().interrupt();
                return null;
            }
        }

        // If we are removing an item from a full queue, we want to notify all waiting threads (they may be waiting te enqueue)
        if (length == queueArray.length) {
            notifyAll();
        }

        // Pop an item off the front of the queue and update the queue header index
        T popItem = this.queueArray[headIdx];
        this.headIdx = (headIdx + 1) % queueArray.length;
        this.length--;
        return popItem;
    }
}
