package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment; // Dodato
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
import rs.ac.uns.ftn.informatika.rest.repository.PostLikeRepository;




import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*") // Dodato za CORS podršku
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private Environment environment; // Dodato za pristup portu aplikacije

    // Pomoćna metoda za logovanje porta
    private void logRequestPort(String endpoint) {
        String port = environment.getProperty("local.server.port");
        System.out.println(">>> Request for " + endpoint + " received on OnlyBans instance running on port: " + port);
    }


    @GetMapping
    public List<Post> getAllPosts(Authentication authentication) {
        logRequestPort("/api/posts - GET (all)"); // Dodato logovanje
        if (authentication == null || !authentication.isAuthenticated()) {
            // Nije ulogovan -> vrati sve postove
            return postService.getAllPosts();
        }

        // Ulogovan korisnik -> vrati postove koje su postavili ljudi koje on prati
        String email = authentication.getName();
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return postService.getPostsFromFollowedUsers(user.getId());
    }


    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody PostDTO postDTO) {
        logRequestPort("/api/posts - POST"); // Dodato logovanje
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
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        logRequestPort("/api/posts/" + id + " - DELETE"); // Dodato logovanje
        String email = authentication.getName();
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        postService.deletePost(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody PostDTO postDTO, Authentication authentication) {
        logRequestPort("/api/posts/" + id + " - PUT"); // Dodato logovanje
        String email = authentication.getName();
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(postService.updatePost(id, postDTO, user.getId()));
    }


    @GetMapping("/sorted")
    public List<Post> getAllPostsSortedByDate() {
        logRequestPort("/api/posts/sorted - GET"); // Dodato logovanje
        return postService.getAllPostsSortedByDate();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/like")
    public ResponseEntity<?> Like(@PathVariable Long id, Authentication authentication) {
        logRequestPort("/api/posts/" + id + "/like - POST"); // Dodato logovanje
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
        logRequestPort("/api/posts/liked - GET"); // Dodato logovanje
        String email = authentication.getName();
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return postService.getLikedPostsByUser(user.getId());
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyPosts(
            Authentication authentication,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm
    ) {
        logRequestPort("/api/posts/nearby - GET"); // Dodato logovanje
        String userEmail = authentication.getName();  // Email ulogovanog korisnika
        Optional<UserAccount> optionalUser = userAccountRepository.findByEmail(userEmail);

        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Korisnik nije pronađen.");
        }

        UserAccount currentUser = optionalUser.get();

        List<Post> nearby = postService.findPostsNearby(latitude, longitude, radiusKm);

        // Filtriraj da ne prikazuje objave trenutnog korisnika
        List<Map<String, Object>> response = new ArrayList<>();
        for (Post post : nearby) {
            if (!post.getUserId().equals(currentUser.getId())) {
                Map<String, Object> p = new HashMap<>();
                p.put("title", post.getDescription());
                p.put("latitude", post.getLatitude());
                p.put("longitude", post.getLongitude());
                response.add(p);
            }
        }

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/trends")
    public ResponseEntity<?> getTrends() {
        logRequestPort("/api/posts/trends - GET"); // Dodato logovanje
        Map<String, Object> response = postService.getTrendsCached();
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{postId}/advertisable")
    public ResponseEntity<Void> updatePostAdvertisableStatus(@PathVariable Long postId, @RequestBody boolean isAdvertisable) {
        logRequestPort("/api/posts/" + postId + "/advertisable - PUT"); // Dodato logovanje
        postService.updatePostAdvertisableStatus(postId, isAdvertisable);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUser(@PathVariable Long userId) {
        logRequestPort("/api/posts/user/" + userId + " - GET"); // Dodato logovanje
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<Post>> getAllPostsFromAllUsers() {
        logRequestPort("/api/posts/all - GET"); // Dodato logovanje
        return ResponseEntity.ok(postService.getAllPosts());
    }

}