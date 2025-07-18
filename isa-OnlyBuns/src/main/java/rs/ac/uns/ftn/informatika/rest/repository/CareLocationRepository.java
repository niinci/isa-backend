package rs.ac.uns.ftn.informatika.rest.repository;

import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.informatika.rest.domain.CareLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import rs.ac.uns.ftn.informatika.rest.domain.CareLocation;

@Repository
public interface CareLocationRepository extends JpaRepository<CareLocation, Long> {
}
