package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;
import org.springframework.mail.SimpleMailMessage;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InactiveUserNotifierService {
    private final UserAccountRepository userAccountRepository;
    private final JavaMailSender mailSender;

    @Autowired
    public InactiveUserNotifierService(UserAccountRepository userAccountRepository, JavaMailSender mailSender /*, PostService postService, FollowService followService, LikeService likeService */) {
        this.userAccountRepository = userAccountRepository;
        this.mailSender = mailSender;
        // dodati sta treba za statistiku
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void notifyInactiveUsers() {
        // provjera neaktivnosti
        //LocalDateTime activityThreshold  = LocalDateTime.now().minusMinutes(5); // za testiranje
        LocalDateTime activityThreshold  = LocalDateTime.now().minusDays(7);

        // period mirovanja notifikacija
        LocalDateTime notificationThreshold = LocalDateTime.now().minusDays(7);

        List<UserAccount> inactiveUsers = userAccountRepository.findByLastActivityDateBeforeAndIsEnabledTrueAndLastNotificationSentDateBeforeOrLastNotificationSentDateIsNull(
                activityThreshold , notificationThreshold);
        if (inactiveUsers.isEmpty()) {
            System.out.println("No inactive users found!");
            return;
        }

        for (UserAccount user : inactiveUsers) {
            System.out.println("Inactive user: " + user.getEmail()); // Za debug

            String subject = "OnlyBuns: We Miss You! Here's What You’ve Missed This Week \uD83C\uDF1F";
            String body = buildEmailBody(user);

            try {
                sendEmail(user.getEmail(), subject, body);
                user.setLastNotificationSentDate(LocalDateTime.now());
                userAccountRepository.save(user);
            } catch (Exception e) {
                System.err.println("Error sending email to user " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }

    private String buildEmailBody(UserAccount user) {
        // Implementacija prikupljanja i formatiranja statistike ovdje
        // Trenutno je samo genericka poruka za testiranje

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("Hey ").append(user.getFirstName()).append(",\n\n");
        emailContent.append("It’s been a quiet week without you around! \uD83D\uDC40 ");
        emailContent.append("Here’s what you missed over the past week while you were away:\n\n");
        emailContent.append("   - number of new followers: ");
        emailContent.append("   - total likes on your posts: ");
        emailContent.append("   - total comments on your posts: ");
        // Ovdje ide sumirana statistika
        // emailContent.append("U posljednjih 7 dana, imao/la si X novih pratilaca, Y novih lajkova...\n\n");
        emailContent.append("Hope to see you soon,\n");
        emailContent.append("OnlyBuns Team \uD83D\uDE80");

        return emailContent.toString();
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("onlybunsteam@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

}
