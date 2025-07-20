package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import rs.ac.uns.ftn.informatika.rest.domain.CareLocation;
import rs.ac.uns.ftn.informatika.rest.dto.CareLocationDTO;
import rs.ac.uns.ftn.informatika.rest.repository.CareLocationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CareLocationService {

    private final CareLocationRepository careLocationRepository;
    private final WebClient webClient;

    @Value("${message.queue.server.url}")
    private String messageQueueServerUrl;

    public CareLocationService(CareLocationRepository careLocationRepository, WebClient.Builder webClientBuilder) {
        this.careLocationRepository = careLocationRepository;
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build(); //povezujemo se na QueueServer
    }

    public Mono<CareLocationDTO> receiveAndSaveLocation() {
        String url = messageQueueServerUrl + "/api/queues/rabbit-locations/receive";
        System.out.println("Trying to get location with URL: " + url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(CareLocationDTO.class)
                .doOnNext(dto -> { // Primamo DTO
                    System.out.println("Received location DTO: " + dto);

                    CareLocation entity = new CareLocation(dto.getId(), dto.getName(), dto.getAddress(), dto.getLatitude(), dto.getLongitude());
                    careLocationRepository.save(entity);
                    System.out.println("Location saved in database: " + entity);
                })
                .doOnError(error -> System.err.println("Error geting location: " + error.getMessage()))
                .onErrorResume(e -> {
                    if (e.getMessage() != null && e.getMessage().contains("204 No Content")) {
                        System.out.println("No new messages in queue.");
                        return Mono.empty();
                    }
                    return Mono.error(e);
                });
    }

    @Scheduled(fixedRateString = "${app.location.receive.interval.ms:5000}", initialDelayString = "${app.location.receive.initial.delay.ms:10000}")
    public void fetchAndSaveLocationsPeriodically() {
        System.out.println("Scheduled task: Trying to receive location...");
        receiveAndSaveLocation()
                .subscribe(
                        dto -> System.out.println("Scheduled task: Location successfully received and saved: " + dto.getName()),
                        error -> System.err.println("Scheduled task: Error receiving location: " + error.getMessage()),
                        () -> System.out.println("Scheduled Task: There are no more locations in the queue at this time.") // Kada je Mono.empty()
                );
    }

    public List<CareLocationDTO> getAllRabbitCareLocations() {
        return careLocationRepository.findAll()
                .stream()
                .map(entity -> new CareLocationDTO(entity.getId(), entity.getName(), entity.getAddress(), entity.getLatitude(), entity.getLongitude())) // Mapiraj entitet u DTO
                .collect(Collectors.toList());
    }
}