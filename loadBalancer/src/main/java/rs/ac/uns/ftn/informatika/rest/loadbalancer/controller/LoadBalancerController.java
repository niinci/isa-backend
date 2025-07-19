package rs.ac.uns.ftn.informatika.rest.loadbalancer.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.*;
import rs.ac.uns.ftn.informatika.rest.loadbalancer.service.BackendService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

@Controller
public class LoadBalancerController {

    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerController.class);

    private final BackendService backendService;
    private final RestTemplate restTemplate;

    @Value("${loadbalancer.retry-attempts}")
    private int retryAttempts;

    @Value("${loadbalancer.request-timeout-ms}")
    private int requestTimeoutMs;

    public LoadBalancerController(BackendService backendService) {
        this.backendService = backendService;
        this.restTemplate = new RestTemplate();
        this.restTemplate.setErrorHandler(new NoOpResponseErrorHandler());
        this.restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory());
        ((org.springframework.http.client.SimpleClientHttpRequestFactory) this.restTemplate.getRequestFactory())
                .setConnectTimeout(requestTimeoutMs);
        ((org.springframework.http.client.SimpleClientHttpRequestFactory) this.restTemplate.getRequestFactory())
                .setReadTimeout(requestTimeoutMs);
    }

    @RequestMapping("/**")
    public ResponseEntity<String> proxyRequest(HttpServletRequest request) {
        if ("websocket".equalsIgnoreCase(request.getHeader("Upgrade"))) {
            logger.warn("Primljen WebSocket zahtjev, ignoriram ga za load balancing.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("WebSocket requests are not handled by the load balancer.");
        }

        int attempts = 0;
        while (attempts < retryAttempts + 1) {
            String targetBackendUrl = backendService.getNextBackendUrl();

            try {
                String requestUri = request.getRequestURI();
                String queryString = request.getQueryString();
                String fullPath = queryString != null ? requestUri + "?" + queryString : requestUri;
                URI targetUri = new URI(targetBackendUrl + fullPath);

                HttpHeaders headers = new HttpHeaders();
                Enumeration<String> headerNames = request.getHeaderNames();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    if (!"host".equalsIgnoreCase(headerName) && !"connection".equalsIgnoreCase(headerName) && !"content-length".equalsIgnoreCase(headerName)) {
                        headers.add(headerName, request.getHeader(headerName));
                    }
                }
                headers.set(HttpHeaders.HOST, new URI(targetBackendUrl).getHost() + ":" + new URI(targetBackendUrl).getPort());

                byte[] body = request.getInputStream().readAllBytes();

                HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
                RequestEntity<byte[]> requestEntity = new RequestEntity<>(body, headers, httpMethod, targetUri);

                logger.info("Proslijeđujem zahtjev {} {} na {} (Pokušaj: {})", httpMethod, requestUri, targetBackendUrl, attempts + 1);

                ResponseEntity<String> backendResponse = restTemplate.exchange(requestEntity, String.class);

                logger.info("Odgovor od {} sa statusom: {}", targetBackendUrl, backendResponse.getStatusCode());
                return backendResponse;

            } catch (ResourceAccessException e) {
                logger.error("Nije moguće doći do backend instance {}: {}", targetBackendUrl, e.getMessage());
                attempts++;
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                logger.error("Backend instanca {} vratila grešku {}: {}", targetBackendUrl, e.getStatusCode(), e.getResponseBodyAs(String.class));
                attempts++;
            } catch (URISyntaxException e) {
                logger.error("Greška u URI sintaksi: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška u konfiguraciji load balancera.");
            } catch (Exception e) {
                logger.error("Nepredviđena greška pri proxyiranju zahtjeva: {}", e.getMessage(), e);
                attempts++;
            }
        }
        logger.error("Svi pokušaji neuspješni. Nema dostupnih backend instanci.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Servis privremeno nedostupan. Molimo pokušajte ponovo kasnije.");
    }
}