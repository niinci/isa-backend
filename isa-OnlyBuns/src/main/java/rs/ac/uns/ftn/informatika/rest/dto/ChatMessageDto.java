package rs.ac.uns.ftn.informatika.rest.dto;

import java.time.LocalDateTime;

public class ChatMessageDto {
        private Long id;
        private String content;
        private String senderUsername;
        private LocalDateTime timestamp;
        private Long chatGroupId;


        // Getteri i setteri

        public Long getId() {
            return id;
        }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getChatGroupId() {
        return chatGroupId;
    }

    public void setChatGroupId(Long chatGroupId) {
        this.chatGroupId = chatGroupId;
    }

}
