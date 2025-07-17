package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// Dodajte ove import-e za Logger, ako želite profesionalnije logovanje
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/lb")
public class LoadBalancerController {

    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerController.class); // <<< OD KOMENTUJTE OVU LINIJU
    // Ako koristite SLF4J Logger
    // private static final Logger logger = LoggerFactory.getLogger(LoadBalancerController.class);

    private final List<String> instances = List.of(
            "http://localhost:8081",
            "http://localhost:8082"
    );

    // currentIndex treba da bude AtomicInteger ili slično u višethreadnom okruženju,
    // ali za ovaj jednostavan round-robin sa synchronized metodom je OK.
    private AtomicInteger currentIndex = new AtomicInteger(0);

    private final RestTemplate restTemplate = new RestTemplate();

    private synchronized String getNextInstanceAvailable() {
        int attempts = 0;
        while (attempts < instances.size()) {
            int index = currentIndex.getAndUpdate(i -> (i + 1) % instances.size());
            String instance = instances.get(index);

            // Provera dostupnosti instance
            try {
                restTemplate.getForEntity(instance + "/actuator/health", String.class);
                return instance;
            } catch (Exception e) {
                logger.warn("!!! Instanca {} nije dostupna. Pokušavam sledeću...", instance);
            }

            attempts++;
        }
        return null;
    }


    private String buildUrl(String base, String path) {
        return UriComponentsBuilder.fromHttpUrl(base)
                .path(path)
                .toUriString();
    }

    private ResponseEntity<String> forwardRequest(HttpMethod method, String path, HttpEntity<?> entity, HttpHeaders incomingHeaders) {
        int attempts = 0;
        int totalInstances = instances.size();

        logger.info("<<< LB: Primljen {} zahtev za putanju: {}", method.name(), path);

        while (attempts < totalInstances) {
            // Uzmi sledeću dostupnu instancu sa health check-om
            String instance = null;
            int checked = 0;

            while (checked < totalInstances) {
                int index = currentIndex.getAndUpdate(i -> (i + 1) % instances.size());
                 instance = instances.get(index);

                // Provera dostupnosti instance pozivom na /actuator/health
                try {
                    restTemplate.getForEntity(instance + "/actuator/health", String.class);
                    // Instanca dostupna, prekini proveru
                    break;
                } catch (Exception e) {
                    logger.warn("!!! Instanca {} nije dostupna (health check failed), preskačem...", instance);
                    instance = null;  // nije dostupna
                }
                checked++;
            }

            if (instance == null) {
                // Nema dostupnih instanci
                logger.error("XXX LB: Nema dostupnih instanci za putanju: {}", path);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Nema dostupnih instanci.");
            }

            String url = buildUrl(instance, path);
            logger.info("--- LB: Pokušavam proslediti zahtev na instancu: {} (Pokušaj: {})", url, attempts + 1);

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                if (incomingHeaders.getFirst(HttpHeaders.AUTHORIZATION) != null) {
                    headers.set(HttpHeaders.AUTHORIZATION, incomingHeaders.getFirst(HttpHeaders.AUTHORIZATION));
                }

                HttpEntity<?> newEntity = new HttpEntity<>(entity != null ? entity.getBody() : null, headers);
                ResponseEntity<String> response = restTemplate.exchange(url, method, newEntity, String.class);

                logger.info("+++ LB: Zahtev uspešno prosleđen instanci: {}. Status odgovor: {}", url, response.getStatusCode());
                return response;
            } catch (Exception e) {
                logger.warn("!!! LB: Greška kod instance: {} -> {}", url, e.getMessage());
                attempts++;
            }
        }

        logger.error("XXX LB: SVE instance su nedostupne za putanju: {}", path);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Sve instance su nedostupne.");
    }



    // GET /api/posts
    @GetMapping("/api/posts")
    public ResponseEntity<String> getAllPosts(@RequestHeader HttpHeaders headers) {
        return forwardRequest(HttpMethod.GET, "/api/posts", null, headers);
    }

    // POST /api/posts
    @PostMapping("/api/posts")
    public ResponseEntity<String> createPost(@RequestHeader HttpHeaders headers, @RequestBody String postDtoJson) {
        HttpEntity<String> entity = new HttpEntity<>(postDtoJson);
        return forwardRequest(HttpMethod.POST, "/api/posts", entity, headers);
    }

    // DELETE /api/posts/{id}
    @DeleteMapping("/api/posts/{id}")
    public ResponseEntity<String> deletePost(@RequestHeader HttpHeaders headers, @PathVariable Long id) {
        return forwardRequest(HttpMethod.DELETE, "/api/posts/" + id, null, headers);
    }


    // PUT /api/posts/{id}
    @PutMapping("/api/posts/{id}")
    public ResponseEntity<String> updatePost(@RequestHeader HttpHeaders headers,
                                             @PathVariable Long id,
                                             @RequestBody String postDtoJson) {
        HttpEntity<String> entity = new HttpEntity<>(postDtoJson);
        return forwardRequest(HttpMethod.PUT, "/api/posts/" + id, entity, headers);
    }

}