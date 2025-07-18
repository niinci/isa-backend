package rs.ac.uns.ftn.informatika.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import rs.ac.uns.ftn.informatika.rest.OnlyBansApplication;
import rs.ac.uns.ftn.informatika.rest.dto.UserAccountDTO;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;
import rs.ac.uns.ftn.informatika.rest.service.UserAccountService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = OnlyBansApplication.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserRegistrationUsernameConcurrencyTest {

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private final int THREAD_COUNT = 10;
    private final String sharedUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);

    @Test
    void concurrentRegistrationWithSameUsername() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        // Generate unique test identifier to avoid conflicts
        String testId = UUID.randomUUID().toString().substring(0, 8);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    // Dodajemo malo random delay da simuliramo realnu konkurentnost
                    Thread.sleep((long) (Math.random() * 50));

                    UserAccountDTO dto = new UserAccountDTO();
                    dto.setUsername(sharedUsername); // Svi koriste isto korisničko ime
                    dto.setFirstName("Ime" + index);
                    dto.setLastName("Prezime" + index);
                    // Koristimo UUID da garantujemo jedinstvene email adrese
                    dto.setEmail("user" + index + "_" + testId + "@example.com");
                    dto.setPassword("test123");

                    userAccountService.registerUser(dto);
                    System.out.println("Thread " + index + ": Uspešno registrovan korisnik");
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

        // Proveravamo da li postoji tačno jedan korisnik sa tim username-om
        long count = userAccountRepository.findAll().stream()
                .filter(u -> sharedUsername.equalsIgnoreCase(u.getUsername()))
                .count();

        assertEquals(1, count, "Samo jedan korisnik sme imati isto korisničko ime.");
    }
}