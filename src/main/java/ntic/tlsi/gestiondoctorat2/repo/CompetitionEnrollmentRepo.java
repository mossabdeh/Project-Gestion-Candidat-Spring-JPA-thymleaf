package ntic.tlsi.gestiondoctorat2.repo;

import ntic.tlsi.gestiondoctorat2.entities.CompetitionEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetitionEnrollmentRepo extends JpaRepository<CompetitionEnrollment, Long> {
    boolean existsByCompetition_IdAndCandidate_Username(Long competitionId, String username);
    long countByCompetition_IdAndCandidate_Id(Long competitionId, Long candidateId);
}
