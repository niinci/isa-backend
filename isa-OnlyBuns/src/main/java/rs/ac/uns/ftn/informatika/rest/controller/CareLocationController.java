package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import rs.ac.uns.ftn.informatika.rest.domain.CareLocation;
import rs.ac.uns.ftn.informatika.rest.repository.CareLocationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('USER')")
@RestController
@RequestMapping("/api/care-locations")
public class CareLocationController {
    private final CareLocationRepository repo;

    public CareLocationController(CareLocationRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<CareLocation> getAll() {
        return repo.findAll();
    }
}
