package rs.ac.uns.ftn.informatika.rest.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import rs.ac.uns.ftn.informatika.rest.domain.Role;
import rs.ac.uns.ftn.informatika.rest.domain.UserPrincipal;
import rs.ac.uns.ftn.informatika.rest.service.RateLimiterService;

import java.io.IOException;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String identifier = getClientIdentifier(request);
        Role role = getUserRole();
        String requestUri = request.getRequestURI();
        String httpMethod = request.getMethod();

        System.out.println("RateLimiterFilter: Incoming request - URI: " + requestUri + ", Method: " + httpMethod);
        System.out.println("RateLimiterFilter: Client Identifier: " + identifier + ", User Role: " + role);

        if (!rateLimiterService.isRequestAllowed(identifier, role, requestUri, httpMethod)) {
            response.setStatus(429); // HTTP Status 429: Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests for this action. Please try again later.\"}");
            System.out.println("RateLimiterFilter: Request BLOCKED (429) for " + identifier + " on " + requestUri);
            return;
        }

        System.out.println("RateLimiterFilter: Request ALLOWED for " + identifier + " on " + requestUri);
        filterChain.doFilter(request, response);
    }

    private String getClientIdentifier(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() &&
                auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            System.out.println("RateLimiterFilter.getClientIdentifier: Authenticated user email: " + principal.getUserAccount().getEmail());
            return principal.getUserAccount().getEmail();
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null) {
            String ip = xForwardedFor.split(",")[0].trim();
            System.out.println("RateLimiterFilter.getClientIdentifier: Unauthenticated (X-Forwarded-For): " + ip);
            return ip;
        }

        String remoteAddr = request.getRemoteAddr();
        System.out.println("RateLimiterFilter.getClientIdentifier: Unauthenticated (Remote Addr): " + remoteAddr);
        return remoteAddr;
    }

    private Role getUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() &&
                auth.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            System.out.println("RateLimiterFilter.getUserRole: Authenticated user role: " + principal.getUserAccount().getRole());
            return principal.getUserAccount().getRole();
        }

        System.out.println("RateLimiterFilter.getUserRole: User not authenticated. Returning UNAUTHENTICATED role.");
        return Role.UNAUTHENTICATED;
    }
}