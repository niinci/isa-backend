package rs.ac.uns.ftn.informatika.rest.repository;

import rs.ac.uns.ftn.informatika.rest.domain.CareLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CareLocationRepository extends JpaRepository<CareLocation, UUID> {}
