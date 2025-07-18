package rs.ac.uns.ftn.informatika.rest.util;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class UsernameBloomFilter {

    private volatile BloomFilter<String> bloomFilter;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Autowired
    private UserAccountRepository userRepository;

    @PostConstruct
    public void init() {
        lock.writeLock().lock();
        try {
            bloomFilter = BloomFilter.create(
                    Funnels.stringFunnel(StandardCharsets.UTF_8),
                    100000,
                    0.001
            );

            // Učitavanje postojećih korisničkih imena
            List<UserAccount> users = userRepository.findAll();
            for (UserAccount user : users) {
                if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                    bloomFilter.put(user.getUsername().toLowerCase().trim());
                }
            }

            System.out.println("UsernameBloomFilter initialized with " + users.size() + " users");
        } catch (Exception e) {
            System.err.println("Error initializing UsernameBloomFilter: " + e.getMessage());
            // Kreiranje praznog filter-a ako se dogodi greška
            bloomFilter = BloomFilter.create(
                    Funnels.stringFunnel(StandardCharsets.UTF_8),
                    100000, 0.001
            );
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean mightContain(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        lock.readLock().lock();
        try {
            return bloomFilter.mightContain(username.toLowerCase().trim());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }

        lock.writeLock().lock();
        try {
            bloomFilter.put(username.toLowerCase().trim());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public double getExpectedFalsePositiveRate() {
        lock.readLock().lock();
        try {
            return bloomFilter.expectedFpp();
        } finally {
            lock.readLock().unlock();
        }
    }
}