package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import rs.ac.uns.ftn.informatika.rest.domain.ChatMessage;
import rs.ac.uns.ftn.informatika.rest.service.ChatMessageService;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    public ChatController(SimpMessagingTemplate messagingTemplate, ChatMessageService chatMessageService) {
        this.messagingTemplate = messagingTemplate;
        this.chatMessageService = chatMessageService;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        chatMessageService.addMessage(
                chatMessage.getChatGroup().getId(),
                chatMessage.getSenderUsername(),
                chatMessage.getContent()
        );
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getChatGroup().getId(), chatMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSenderUsername());
        messagingTemplate.convertAndSend("/topic/" + chatMessage.getChatGroup().getId(), chatMessage);
    }
}

