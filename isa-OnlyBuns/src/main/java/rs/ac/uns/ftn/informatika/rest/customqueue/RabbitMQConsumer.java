package rs.ac.uns.ftn.informatika.rest.customqueue;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.rest.dto.CareLocationDTO;

@Component
public class RabbitMQConsumer {

    private final DirectMessageQueue<CareLocationDTO> queue;

    public RabbitMQConsumer(DirectMessageQueue<CareLocationDTO> queue) {
        this.queue = queue;
    }

    @RabbitListener(queues = "${care.rabbitmq.queue.name}")
    public void receiveMessage(CareLocationDTO location) {
        System.out.println("RabbitMQ primio poruku: " + location.getName());
        queue.send(location);  // ubacujemo poruku u tvoj red
    }
}