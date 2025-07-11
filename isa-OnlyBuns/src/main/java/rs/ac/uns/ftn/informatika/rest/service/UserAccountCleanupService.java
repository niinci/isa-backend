package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAccountCleanupService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    public void deleteInactiveAccountsOlderThan(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        List<UserAccount> toDelete = userAccountRepository
                .findByIsEnabledFalseAndVerificationCodeIsNotNullAndRegistrationDateBefore(cutoff);

        if (!toDelete.isEmpty()) {
            userAccountRepository.deleteAll(toDelete);
            System.out.println("Obrisano neaktiviranih naloga: " + toDelete.size());
        }
    }
}
