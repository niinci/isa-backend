package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import rs.ac.uns.ftn.informatika.rest.dto.CareLocationDTO;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class CareLocationService {

    private final WebClient webClient;

    @Value("${message.queue.server.url}")
    private String messageQueueServerUrl;

    public CareLocationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    @Scheduled(fixedRateString = "${rabbit.location.sending.interval.ms:5000}", initialDelayString = "${rabbit.location.sending.initial.delay.ms:10000}")
    public void sendRandomLocationPeriodically() {

        long randomId = ThreadLocalRandom.current().nextLong(1, 1_000_000);

        String[] types = {"Azil", "Veterinar"};
        String[] adjectives = {"Srećni", "Mali", "Veliki", "Zlatni", "Ljubazni"};
        String animal = "Zeka";
        String[] streets = {"Zečeva", "Zekonja", "Skakača", "Ušiju", "Šargarepina"};

        String type = types[ThreadLocalRandom.current().nextInt(types.length)];
        String adj = adjectives[ThreadLocalRandom.current().nextInt(adjectives.length)];
        String street = streets[ThreadLocalRandom.current().nextInt(streets.length)];

        String name = type + " " + adj + " " + animal + " " + String.format("%04d", ThreadLocalRandom.current().nextInt(10_000));
        String address = "Street " + street + " " + ThreadLocalRandom.current().nextInt(1, 100);

        CareLocationDTO location = new CareLocationDTO(
                randomId,
                name,
                address,
                44.7871 + (Math.random() - 0.8) * 0.1,
                20.4573 + (Math.random() - 0.8) * 0.1
        );


        System.out.println("Automatic location sending: " + location.getName() + " with ID: " + location.getId());

        sendLocationToMessageQueue(location).subscribe(
                response -> System.out.println("Sent successfully: " + response),
                error -> System.err.println("Error sending location: " + error.getMessage())
        );
    }

    private Mono<String> sendLocationToMessageQueue(CareLocationDTO location) {
        String url = "/api/queues/rabbit-locations/send";
        System.out.println("Sending location to URL: " + messageQueueServerUrl + url + ", Location: " + location);

        return webClient.post()
                .uri(url)
                .body(Mono.just(location), CareLocationDTO.class)
                .retrieve()
                .bodyToMono(String.class);
    }
}