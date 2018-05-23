package com.theodorersmith.queue.test;

import com.theodorersmith.queue.ProducerConsumerQueue;

/// This interface extends the base IProducerConsumerQueue interface by adding a getCapacity and getSize method to allow
/// easier testing of alternate implementations. IProducerConsumerQueue test implementations should be modified to implement
/// ITestableQueue and run through the PCQueueTestManager.
public interface TestableQueue<T> extends ProducerConsumerQueue<T> {
    // Gets the maximum capacity of the queue
    int getCapacity();

    // Gets the current length of the queue
    int getLength();

    /// Clears all items from the queue and resets to a default state
    void resetQueue();
}
