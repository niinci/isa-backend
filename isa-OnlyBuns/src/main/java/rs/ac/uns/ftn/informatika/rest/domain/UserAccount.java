package rs.ac.uns.ftn.informatika.rest.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import rs.ac.uns.ftn.informatika.rest.dto.UserAccountDTO;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserAccounts")
public class UserAccount {

    @Id
    @Column(name = "acc_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "user_name")
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "followers_count")
    private int followersCount;

    @Column(name = "post_count")
    private int postCount;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "is_enabled")
    private boolean isEnabled;


    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

    @Column(name = "last_notification_sent_date")
    private LocalDateTime lastNotificationSentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role = Role.REGISTERED_USER; // Default role

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public UserAccount() {
    }

    public UserAccount(UserAccountDTO userAccountDTO){
        this.address = convertAddressToJson(userAccountDTO.getAddress());
        this.firstName = userAccountDTO.getFirstName();
        this.lastName = userAccountDTO.getLastName();
        this.email = userAccountDTO.getEmail();
        this.password = userAccountDTO.getPassword();
        this.followersCount = userAccountDTO.getFollowersCount();
        this.postCount = userAccountDTO.getPostCount();
        this.role = Role.REGISTERED_USER;


    }

    public UserAccount(Long id, String firstName, String lastName, String email, String password, String address, int followersCount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.followersCount = followersCount;
        this.postCount = postCount;
        this.role = Role.REGISTERED_USER;

    }
    public String convertAddressToJson(Address address) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(address);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Address convertJsonToAddress() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(address, Address.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
    // Getteri i setteri za sva polja

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public LocalDateTime getLastActivityDate() {return lastActivityDate;}
    public void setLastActivityDate(LocalDateTime lastActivityDate) {this.lastActivityDate = lastActivityDate;}

    public LocalDateTime getLastNotificationSentDate() {return lastNotificationSentDate;}
    public void setLastNotificationSentDate(LocalDateTime lastNotificationSentDate) {this.lastNotificationSentDate = lastNotificationSentDate;}

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
