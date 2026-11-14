package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.CompetitionEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompetitionEnrollmentRepo extends JpaRepository<CompetitionEnrollment, Long> {
    boolean existsByCompetition_IdAndCandidate_Username(Long competitionId, String username);
    long countByCompetition_IdAndCandidate_Id(Long competitionId, Long candidateId);
    List<CompetitionEnrollment> findByCompetition_IdOrderByIdAsc(Long competitionId);
    Optional<CompetitionEnrollment> findByCompetition_IdAndCandidate_Username(Long competitionId, String username);
    boolean existsByCompetition_Id(Long competitionId);
}
