package rs.ac.uns.ftn.informatika.rest.customqueue;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.rest.dto.CareLocationDTO;

@Component
public class RabbitMQProducer {
    private final AmqpTemplate amqpTemplate;

    @Value("${care.rabbitmq.exchange.name}")
    private String exchange;

    @Value("${care.rabbitmq.routing.key}")
    private String routingKey;

    public RabbitMQProducer(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendMessage(CareLocationDTO dto) {
        System.out.println("RabbitMQ šalje lokaciju: " + dto.getName());
        amqpTemplate.convertAndSend(exchange, routingKey, dto);
    }
}