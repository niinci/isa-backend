package rs.ac.uns.ftn.informatika.rest.rabbitmq.producer;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.dto.AdNotificationDTO;

@Service
public class AdNotificationProducer {

    private final AmqpTemplate rabbitTemplate; // AmqpTemplate je Springov interfejs za slanje i primanje AMQP poruka

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public AdNotificationProducer(AmqpTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAdNotification(AdNotificationDTO adNotification) {
        // Logovanje poruke prije slanja (za debagovanje)
        System.out.println("Sending ad notification: " + adNotification);

        // Slanje poruke na RabbitMQ
        rabbitTemplate.convertAndSend(exchange, routingKey, adNotification);

        System.out.println("Ad notification sent successfully!");
    }
}
