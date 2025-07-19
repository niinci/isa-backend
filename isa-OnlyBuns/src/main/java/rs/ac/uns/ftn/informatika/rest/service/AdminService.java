package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.Role;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /**
     * Registruje novog administratora
     */
    public UserAccount registerAdmin(String email, String password, String firstName, String lastName) {
        // Proveri da li već postoji korisnik sa tim email-om
        Optional<UserAccount> existingUser = userAccountRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        UserAccount admin = new UserAccount();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setFollowersCount(0L);
        admin.setPostCount(0);

        return userAccountRepository.save(admin);
    }

    /**
     * Vraća sve administratore
     */
    public List<UserAccount> getAllAdmins() {
        return userAccountRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .collect(Collectors.toList());
    }

    /**
     * Vraća sve registrovane korisnike
     */
    public List<UserAccount> getAllUsers() {
        return userAccountRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.REGISTERED_USER)
                .collect(Collectors.toList());
    }

    /**
     * Briše korisnika po ID-u
     */
    public boolean deleteUser(Long userId) {
        Optional<UserAccount> userOptional = userAccountRepository.findById(userId);
        if (userOptional.isPresent()) {
            UserAccount user = userOptional.get();
            if (user.getRole() != Role.ADMIN) {
                userAccountRepository.deleteById(userId);
                return true;
            }
        }
        return false;
    }

    /**
     * Menja ulogu korisnika
     */
    public UserAccount changeUserRole(Long userId, Role newRole) {
        Optional<UserAccount> userOptional = userAccountRepository.findById(userId);
        if (userOptional.isPresent()) {
            UserAccount user = userOptional.get();
            user.setRole(newRole);
            return userAccountRepository.save(user);
        }
        return null;
    }

    /**
     * Generiše osnovni izveštaj o aplikaciji
     */
    public Map<String, Object> generateApplicationReport() {
        List<UserAccount> allUsers = userAccountRepository.findAll();

        Map<String, Object> report = new HashMap<>();
        report.put("totalUsers", allUsers.size());
        report.put("registeredUsers", allUsers.stream()
                .filter(user -> user.getRole() == Role.REGISTERED_USER)
                .count());
        report.put("adminUsers", allUsers.stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .count());
        report.put("enabledUsers", allUsers.stream()
                .filter(UserAccount::isEnabled)
                .count());
        report.put("disabledUsers", allUsers.stream()
                .filter(user -> !user.isEnabled())
                .count());

        // Dodaj dodatne statistike
        report.put("totalPosts", allUsers.stream()
                .mapToInt(UserAccount::getPostCount)
                .sum());
        report.put("totalFollowers", allUsers.stream()
                .mapToLong(UserAccount::getFollowersCount) // mapToLong radi sa Long vrednostima
                .sum());


        return report;
    }

    /**
     * Blokira/odblokira korisnika
     */
    public UserAccount toggleUserStatus(Long userId) {
        Optional<UserAccount> userOptional = userAccountRepository.findById(userId);
        if (userOptional.isPresent()) {
            UserAccount user = userOptional.get();
            if (user.getRole() != Role.ADMIN) {
                user.setEnabled(!user.isEnabled());
                return userAccountRepository.save(user);
            }
        }
        return null;
    }
}