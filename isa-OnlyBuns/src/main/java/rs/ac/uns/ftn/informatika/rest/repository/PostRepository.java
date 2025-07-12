package rs.ac.uns.ftn.informatika.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.informatika.rest.domain.Post;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByDeletedFalse();
    Post findByImageUrl(String imageUrl);
    List<Post> findByUserId(Long id);

    int countByCreationTimeAfter(LocalDateTime time);
    // Lista  korisnika koji su napravili postove
    @Query("SELECT DISTINCT p.userId FROM Post p")
    List<Long> findDistinctUserIds();

}
