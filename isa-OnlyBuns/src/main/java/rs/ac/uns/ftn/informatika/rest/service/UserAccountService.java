package rs.ac.uns.ftn.informatika.rest.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import rs.ac.uns.ftn.informatika.rest.domain.AuthRequest;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.dto.PasswordChangeDTO;
import rs.ac.uns.ftn.informatika.rest.dto.UserAccountDTO;
import rs.ac.uns.ftn.informatika.rest.dto.UserProfileEditDTO;

import java.util.Collection;
import java.util.List;

public interface UserAccountService {

    // Javne metode - dostupne svima
    UserAccount create(UserAccountDTO userAccountDto, HttpServletRequest request) throws Exception;

    /**
     * Registracija korisnika - javna za sve
     */
    UserAccount registerUser(UserAccountDTO userAccountDTO);

    /**
     * Dobijanje profila korisnika - javno za sve
     */
    UserAccount getUserProfile(Long userId);
    Collection<UserAccount> findAll();
    String getUsernameById(Long userId);
    Page<UserAccountDTO> findAllUsersForDisplay(Pageable pageable);
    UserAccount findById(Long id);
    String verify(AuthRequest authRequest);
    void sendVerificationEmail(UserAccount savedAcc, HttpServletRequest request) throws Exception;
    boolean verifyVerificationCode(String verificationCode);
    List<UserAccount> searchByFirstName(String firstName);
    List<UserAccount> searchByLastName(String lastName);
    List<UserAccount> searchByEmail(String email);
    public List<UserAccount> searchByUsername(String username);
    List<UserAccount> searchByPostCount(int min, int max);
    List<UserAccount> sortByFollowingCount();
    List<UserAccount> sortByEmail();

    // Metode sa ograničenim pristupom

    /**
     * Ažuriranje profila - samo vlasnik ili admin
     */
    @PreAuthorize("hasRole('ADMIN') or @roleUtils.canAccessResource(#userId)")
    UserAccount updateUserProfile(Long userId, UserAccountDTO updateData);

    /**
     * Praćenje korisnika - samo za registrovane korisnike
     */
    @PreAuthorize("hasRole('USER')")
    boolean followUser(Long userToFollowId);

    /**
     * Brisanje korisnika - samo admin
     */
    @PreAuthorize("hasRole('ADMIN')")
    UserAccount delete(Long id);

    boolean changePassword(String userEmail, PasswordChangeDTO passwordChangeDTO);
    UserAccount updateProfile(Long userId, @Valid UserProfileEditDTO profileData);
}