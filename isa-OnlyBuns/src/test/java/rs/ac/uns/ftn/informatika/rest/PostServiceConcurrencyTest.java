package rs.ac.uns.ftn.informatika.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import rs.ac.uns.ftn.informatika.rest.domain.Post;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.repository.PostLikeRepository;
import rs.ac.uns.ftn.informatika.rest.repository.PostRepository;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;
import rs.ac.uns.ftn.informatika.rest.service.PostService;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static rs.ac.uns.ftn.informatika.rest.domain.Role.REGISTERED_USER;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD) // Resetuje Spring kontekst pre svakog testa
public class PostServiceConcurrencyTest {
    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    private Post testPost;
    private UserAccount[] testUsers;
    private final int NUMBER_OF_THREADS = 100; // Broj simuliranih korisnika

    @BeforeEach
    void setup() {
        // brise sve prethodne podatke da bi test bio izolovan
        postLikeRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        // test post
        testPost = new Post("Test opis", "test_image.jpg", 1L, false, 0.0, 0.0, LocalDateTime.now(), 0L, null);
        testPost.setUserId(1L);
        testPost = postRepository.save(testPost);

        // korisnici za testiranje
        testUsers = new UserAccount[NUMBER_OF_THREADS];
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            UserAccount user = new UserAccount();
            user.setUsername("testuser" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPassword("password");
            user.setRole(REGISTERED_USER);
            user.setEnabled(true);
            user.setPostCount(0);
            user = userRepository.save(user);
            testUsers[i] = user;
        }
    }

    @Test
    void testConcurrentLikes() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        CountDownLatch latch = new CountDownLatch(NUMBER_OF_THREADS); // brojac za cekanje svih niti

        System.out.println("Starting concurrent like test for post ID: " + testPost.getId());

        // simulacija istovremenih lajkova od strane vise korisnika
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            final Long currentUserId = testUsers[i].getId();
            executorService.execute(() -> {
                try {
                    Thread.sleep(Math.round(Math.random() * 10)); // kasnjenje do 10ms
                    postService.like(testPost.getId(), currentUserId);
                } catch (Exception e) {
                    System.err.println("Error liking post: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS); // ceka najvise 30 sekundi
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        // azurirana vrijednost likesCount iz baze podataka
        Post updatedPost = postRepository.findById(testPost.getId()).orElse(null);

        System.out.println("Final likes count: " + (updatedPost != null ? updatedPost.getLikesCount() : "N/A"));

        // provjera da li je likesCount tačno jednak broju niti
        // Ako je svaki korisnik lajkovao post, broj lajkova bi trebao biti jednak broju korisnika
        assertEquals(Long.valueOf(NUMBER_OF_THREADS), updatedPost.getLikesCount());
    }
}





