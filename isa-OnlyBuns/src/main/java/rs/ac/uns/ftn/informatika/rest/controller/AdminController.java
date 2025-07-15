package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.rest.domain.Role;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.service.AdminService;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Registruje novog administratora
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@RequestBody AdminRegistrationRequest request) {
        try {
            UserAccount admin = adminService.registerAdmin(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName()
            );
            return ResponseEntity.ok(new AdminResponse(admin));
        } catch (RuntimeException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorMap);
        }
    }
    /**
     * Vraća sve administratore
     */
    @GetMapping("/admins")
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        List<UserAccount> admins = adminService.getAllAdmins();
        List<AdminResponse> adminResponses = admins.stream()
                .map(AdminResponse::new).collect(Collectors.toList());

        return ResponseEntity.ok(adminResponses);
    }

    /**
     * Vraća sve registrovane korisnike
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminResponse>> getAllUsers() {
        List<UserAccount> users = adminService.getAllUsers();
        List<AdminResponse> userResponses = users.stream()
                .map(AdminResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    /**
     * Briše korisnika
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        boolean deleted = adminService.deleteUser(userId);
        if (deleted) {
            Map<String, String> successMap = new HashMap<>();
            successMap.put("message", "User deleted successfully");
            return ResponseEntity.ok(successMap);
        } else {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Cannot delete user");
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    /**
     * Menja ulogu korisnika
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long userId, @RequestBody RoleChangeRequest request) {
        UserAccount user = adminService.changeUserRole(userId, request.getRole());
        if (user != null) {
            return ResponseEntity.ok(new AdminResponse(user));
        } else {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "User not found");
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    /**
     * Generiše izveštaj aplikacije
     */
    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> getApplicationReport() {
        Map<String, Object> report = adminService.generateApplicationReport();
        return ResponseEntity.ok(report);
    }

    /**
     * Blokira/odblokira korisnika
     */
    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        UserAccount user = adminService.toggleUserStatus(userId);
        if (user != null) {
            return ResponseEntity.ok(new AdminResponse(user));
        } else {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "Cannot change user status");
            return ResponseEntity.badRequest().body(errorMap);
        }
    }

    // DTO klase
    public static class AdminRegistrationRequest {
        private String email;
        private String password;
        private String firstName;
        private String lastName;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class RoleChangeRequest {
        private Role role;

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
    }

    public static class AdminResponse {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private Role role;
        private boolean enabled;
        private Long followersCount;
        private int postCount;

        public AdminResponse(UserAccount user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.role = user.getRole();
            this.enabled = user.isEnabled();
            this.followersCount = user.getFollowersCount();
            this.postCount = user.getPostCount();
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public Long getFollowersCount() { return followersCount; }
        public void setFollowersCount(long followersCount) { this.followersCount = followersCount; }

        public int getPostCount() { return postCount; }
        public void setPostCount(int postCount) { this.postCount = postCount; }
    }
}