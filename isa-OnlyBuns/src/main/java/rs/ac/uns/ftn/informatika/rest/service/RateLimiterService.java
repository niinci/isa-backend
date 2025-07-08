package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.Role;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RateLimiterService {

    private final ConcurrentMap<String, UserRequestTracker> requestTrackers = new ConcurrentHashMap<>();

    // Rate limits per minute
    private static final int UNAUTHENTICATED_LIMIT = 10;
    private static final int REGISTERED_USER_LIMIT = 50;
    private static final int ADMIN_LIMIT = 100;

    public boolean isRequestAllowed(String identifier, Role role) {
        String key = identifier + "_" + role.name();

        UserRequestTracker tracker = requestTrackers.computeIfAbsent(key,
                k -> new UserRequestTracker());

        return tracker.isRequestAllowed(getLimitForRole(role));
    }

    private int getLimitForRole(Role role) {
        switch (role) {
            case UNAUTHENTICATED:
                return UNAUTHENTICATED_LIMIT;
            case REGISTERED_USER:
                return REGISTERED_USER_LIMIT;
            case ADMIN:
                return ADMIN_LIMIT;
            default:
                return UNAUTHENTICATED_LIMIT;
        }
    }

    private static class UserRequestTracker {
        private int requestCount = 0;
        private LocalDateTime windowStart = LocalDateTime.now();

        public synchronized boolean isRequestAllowed(int limit) {
            LocalDateTime now = LocalDateTime.now();

            // Reset window if more than 1 minute has passed
            if (now.isAfter(windowStart.plusMinutes(1))) {
                requestCount = 0;
                windowStart = now;
            }

            if (requestCount >= limit) {
                return false;
            }

            requestCount++;
            return true;
        }
    }

    public void cleanupOldEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        requestTrackers.entrySet().removeIf(entry ->
                entry.getValue().windowStart.isBefore(cutoff));
    }
}