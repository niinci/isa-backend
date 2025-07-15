package rs.ac.uns.ftn.informatika.rest.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ImageCacheManager {

    private final Cache<String, Resource> imageCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    public Resource get(String imageName) {
        Resource resource = imageCache.getIfPresent(imageName);
        if (resource != null) {
            System.out.println("[CACHE HIT] Slika '" + imageName + "' pronađena u kešu.");
        } else {
            System.out.println("[CACHE MISS] Slika '" + imageName + "' NIJE u kešu.");
        }
        return resource;
    }

    public void put(String imageName, Resource image) {
        System.out.println("[CACHE PUT] Slika '" + imageName + "' ubačena u keš.");
        imageCache.put(imageName, image);
    }

    public void remove(String imageName) {
        System.out.println("[CACHE REMOVE] Slika '" + imageName + "' uklonjena iz keša.");
        imageCache.invalidate(imageName);
    }

}
