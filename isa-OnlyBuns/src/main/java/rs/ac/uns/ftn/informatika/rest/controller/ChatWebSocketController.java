package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import rs.ac.uns.ftn.informatika.rest.dto.ChatMessageDto;
import rs.ac.uns.ftn.informatika.rest.service.ChatService;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    @MessageMapping("/chat.sendMessage") // Klijent šalje na /app/chat.sendMessage
    public void sendMessage(@Payload ChatMessageDto message) {
        System.out.println("Primljena poruka: " + message.getContent());
        // 1. Sačuvaj poruku u bazi (ili ne moraš ako želiš samo emitovanje)
        ChatMessageDto saved = chatService.sendMessage(message);

        // 2. Pošalji poruku svima koji su pretplaćeni na /topic/group/{id}
        messagingTemplate.convertAndSend("/topic/group/" + saved.getChatGroupId(), saved);
    }
}
