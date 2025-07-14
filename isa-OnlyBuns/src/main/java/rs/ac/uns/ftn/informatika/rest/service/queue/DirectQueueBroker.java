package rs.ac.uns.ftn.informatika.rest.service.queue;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.dto.LocationMsg;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class DirectQueueBroker {
    private final Map<String, Deque<LocationMsg>> queues = new ConcurrentHashMap<>();

    public void declareQueue(String name) {
        queues.computeIfAbsent(name, k -> new ConcurrentLinkedDeque<>());
    }

    public void publish(String queue, LocationMsg msg) {
        declareQueue(queue);
        queues.get(queue).addLast(msg);
    }

    public LocationMsg consume(String queue) {
        declareQueue(queue);
        return queues.get(queue).pollFirst();
    }

    public List<LocationMsg> peekAll(String queue) {
        return new ArrayList<>(queues.getOrDefault(queue, new LinkedList<>()));
    }
}
