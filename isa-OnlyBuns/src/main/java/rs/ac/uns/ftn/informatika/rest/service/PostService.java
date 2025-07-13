package rs.ac.uns.ftn.informatika.rest.service;

//import jakarta.persistence.Cacheable;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.rest.domain.PostLike;
import rs.ac.uns.ftn.informatika.rest.exception.ResourceNotFoundException;
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

@Service

public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAllByDeletedFalse();
    }

    public Post createPost(PostDTO postDTO) throws IOException {

        Post post = new Post(
                postDTO.getDescription(),
                postDTO.getImageUrl(),
                postDTO.getUserId(),
                false,
                postDTO.getLatitude(),
                postDTO.getLongitude(),
                null,
                0L
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

        return savedPost;
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
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
    }

    public void deletePost(Long postId,Long userId) {
        Post post = getPostById(postId);
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Nemaš dozvolu da obrišeš ovaj post.");
        }
        post.setDeleted(true);
        postRepository.save(post);
    }

    public Post updatePost(Long postId, PostDTO postDTO,Long userId) {
        Post post = getPostById(postId);
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Nemaš dozvolu da izmeniš ovaj post.");
        }
        post.setDescription(postDTO.getDescription());

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



}
