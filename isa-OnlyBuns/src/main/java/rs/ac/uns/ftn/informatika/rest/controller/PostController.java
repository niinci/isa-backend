package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.rest.domain.Post;
import rs.ac.uns.ftn.informatika.rest.dto.PostDTO;
import rs.ac.uns.ftn.informatika.rest.service.PostService;
import rs.ac.uns.ftn.informatika.rest.service.ImageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private ImageService imageService;

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody PostDTO postDTO) {
        try {
            String imageUrl = imageService.saveImage(postDTO.getImageBase64());
            if (imageUrl != null) {
                postDTO.setImageUrl(imageUrl);
            }

            return ResponseEntity.ok(postService.createPost(postDTO));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody PostDTO postDTO) {
        return ResponseEntity.ok(postService.updatePost(id, postDTO));
    }

    @GetMapping("/sorted")
    public List<Post> getAllPostsSortedByDate() {
        return postService.getAllPostsSortedByDate();
    }

    
}
