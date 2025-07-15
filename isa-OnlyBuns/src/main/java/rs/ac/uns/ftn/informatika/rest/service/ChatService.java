package rs.ac.uns.ftn.informatika.rest.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.informatika.rest.domain.ChatGroup;
import rs.ac.uns.ftn.informatika.rest.domain.ChatMessage;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.dto.ChatMessageDto;
import rs.ac.uns.ftn.informatika.rest.repository.ChatGroupRepository;
import rs.ac.uns.ftn.informatika.rest.repository.ChatMessageRepository;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;
import org.springframework.data.domain.Pageable;
import rs.ac.uns.ftn.informatika.rest.dto.ChatGroupDto;
import rs.ac.uns.ftn.informatika.rest.dto.UserAccountWithoutAddressDto;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatGroupRepository chatGroupRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserAccountService userAccountService;


    public ChatService(ChatGroupRepository chatGroupRepository,
                       ChatMessageRepository chatMessageRepository,
                       UserAccountRepository userAccountRepository,
                       UserAccountService userAccountService) {
        this.chatGroupRepository = chatGroupRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userAccountRepository = userAccountRepository;
        this.userAccountService = userAccountService;
    }

    // ======================= Chat poruke ============================

    public ChatMessageDto sendMessage(ChatMessageDto dto) {
        ChatGroup group = chatGroupRepository.findById(dto.getChatGroupId())
                .orElseThrow(() -> new RuntimeException("Chat group not found"));

        if (!isMember(group, dto.getSenderUsername())) {
            throw new RuntimeException("Sender is not a member of the group");
        }

        ChatMessage message = new ChatMessage();
        message.setContent(dto.getContent());
        message.setSenderUsername(dto.getSenderUsername());
        message.setChatGroup(group);

        chatMessageRepository.save(message);

        return mapToDto(message);
    }

    public List<ChatMessageDto> getLastMessages(Long groupId, int count) {
        return chatMessageRepository.findLastMessagesByGroupId(groupId, (Pageable) PageRequest.of(0, count))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private boolean isMember(ChatGroup group, String username) {
        return group.getMembers().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    private ChatMessageDto mapToDto(ChatMessage msg) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(msg.getId());
        dto.setContent(msg.getContent());
        dto.setTimestamp(msg.getTimestamp());
        dto.setSenderUsername(msg.getSenderUsername());
        dto.setChatGroupId(msg.getChatGroup().getId());
        return dto;
    }

    // ======================= Chat grupe ============================

    public ChatGroup createGroup(String groupName, String adminUsername) {
        UserAccount admin = userAccountRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        ChatGroup group = new ChatGroup();
        group.setName(groupName);
        group.setAdmin(admin);
        group.getMembers().add(admin);

        return chatGroupRepository.save(group);
    }

    public void addUserToGroup(Long groupId, String usernameToAdd, String requestingUser) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!group.getAdmin().getUsername().equals(requestingUser)) {
            throw new RuntimeException("Only the group admin can add users");
        }

        UserAccount userToAdd = userAccountRepository.findByUsername(usernameToAdd)
                .orElseThrow(() -> new RuntimeException("User to add not found"));

        if (!group.getMembers().contains(userToAdd)) {
            group.getMembers().add(userToAdd);
            chatGroupRepository.save(group);
        }
    }

    public void removeUserFromGroup(Long groupId, String usernameToRemove, String requestingUser) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!group.getAdmin().getUsername().equals(requestingUser)) {
            throw new RuntimeException("Only the group admin can remove users");
        }

        Optional<UserAccount> userToRemoveOpt = group.getMembers().stream()
                .filter(u -> u.getUsername().equals(usernameToRemove))
                .findFirst();

        if (userToRemoveOpt.isPresent()) {
            group.getMembers().remove(userToRemoveOpt.get());
            chatGroupRepository.save(group);
        }
    }

    public ChatGroup getGroupById(Long id) {
        return chatGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    public List<ChatGroupDto> getGroupsForUser(String username) {
        return chatGroupRepository.findAll().stream()
                .filter(g -> g.getMembers().stream().anyMatch(u -> u.getUsername().equals(username)))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    public ChatGroupDto mapToDto(ChatGroup group) {
        ChatGroupDto dto = new ChatGroupDto();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setAdmin(mapUserToDto(group.getAdmin()));
        dto.setMembers(group.getMembers().stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList()));
        return dto;
    }

    public UserAccountWithoutAddressDto mapUserToDto(UserAccount user) {
        UserAccountWithoutAddressDto dto = new UserAccountWithoutAddressDto();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setFollowersCount(user.getFollowersCount());
        dto.setPostCount(user.getPostCount());
        return dto;
    }
    public List<UserAccountWithoutAddressDto> searchUsers(String query) {
        return userAccountRepository.findByUsernameContainingIgnoreCase(query).stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList());
    }
}
