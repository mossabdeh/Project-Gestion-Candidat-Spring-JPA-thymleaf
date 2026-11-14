package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.EnrollmentResultSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EnrollmentResultSnapshotRepo extends JpaRepository<EnrollmentResultSnapshot, Long> {
    Optional<EnrollmentResultSnapshot> findByEnrollment_Id(Long enrollmentId);
    boolean existsByEnrollment_Competition_Id(Long competitionId);
}
