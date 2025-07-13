package rs.ac.uns.ftn.informatika.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.informatika.rest.domain.ChatGroup;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
}
