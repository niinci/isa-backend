package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CleanupScheduler {

    @Autowired
    private RateLimiterService rateLimiterService;

    /**
     * Pokreće cleanup stare rate limiter statistike svakih 5 minuta
     */
    @Scheduled(fixedRate = 300000) // 5 minuta = 300000 ms
    public void cleanupRateLimiterData() {
        rateLimiterService.cleanupOldEntries();
    }
}