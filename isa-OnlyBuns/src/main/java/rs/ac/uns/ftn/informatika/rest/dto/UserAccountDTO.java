package rs.ac.uns.ftn.informatika.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import rs.ac.uns.ftn.informatika.rest.domain.Address;

public class UserAccountDTO {

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    private String email;

    @NotEmpty
    private String password;


    private Address address;

    public  Address getAddress() {
        return address;
    }

    public void setAddress( Address address) {
        this.address = address;
    }

    @Min(value = 0)
    private int followersCount;

    @Min(value = 0)
    private int postCount;



    public UserAccountDTO() {
    }

    // Getteri i setteri za sva polja

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }


}
