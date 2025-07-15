package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.informatika.rest.util.ImageCacheManager;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@RestController
public class ImageController {

    @Value("${images.upload-dir}")
    private String uploadDir;

    private final ImageCacheManager imageCacheManager;

    public ImageController(ImageCacheManager imageCacheManager) {
        this.imageCacheManager = imageCacheManager;
    }


    @GetMapping("/images/posts/{imageName}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        try {
            Resource cachedResource = imageCacheManager.get(imageName);
            if (cachedResource != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS)) // <--- OVDE
                        .body(cachedResource);
            }

            Path imagePath = Paths.get(uploadDir).resolve(imageName).normalize();
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                imageCacheManager.put(imageName, resource); // keširaj u Caffeine
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS)) // <--- I OVDE
                        .body(resource);
            } else {
                throw new FileNotFoundException("Image not found: " + imageName);
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
