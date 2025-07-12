package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.dto.AnalyticsCountsDTO;
import rs.ac.uns.ftn.informatika.rest.dto.UserActivityDistributionDTO;
import rs.ac.uns.ftn.informatika.rest.repository.CommentRepository;
import rs.ac.uns.ftn.informatika.rest.repository.PostRepository;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserAccountRepository userRepository;

    public AnalyticsCountsDTO getCounts() {
        LocalDateTime now = LocalDateTime.now();

        return new AnalyticsCountsDTO(
                //br kom napravljeno u poslednjih ned dana od sad
                commentRepository.countByCommentedAtAfter(now.minusWeeks(1)),
                commentRepository.countByCommentedAtAfter(now.minusMonths(1)),
                commentRepository.countByCommentedAtAfter(now.minusYears(1)),

                postRepository.countByCreationTimeAfter(now.minusWeeks(1)),
                postRepository.countByCreationTimeAfter(now.minusMonths(1)),
                postRepository.countByCreationTimeAfter(now.minusYears(1))
        );
    }

    public UserActivityDistributionDTO getUserActivityDistribution() {
        //oni koji su kreirali bar jedan post
        List<Long> postUsers = postRepository.findDistinctUserIds();
        //korisnici koji su bar jednom kom
        List<Long> commentUsers = commentRepository.findDistinctUserIds();
        //svi korisnici
        List<Long> allUsers = userRepository.findAllUserIds();

        //svi koji su napr postove
        Set<Long> usersWithPosts = new HashSet<>(postUsers);
        //svi koji su ostav kom
        Set<Long> usersWithOnlyComments = commentUsers.stream()
                .filter(id -> !usersWithPosts.contains(id))
                .collect(Collectors.toSet());

        //skup neaktivnih
        Set<Long> inactiveUsers = allUsers.stream()
                .filter(id -> !usersWithPosts.contains(id) && !commentUsers.contains(id))
                .collect(Collectors.toSet());

        int total = allUsers.size();

        return new UserActivityDistributionDTO(
                (double) usersWithPosts.size() / total * 100,
                (double) usersWithOnlyComments.size() / total * 100,
                (double) inactiveUsers.size() / total * 100
        );
    }
}
