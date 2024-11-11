package rs.ac.uns.ftn.informatika.rest.service;

import rs.ac.uns.ftn.informatika.rest.domain.User;
import rs.ac.uns.ftn.informatika.rest.dto.UserRegistrationDTO;

import java.util.Collection;

public interface UserService {
    Collection<User> findAll();
    User findById(Long id);
    User create(UserRegistrationDTO registrationDTO) throws Exception;
    User delete(Long id);
    User update(UserRegistrationDTO userRegistrationDTO, Long id) throws Exception;
    String verify(String username, String password);
}
