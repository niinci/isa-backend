package rs.ac.uns.ftn.informatika.rest.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany
    @JoinTable(
            name = "post_likes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserAccount> likedByUsers = new ArrayList<>();  // Lista korisnika koji su lajkovali post

    // Defaultni konstruktor
    public Post() {}

    // Konstruktor sa osnovnim parametrima
    public Post(String description, String imageUrl, int likes, boolean deleted) {
        this.description = description;
        this.imageUrl = imageUrl;
        this.likes = likes;
        this.deleted = deleted;
    }

    // Konstruktor sa svim parametrima
    public Post(String description, String imageUrl, int likes, boolean deleted, double latitude, double longitude, LocalDateTime creationTime) {
        this.description = description;
        this.imageUrl = imageUrl;
        this.likes = likes;
        this.deleted = deleted;
        this.latitude = latitude;
        this.longitude = longitude;
        this.creationTime = creationTime;
    }

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

    public List<UserAccount> getLikedByUsers() {
        return likedByUsers;
    }

    public void setLikedByUsers(List<UserAccount> likedByUsers) {
        this.likedByUsers = likedByUsers;
    }
}
