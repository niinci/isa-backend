package rs.ac.uns.ftn.informatika.rest.service;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.rest.exception.ResourceNotFoundException;
import rs.ac.uns.ftn.informatika.rest.repository.PostRepository;
import rs.ac.uns.ftn.informatika.rest.domain.Post;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import rs.ac.uns.ftn.informatika.rest.dto.PostDTO;
import org.springframework.data.domain.Sort;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;

@Service

public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserAccountRepository userRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAllByDeletedFalse();
    }

    public Post createPost(PostDTO postDTO) throws IOException {

        Post post = new Post(
                postDTO.getDescription(),
                postDTO.getImageUrl(),
                postDTO.getUserId(),
                0,
                false,
                postDTO.getLatitude(),
                postDTO.getLongitude(),
                LocalDateTime.now()
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

    public boolean like(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserAccount> likedUsers = post.getLikedByUsers();

        if (likedUsers.contains(user)) {
            likedUsers.remove(user);
            post.setLikes(post.getLikes() - 1);
            postRepository.save(post);
            return false; // unlike
        } else {
            likedUsers.add(user);
            post.setLikes(post.getLikes() + 1);
            postRepository.save(post);
            return true; // like
        }
    }

    public List<Post> getLikedPostsByUser(Long userId) {
        // upit u repozitorijumu
        return postRepository.findByLikedByUsers_Id(userId);
    }
    private PostDTO mapToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setDescription(post.getDescription());
        dto.setImageUrl(post.getImageUrl());
        dto.setLikes(post.getLikes());
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



}
