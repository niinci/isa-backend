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
import rs.ac.uns.ftn.informatika.rest.repository.PostLikeRepository;




import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import java.util.stream.Collectors;

@RestController
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

    @GetMapping
    public List<Post> getAllPosts(Authentication authentication) {
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
        String email = authentication.getName();
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        postService.deletePost(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody PostDTO postDTO, Authentication authentication) {
        String email = authentication.getName();
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(postService.updatePost(id, postDTO, user.getId()));
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

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyPosts(
            Authentication authentication,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm
    ) {
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

    @GetMapping("/trends")
    public ResponseEntity<?> getTrends() {
        Map<String, Object> response = new HashMap<>();

        // Ukupan broj objava
        long totalPosts = postService.getAllPosts().size();
        response.put("totalPosts", totalPosts);

        // Broj objava u poslednjih mesec dana
        long postsLastMonth = postService.getAllPosts().stream()
                .filter(post -> post.getCreationTime().isAfter(LocalDateTime.now().minusMonths(1)))
                .count();
        response.put("postsLastMonth", postsLastMonth);

        // Top 5 postova u poslednjih 7 dana (po broju lajkova)
        List<Post> top5Last7Days = postService.getAllPosts().stream()
                .filter(post -> post.getCreationTime().isAfter(LocalDateTime.now().minusDays(7)))
                .sorted(Comparator.comparing(Post::getLikesCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
        response.put("top5PostsLast7Days", top5Last7Days);

        // Top 10 postova ikada
        List<Post> top10Ever = postService.getAllPosts().stream()
                .sorted(Comparator.comparing(Post::getLikesCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
        response.put("top10PostsEver", top10Ever);

        // Top 10 korisnika koji su dali najviše lajkova u poslednjih 7 dana
        List<UserAccount> top10Users = postLikeRepository
                .findTop10UsersByLikesGivenAfter(LocalDateTime.now().minusDays(7));
        response.put("top10UsersByLikesGivenLast7Days", top10Users);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{postId}/advertisable")
    public ResponseEntity<Void> updatePostAdvertisableStatus(@PathVariable Long postId, @RequestBody boolean isAdvertisable) {
        postService.updatePostAdvertisableStatus(postId, isAdvertisable);
        return ResponseEntity.ok().build();
    }
}
