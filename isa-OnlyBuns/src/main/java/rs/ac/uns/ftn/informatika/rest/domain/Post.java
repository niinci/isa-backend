package rs.ac.uns.ftn.informatika.rest.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.Set;


@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    private String imageUrl;

    private int likes;

    @ElementCollection
    private Set<Long> likedUserIds = new HashSet<>();

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creationTime;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "post_id")
    @JsonManagedReference
    private List<Comment> comments = new ArrayList<>();

    public Post() {

    }
    public Post(String description, String imageUrl, int likes, boolean deleted) {
        this.description = description;
        this.imageUrl = imageUrl;
        this.likes = likes;
        this.deleted = deleted;
    }

    public Post(String description, String imageUrl, int likes, boolean deleted, double latitude, double longitude, LocalDateTime creationTime, List<Comment> comments, Set<Long> likedUserIds) {
        this.description = description;
        this.imageUrl = imageUrl;
        this.likes = likes;
        this.likedUserIds = likedUserIds;
        this.deleted = deleted;
        this.latitude = latitude;
        this.longitude = longitude;
        this.creationTime = creationTime;
        this.comments = comments;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public double getLatitude() { return latitude;}

    public void setLatitude(double latitude) { this.latitude = latitude;}

    public double getLongitude() { return longitude;}

    public void setLongitude(double longitude) { this.longitude = longitude;}

    public LocalDateTime getCreationTime() { return creationTime;}

    public void setCreationTime(LocalDateTime creationTime) { this.creationTime = creationTime;}

    public Set<Long> getLikedUserIds() {
        return likedUserIds;
    }

    public void setLikedUserIds(Set<Long> likedUserIds) {
        this.likedUserIds = likedUserIds;
    }
}
