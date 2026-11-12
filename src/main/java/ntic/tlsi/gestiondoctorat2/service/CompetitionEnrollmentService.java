package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.CompetitionEnrollment;
import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.CompetitionEnrollmentRepo;
import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Handles registration only. No exam paper or grade is created by enrollment. */
@Service
public class CompetitionEnrollmentService {
    private final CompetitionEnrollmentRepo enrollments;
    private final InfoConRepo competitions;
    private final CandidatRepo candidates;

    public CompetitionEnrollmentService(CompetitionEnrollmentRepo enrollments,
                                        InfoConRepo competitions,
                                        CandidatRepo candidates) {
        this.enrollments = enrollments;
        this.competitions = competitions;
        this.candidates = candidates;
    }

    @Transactional(readOnly = true)
    public boolean isRegistered(Long competitionId, String candidateUsername) {
        return enrollments.existsByCompetition_IdAndCandidate_Username(competitionId, candidateUsername);
    }

    @Transactional
    public void register(Long competitionId, String candidateUsername) {
        // Serialize registration requests for the same competition; the database
        // unique constraint also protects against concurrent duplicate inserts.
        InfoConcour competition = competitions.findLockedById(competitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Competition not found"));
        InfoConcour latest = competitions.findTopByOrderByIdDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "No competition available"));
        if (!competition.getId().equals(latest.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Registration is only available for the latest competition");
        }
        if (competition.getResultsPublishedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Registration is closed after results are published");
        }
        Candidat candidate = candidates.findCandidatByUsername(candidateUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Candidate account not found"));
        if (enrollments.existsByCompetition_IdAndCandidate_Username(competitionId, candidateUsername)) {
            return; // Repeat POST from the same Candidate is safe.
        }
        enrollments.saveAndFlush(new CompetitionEnrollment(competition, candidate));
    }
}
