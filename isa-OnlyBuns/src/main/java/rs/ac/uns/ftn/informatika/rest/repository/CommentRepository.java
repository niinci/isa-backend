package rs.ac.uns.ftn.informatika.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.informatika.rest.domain.Comment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);

    // Broj komentara od odredjenog vremena (za ned, mes, god)
    int countByCommentedAtAfter(LocalDateTime time);

    // Lista  korisnika koji su komentarisali
    @Query("SELECT DISTINCT c.userId FROM Comment c")
    List<Long> findDistinctUserIds();

    // Broj komentara korisnika u posljednjih sat vremena
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.userId = :userId AND c.commentedAt >= :oneHourAgo")
    long countCommentsByUserIdAndCommentedAtAfter(Long userId, LocalDateTime oneHourAgo);
}
