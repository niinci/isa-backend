package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import rs.ac.uns.ftn.informatika.rest.domain.Address;
import rs.ac.uns.ftn.informatika.rest.domain.LatLon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate;

    public GeocodingService() {
        this.restTemplate = new RestTemplate();
    }

    public Optional<LatLon> geocode(Address adr) {
        List<String> queries = new ArrayList<>();

        if (adr.getStreet() != null && adr.getNumber() != null) {
            queries.add(adr.getStreet() + " " + adr.getNumber() + ", " + adr.getCity() + ", " + adr.getCountry());
        }
        if (adr.getStreet() != null) {
            queries.add(adr.getStreet() + ", " + adr.getCity() + ", " + adr.getCountry());
        }
        queries.add(adr.getCity() + ", " + adr.getCountry());

        for (String query : queries) {
            try {
                String url = UriComponentsBuilder.fromHttpUrl("https://nominatim.openstreetmap.org/search")
                        .queryParam("q", query)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build().toUriString();

                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "OnlyBuns/1.0 (kontakt@nince.rs)");
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<NominatimDto[]> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        NominatimDto[].class
                );

                if (response.getBody() != null && response.getBody().length > 0) {
                    return Optional.of(new LatLon(
                            Double.parseDouble(response.getBody()[0].lat),
                            Double.parseDouble(response.getBody()[0].lon)
                    ));
                }

            } catch (Exception e) {
                System.err.println("Greška pri geokodiranju: " + e.getMessage());
            }
        }

        return Optional.empty();
    }

    // DTO za JSON odgovor Nominatim API-ja
    private static class NominatimDto {
        public String lat;
        public String lon;
    }
}
