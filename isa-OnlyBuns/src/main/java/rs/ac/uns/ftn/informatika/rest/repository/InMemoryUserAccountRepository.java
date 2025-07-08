package rs.ac.uns.ftn.informatika.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public interface InMemoryUserAccountRepository extends JpaRepository<UserAccount, Long> {

    UserAccount findByEmail(String email);
    List<UserAccount> findByFirstNameContaining(String firstName);

    List<UserAccount> findByLastNameContaining(String lastName);

    List<UserAccount> findByEmailContaining(String email);

    List<UserAccount> findByPostCountBetween(int minPosts, int maxPosts);

    @Query("SELECT u FROM UserAccount u ORDER BY u.followersCount ASC")
    List<UserAccount> findAllSortedByFollowingCount();

    @Query("SELECT u FROM UserAccount u ORDER BY u.email ASC")
    List<UserAccount> findAllSortedByEmail();

    UserAccount findByVerificationCode(String verificationCode);
    /*private static AtomicLong counter = new AtomicLong();
    private final ConcurrentMap<Long, UserAccount> userAccounts = new ConcurrentHashMap<>();

    @Override
    public Collection<UserAccount> findAll() { return userAccounts.values(); }

    @Override
    public UserAccount findById(Long id) { return userAccounts.get(id); }

    @Override
    public UserAccount create(UserAccount userAccount) {

        //userAccounts.put(userAccount.getId(), userAccount);
        return userAcc;
    }

    @Override
    public UserAccount update(UserAccount userAccount) {
        Long id = userAccount.getId();
        userAccounts.put(id, userAccount);
        return userAccount;
    }

    @Override
    public UserAccount delete(Long id) {
        UserAccount userAccount = userAccounts.remove(id);
        return userAccount;
    }*/


}
