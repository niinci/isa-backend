package rs.ac.uns.ftn.informatika.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.informatika.rest.config.Utility;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import rs.ac.uns.ftn.informatika.rest.domain.Address;
import rs.ac.uns.ftn.informatika.rest.domain.AuthRequest;
import rs.ac.uns.ftn.informatika.rest.domain.UserAccount;
import rs.ac.uns.ftn.informatika.rest.domain.UserInfo;
import rs.ac.uns.ftn.informatika.rest.dto.PasswordChangeDTO;
import rs.ac.uns.ftn.informatika.rest.dto.UserAccountDTO;
import rs.ac.uns.ftn.informatika.rest.dto.UserProfileEditDTO;
import rs.ac.uns.ftn.informatika.rest.service.UserAccountService;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/userAccount") // Tačna ruta
public class UserAccountController {
    @Autowired
    private UserAccountService userAccountService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(description = "Get all users with pagination", method = "GET")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UserAccount>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<UserAccount> userAccounts = userAccountService.findAll(PageRequest.of(page, size));
        return new ResponseEntity<>(userAccounts, HttpStatus.OK);
    }

    @Operation(description = "Create new user", method = "POST")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserAccount.class))}),
            @ApiResponse(responseCode = "409", description = "Not possible to create new greeting when given id is not null or empty",
                    content = @Content)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, path = "/register")
    public ResponseEntity<UserAccount> createUser(@Valid @RequestBody UserAccountDTO userAccountDto, HttpServletRequest request) throws ConstraintViolationException {
        UserAccount newAccount = null;
        System.out.println("Address" + userAccountDto.getAddress());
        try{
            newAccount = userAccountService.create(userAccountDto, request);


            return  new ResponseEntity<UserAccount>(newAccount, HttpStatus.CREATED);
        } catch (Exception e){
            return  new ResponseEntity<UserAccount>(HttpStatus.CONFLICT);
        }
    }

    @PostMapping(path = "/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest credentials) {
        System.out.println(credentials);

        String token = userAccountService.verify(credentials);
        System.out.println(token);

        if(token.equals("Failure")){
            return new ResponseEntity<String>("Email not verified",HttpStatus.UNAUTHORIZED);
        }else{
            return new ResponseEntity<String>(token, HttpStatus.OK);
        }
    }
    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/getUserInfo")
    public ResponseEntity<UserInfo> getUserInfo(@RequestParam String email) {
        List<UserAccount> acc = userAccountService.searchByEmail(email);
        if(acc.isEmpty()){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }else{
            UserInfo userInfo = new UserInfo();
            Address usersAddress = acc.get(0).convertJsonToAddress();

            userInfo.address = usersAddress;
            userInfo.email = email;
            userInfo.firstName = acc.get(0).getFirstName();
            userInfo.lastName = acc.get(0).getLastName();
            return new ResponseEntity<>(userInfo, HttpStatus.OK);
        }
    }



    @Operation(description = "Delete user", method = "DELETE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "204", description = "User Account successfully deleted", content = @Content)
    })
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<UserAccount> deleteUser(@Parameter(description = "user id", required = true) @PathVariable("id") Long id) {
        UserAccount userAccount = userAccountService.delete(id);
        if (userAccount == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<UserAccount>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/verify")
    public String verifyUser(@Param("code") String code) {
        if (userAccountService.verifyVerificationCode(code)) {
            return "verify_success";
        } else {
            return "verify_fail";
        }
    }

    // New search and sort endpoints

    @Operation(description = "Search users by first name", method = "GET")
    @GetMapping(value = "/search/firstName", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserAccount>> searchByFirstName(@RequestParam("firstName") String firstName) {
        List<UserAccount> users = userAccountService.searchByFirstName(firstName);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    @GetMapping("/{userId}/username")
    public ResponseEntity<String> getUsernameById(@PathVariable("userId") Long userId) {
        String username = userAccountService.getUsernameById(userId);
        if (username == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(username, HttpStatus.OK);
    }
    @Operation(description = "Search users by last name", method = "GET")
    @GetMapping(value = "/search/lastName", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserAccount>> searchByLastName(@RequestParam("lastName") String lastName) {
        List<UserAccount> users = userAccountService.searchByLastName(lastName);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(description = "Search users by email", method = "GET")
    @GetMapping(value = "/search/email", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserAccount>> searchByEmail(@RequestParam("email") String email) {
        List<UserAccount> users = userAccountService.searchByEmail(email);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(description = "Search users by post count range", method = "GET")
    @GetMapping(value = "/search/postCount", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserAccount>> searchByPostCount(@RequestParam("min") int min, @RequestParam("max") int max) {
        List<UserAccount> users = userAccountService.searchByPostCount(min, max);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(description = "Sort users by following count", method = "GET")
    @GetMapping(value = "/sort/followingCount", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserAccount>> sortByFollowingCount() {
        List<UserAccount> users = userAccountService.sortByFollowingCount();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(description = "Sort users by email", method = "GET")
    @GetMapping(value = "/sort/email", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserAccount>> sortByEmail() {
        List<UserAccount> users = userAccountService.sortByEmail();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(description = "Change user password", method = "POST")
    @PostMapping(value = "/changePassword", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> changePassword(@RequestParam("email") String email,
                                                 @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        boolean success = userAccountService.changePassword(email, passwordChangeDTO);

        if (success) {
            return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to change password", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(value = "/{userId}/profile", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserAccount> updateProfile(@PathVariable("userId") Long userId,
                                                     @Valid @RequestBody UserProfileEditDTO profileData) {
        UserAccount updatedUser = userAccountService.updateProfile(userId, profileData);

        if (updatedUser != null) {
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
