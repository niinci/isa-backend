package rs.ac.uns.ftn.informatika.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.rest.domain.PostLike;
import rs.ac.uns.ftn.informatika.rest.domain.Post;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, UserAccount user);
    List<PostLike> findByPostUserIdAndLikedAtAfter(Long postOwnerUserId, LocalDateTime likedAt);
    long countByPost(Post post);
    void delete(PostLike postLike);
    List<PostLike> findByUser_Id(Long userId);
    @Query("SELECT pl.user FROM PostLike pl WHERE pl.likedAt >= :date GROUP BY pl.user ORDER BY COUNT(pl) DESC")
    List<UserAccount> findTop10UsersByLikesGivenAfter(@Param("date") LocalDateTime date);

}