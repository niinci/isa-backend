package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.Comment;
import rs.ac.uns.ftn.informatika.rest.domain.Post;
import rs.ac.uns.ftn.informatika.rest.dto.CommentDTO;
import rs.ac.uns.ftn.informatika.rest.exception.ResourceNotFoundException;
import rs.ac.uns.ftn.informatika.rest.repository.CommentRepository;
import rs.ac.uns.ftn.informatika.rest.repository.PostRepository;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;


    public CommentDTO addComment(Long postId, CommentDTO dto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        Comment comment = new Comment(dto.getUserId(), dto.getContent(), post);
        Comment savedComment = commentRepository.save(comment);

        return mapToDTO(savedComment);  // mapira i vraća DTO sa username
    }


    public List<CommentDTO> getCommentsByPost(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

    }


    /*   public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        commentRepository.delete(comment);
    }*/
 private CommentDTO mapToDTO(Comment comment) {
     CommentDTO dto = new CommentDTO();
     dto.setId(comment.getId());
     dto.setContent(comment.getContent());
     dto.setUserId(comment.getUserId());

     System.out.println("mapToDTO - comment userId: " + comment.getUserId());

     // Nađi korisnika po ID-u i postavi username
     userAccountRepository.findById(comment.getUserId())
             .ifPresent(user -> {
                 System.out.println("Korisnik pronađen: " + user.getUsername());
                 dto.setUsername(user.getUsername());
             });

     return dto;
 }

}
