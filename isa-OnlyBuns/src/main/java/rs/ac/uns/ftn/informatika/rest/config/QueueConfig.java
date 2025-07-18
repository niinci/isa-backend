package rs.ac.uns.ftn.informatika.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rs.ac.uns.ftn.informatika.rest.customqueue.DirectMessageQueue;
import rs.ac.uns.ftn.informatika.rest.dto.CareLocationDTO;

@Configuration
public class QueueConfig {

    @Bean
    public DirectMessageQueue<CareLocationDTO> careLocationQueue() {
        return new DirectMessageQueue<>();
    }
}