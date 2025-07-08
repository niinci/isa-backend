package rs.ac.uns.ftn.informatika.rest.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import rs.ac.uns.ftn.informatika.rest.domain.Role;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.domain.UserPrincipal;

public class RoleUtils {

    /**
     * Vraća trenutno ulogovanog korisnika
     */
    public static UserAccount getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() &&
                auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            return principal.getUserAccount();
        }

        return null;
    }

    /**
     * Vraća ulogu trenutno ulogovanog korisnika
     */
    public static Role getCurrentUserRole() {
        UserAccount user = getCurrentUser();
        return user != null ? user.getRole() : Role.UNAUTHENTICATED;
    }

    /**
     * Proverava da li je korisnik admin
     */
    public static boolean isCurrentUserAdmin() {
        return getCurrentUserRole() == Role.ADMIN;
    }

    /**
     * Proverava da li je korisnik registrovan
     */
    public static boolean isCurrentUserRegistered() {
        Role role = getCurrentUserRole();
        return role == Role.REGISTERED_USER || role == Role.ADMIN;
    }

    /**
     * Proverava da li korisnik ima potrebnu ulogu
     */
    public static boolean hasRole(Role requiredRole) {
        Role userRole = getCurrentUserRole();

        // Admin može sve
        if (userRole == Role.ADMIN) {
            return true;
        }

        return userRole == requiredRole;
    }

    /**
     * Proverava da li korisnik može da pristupi resursu
     */
    public static boolean canAccessResource(Long resourceOwnerId) {
        UserAccount currentUser = getCurrentUser();

        if (currentUser == null) {
            return false;
        }

        // Admin može sve
        if (currentUser.getRole() == Role.ADMIN) {
            return true;
        }

        // Korisnik može pristupiti samo svojim resursima
        return currentUser.getId().equals(resourceOwnerId);
    }

    /**
     * Proverava da li je korisnik vlasnik resursa ili admin
     */
    public static boolean isOwnerOrAdmin(Long resourceOwnerId) {
        return canAccessResource(resourceOwnerId);
    }
}