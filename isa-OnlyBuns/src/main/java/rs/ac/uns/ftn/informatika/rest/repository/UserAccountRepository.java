package rs.ac.uns.ftn.informatika.rest.repository;

import org.apache.catalina.User;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;

import java.util.Collection;
import java.util.List;
@Repository
public interface UserAccountRepository {

    UserAccount findById(Long id);
    UserAccount create(UserAccount userAccount);
    UserAccount update(UserAccount userAccount);
    UserAccount delete(Long id);
    Collection<UserAccount> findAll();

}
