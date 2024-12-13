package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.rest.exception.ResourceNotFoundException;
import rs.ac.uns.ftn.informatika.rest.repository.PostRepository;
import rs.ac.uns.ftn.informatika.rest.domain.Post;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import rs.ac.uns.ftn.informatika.rest.dto.PostDTO;
import org.springframework.data.domain.Sort;

@Service

public class PostService {

    @Autowired
    private PostRepository postRepository;

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

        return postRepository.save(post);
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

    public void deletePost(Long postId) {
        Post post = getPostById(postId);
        post.setDeleted(true);
        postRepository.save(post);
    }

    public Post updatePost(Long postId, PostDTO postDTO) {
        Post post = getPostById(postId);
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



}
