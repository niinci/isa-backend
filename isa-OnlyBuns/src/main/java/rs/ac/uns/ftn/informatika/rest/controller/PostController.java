package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.rest.domain.Post;
import rs.ac.uns.ftn.informatika.rest.dto.PostDTO;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;
import rs.ac.uns.ftn.informatika.rest.service.PostService;
import rs.ac.uns.ftn.informatika.rest.service.ImageService;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;




import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.Collections;



@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserAccountRepository userAccountRepository;

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

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/like")
    public ResponseEntity<?> Like(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();

        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));


        boolean liked = postService.like(id, user.getId());
        return ResponseEntity.ok(Collections.singletonMap("liked", liked));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/liked")
    public List<Post> getLikedPosts(Authentication authentication) {
        String email = authentication.getName();
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return postService.getLikedPostsByUser(user.getId());
    }

}
