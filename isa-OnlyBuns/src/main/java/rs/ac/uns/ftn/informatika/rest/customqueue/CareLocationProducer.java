package rs.ac.uns.ftn.informatika.rest.customqueue;

import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.rest.dto.CareLocationDTO;

@Component
public class CareLocationProducer {

    private final DirectMessageQueue<CareLocationDTO> queue;

    public CareLocationProducer(DirectMessageQueue<CareLocationDTO> queue) {
        this.queue = queue;
    }

    public void sendCareLocation(CareLocationDTO location) {
        System.out.println("Producer šalje lokaciju: " + location.getName());
        queue.send(location);
    }
}