package rs.ac.uns.ftn.informatika.rest.service;

//import jakarta.persistence.Cacheable;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityNotFoundException;
import org.apache.catalina.User;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.rest.domain.PostLike;
import rs.ac.uns.ftn.informatika.rest.dto.AdNotificationDTO;
import rs.ac.uns.ftn.informatika.rest.exception.ResourceNotFoundException;
import rs.ac.uns.ftn.informatika.rest.rabbitmq.producer.AdNotificationProducer;
import rs.ac.uns.ftn.informatika.rest.repository.CommentRepository;
import rs.ac.uns.ftn.informatika.rest.repository.PostLikeRepository;
import rs.ac.uns.ftn.informatika.rest.repository.PostRepository;
import rs.ac.uns.ftn.informatika.rest.domain.Post;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import rs.ac.uns.ftn.informatika.rest.dto.PostDTO;
import org.springframework.data.domain.Sort;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;
import jakarta.transaction.Transactional;
import rs.ac.uns.ftn.informatika.rest.util.LocationCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LocationCacheManager locationCacheManager;

    @Autowired
    private AdNotificationProducer adNotificationProducer;

    @Autowired
    private ImageService imageService;

    @Autowired
    private MeterRegistry meterRegistry;


    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    public List<Post> getAllPosts() {
        return postRepository.findAllByDeletedFalse();
    }

    // @Timed("post.create.duration")
    public Post createPost(PostDTO postDTO) throws IOException {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Post post = new Post(
                    postDTO.getDescription(),
                    postDTO.getImageUrl(),
                    postDTO.getUserId(),
                    false,
                    postDTO.getLatitude(),
                    postDTO.getLongitude(),
                    null,
                    0L,
                    postDTO.getLocationAddress(),
                    false
            );

            Post savedPost = postRepository.save(post);

            Optional<UserAccount> userOpt = userRepository.findById(postDTO.getUserId());
            if(userOpt.isPresent()) {
                UserAccount user = userOpt.get();
                user.setPostCount(user.getPostCount() + 1);
                userRepository.save(user);
            } else {
                System.out.println("User not found with id: " + postDTO.getUserId());
            }

            // KESIRANJE LOKACIJE NAKON STO JE OBJAVA SACUVANA
            if (savedPost.getId() != null) {
                locationCacheManager.putLocation(
                        savedPost.getId(),
                        savedPost.getLongitude(),
                        savedPost.getLatitude(),
                        savedPost.getLocationAddress()
                );
                logger.info("LOKACIJA KEŠIRANA: Post ID: {}", savedPost.getId());
            }

            return savedPost;
        } finally {

            sample.stop(meterRegistry.timer("post_create_duration_seconds", "component", "PostService"));
        }
    }

    public LocationCacheManager.LocationData getPostLocationFromCacheOrDb(Long postId) {
        LocationCacheManager.LocationData cachedLocation = locationCacheManager.getLocation(postId);
        if (cachedLocation != null) {
            System.out.println("Lokacija za post ID " + postId + " pronađena u kešu.");
            return cachedLocation;
        } else {
            System.out.println("Lokacija za post ID " + postId + " nije pronađena u kešu, dohvatam iz baze.");
            Post post = postRepository.findById(postId).orElse(null);
            if (post != null) {
                locationCacheManager.putLocation(
                        post.getId(),
                        post.getLongitude(),
                        post.getLatitude(),
                        post.getLocationAddress()
                );
                return new LocationCacheManager.LocationData(
                        post.getLongitude(),
                        post.getLatitude(),
                        post.getLocationAddress()
                );
            }
        }
        return null;
    }

    private String saveImage(MultipartFile image) throws IOException {
        String fileName = UUID.randomUUID().toString() + "." + getExtension(image.getOriginalFilename());
        Path path = Paths.get("uploads/images/" + fileName);
        Files.createDirectories(path.getParent());
        Files.copy(image.getInputStream(), path);

        return "/uploads/images/" + fileName;
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    public Post getPostById(Long postId) {
        /*LocationCacheManager.LocationData cachedLocation = locationCacheManager.getLocation(postId);
        if (cachedLocation != null) {
            logger.info("DEMO KEŠ HIT: Lokacija za Post ID {} PRONAĐENA U KEŠU.", postId);
        } else {
            logger.info("DEMO KEŠ MISS: Lokacija za Post ID {} NIJE PRONAĐENA U KEŠU, dohvatam iz baze.", postId);
        }*/
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = getPostById(postId);
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Nemaš dozvolu da obrišeš ovaj post.");
        }

        post.setDeleted(true);
        postRepository.save(post);

        commentRepository.deleteAll(commentRepository.findByPostId(postId));
    }


    public Post updatePost(Long postId, PostDTO postDTO, Long userId) {
        Post post = getPostById(postId);

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Nemaš dozvolu da izmeniš ovaj post.");
        }

        post.setDescription(postDTO.getDescription());

        // Ako je korisnik poslao novu sliku
        if (postDTO.getImageBase64() != null && !postDTO.getImageBase64().isEmpty()) {
            try {
                String imageUrl = imageService.saveImage(postDTO.getImageBase64());
                post.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Greška prilikom čuvanja slike.");
            }
        }

        // Lokacija
        if (postDTO.getLatitude() != 0 && postDTO.getLongitude() != 0) {
            post.setLatitude(postDTO.getLatitude());
            post.setLongitude(postDTO.getLongitude());
            post.setLocationAddress(postDTO.getLocationAddress());

            // Ažuriranje keša lokacije
            locationCacheManager.putLocation(
                    post.getId(),
                    post.getLongitude(),
                    post.getLatitude(),
                    post.getLocationAddress()
            );
        }

        return postRepository.save(post);
    }


    public List<Post> getAllPostsSortedByDate() {
        // Dohvati sve objave koje nisu obrisane
        List<Post> posts = postRepository.findAllByDeletedFalse();

        // Sortiraj objave po datumu kreiranja u opadajućem redosledu
        return posts.stream()
                .sorted(Comparator.comparing(Post::getCreationTime).reversed())
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean like(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));

        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Optional<PostLike> existingLike = postLikeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            post.getLikes().remove(existingLike.get());
            postRepository.decrementPostLikesCount(postId);
            return false;
        } else {
            PostLike newLike = new PostLike(post, user);
            postLikeRepository.save(newLike);
            post.getLikes().add(newLike);
            postRepository.incrementLikesCount(postId);
            return true;
        }
    }

    public List<Post> getLikedPostsByUser(Long userId) {
        List<PostLike> userLikes = postLikeRepository.findByUser_Id(userId);

        return userLikes.stream()
                .map(PostLike::getPost)
                .filter(post -> !post.isDeleted())
                .collect(Collectors.toList());
    }
    private PostDTO mapToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setDescription(post.getDescription());
        dto.setImageUrl(post.getImageUrl());
        dto.setLikesCount(post.getLikesCount());
        dto.setUserId(post.getUserId());  // ← KLJUČNO
        dto.setLongitude(post.getLongitude());
        dto.setLatitude(post.getLatitude());
        dto.setCreatedAt(post.getCreationTime());
        dto.setIsAdvertisable(post.getIsAdvertisable());
        return dto;
    }

    public List<PostDTO> getAllPostDTOs() {
        return postRepository.findAllByDeletedFalse()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<Post> findPostsNearby(double lat, double lon, double radiusKm) {
        return postRepository.findAll().stream()
                .filter(p -> distanceKm(lat, lon, p.getLatitude(), p.getLongitude()) <= radiusKm)
                .collect(Collectors.toList());
    }
    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Zemljin poluprečnik
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    @Cacheable("totalPosts")
    public long getTotalPostCount() {
        return postRepository.countByDeletedFalse();
    }

    @Cacheable("postsLastMonth")
    public long getPostCountLastMonth() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minus(1, ChronoUnit.MONTHS);
        return postRepository.countByCreationTimeAfterAndDeletedFalse(oneMonthAgo);
    }

    @Cacheable("top5PostsLast7Days")
    public List<Post> getTop5PostsLast7Days() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return postRepository.findTop5ByCreationTimeAfterAndDeletedFalseOrderByLikesCountDesc(sevenDaysAgo);
    }

    @Cacheable("top10PostsEver")
    public List<Post> getTop10PostsEver() {
        return postRepository.findTop10ByDeletedFalseOrderByLikesCountDesc();
    }

    @Cacheable("top10UsersByLikesGivenLast7Days")
    public List<UserAccount> getTop10UsersByLikesGivenLast7Days() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return postLikeRepository.findTop10UsersByLikesGivenAfter(sevenDaysAgo);
    }

    @Transactional
    public void updatePostAdvertisableStatus(Long postId, boolean isAdvertisable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        if (post.getIsAdvertisable() != isAdvertisable) {  // da li se status mijenja
            post.setIsAdvertisable(isAdvertisable);
            postRepository.save(post);

            // AKO JE POSTAVLJENA KAO REKLAMIRANA (TRUE), POSALJI PORUKU NA RABBITMQ
            if (isAdvertisable) {
                UserAccount user = userRepository.findById(post.getUserId())
                        .orElseThrow(() -> new EntityNotFoundException("User not found for post with id: " + postId));

                AdNotificationDTO notificationDTO = new AdNotificationDTO(
                        post.getId(),
                        post.getDescription(),
                        user.getUsername(),
                        LocalDateTime.now()
                );
                adNotificationProducer.sendAdNotification(notificationDTO);
            }
        }
    }
}
