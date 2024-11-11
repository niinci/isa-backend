package rs.ac.uns.ftn.informatika.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // Omogućava CORS za sve rute
                .allowedOrigins("http://localhost:4200")  // Dozvoljava Angular aplikaciji da šalje zahteve
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Dozvoljeni HTTP metodi
                .allowedHeaders("*")  // Dozvoljava sva zaglavlja
                .allowCredentials(true);  // Ako koristiš autentifikaciju sa kolačićima
    }
}
