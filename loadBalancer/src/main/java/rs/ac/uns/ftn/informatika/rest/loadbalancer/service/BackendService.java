package rs.ac.uns.ftn.informatika.rest.loadbalancer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BackendService {

    private static final Logger logger = LoggerFactory.getLogger(BackendService.class);

    @Value("${loadbalancer.backends}")
    private List<String> backendUrls;

    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public String getNextBackendUrl() {
        if (backendUrls.isEmpty()) {
            throw new IllegalStateException("No backend instances configured.");
        }
        // Round Robin algoritam
        int index = currentIndex.getAndUpdate(i -> (i + 1) % backendUrls.size());
        String url = backendUrls.get(index);
        logger.info("Odabrana backend instanca (Round Robin): {}", url);
        return url;
    }
}