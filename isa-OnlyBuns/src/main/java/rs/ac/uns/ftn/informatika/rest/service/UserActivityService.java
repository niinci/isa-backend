package rs.ac.uns.ftn.informatika.rest.service;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class UserActivityService {

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void registerActiveUsersGauge() {
        // Registracija Gauge metrike za aktivne korisnike u poslednja 24 sata
        meterRegistry.gauge("app_active_users_24h", this, service -> service.getActiveUsersCountLast24Hours());
        System.out.println("Gauge 'app_active_users_24h' registered for active users in last 24 hours.");

        // za testiranje
        // meterRegistry.gauge("app_active_users_30s", this, service -> service.getActiveUsersCountLastXSeconds(30));
    }

    @Transactional
    public void recordUserActivity(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastActivityDate(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    // Metoda za dobijanje broja aktivnih korisnika u posljednjih 24h
    public long getActiveUsersCountLast24Hours() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minus(24, ChronoUnit.HOURS);
        return userRepository.countByLastActivityDateAfter(twentyFourHoursAgo);
    }

    // Metoda za dobijanje broja aktivnih korisnika u posljednjih X sekundi (za testiranje)
    public long getActiveUsersCountLastXSeconds(long seconds) {
        LocalDateTime xSecondsAgo = LocalDateTime.now().minus(seconds, ChronoUnit.SECONDS);
        return userRepository.countByLastActivityDateAfter(xSecondsAgo);
    }
}