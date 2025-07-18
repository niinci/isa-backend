package rs.ac.uns.ftn.informatika.rest.customqueue;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.rest.dto.CareLocationDTO;
import rs.ac.uns.ftn.informatika.rest.domain.CareLocation;
import rs.ac.uns.ftn.informatika.rest.repository.CareLocationRepository;

@Component
public class CareLocationConsumer {

    private final DirectMessageQueue<CareLocationDTO> queue;
    private final CareLocationRepository repository;

    public CareLocationConsumer(DirectMessageQueue<CareLocationDTO> queue, CareLocationRepository repository) {
        this.queue = queue;
        this.repository = repository;
    }

    @PostConstruct
    public void startConsuming() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    CareLocationDTO dto = queue.receive();
                    System.out.println("Consumer primio lokaciju: " + dto.getName());

                    CareLocation entity = new CareLocation();
                    entity.setName(dto.getName());
                    entity.setAddress(dto.getAddress());
                    entity.setLatitude(dto.getLatitude());
                    entity.setLongitude(dto.getLongitude());

                    repository.save(entity);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}