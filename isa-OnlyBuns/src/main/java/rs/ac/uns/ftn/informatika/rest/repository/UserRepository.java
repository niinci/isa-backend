package rs.ac.uns.ftn.informatika.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.rest.domain.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Pronalaženje korisnika po id-u
    //User findById(Long id);

    // Provera da li korisnik sa datim emailom već postoji
    boolean existsByEmail(String email);

    // Provera da li korisničko ime već postoji
    boolean existsByUsername(String username);
}
