package rs.ac.uns.ftn.informatika.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.informatika.rest.domain.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Metoda da dohvati poslednjih N poruka po grupi sortirano opadajuće po vremenu
    List<ChatMessage> findTop10ByChatGroupIdOrderByTimestampDesc(Long chatGroupId);

    // Za parametar count:
    default List<ChatMessage> findTopNByChatGroupIdOrderByTimestampDesc(Long chatGroupId, int count) {
        // Ovo nije podržano direktno u JPA repository, koristi findTop10 i limit na servis nivou, ili query sa @Query (ako želiš)
        return findTop10ByChatGroupIdOrderByTimestampDesc(chatGroupId); // za primer vraća 10
    }
}
