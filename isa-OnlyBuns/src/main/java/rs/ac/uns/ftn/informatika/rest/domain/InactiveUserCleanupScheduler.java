package rs.ac.uns.ftn.informatika.rest.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.rest.service.UserAccountCleanupService;

import java.time.LocalDate;

@Component
public class InactiveUserCleanupScheduler {

    @Autowired
    private UserAccountCleanupService cleanupService;

    // Pokrece se svakog dana u 23:59
    @Scheduled(cron = "0 23 59 L * ?")
    public void scheduleInactiveUserCleanup() {
            cleanupService.deleteInactiveAccountsOlderThan(1);
    }

   /* @Scheduled(cron = "0 45 18 * * ?")
    public void scheduleInactiveUserCleanup() {
        cleanupService.deleteInactiveAccountsOlderThan(1);
    }*/

}

