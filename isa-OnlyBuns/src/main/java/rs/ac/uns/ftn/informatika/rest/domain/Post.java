package rs.ac.uns.ftn.informatika.rest.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = true)
    private String locationAddress;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creationTime;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "likes_count", nullable = false)
    private Long likesCount = 0L;

    @Column(nullable = false)
    private boolean isAdvertisable = false;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostLike> likes = new HashSet<>();

    // Defaultni konstruktor
    public Post() {}

    // Konstruktor sa osnovnim parametrima
    public Post(String description, String imageUrl, boolean deleted) {
        this.description = description;
        this.imageUrl = imageUrl;
        this.deleted = deleted;
        this.likesCount = 0L;
        this.isAdvertisable = false;
    }

    // Konstruktor sa svim parametrima
    public Post(String description, String imageUrl, Long userId, boolean deleted, double latitude, double longitude, LocalDateTime creationTime, Long likesCount, String locationAddress, boolean isAdvertisable) {
        this.description = description;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.deleted = deleted;
        this.latitude = latitude;
        this.longitude = longitude;
        this.creationTime = creationTime;
        this.likesCount = likesCount;
        this.locationAddress = locationAddress;
        this.isAdvertisable = isAdvertisable;
    }

    public Long getLikesCount() { return this.likesCount; }
    public void setLikesCount(Long likesCount) { this.likesCount = likesCount; }

    // Getter-i i Setter-i
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
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public Set<PostLike> getLikes() {return likes;}
    public void setLikes(Set<PostLike> likes) {this.likes = likes;}

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }

    public boolean getIsAdvertisable() { return isAdvertisable; }
    public void setIsAdvertisable(boolean isAdvertisable) { this.isAdvertisable = isAdvertisable; }
}
