package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.CompetitionEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionEnrollmentRepo extends JpaRepository<CompetitionEnrollment, Long> {
    boolean existsByCompetitionIdAndCandidateUsername(Long competitionId, String username);
    long countByCompetitionIdAndCandidateId(Long competitionId, Long candidateId);
}
