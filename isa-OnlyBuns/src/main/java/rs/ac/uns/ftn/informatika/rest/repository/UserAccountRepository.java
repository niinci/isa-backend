package rs.ac.uns.ftn.informatika.rest.repository;

//import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    // Ovaj metod je potreban za AdminService
    Optional<UserAccount> findByEmail(String email);

    // Ostali custom metodi ako su potrebni
    Optional<UserAccount> findByUsername(String username);

    List<UserAccount> findByLastActivityDateBeforeAndIsEnabledTrueAndLastNotificationSentDateBeforeOrLastNotificationSentDateIsNull(LocalDateTime activityDateTime, LocalDateTime notificationDateTime);

    Optional<UserAccount> findByVerificationCode(String verificationCode);

    List<UserAccount> findByIsEnabledFalseAndVerificationCodeIsNotNullAndRegistrationDateBefore(LocalDateTime cutoff);

}


