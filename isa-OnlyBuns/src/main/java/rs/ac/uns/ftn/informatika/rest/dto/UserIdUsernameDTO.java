package rs.ac.uns.ftn.informatika.rest.dto;

public class UserIdUsernameDTO {
    private Long userId;
    private String username;

    public UserIdUsernameDTO() {}

    public UserIdUsernameDTO(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    // getteri i setteri

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}