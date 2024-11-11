package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.User;
import rs.ac.uns.ftn.informatika.rest.dto.UserRegistrationDTO;
import rs.ac.uns.ftn.informatika.rest.repository.UserRepository;

import java.util.Collection;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtService jwtService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Override
    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User create(UserRegistrationDTO registrationDTO) throws Exception {
        // Check if email or username already exists
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new Exception("Email is already registered.");
        }
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new Exception("Username is already taken.");
        }

        // Encode password
        registrationDTO.setPassword(encoder.encode(registrationDTO.getPassword()));

        // Create and save user
        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(registrationDTO.getPassword());
        user.setName(registrationDTO.getName());
        user.setAddress(registrationDTO.getAddress());
        user.setActive(false);  // User is inactive until email is confirmed

        return userRepository.save(user);
    }

    @Override
    public User delete(Long id) {
        User deletedUser = userRepository.findById(id).orElse(null);
        if (deletedUser != null) {
            userRepository.deleteById(id);
        }
        return deletedUser;
    }

    @Override
    public User update(UserRegistrationDTO userRegistrationDTO, Long id) throws Exception {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found with id: " + id));

        existingUser.setName(userRegistrationDTO.getName());
        existingUser.setEmail(userRegistrationDTO.getEmail());
        existingUser.setUsername(userRegistrationDTO.getUsername());
        existingUser.setAddress(userRegistrationDTO.getAddress());
        existingUser.setPassword(encoder.encode(userRegistrationDTO.getPassword()));

        return userRepository.save(existingUser);
    }

    @Override
    public String verify(String username, String password) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(username);
        } else {
            return "Failure";
        }
    }
}
