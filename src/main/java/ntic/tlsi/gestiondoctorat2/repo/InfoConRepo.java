package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InfoConRepo extends JpaRepository<InfoConcour,Long> {
    // Choose the most recently created competition deterministically.
    Optional<InfoConcour> findTopByOrderByIdDesc();
}
