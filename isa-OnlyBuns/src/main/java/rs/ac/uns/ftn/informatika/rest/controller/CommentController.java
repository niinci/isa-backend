package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.rest.domain.Comment;
import rs.ac.uns.ftn.informatika.rest.dto.CommentDTO;
import rs.ac.uns.ftn.informatika.rest.dto.UserIdUsernameDTO;
import rs.ac.uns.ftn.informatika.rest.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long postId, @RequestBody CommentDTO dto) {
        CommentDTO createdComment = commentService.addComment(postId, dto);
        return ResponseEntity.ok(createdComment);
    }


    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

  @PostMapping("/usernames")
  public ResponseEntity<List<UserIdUsernameDTO>> getUsernamesByUserIds(@RequestBody List<Long> userIds) {
      List<UserIdUsernameDTO> usernames = commentService.getUsernamesByUserIds(userIds);
      return ResponseEntity.ok(usernames);
  }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasRole('ADMIN')")  
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
