package rs.ac.uns.ftn.informatika.rest.service;


import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.MessageQueue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DirectMessageQueueService {

    private final Map<String, MessageQueue<Object>> queues = new ConcurrentHashMap<>();

    public MessageQueue<Object> getOrCreateQueue(String queueName) {
        return queues.computeIfAbsent(queueName, name -> {
            System.out.println("New queue created: " + name);
            return new MessageQueue<>(name);
        });
    }

    public void sendMessage(String queueName, Object message) {
        MessageQueue<Object> queue = getOrCreateQueue(queueName);
        queue.enqueue(message);
    }

    public Object receiveMessage(String queueName) {
        MessageQueue<Object> queue = getOrCreateQueue(queueName);
        return queue.dequeue();
    }
}