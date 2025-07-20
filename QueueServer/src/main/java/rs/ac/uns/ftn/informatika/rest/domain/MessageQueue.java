package rs.ac.uns.ftn.informatika.rest.domain;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageQueue<T> {
    private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
    private final String name;

    public MessageQueue(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void enqueue(T message) {
        queue.offer(message);
        System.out.println("Message added to queue '" + name + "': " + message);
    }

    public T dequeue() {
        T message = queue.poll();
        if (message != null) {
            System.out.println("Message removed from queue '" + name + "': " + message);
        } else {
            System.out.println("Queue '" + name + "' is empty.");
        }
        return message;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}