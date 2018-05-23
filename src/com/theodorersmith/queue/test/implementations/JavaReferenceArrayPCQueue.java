package com.theodorersmith.queue.test.implementations;

// Copyright Theodore Smith, 2018 - All Rights Reserved

import com.theodorersmith.queue.test.TestableQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

// This is an implementation of the ProducerConsumerQueue that uses the java LinkedBlockingQueue. This is NOT a solution
// to the coding challenge - it is only included as a reference because I was curious about how my implementations stacked up.
public final class JavaReferenceArrayPCQueue<T> implements TestableQueue<T> {

    /// Member Variables
    private final int capacity;
    private final BlockingQueue<T> javaBlockingQueue;

    /// Construction and Initialization
    public JavaReferenceArrayPCQueue(int capacity) {
        this.capacity = capacity;
        javaBlockingQueue = new ArrayBlockingQueue<T>(this.capacity);
    }

    /// ITestableQueue Implementation (Just used for testing)
    @Override
    // Gets the maximum capacity of the queue
    public int getCapacity() {
        return this.capacity;
    }
    @Override
    // Gets the current length of the queue
    public int getLength() {
        return javaBlockingQueue.size();
    }
    @Override
    // Clears all items from the queue and resets to a default state
    public void resetQueue() {
        javaBlockingQueue.clear();
    }

    // IProducerConsumerQueue Implementation
    @Override
    /// Threadsafe add an item to the end of the queue. Blocks if queue is full.
    public void enqueue(T item) {
        try {
            javaBlockingQueue.put(item);
        } catch (InterruptedException iEx) {
            // NOTE: Would normally rethrow an InterruptedException in a place like this; however the interface doesn't
            // throw InterruptedException, so we can't rethrow. Instead print message to system.out, reset the interrupt
            // flag, and exit. Note that this means a thread interrupt while this method is blocking will unblock as
            // soon as possible WITHOUT enqueuing the item. (And future enqueues will be interrupted out of wait() until
            // the interrupt is cleared or otherwise handled.)
            System.out.println("com.theodorersmith.queue.test.implementations.JavaReferenceArrayPCQueue->enqueue: Thread " + Thread.currentThread().getName() + " received an interrupt. Exiting without enqueuing item.");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    /// Threadsafe pops an item off the front of the queue. Blocks if queue is full
    public T dequeue() {
        try {
            return javaBlockingQueue.take();
        } catch (InterruptedException iEx) {
            // NOTE: A thread interrupt while this method is blocking will unblock as soon as possible, reset the interrupt flag, and return null;
            System.out.println("com.theodorersmith.queue.test.implementations.JavaReferenceArrayPCQueue->dequeue: Thread " + Thread.currentThread().getName() + " received an interrupt. Exiting returning null.");
            Thread.currentThread().interrupt();
            return null;
        }
    }
}



