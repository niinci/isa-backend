package rs.ac.uns.ftn.informatika.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.rest.domain.ChatGroup;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.dto.ChatGroupDto;
import rs.ac.uns.ftn.informatika.rest.dto.ChatMessageDto;
import rs.ac.uns.ftn.informatika.rest.dto.UserAccountWithoutAddressDto;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;
import rs.ac.uns.ftn.informatika.rest.service.ChatService;
import rs.ac.uns.ftn.informatika.rest.service.UserAccountService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserAccountService userAccountService;


    private final UserAccountRepository userAccountRepository;

    public ChatController(ChatService chatService,UserAccountRepository userAccountRepository
    , UserAccountService userAccountService) {
        this.chatService = chatService;
        this.userAccountRepository = userAccountRepository;
        this.userAccountService = userAccountService;
    }

    // --- Slanje poruke ---
    @PostMapping("/send")
    public ResponseEntity<ChatMessageDto> sendMessage(@RequestBody ChatMessageDto dto) {
        ChatMessageDto sentMessage = chatService.sendMessage(dto);
        return ResponseEntity.ok(sentMessage);
    }

    // --- Dohvatanje poslednjih 10 poruka u grupi ---
    @GetMapping("/{groupId}/last-messages")
    public ResponseEntity<List<ChatMessageDto>> getLastMessages(@PathVariable Long groupId) {
        List<ChatMessageDto> messages = chatService.getLastMessages(groupId, 10);
        return ResponseEntity.ok(messages);
    }

    // --- Kreiranje nove chat grupe ---
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/group/create")
    public ResponseEntity<ChatGroup> createGroup(@RequestParam String groupName,
                                                 @RequestParam String adminUsername) {
        ChatGroup group = chatService.createGroup(groupName, adminUsername);
        return ResponseEntity.ok(group);
    }

    // --- Dodavanje korisnika u grupu (samo admin) ---
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/group/{groupId}/add-user")
    public ResponseEntity<Void> addUserToGroup(@PathVariable Long groupId,
                                               @RequestParam String usernameToAdd,
                                               @RequestParam String requestingUser) {
        chatService.addUserToGroup(groupId, usernameToAdd, requestingUser);
        return ResponseEntity.ok().build();
    }

    // --- Uklanjanje korisnika iz grupe (samo admin) ---
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/group/{groupId}/remove-user")
    public ResponseEntity<Void> removeUserFromGroup(@PathVariable Long groupId,
                                                    @RequestParam String usernameToRemove,
                                                    @RequestParam String requestingUser) {
        chatService.removeUserFromGroup(groupId, usernameToRemove, requestingUser);
        return ResponseEntity.ok().build();
    }

    // --- Dohvatanje svih grupa korisnika ---
    @GetMapping("/groups/{username}")
    public List<ChatGroupDto> getGroupsForUser(@PathVariable String username) {
        return chatService.getGroupsForUser(username);
    }


    @GetMapping("/username-by-email")
    public ResponseEntity<String> getUsernameByEmail(@RequestParam String email) {
        Optional<UserAccount> user = userAccountRepository.findByEmail(email);
        if (user.isPresent() && user.get().getUsername() != null) {
            return ResponseEntity.ok(user.get().getUsername());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/users/search")
    public List<UserAccountWithoutAddressDto> searchUsers(@RequestParam String query) {
        return userAccountService.searchUsers(query);
    }









}
