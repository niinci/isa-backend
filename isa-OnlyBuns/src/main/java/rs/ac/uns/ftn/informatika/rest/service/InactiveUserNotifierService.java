package rs.ac.uns.ftn.informatika.rest.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.Post;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.repository.PostLikeRepository;
import rs.ac.uns.ftn.informatika.rest.repository.PostRepository;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;
import org.springframework.mail.SimpleMailMessage;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InactiveUserNotifierService {
    private final UserAccountRepository userAccountRepository;
    private final PostRepository postRepository;
    private final JavaMailSender mailSender;
    private final UserStatisticsService userStatisticsService;

    @Autowired
    public InactiveUserNotifierService(UserAccountRepository userAccountRepository, PostLikeRepository postLikeRepository, PostRepository postRepository, JavaMailSender mailSender, UserStatisticsService userStatisticsService /*, PostService postService, FollowService followService, LikeService likeService */) {
        this.userAccountRepository = userAccountRepository;
        this.postRepository = postRepository;
        this.mailSender = mailSender;
        // dodati sta treba za statistiku
        this.userStatisticsService = userStatisticsService;
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void notifyInactiveUsers() {
        // provjera neaktivnosti
        // LocalDateTime activityThreshold  = LocalDateTime.now().minusMinutes(5); // za testiranje
        LocalDateTime activityThreshold  = LocalDateTime.now().minusDays(7);

        // period mirovanja notifikacija
        LocalDateTime notificationThreshold = LocalDateTime.now().minusDays(7);
        //LocalDateTime notificationThreshold = LocalDateTime.now().minusMinutes(10); // za testiranje

        List<UserAccount> inactiveUsers = userAccountRepository.findByLastActivityDateBeforeAndIsEnabledTrueAndLastNotificationSentDateBeforeOrLastNotificationSentDateIsNull(
                activityThreshold , notificationThreshold);
        if (inactiveUsers.isEmpty()) {
            System.out.println("No inactive users found!");
            return;
        }

        for (UserAccount user : inactiveUsers) {
            System.out.println("Inactive user: " + user.getEmail()); // Za debug

            String subject = "OnlyBuns: We Miss You! Here's What You’ve Missed This Week \uD83C\uDF1F";
            String body = userStatisticsService.buildEmailBodyWithStatistics(user);

            try {
                sendEmail(user.getEmail(), subject, body);
                user.setLastNotificationSentDate(LocalDateTime.now());
                userAccountRepository.save(user);
            } catch (Exception e) {
                System.err.println("Error sending email to user " + user.getEmail() + ": " + e.getMessage());
            }
        }
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
