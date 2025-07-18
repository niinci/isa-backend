package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.rest.customqueue.CareLocationProducer;
import rs.ac.uns.ftn.informatika.rest.dto.CareLocationDTO;
import rs.ac.uns.ftn.informatika.rest.domain.CareLocation;
import rs.ac.uns.ftn.informatika.rest.repository.CareLocationRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/care-locations")
public class CareLocationController {

    private final CareLocationProducer producer;
    private final CareLocationRepository repository;

    public CareLocationController(CareLocationProducer producer, CareLocationRepository repository) {
        this.producer = producer;
        this.repository = repository;
    }

    @PostMapping
    public void sendLocation(@RequestBody CareLocationDTO dto) {
        producer.sendCareLocation(dto);
    }

    @GetMapping
    public List<CareLocationDTO> getAll() {
        List<CareLocation> entities = repository.findAll();

        return entities.stream().map(e -> new CareLocationDTO(
                e.getId(),
                e.getName(),
                e.getAddress(),
                e.getLatitude(),
                e.getLongitude()
        )).collect(Collectors.toList());
    }
}