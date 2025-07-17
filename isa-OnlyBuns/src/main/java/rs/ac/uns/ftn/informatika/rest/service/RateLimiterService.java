package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.Role;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;

@Service
public class RateLimiterService {

    private final ConcurrentMap<String, UserRequestTracker> requestTrackers = new ConcurrentHashMap<>();

    private static final int COMMENT_REGISTERED_USER_LIMIT_PER_MINUTE = 5;
    private static final int COMMENT_REGISTERED_USER_LIMIT_PER_HOUR = 60;
    private static final int COMMENT_UNAUTHENTICATED_LIMIT_PER_MINUTE = 0; // Neautentifikovani ne mogu komentarisati

    private static final int FOLLOW_REGISTERED_USER_LIMIT_PER_MINUTE = 50;
    private static final int FOLLOW_UNAUTHENTICATED_LIMIT_PER_MINUTE = 0; // Neautentifikovani ne mogu pratiti

    private static final int LOGIN_ATTEMPT_IP_LIMIT_PER_MINUTE = 5;

    private static final int DEFAULT_UNAUTHENTICATED_LIMIT_PER_MINUTE = 60;
    private static final int DEFAULT_REGISTERED_USER_LIMIT_PER_MINUTE = 300;
    private static final int DEFAULT_ADMIN_LIMIT_PER_MINUTE = 1000;

    private static final long ONE_MINUTE_MILLIS = TimeUnit.MINUTES.toMillis(1);
    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000;
    private static final long GRACE_PERIOD_CLEANUP_MILLIS = TimeUnit.HOURS.toMillis(2); // Keep trackers for 2 hours if empty before cleaning up


    public boolean isRequestAllowed(String identifier, Role role, String requestUri, String httpMethod) {
        String actionType = getActionTypeFromUri(requestUri, httpMethod);

        String effectiveIdentifier = (actionType.equals("LOGIN_ATTEMPT")) ? identifier : identifier + "_" + role.name();

        String key = effectiveIdentifier + "_" + actionType;

        UserRequestTracker tracker = requestTrackers.computeIfAbsent(key,
                k -> new UserRequestTracker());

        // Specifična logika za komentare koji imaju dva vremenska limita
        if ("COMMENT_POST".equals(actionType) && role == Role.REGISTERED_USER) {
            System.out.println("RateLimiterService: Checking COMMENT_POST request (two limits):");
            System.out.println("  Identifier: " + identifier);
            System.out.println("  Role: " + role);
            System.out.println("  Effective Key: " + key);
            System.out.println("  Minute Limit: " + COMMENT_REGISTERED_USER_LIMIT_PER_MINUTE);
            System.out.println("  Hour Limit: " + COMMENT_REGISTERED_USER_LIMIT_PER_HOUR);

            return tracker.isRequestAllowed(
                    COMMENT_REGISTERED_USER_LIMIT_PER_MINUTE, ONE_MINUTE_MILLIS,
                    COMMENT_REGISTERED_USER_LIMIT_PER_HOUR, ONE_HOUR_MILLIS,
                    key
            );
        } else {
            // Postojeća logika za sve ostale akcije koje imaju samo jedan limit (po minuti)
            int limit = getLimitForActionAndRole(actionType, role);
            long timeWindowMillis = getTimeWindowForActionType(actionType);

            System.out.println("RateLimiterService: Checking DEFAULT request:");
            System.out.println("  Identifier: " + identifier);
            System.out.println("  Role: " + role);
            System.out.println("  Request URI: " + requestUri);
            System.out.println("  HTTP Method: " + httpMethod);
            System.out.println("  Determined Action Type: " + actionType);
            System.out.println("  Effective Key: " + key);
            System.out.println("  Calculated Limit: " + limit);
            System.out.println("  Time Window (ms): " + timeWindowMillis);

            // Za akcije koje nemaju satni limit, prosleđujemo vrlo visok limit i dug prozor
            return tracker.isRequestAllowed(
                    limit, timeWindowMillis,
                    Integer.MAX_VALUE, TimeUnit.DAYS.toMillis(365), // Efektivno nema satnog limita
                    key
            );
        }
    }

    private int getLimitForActionAndRole(String actionType, Role role) {
        int limit;
        switch (actionType) {
            case "COMMENT_POST":
                limit = (role == Role.REGISTERED_USER) ? COMMENT_REGISTERED_USER_LIMIT_PER_MINUTE : COMMENT_UNAUTHENTICATED_LIMIT_PER_MINUTE;
                break;
            case "FOLLOW_USER":
                limit = (role == Role.REGISTERED_USER) ? FOLLOW_REGISTERED_USER_LIMIT_PER_MINUTE : FOLLOW_UNAUTHENTICATED_LIMIT_PER_MINUTE;
                break;
            case "LOGIN_ATTEMPT":
                limit = LOGIN_ATTEMPT_IP_LIMIT_PER_MINUTE;
                break;
            case "LIKE_POST":
                if (role == Role.UNAUTHENTICATED) {
                    limit = 0;
                } else {
                    limit = DEFAULT_REGISTERED_USER_LIMIT_PER_MINUTE;
                }
                break;
            default:
                switch (role) {
                    case UNAUTHENTICATED: limit = DEFAULT_UNAUTHENTICATED_LIMIT_PER_MINUTE; break;
                    case REGISTERED_USER: limit = DEFAULT_REGISTERED_USER_LIMIT_PER_MINUTE; break;
                    case ADMIN: limit = DEFAULT_ADMIN_LIMIT_PER_MINUTE; break;
                    default: limit = DEFAULT_UNAUTHENTICATED_LIMIT_PER_MINUTE; break;
                }
        }
        System.out.println("RateLimiterService.getLimitForActionAndRole: actionType=" + actionType + ", role=" + role + ", limit=" + limit);
        return limit;
    }

    private long getTimeWindowForActionType(String actionType) {
        long timeWindow;
        switch (actionType) {
            case "COMMENT_POST":
            case "FOLLOW_USER":
            case "LOGIN_ATTEMPT":
                timeWindow = TimeUnit.MINUTES.toMillis(1);
                break;
            default:
                timeWindow = TimeUnit.MINUTES.toMillis(1);
                break;
        }
        System.out.println("RateLimiterService.getTimeWindowForActionType: actionType=" + actionType + ", timeWindow=" + timeWindow + "ms");
        return timeWindow;
    }

    private String getActionTypeFromUri(String uri, String httpMethod) {
        String actionType = "DEFAULT";

        if (uri.matches("/api/comments/post/\\d+")) {
            if ("POST".equalsIgnoreCase(httpMethod)) {
                actionType = "COMMENT_POST";
            }
        }
        // Samo POST zahtevi za /api/follows/follow* se tretiraju kao "FOLLOW_USER" za rate limit
        else if (uri.matches("/api/follows/follow.*") && "POST".equalsIgnoreCase(httpMethod)) {
            actionType = "FOLLOW_USER";
        }
        else if (uri.equals("/api/userAccount/login")) {
            actionType = "LOGIN_ATTEMPT";
        } else if (uri.matches("/api/posts/\\d+/like")) {
            actionType = "LIKE_POST";
        }

        System.out.println("RateLimiterService.getActionTypeFromUri: URI='" + uri + "', Method='" + httpMethod + "', Detected ActionType='" + actionType + "'");
        return actionType;
    }

    private static class UserRequestTracker {
        // Skladišti timestamp-ove (u milisekundama) svakog zahteva
        private final ConcurrentLinkedQueue<Long> requestTimestamps = new ConcurrentLinkedQueue<>();
        private long lastActivityTimestamp = System.currentTimeMillis(); // prati poslednju aktivnost za cleanup

        // Metoda sada prima oba limita i vremenska prozora
        public synchronized boolean isRequestAllowed(
                int limitPerMinute, long timeWindowMinuteMillis,
                int limitPerHour, long timeWindowHourMillis,
                String key) {

            long nowMillis = System.currentTimeMillis();
            this.lastActivityTimestamp = nowMillis; // Ažuriraj vreme poslednje aktivnosti

            // 1. Očistite stare unose iz queue-a (starije od najdužeg prozora, tj. 1 sat)
            Iterator<Long> iterator = requestTimestamps.iterator();
            while (iterator.hasNext()) {
                Long timestamp = iterator.next();
                if (nowMillis - timestamp > timeWindowHourMillis) { // Ukloni sve što je starije od sat vremena
                    iterator.remove();
                } else {
                    // Pošto su timestamp-ovi dodati sekvencijalno,
                    // čim naiđemo na onaj koji nije stariji od 1 sata,
                    // znamo da ni sledeći neće biti, pa možemo prekinuti.
                    break;
                }
            }

            // 2. Prebrojte zahteve za svaki prozor
            int countInLastMinute = 0;
            for (Long timestamp : requestTimestamps) {
                if (nowMillis - timestamp <= timeWindowMinuteMillis) {
                    countInLastMinute++;
                }
            }
            // requestTimestamps.size() je već broj u poslednjem satu nakon čišćenja
            int countInLastHour = requestTimestamps.size();

            System.out.println("UserRequestTracker[" + key + "]: Trenutni zahtevi -> Minut: " + countInLastMinute + "/" + limitPerMinute + ", Sat: " + countInLastHour + "/" + limitPerHour);

            // 3. Proverite limite
            if (countInLastMinute >= limitPerMinute) {
                System.out.println("UserRequestTracker[" + key + "]: Prekoračen limit za minutni prozor.");
                return false;
            }

            if (countInLastHour >= limitPerHour) {
                System.out.println("UserRequestTracker[" + key + "]: Prekoračen limit za satni prozor.");
                return false;
            }

            // 4. Dodajte trenutni zahtev i dozvolite
            requestTimestamps.add(nowMillis);
            System.out.println("UserRequestTracker[" + key + "]: Zahtev DOZVOLJEN.");
            return true;
        }
    }

    @Scheduled(fixedRate = ONE_HOUR_MILLIS) // Pokreće se svakih sat vremena
    public void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        requestTrackers.entrySet().removeIf(entry -> {
            UserRequestTracker tracker = entry.getValue();
            // Ukloni trackere ako je njihov red prazan I nisu bili aktivni duže od definisanog grace perioda
            return tracker.requestTimestamps.isEmpty() &&
                    (now - tracker.lastActivityTimestamp > GRACE_PERIOD_CLEANUP_MILLIS);
        });
        System.out.println("RateLimiterService: Očišćeni stari unosi. Preostali trackeri: " + requestTrackers.size());
    }
}