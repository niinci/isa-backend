package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.rest.domain.ChatGroup;
import rs.ac.uns.ftn.informatika.rest.domain.ChatMessage;
import rs.ac.uns.ftn.informatika.rest.repository.ChatGroupRepository;
import rs.ac.uns.ftn.informatika.rest.repository.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ChatMessageService {


    private final ChatMessageRepository chatMessageRepository;
    private final ChatGroupRepository chatGroupRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository, ChatGroupRepository chatGroupRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatGroupRepository = chatGroupRepository;
    }

    // Dodavanje nove poruke u grupu
    public ChatMessage addMessage(Long groupId, String senderUsername, String content) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Chat group not found"));

        ChatMessage message = new ChatMessage();
        message.setChatGroup(group);
        message.setSenderUsername(senderUsername);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        return chatMessageRepository.save(message);
    }

    // Dohvatanje poslednjih 10 poruka iz grupe (ili manje ako ih nema toliko)
    public List<ChatMessage> getLastMessages(Long groupId, int count) {
        return chatMessageRepository.findTopNByChatGroupIdOrderByTimestampDesc(groupId, count);
    }
}
