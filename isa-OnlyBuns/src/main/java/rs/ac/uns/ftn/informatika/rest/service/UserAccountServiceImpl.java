package rs.ac.uns.ftn.informatika.rest.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.config.Utility;
import rs.ac.uns.ftn.informatika.rest.domain.AuthRequest;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.domain.Role;
import rs.ac.uns.ftn.informatika.rest.dto.PasswordChangeDTO;
import rs.ac.uns.ftn.informatika.rest.dto.UserAccountDTO;
import rs.ac.uns.ftn.informatika.rest.dto.UserProfileEditDTO;
import rs.ac.uns.ftn.informatika.rest.repository.InMemoryUserAccountRepository;
import rs.ac.uns.ftn.informatika.rest.util.RoleUtils;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    @Autowired
    private final InMemoryUserAccountRepository userAccountRepository;

    @Autowired
    private AuthenticationManager authManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    public UserAccountServiceImpl(InMemoryUserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public Page<UserAccount> findAll(Pageable pageable) {
        return userAccountRepository.findAll(pageable);
    }

    @Override
    public UserAccount findById(Long id) {
        return userAccountRepository.findById(id).orElse(null);
    }

    @Override
    public String getUsernameById(Long userId) {
        UserAccount user = userAccountRepository.findById(userId).orElse(null);
        if (user != null) {
            return user.getFirstName() + " " + user.getLastName(); // Ili prilagodite prikaz korisničkog imena
        }
        return null;
    }

    @Override
    public UserAccount create(UserAccountDTO accountDTO, HttpServletRequest request) throws Exception {

        if (userAccountRepository.findByEmail(accountDTO.getEmail()) != null) {
            throw new Exception("Email already exists");
        } else {
            accountDTO.setPassword(encoder.encode(accountDTO.getPassword()));

            UserAccount savedAcc = new UserAccount(accountDTO);
            savedAcc.setEnabled(false);
            String randomCode = RandomStringUtils.randomAlphanumeric(64);
            savedAcc.setVerificationCode(randomCode);
            userAccountRepository.save(savedAcc);
            sendVerificationEmail(savedAcc, request);
            return savedAcc;
        }
    }

    /**
     * Metoda za registraciju korisnika - javna za sve
     */
    @Override
    public UserAccount registerUser(UserAccountDTO userAccountDTO) {
        // Provjeri da li već postoji korisnik
        if (userAccountRepository.findByEmail(userAccountDTO.getEmail()) != null) {
            throw new RuntimeException("User already exists");
        }

        // Enkriptuj lozinku
        userAccountDTO.setPassword(encoder.encode(userAccountDTO.getPassword()));

        UserAccount user = new UserAccount(userAccountDTO);
        user.setRole(Role.REGISTERED_USER); // Postavlja default ulogu
        user.setEnabled(false); // Potrebna verifikacija

        // Generiši verifikacioni kod
        String randomCode = RandomStringUtils.randomAlphanumeric(64);
        user.setVerificationCode(randomCode);

        return userAccountRepository.save(user);
    }

    /**
     * Dobijanje profila korisnika - javno za sve
     */
    @Override
    public UserAccount getUserProfile(Long userId) {
        return userAccountRepository.findById(userId).orElse(null);
    }

    /**
     * Ažuriranje profila - samo vlasnik ili admin
     */
    @Override
    public UserAccount updateUserProfile(Long userId, UserAccountDTO updateData) {
        UserAccount user = userAccountRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        user.setFirstName(updateData.getFirstName());
        user.setLastName(updateData.getLastName());
        user.setEmail(updateData.getEmail());
        // Konvertuj Address objekat u String ili dodijeli odgovarajuće polje
        if (updateData.getAddress() != null) {
            // Opcija 1: Ako Address ima toString() metodu
            user.setAddress(updateData.getAddress().toString());
            // Opcija 2: Ili ako Address ima određeno polje koje želite koristiti
            // user.setAddress(updateData.getAddress().getFullAddress());
        }

        // Ako je lozinka prosledjena, enkriptuj je
        if (updateData.getPassword() != null && !updateData.getPassword().isEmpty()) {
            user.setPassword(encoder.encode(updateData.getPassword()));
        }

        return userAccountRepository.save(user);
    }

    /**
     * Praćenje korisnika - samo za registrovane korisnike
     */
    @Override
    public boolean followUser(Long userToFollowId) {
        UserAccount currentUser = RoleUtils.getCurrentUser();
        UserAccount userToFollow = userAccountRepository.findById(userToFollowId).orElse(null);

        if (currentUser == null || userToFollow == null) {
            return false;
        }

        // Logika za praćenje
        userToFollow.setFollowersCount(userToFollow.getFollowersCount() + 1);

        // Azuriranje lastActivityDate
        userToFollow.setLastActivityDate(LocalDateTime.now());

        userAccountRepository.save(userToFollow);

        return true;
    }

    @Override
    public Collection<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }

    @Override
    public void sendVerificationEmail(UserAccount savedAcc, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        String toAddress = savedAcc.getEmail();
        String fromAddress = "onlybunsteam@gmail.com";
        String subject = "Verification Email";
        String senderName = "OnlyBuns Team";
        String content = "<p>Dear " + savedAcc.getFirstName() + ",<p>";
        content += "<p>Please click the link below to verify your account</p>";
        String siteUrl = Utility.getSiteURL(request) + "/api/userAccount/verify?code=" + savedAcc.getVerificationCode();
        content += "<h3><a href=\"" + siteUrl + "\">VERIFY</a></h3>";
        content += "<p>The OnlyBuns Team</p>";
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        helper.setText(content, true); // Setting 'true' enables HTML

        mailSender.send(message);
    }

    /**
     * Brisanje korisnika - samo admin (sa @PreAuthorize u interfejsu)
     */
    @Override
    public UserAccount delete(Long id) {
        UserAccount user = userAccountRepository.findById(id).orElse(null);
        if (user != null && user.getRole() != Role.ADMIN) {
            userAccountRepository.deleteById(id);
            return user;
        }
        return null;
    }

    @Override
    public String verify(AuthRequest credentials) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword()));

        if (authentication.isAuthenticated()) {
            UserAccount user = userAccountRepository.findByEmail(credentials.getUsername());

            if (user != null && user.isEnabled()) {
                // Azuriranje lastActivityDate
                user.setLastActivityDate(LocalDateTime.now());
                userAccountRepository.save(user);

                return jwtService.generateToken(user.getEmail(), user.getId(), user.getRole().toString());
            }
        }
        return "Failure";
    }

    @Override
    public boolean verifyVerificationCode(String verificationCode) {
        UserAccount user = userAccountRepository.findByVerificationCode(verificationCode);

        if (user == null || user.isEnabled()) {
            return false;
        } else {
            user.setVerificationCode(null);
            user.setEnabled(true);
            userAccountRepository.save(user);

            return true;
        }
    }

    @Override
    public List<UserAccount> searchByFirstName(String firstName) {
        return userAccountRepository.findByFirstNameContaining(firstName);
    }

    @Override
    public List<UserAccount> searchByLastName(String lastName) {
        return userAccountRepository.findByLastNameContaining(lastName);
    }

    @Override
    public List<UserAccount> searchByEmail(String email) {
        return userAccountRepository.findByEmailContaining(email);
    }

    @Override
    public List<UserAccount> searchByPostCount(int minPosts, int maxPosts) {
        return userAccountRepository.findByPostCountBetween(minPosts, maxPosts);
    }

    @Override
    public List<UserAccount> sortByFollowingCount() {
        return userAccountRepository.findAllSortedByFollowingCount();
    }

    @Override
    public List<UserAccount> sortByEmail() {
        return userAccountRepository.findAllSortedByEmail();
    }

    @Override
    public boolean changePassword(String userEmail, PasswordChangeDTO passwordChangeDTO) {
        UserAccount user = userAccountRepository.findByEmail(userEmail);

        if (user != null) {
            // Proverava trenutni password
            if (encoder.matches(passwordChangeDTO.getCurrentPassword(), user.getPassword())) {
                // Proverava da li se novi password i potvrda poklapaju
                if (passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
                    // Enkriptuje i čuva novi password
                    user.setPassword(encoder.encode(passwordChangeDTO.getNewPassword()));
                    // Azuriranje lastActivityDate
                    user.setLastActivityDate(LocalDateTime.now());
                    userAccountRepository.save(user);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public UserAccount updateProfile(Long userId, UserProfileEditDTO profileData) {
        Optional<UserAccount> userOptional = userAccountRepository.findById(userId);

        if (userOptional.isPresent()) {
            UserAccount user = userOptional.get();

            if (profileData.getFirstName() != null) {
                user.setFirstName(profileData.getFirstName());
            }
            if (profileData.getLastName() != null) {
                user.setLastName(profileData.getLastName());
            }
            if (profileData.getAddress() != null) {
                user.setAddress(user.convertAddressToJson(profileData.getAddress()));
            }

            user.setLastActivityDate(LocalDateTime.now());

            return userAccountRepository.save(user);
        }

        return null;
    }

}