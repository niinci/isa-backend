package rs.ac.uns.ftn.informatika.rest.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LocationCacheManager {

    // Interna klasa za čuvanje podataka o lokaciji u kesu
    public static class LocationData {
        private double longitude;
        private double latitude;
        private String address;

        public LocationData(double longitude, double latitude, String address) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.address = address;
        }

        public double getLongitude() { return longitude; }
        public double getLatitude() { return latitude; }
        public String getAddress() { return address; }
    }

    // Inicijalizacija Caffeine kesa
    private final Cache<Long, LocationData> locationCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS) // Kes istice 1 sat nakon posljednjeg upisa
            .maximumSize(10_000) // Maksimalan broj stavki u kesu
            .build();

    public void putLocation(Long postId, double longitude, double latitude, String address) {
        locationCache.put(postId, new LocationData(longitude, latitude, address));
    }

    public LocationData getLocation(Long postId) {
        return locationCache.getIfPresent(postId);
    }

    public void removeLocation(Long postId) {
        locationCache.invalidate(postId);
    }
}