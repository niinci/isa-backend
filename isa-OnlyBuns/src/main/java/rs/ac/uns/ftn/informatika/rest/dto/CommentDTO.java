package rs.ac.uns.ftn.informatika.rest.dto;

import java.time.LocalDateTime;

public class CommentDTO {
    private Long id;
    private String content;
    private Long userId;
    private String username; // DODATO
    private LocalDateTime commentedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public LocalDateTime getCommentedAt() {return commentedAt;}
    public void setCommentedAt(LocalDateTime commentedAt) {this.commentedAt = commentedAt;}
}
