package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.informatika.rest.service.ImageCompressionService;

@RestController
@RequestMapping("/api")
public class ImageCompressionController {
    private final ImageCompressionService compressionService;

    public ImageCompressionController(ImageCompressionService compressionService) {
        this.compressionService = compressionService;
    }

    @GetMapping("/compression-status")
    public String getCompressionStatus() {
        return "Compression task ran successfully.";
    }
}
