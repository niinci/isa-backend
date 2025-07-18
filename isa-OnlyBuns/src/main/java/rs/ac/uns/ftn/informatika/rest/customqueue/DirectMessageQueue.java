package rs.ac.uns.ftn.informatika.rest.customqueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DirectMessageQueue<T> {
    private final BlockingQueue<T> queue = new LinkedBlockingQueue<>();

    public void send(T message) {
        queue.offer(message);
    }

    public T receive() throws InterruptedException {
        return queue.take();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}