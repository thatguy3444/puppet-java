package com.theodorersmith.queue;

public interface ProducerConsumerQueue<T>
{
    public void enqueue(T item);
    public T dequeue();
}