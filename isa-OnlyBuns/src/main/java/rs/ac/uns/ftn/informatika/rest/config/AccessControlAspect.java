package rs.ac.uns.ftn.informatika.rest.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.informatika.rest.domain.Role;
import rs.ac.uns.ftn.informatika.rest.domain.UserPrincipal;

@Aspect
@Component
public class AccessControlAspect {

    @Around("@annotation(requiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequiresRole requiresRole) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        if (auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            Role userRole = principal.getUserAccount().getRole();

            // Provjeri da li korisnik ima potrebnu ulogu
            if (!hasRequiredRole(userRole, requiresRole.value())) {
                throw new AccessDeniedException("Insufficient privileges");
            }
        }

        return joinPoint.proceed();
    }

    private boolean hasRequiredRole(Role userRole, Role requiredRole) {
        // Admin može sve
        if (userRole == Role.ADMIN) {
            return true;
        }

        // Provjeri specifične uloge
        return userRole == requiredRole;
    }
}

// Kreiranje custom anotacije
@interface RequiresRole {
    Role value();
}