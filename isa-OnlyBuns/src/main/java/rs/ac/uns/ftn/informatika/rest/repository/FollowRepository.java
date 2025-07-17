package rs.ac.uns.ftn.informatika.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.rest.domain.Follow;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    List<Follow> findByFollowingId(Long followingId);

    List<Follow> findByFollowerId(Long followerId);
    long countByFollowingId(Long followingId);

    long countByFollowingIdAndCreatedAtAfter(Long id, LocalDateTime sevenDaysAgo);
}
