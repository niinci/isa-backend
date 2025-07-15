package rs.ac.uns.ftn.informatika.rest.repository;

//import org.apache.catalina.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    //vracam samo polje, a ne ceo entitet
    @Query("SELECT u.id FROM UserAccount u")
    List<Long> findAllUserIds();

    @Modifying
    @Query("UPDATE UserAccount u SET u.followersCount = u.followersCount + 1 WHERE u.id = :userId")
    void incrementFollowersCount(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserAccount u SET u.followersCount = u.followersCount - 1 WHERE u.id = :userId AND u.followersCount > 0")
    void decrementFollowersCount(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserAccount u WHERE u.id = :id")
    Optional<UserAccount> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT u FROM UserAccount u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<UserAccount> findByEmailIgnoreCase(@Param("email") String email);

    List<UserAccount> findByUsernameContainingIgnoreCase(String usernamePart);


    long countByLastActivityDateAfter(LocalDateTime twentyFourHoursAgo);
}


