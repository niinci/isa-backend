package rs.ac.uns.ftn.informatika.rest.service;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.informatika.rest.domain.ChatGroup;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.repository.ChatGroupRepository;
import rs.ac.uns.ftn.informatika.rest.repository.UserAccountRepository;

import java.util.Optional;

@Service
@Transactional
public class ChatGroupService {

    private final ChatGroupRepository chatGroupRepository;
    private final UserAccountRepository userAccountRepository;

    public ChatGroupService(ChatGroupRepository chatGroupRepository, UserAccountRepository userAccountRepository) {
        this.chatGroupRepository = chatGroupRepository;
        this.userAccountRepository = userAccountRepository;
    }

    // Kreiranje grupnog četa gde je admin korisnik koji ga kreira
    public ChatGroup createGroup(String groupName, String adminUsername) {
        UserAccount admin = userAccountRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        ChatGroup group = new ChatGroup();
        group.setName(groupName);
        group.setAdmin(admin);
        group.setMembers(new java.util.ArrayList<>());
        group.getMembers().add(admin);

        return chatGroupRepository.save(group);
    }

    // Dodavanje člana u grupu (samo admin može)
    public ChatGroup addMember(Long groupId, String usernameToAdd, String adminUsername) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Chat group not found"));

        if (!group.getAdmin().getUsername().equals(adminUsername)) {
            throw new SecurityException("Only admin can add members");
        }

        UserAccount userToAdd = userAccountRepository.findByUsername(usernameToAdd)
                .orElseThrow(() -> new IllegalArgumentException("User to add not found"));

        if (!group.getMembers().contains(userToAdd)) {
            group.getMembers().add(userToAdd);
            chatGroupRepository.save(group);
        }

        return group;
    }

    // Uklanjanje člana iz grupe (samo admin može)
    public ChatGroup removeMember(Long groupId, String usernameToRemove, String adminUsername) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Chat group not found"));

        if (!group.getAdmin().getUsername().equals(adminUsername)) {
            throw new SecurityException("Only admin can remove members");
        }

        UserAccount userToRemove = userAccountRepository.findByUsername(usernameToRemove)
                .orElseThrow(() -> new IllegalArgumentException("User to remove not found"));

        if (group.getMembers().contains(userToRemove)) {
            group.getMembers().remove(userToRemove);
            chatGroupRepository.save(group);
        }

        return group;
    }

    // Pronalazak grupe po ID
    public ChatGroup getGroup(Long groupId) {
        return chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Chat group not found"));
    }
}
