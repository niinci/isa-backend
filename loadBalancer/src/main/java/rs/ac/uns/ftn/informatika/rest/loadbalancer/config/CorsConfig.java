package rs.ac.uns.ftn.informatika.rest.loadbalancer.config; // Ili odgovarajući paket za tvoje config klase

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Dozvoli CORS za sve putanje
                        .allowedOrigins("http://localhost:4200") // Dozvoli pristup samo sa Angular frontenda
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Dozvoli navedene HTTP metode
                        .allowedHeaders("*") // Dozvoli sve headere
                        .allowCredentials(true); // Dozvoli slanje credentiala (cookies, authentication headers)
            }
        };
    }
}