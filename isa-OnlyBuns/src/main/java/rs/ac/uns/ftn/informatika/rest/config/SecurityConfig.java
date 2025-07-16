package rs.ac.uns.ftn.informatika.rest.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private RateLimiterFilter rateLimiterFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(customizer -> customizer.disable());
        http.cors(Customizer.withDefaults());
        http.authorizeHttpRequests(request -> request
                // Public endpoints - dostupni svima (uključujući neautentifikovane)
                .requestMatchers("/api/posts").permitAll()
                .requestMatchers("/api/posts/sorted").permitAll()
                .requestMatchers("/api/userAccount/register").permitAll()
                .requestMatchers("/api/userAccount/login").permitAll()
                .requestMatchers("/api/userAccount/verify").permitAll()
                .requestMatchers("/api/userAccount/getUserInfo").hasRole("USER")
                .requestMatchers("/api/userAccount/profile/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/comments/post/**").permitAll()
                .requestMatchers("/api/userAccount/location").hasRole("USER")
                .requestMatchers("/mq/**").hasRole("USER")

                // Swagger endpoints
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()

                // pristup /actuator/prometheus endpointu
                .requestMatchers("/actuator/**").permitAll()

                // User endpoints - potrebna USER uloga
                .requestMatchers("/api/posts/create").hasRole("USER")
                .requestMatchers("/api/posts/*/like").hasRole("USER")
                .requestMatchers("/api/posts/*/comment").hasRole("USER")
                .requestMatchers("/api/userAccount/follow/**").hasRole("USER")
                .requestMatchers("/api/userAccount/profile/update").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/comments/post/**").hasRole("USER")
                .requestMatchers("/api/trends").hasRole("USER")

                // Admin endpoints - potrebna ADMIN uloga
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/posts/*/delete").hasRole("ADMIN")
                .requestMatchers("/api/comments/*/delete").hasRole("ADMIN")
                .requestMatchers("/api/userAccount/admin/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.OPTIONS, "/chat-websocket/**").permitAll()
                .requestMatchers("/chat-websocket/**").permitAll()

                // Sve ostalo zahteva autentifikaciju
                .anyRequest().authenticated());

        http.httpBasic(Customizer.withDefaults());
        http.sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimiterFilter, JwtFilter.class);
        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:4200");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


}
