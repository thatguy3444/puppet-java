package com.theodorersmith.queue;

// Copyright Theodore Smith, 2018 - All Rights Reserved

import java.util.concurrent.atomic.AtomicInteger;

// This implementation uses two objects for synchronization - a readLockObject (dequeue) and a writeLockObject (enqueue).
// I am using a circular array, so we should be able to write to a free slot without worrying about concurrent
// reads, and vice versa. Instead of managing the head index and length, we are going to track the head, tail, and
// length. The head will only be used by the dequeue, and the tail only by the enqueue, so the only shared value
// that needs to be threadsafe between enqueue and dequeue threads is the queue length - we give it thread safety by
// using the atomic library.
public class ProducerConsumerConcurrentQueue<T> implements ProducerConsumerQueue<T> {

    /// Member Variables
    private final T[] queueArray; // The internal array backing the queue. (Treated as circular array around headIdx)

    // We are going to synchronize with two objects - one for reading from the queue and one for writing.
    private final Object writeLockObj = new Object();
    private int headIdx;
    private final Object readLockObj = new Object();
    private int tailIdx ;

    // The length can be written from within both the enqueue and dequeue sync blocks, so must be threadsafe.
    private AtomicInteger length;

    /// Construction and Initialization
    @SuppressWarnings("unchecked")
    public ProducerConsumerConcurrentQueue(int capacity) {
        if (capacity < 1) {
            // If the capacity is less than 1, throw an Illegal Argument Exception
            // Note: This is an ApplicationException, so it won't be checked at compile time
            throw new IllegalArgumentException("Queue capacity must be greater than zero");
        }

        this.queueArray = (T[])new Object[capacity];
        this.headIdx = 0;
        this.tailIdx = 0;
        this.length = new AtomicInteger(0);
    }

    // Gets the maximum capacity of the queue
    public synchronized int getCapacity() {
        return this.queueArray.length;
    }

    // IProducerConsumerQueue Implementation
    @Override
    /// Threadsafe add an item to the end of the queue. Blocks if queue is full.
    public void enqueue(T item) {
        // Sync on the write lock
        synchronized (writeLockObj) {
            // Check if we have space to add new items
            while (length.get() == getCapacity()) {
                // The length is at capacity. Block until we have free space
                try {
                    writeLockObj.wait();
                } catch (InterruptedException iEx) {
                    // NOTE: Would normally rethrow an InterruptedException in a place like this; however the interface doesn't
                    // throw InterruptedException, so we can't rethrow. Instead print message to system.out, reset the interrupt
                    // flag, and exit. Note that this means a thread interrupt while this method is blocking will unblock as
                    // soon as possible WITHOUT enqueuing the item. (And future enqueues will be interrupted out of wait() until
                    // the interrupt is cleared or otherwise handled.)
                    System.out.println("com.theodorersmith.queue.ProducerConsumerConcurrentQueue->enqueue: Thread " + Thread.currentThread().getName() + " received an interrupt. Exiting without enqueuing item.");
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            // We have space for items
            // First, Add our item to the tail of the array and update the tail to point to the next (Circular) cell.
            queueArray[tailIdx] = item;
            tailIdx = (tailIdx + 1) % queueArray.length;

            // Next, we are going to increment the array length
            int oldLength = length.getAndIncrement();

            // If the old length was zero, we want to wake up all waiting dequeue threads (we have to notify in a dequeue
            // sync loop, so here just return if it was bigger than zero
            if (oldLength > 0) {
                return;
            }
        }
        // If we are still here, grab the read lock and wake up all the read threads
        synchronized (readLockObj) {
            readLockObj.notifyAll();
        }
    }

    @Override
    /// Threadsafe pops an item off the front of the queue. Blocks if queue is full
    public T dequeue() {
        // Sync on the read lock
        T item;
        synchronized (readLockObj) {
            // Check if there are items to pop off the queue
            while (length.get() == 0) {
                // The length is at capacity. Block until we have free space
                try {
                    readLockObj.wait();
                } catch (InterruptedException iEx) {
                    System.out.println("com.theodorersmith.queue.ProducerConsumerConcurrentQueue->dequeue: Thread " + Thread.currentThread().getName() + " received an interrupt. Exiting without enqueuing item.");
                    Thread.currentThread().interrupt();
                    return null;
                }
            }

            // We have a non-empty queue
            // First get our item from the front of the queue.
            item = queueArray[headIdx];

            // Now decrement the length and update the head index
            headIdx = (headIdx + 1) % queueArray.length;
            int oldLength = length.getAndDecrement();

            // If the old length was at capacitiy, we want to notify all the waiting enqueue threads.
            if (oldLength < getCapacity()) {
                // return early if we don't need to notify
                return item;
            }
        }
        // If we are still here, grab the write lock and wake up all the write threads
        synchronized (writeLockObj) {
            writeLockObj.notifyAll();
        }

        return item;
    }
}
