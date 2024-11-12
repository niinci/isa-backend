package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.rest.domain.Comment;
import rs.ac.uns.ftn.informatika.rest.dto.CommentDTO;
import rs.ac.uns.ftn.informatika.rest.exception.ResourceNotFoundException;
import rs.ac.uns.ftn.informatika.rest.repository.PostRepository;
import rs.ac.uns.ftn.informatika.rest.domain.Post;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import rs.ac.uns.ftn.informatika.rest.dto.PostDTO;

@Service

public class PostService {

    @Autowired
    private PostRepository postRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAllByDeletedFalse();
    }

    public Post createPost(PostDTO postDTO, MultipartFile imageFile) throws IOException {
        String imageUrl = saveImage(imageFile);

        Post post = new Post(
                postDTO.getDescription(),
                imageUrl,
                0,
                false,
                postDTO.getLatitude(),
                postDTO.getLongitude(),
                LocalDateTime.now(),
                new ArrayList<>(),
                new HashSet<>()
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

    public void likePost(Long postId, Long userId) {
        Post post = getPostById(postId);

        if (!post.getLikedUserIds().contains(userId)) {
            post.getLikedUserIds().add(userId);
            post.setLikes(post.getLikes() + 1);
            postRepository.save(post);
        }
    }

    public Comment addComment(Long postId, CommentDTO commentDTO) {
        Post post = getPostById(postId);
        Comment comment = new Comment(commentDTO.getContent(), commentDTO.getUserId());
        post.getComments().add(comment);
        postRepository.save(post);
        return comment;
    }
}
