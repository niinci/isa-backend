package rs.ac.uns.ftn.informatika.rest.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AdNotificationDTO implements Serializable {
    private Long postId;
    private String postDescription;
    private String username; //
    private LocalDateTime notificationTime;

    public AdNotificationDTO() {
    }

    public AdNotificationDTO(Long postId, String postDescription, String username, LocalDateTime notificationTime) {
        this.postId = postId;
        this.postDescription = postDescription;
        this.username = username;
        this.notificationTime = notificationTime;
    }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getPostDescription() { return postDescription; }
    public void setPostDescription(String postDescription) { this.postDescription = postDescription; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getNotificationTime() { return notificationTime; }
    public void setNotificationTime(LocalDateTime notificationTime) { this.notificationTime = notificationTime; }

    @Override
    public String toString() {
        return "AdNotificationDTO{" +
                "postId=" + postId +
                ", postDescription='" + postDescription + '\'' +
                ", username='" + username + '\'' +
                ", notificationTime=" + notificationTime +
                '}';
    }
}
