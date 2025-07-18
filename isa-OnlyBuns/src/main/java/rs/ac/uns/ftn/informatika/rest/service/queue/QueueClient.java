/*package rs.ac.uns.ftn.informatika.rest.service.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.CareLocation;
import rs.ac.uns.ftn.informatika.rest.dto.LocationMsg;
import rs.ac.uns.ftn.informatika.rest.repository.CareLocationRepository;

@Service
@Slf4j
public class QueueClient {
    private final DirectQueueBroker broker;
    private final CareLocationRepository repo;

    public QueueClient(DirectQueueBroker broker, CareLocationRepository repo) {
        this.broker = broker;
        this.repo = repo;
    }

    @Scheduled(fixedDelay = 2000)
    public void pollQueue() {
        LocationMsg msg;
        while ((msg = broker.consume("rabbit-care")) != null) {
            CareLocation loc = new CareLocation(msg.getId(), msg.getName(), msg.getLat(), msg.getLng());
            repo.save(loc);
            log.info("Saved care location: {}", msg.getName());
        }
    }
}
*/