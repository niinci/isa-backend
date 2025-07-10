package rs.ac.uns.ftn.informatika.rest.dto;

import jakarta.validation.constraints.NotEmpty;
import rs.ac.uns.ftn.informatika.rest.domain.Address;

public class UserProfileEditDTO {

    @NotEmpty(message = "First name is required")
    private String firstName;

    @NotEmpty(message = "Last name is required")
    private String lastName;

    private Address address;

    public UserProfileEditDTO() {
    }

    public UserProfileEditDTO(String firstName, String lastName, Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    // Getters and setters
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
