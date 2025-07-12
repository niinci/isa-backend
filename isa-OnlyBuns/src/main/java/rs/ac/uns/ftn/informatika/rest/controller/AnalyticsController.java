package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.rest.dto.AnalyticsCountsDTO;
import rs.ac.uns.ftn.informatika.rest.dto.UserActivityDistributionDTO;
import rs.ac.uns.ftn.informatika.rest.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    // Broj komentara i objava na ned, mes i god nivou
    @GetMapping("/counts")
    public ResponseEntity<AnalyticsCountsDTO> getCounts() {
        return ResponseEntity.ok(analyticsService.getCounts());
    }

    // Procenti korisnika koji su napr objavu kom i nijedno
    @GetMapping("/user-distribution")
    public ResponseEntity<UserActivityDistributionDTO> getUserActivityDistribution() {
        return ResponseEntity.ok(analyticsService.getUserActivityDistribution());
    }
}
