package rs.ac.uns.ftn.informatika.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.dto.UserAccountDTO;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;
import rs.ac.uns.ftn.informatika.rest.service.UserAccountService;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserRegistrationConcurrencyTest {

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private final int THREAD_COUNT = 5;
    private final String sharedEmail = "same.email@example.com";

    @Test
    void concurrentRegistrationWithSameEmail() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    Thread.sleep((long) (Math.random() * 20));
                    UserAccountDTO dto = new UserAccountDTO();
                    dto.setUsername("user" + index);
                    dto.setFirstName("Ime" + index);
                    dto.setLastName("Prezime" + index);
                    dto.setEmail(sharedEmail); // Svi koriste isti email
                    dto.setPassword("test123");
                    userAccountService.registerUser(dto);
                } catch (Exception e) {
                    System.out.println("Thread " + index + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long count = userAccountRepository.findAll().stream()
                .filter(u -> sharedEmail.equalsIgnoreCase(u.getEmail()))
                .count();

        assertEquals(1, count, "Samo jedan korisnik sme imati isti email.");
    }
}
