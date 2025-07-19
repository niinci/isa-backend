package rs.ac.uns.ftn.informatika.rest.rabbitmq.consumer;  

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.rest.dto.AdNotificationDTO;

@Component
public class AdNotificationConsumer {

    @RabbitListener(queues = "#{queue.name}")
    public void receiveAdNotification(AdNotificationDTO adNotification) {
        try {
            System.out.println("Received ad notification: " + adNotification);
            System.out.println("Processing notification for Post ID: " + adNotification.getPostId());
            System.out.println("Description: " + adNotification.getPostDescription());
            System.out.println("Posted by user: " + adNotification.getUsername());
            System.out.println("Time: " + adNotification.getNotificationTime());
            System.out.println("------------------------------------");
        } catch (Exception e) {
            System.err.println("Error processing ad notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}