package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import rs.ac.uns.ftn.informatika.rest.dto.CareLocationDTO;
import rs.ac.uns.ftn.informatika.rest.service.CareLocationService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CareLocationController {

    private final CareLocationService careLocationService;

    public CareLocationController(CareLocationService careLocationService) {
        this.careLocationService = careLocationService;
    }

    @GetMapping("/receive-rabbit-locations")
    public Mono<ResponseEntity<String>> receiveRabbitLocationsManually() {
        return careLocationService.receiveAndSaveLocation()
                .map(locationDto -> ResponseEntity.ok("Location received and saved: " + locationDto.getName()))
                .defaultIfEmpty(ResponseEntity.ok("No new locations to receive (or an error occurred)."));
    }

    @GetMapping("/rabbit-care-locations")
    public List<CareLocationDTO> getRabbitCareLocations() {
        return careLocationService.getAllRabbitCareLocations();
    }
}