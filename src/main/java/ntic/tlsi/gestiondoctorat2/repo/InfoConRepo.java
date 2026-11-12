package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InfoConRepo extends JpaRepository<InfoConcour,Long> {
    // Choose the most recently created competition deterministically.
    Optional<InfoConcour> findTopByOrderByIdDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select competition from InfoConcour competition where competition.id = :id")
    Optional<InfoConcour> findLockedById(@Param("id") Long id);
}
