package rs.ac.uns.ftn.informatika.rest.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ChatGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //naziv ceta
    private String name;

    @ManyToOne
    private UserAccount admin;
    //lista clanova
    @ManyToMany
    private List<UserAccount> members = new ArrayList<>();
    //lista poruka
    @OneToMany(mappedBy = "chatGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    // Getteri i setteri
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UserAccount getAdmin() { return admin; }
    public void setAdmin(UserAccount admin) { this.admin = admin; }

    public List<UserAccount> getMembers() { return members; }
    public void setMembers(List<UserAccount> members) { this.members = members; }

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
}
