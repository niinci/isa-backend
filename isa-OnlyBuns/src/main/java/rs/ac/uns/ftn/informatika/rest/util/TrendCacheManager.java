package rs.ac.uns.ftn.informatika.rest.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.rest.domain.Post;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class TrendCacheManager {

    private final Cache<String, Object> trendCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES) // sve keširano 10 minuta
            .maximumSize(100)
            .build();

    public void put(String key, Object value) {
        trendCache.put(key, value);
    }

    public Object get(String key) {
        return trendCache.getIfPresent(key);
    }

    public void evict(String key) {
        trendCache.invalidate(key);
    }

    public void clearAll() {
        trendCache.invalidateAll();
    }
}
