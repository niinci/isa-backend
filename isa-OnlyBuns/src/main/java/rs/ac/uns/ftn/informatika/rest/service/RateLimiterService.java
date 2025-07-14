package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.Role;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private final ConcurrentMap<String, UserRequestTracker> requestTrackers = new ConcurrentHashMap<>();

    private static final int COMMENT_REGISTERED_USER_LIMIT_PER_MINUTE = 5;
    private static final int COMMENT_UNAUTHENTICATED_LIMIT_PER_MINUTE = 0; // Neautentifikovani ne mogu komentarisati

    private static final int FOLLOW_REGISTERED_USER_LIMIT_PER_MINUTE = 50;
    private static final int FOLLOW_UNAUTHENTICATED_LIMIT_PER_MINUTE = 0; // Neautentifikovani ne mogu pratiti

    private static final int LOGIN_ATTEMPT_IP_LIMIT_PER_MINUTE = 5;

    private static final int DEFAULT_UNAUTHENTICATED_LIMIT_PER_MINUTE = 60;
    private static final int DEFAULT_REGISTERED_USER_LIMIT_PER_MINUTE = 300;
    private static final int DEFAULT_ADMIN_LIMIT_PER_MINUTE = 1000;


    public boolean isRequestAllowed(String identifier, Role role, String requestUri, String httpMethod) {
        String actionType = getActionTypeFromUri(requestUri, httpMethod);

        String effectiveIdentifier = (actionType.equals("LOGIN_ATTEMPT")) ? identifier : identifier + "_" + role.name();

        String key = effectiveIdentifier + "_" + actionType;

        UserRequestTracker tracker = requestTrackers.computeIfAbsent(key,
                k -> new UserRequestTracker());

        int limit = getLimitForActionAndRole(actionType, role);
        long timeWindowMillis = getTimeWindowForActionType(actionType);

        System.out.println("RateLimiterService: Checking request:");
        System.out.println("  Identifier: " + identifier);
        System.out.println("  Role: " + role);
        System.out.println("  Request URI: " + requestUri);
        System.out.println("  HTTP Method: " + httpMethod);
        System.out.println("  Determined Action Type: " + actionType);
        System.out.println("  Effective Key: " + key);
        System.out.println("  Calculated Limit: " + limit);
        System.out.println("  Time Window (ms): " + timeWindowMillis);


        return tracker.isRequestAllowed(limit, timeWindowMillis, key);
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
        } else if (uri.matches("/api/userAccount/follow/\\d+")) {
            actionType = "FOLLOW_USER";
        } else if (uri.equals("/api/userAccount/login")) {
            actionType = "LOGIN_ATTEMPT";
        } else if (uri.matches("/api/posts/\\d+/like")) {
            actionType = "LIKE_POST";
        }

        System.out.println("RateLimiterService.getActionTypeFromUri: URI='" + uri + "', Method='" + httpMethod + "', Detected ActionType='" + actionType + "'");
        return actionType;
    }

    private static class UserRequestTracker {
        private int requestCount = 0;
        private long windowStartMillis = System.currentTimeMillis();

        public synchronized boolean isRequestAllowed(int limit, long timeWindowMillis, String key) {
            long nowMillis = System.currentTimeMillis();

            if (nowMillis > windowStartMillis + timeWindowMillis) {
                System.out.println("UserRequestTracker[" + key + "]: Window expired. Resetting count. Old window started at: " + LocalDateTime.ofInstant(Instant.ofEpochMilli(windowStartMillis), ZoneId.systemDefault()) + ", now: " + LocalDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), ZoneId.systemDefault()));
                requestCount = 0;
                windowStartMillis = nowMillis;
            }

            System.out.println("UserRequestTracker[" + key + "]: Current count: " + requestCount + ", Limit: " + limit);

            if (requestCount >= limit) {
                System.out.println("UserRequestTracker[" + key + "]: LIMIT EXCEEDED! (" + requestCount + " >= " + limit + "). Remaining time in window: " + (windowStartMillis + timeWindowMillis - nowMillis) + "ms");
                return false;
            }

            requestCount++;
            System.out.println("UserRequestTracker[" + key + "]: Request allowed. New count: " + requestCount + ", Limit: " + limit);
            return true;
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanupOldEntries() {
        long cutoffMillis = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2);

        requestTrackers.entrySet().removeIf(entry -> {
            UserRequestTracker tracker = entry.getValue();
            return tracker.requestCount == 0 && (System.currentTimeMillis() - tracker.windowStartMillis) > TimeUnit.HOURS.toMillis(1);
        });
        System.out.println("RateLimiterService: Cleanup performed. Current trackers count: " + requestTrackers.size());
    }
}