package rs.ac.uns.ftn.informatika.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.informatika.rest.domain.Post;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    //prikaz svih postova
    List<Post> findAllByDeletedFalse();
    Post findByImageUrl(String imageUrl);
    List<Post> findByUserId(Long id);

    @Modifying
    @Query("UPDATE Post p SET p.likesCount = p.likesCount - 1 WHERE p.id = :postId AND p.likesCount > 0")
    void decrementPostLikesCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.likesCount = p.likesCount + 1 WHERE p.id = :postId")
    void incrementLikesCount(@Param("postId") Long postId);

   // int countByCreationTimeAfter(LocalDateTime time);
    // Lista  korisnika koji su napravili postove
   // @Query("SELECT DISTINCT p.userId FROM Post p")
    //List<Long> findDistinctUserIds();

    long countByDeletedFalse();

    long countByCreationTimeAfterAndDeletedFalse(LocalDateTime date);

    List<Post> findTop5ByCreationTimeAfterAndDeletedFalseOrderByLikesCountDesc(LocalDateTime date);

    List<Post> findTop10ByDeletedFalseOrderByLikesCountDesc();

    @Query("SELECT DISTINCT p.userId FROM Post p WHERE p.deleted = false")
    List<Long> findDistinctUserIdsByDeletedFalse();

    long countByUserIdAndDeletedFalse(Long userId);

    @Query("SELECT p FROM Post p WHERE (p.userId IN (" +
            "SELECT f.followingId FROM Follow f WHERE f.followerId = :userId" +
            ") OR p.userId = :userId) AND p.deleted = false")
    List<Post> findPostsByFollowedUsers(@Param("userId") Long userId);

    List<Post> findByUserIdAndDeletedFalse(Long userId);







}
