package rs.ac.uns.ftn.informatika.rest.dto;

import java.util.List;

public class ChatGroupDto {
    private Long id;
    private String name;
    private UserAccountWithoutAddressDto admin;
    private List<UserAccountWithoutAddressDto> members;

    public ChatGroupDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserAccountWithoutAddressDto getAdmin() {
        return admin;
    }

    public void setAdmin(UserAccountWithoutAddressDto admin) {
        this.admin = admin;
    }

    public List<UserAccountWithoutAddressDto> getMembers() {
        return members;
    }

    public void setMembers(List<UserAccountWithoutAddressDto> members) {
        this.members = members;
    }
}
