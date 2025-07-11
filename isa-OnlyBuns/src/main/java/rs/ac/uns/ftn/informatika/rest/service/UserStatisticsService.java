// rs.ac.uns.ftn.informatika.rest.service/UserStatisticsService.java
package rs.ac.uns.ftn.informatika.rest.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.Post;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserStatisticsService {

    private final PostRepository postRepository;

    @Autowired
    public UserStatisticsService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional
    public String buildEmailBodyWithStatistics(UserAccount user) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Post> userPosts = postRepository.findByUserId(user.getId());

        // LAJKOVI:
        long newLikesOnUserPosts = userPosts.stream()
                .flatMap(post -> post.getLikes().stream())
                .filter(like -> like.getLikedAt().isAfter(sevenDaysAgo))
                .count();

        // KOMENTARI:
        long newCommentsOnUserPosts = userPosts.stream()
                .flatMap(post -> post.getComments().stream())
                .filter(comment -> comment.getCommentedAt().isAfter(sevenDaysAgo))
                .count();

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("Hey ").append(user.getFirstName()).append(",\n\n");
        emailContent.append("It’s been a quiet week without you around! \uD83D\uDC40 ");
        emailContent.append("Here’s what you missed over the past week while you were away:\n\n");
        emailContent.append("   - total likes on your posts: ").append(newLikesOnUserPosts).append("\n");
        emailContent.append("   - total comments on your posts: ").append(newCommentsOnUserPosts).append("\n");
        emailContent.append("\nHope to see you soon,\n");
        emailContent.append("OnlyBuns Team \uD83D\uDE80");

        return emailContent.toString();
    }
}