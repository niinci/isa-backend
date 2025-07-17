package rs.ac.uns.ftn.informatika.rest;

import org.springframework.transaction.annotation.Transactional; // ✅ Ovo koristi
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.repository.FollowRepository;
import rs.ac.uns.ftn.informatika.rest.repository.PostLikeRepository;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;
import rs.ac.uns.ftn.informatika.rest.service.FollowService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;
import static rs.ac.uns.ftn.informatika.rest.domain.Role.REGISTERED_USER;



@SpringBootTest(classes = OnlyBansApplication.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FollowServiceConcurrencyTest {

    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserAccountRepository userRepository;
    @Autowired
    private PostLikeRepository postLikeRepository;

    private UserAccount targetUser;
    private UserAccount[] followerUsers;

    private final int NUMBER_OF_THREADS = 100; //broj korisnika

    @BeforeEach
    void setup() {
        postLikeRepository.deleteAll();   // OBRIŠI PRVO post_likes
        followRepository.deleteAll();     // pa follow-ove
        userRepository.deleteAll();       // pa korisnik

        // Korisnik koji će biti praćen
        targetUser = new UserAccount();
        targetUser.setUsername("targetUser");
        targetUser.setEmail("target@example.com");
        targetUser.setPassword("pass");
        targetUser.setRole(REGISTERED_USER);
        targetUser.setEnabled(true);
        targetUser.setFollowersCount(0);
        targetUser = userRepository.save(targetUser);

        // Korisnici koji će ga zapratiti
        followerUsers = new UserAccount[NUMBER_OF_THREADS];
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            UserAccount user = new UserAccount();
            user.setUsername("follower" + i);
            user.setEmail("follower" + i + "@example.com");
            user.setPassword("pass");
            user.setRole(REGISTERED_USER);
            user.setEnabled(true);
            user = userRepository.save(user);
            followerUsers[i] = user;
        }
    }

    @Test
    void testConcurrentFollows() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        CountDownLatch latch = new CountDownLatch(NUMBER_OF_THREADS);

        System.out.println("Starting concurrent follow test for user ID: " + targetUser.getId());

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            final Long followerId = followerUsers[i].getId();
            executorService.execute(() -> {
                try {
                    // Simulacija realnog kašnjenja i trkanja među nitima
                    Thread.sleep(Math.round(Math.random() * 10));
                    followService.follow(followerId, targetUser.getId());
                } catch (Exception e) {
                    System.err.println("Follow failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        //  Provera da li je broj pratilaca ispravno inkrementisan
        UserAccount refreshedTargetUser = userRepository.findById(targetUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("Final followers count: " + refreshedTargetUser.getFollowersCount());

        assertEquals(Long.valueOf(NUMBER_OF_THREADS), refreshedTargetUser.getFollowersCount());
    }
}
