package com.theodorersmith.queue.test.implementations;

// Copyright Theodore Smith, 2018 - All Rights Reserved

import com.theodorersmith.queue.test.TestableQueue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Implementation of a producer-consumer queue using a lock with two conditions. Still isn't that efficient, because
// queue and unqueue use the same lock. This implementation notifies a single waiting thread when things are enqueued
// or dequeued (instead of notifying all when the queue becomes not-empty or not-full)
public final class ConcurrentTwoConditionLockArrayPCQueue<T> implements TestableQueue<T> {

    /// Member Variables
    private int headIdx;  // The index of the first item in the queue
    private int length;  // The current length of the queue
    private final T[] queueArray; // The internal array backing the queue. (Treated as circular array around headIdx)

    private Lock queueLock = new ReentrantLock();
    private Condition queueNotFullCondition = queueLock.newCondition();
    private Condition queueNotEmptyCondition = queueLock.newCondition();

    /// Construction and Initialization
    public ConcurrentTwoConditionLockArrayPCQueue(int capacity) {
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
    public int getCapacity() {
        return this.queueArray.length;
    }
    @Override
    // Gets the current length of the queue
    public int getLength() {
        queueLock.lock();
        int retVal = this.length;
        queueLock.unlock();
        return retVal;
    }
    @Override
    // Resets queue to a default empty state
    public void resetQueue() {
        queueLock.lock();
        this.headIdx = 0;
        this.length = 0;
        queueLock.unlock();
    }

    // IProducerConsumerQueue Implementation
    @Override
    /// Threadsafe add an item to the end of the queue. Blocks if queue is full.
    public void enqueue(T item) {
        // Acquire the lock
        queueLock.lock();

        // Check if we have space to add new items
        while (length == queueArray.length) {
            // The queue is full and we don't have space to add items. Surrender lock and wait for the queue to have space.
            try {
                queueNotFullCondition.await();
            } catch (InterruptedException iEx) {
                // NOTE: I would normally rethrow an InterruptedException in a place like this; however the interface doesn't
                // throw InterruptedException, so we can't rethrow. Instead print message to system.out, reset the interrupt
                // flag, and exit. Note that this means a thread interrupt while this method is blocking will unblock as
                // soon as possible WITHOUT enqueuing the item. (And future enqueues will be interrupted out of wait() until
                // the interrupt is cleared or otherwise handled.)
                System.out.println("com.theodorersmith.queue.test.implementations.ConcurrentTwoConditionLockArrayPCQueue->enqueue: Thread " + Thread.currentThread().getName() + " received an interrupt. Exiting without enqueuing item.");
                Thread.currentThread().interrupt();
                return;
            }
        }

        // We are adding something, so the queue is about to be not empty. Signal someone waiting for the queue to be not empty.
        queueNotEmptyCondition.signal();

        // Enqueue the item at the end of the (circular) array and update the queue length
        int nextQueueIdx = (headIdx + length) % queueArray.length;
        this.queueArray[nextQueueIdx] = item;
        this.length++;

        // Surrender the lock
        queueLock.unlock();
    }

    @Override
    /// Threadsafe pops an item off the front of the queue. Blocks if queue is full
    public synchronized T dequeue() {
        queueLock.lock();

        // Check if there is anything to remove
        while (length == 0) {
            // We don't have anything to remove from the queue. Surrender the lock and wait for the not empty condition.
            try {
                queueNotEmptyCondition.await();
            } catch (InterruptedException iEx) {
                // NOTE: A thread interrupt while this method is blocking will unblock as soon as possible, reset the interrupt flag, and return null;
                System.out.println("com.theodorersmith.queue.test.implementations.ConcurrentTwoConditionLockArrayPCQueue->dequeue: Thread " + Thread.currentThread().getName() + " received an interrupt. Exiting returning null.");
                Thread.currentThread().interrupt();
                return null;
            }
        }

        // We are removing something, so the queue is about to have space to add. Signal someone waiting on queueNotFull
        queueNotFullCondition.signal();

        // Pop an item off the front of the queue and update the queue header index
        T popItem = this.queueArray[headIdx];
        this.headIdx = (headIdx + 1) % queueArray.length;
        this.length--;

        // Surrender the lock
        queueLock.unlock();

        return popItem;
    }
}
