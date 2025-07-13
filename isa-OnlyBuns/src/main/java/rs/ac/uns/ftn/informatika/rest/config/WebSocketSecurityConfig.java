package rs.ac.uns.ftn.informatika.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSocketSecurityConfig {

    @Bean
    public SecurityFilterChain webSocketSecurityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/app/**").authenticated()
                        .anyRequest().permitAll()
                )
                // Ovde možeš dodati ostale security konfiguracije po potrebi
                .csrf(csrf -> csrf.disable()) // ako želiš, možeš isključiti CSRF za WebSocket
        ;

        return http.build();
    }
}
