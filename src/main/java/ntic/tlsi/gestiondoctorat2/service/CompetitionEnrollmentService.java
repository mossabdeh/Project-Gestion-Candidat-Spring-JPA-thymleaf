package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.CompetitionEnrollment;
import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.CompetitionEnrollmentRepo;
import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
import ntic.tlsi.gestiondoctorat2.repo.EnrollmentExamPaperRepo;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Handles registration only. No exam paper or grade is created by enrollment. */
@Service
public class CompetitionEnrollmentService {
    private final CompetitionEnrollmentRepo enrollments;
    private final InfoConRepo competitions;
    private final CandidatRepo candidates;
    private final EnrollmentExamPaperRepo papers;

    public CompetitionEnrollmentService(CompetitionEnrollmentRepo enrollments,
                                        InfoConRepo competitions,
                                        CandidatRepo candidates,
                                        EnrollmentExamPaperRepo papers) {
        this.enrollments = enrollments;
        this.competitions = competitions;
        this.candidates = candidates;
        this.papers = papers;
    }

    private static final ZoneId COMPETITION_ZONE = ZoneId.of("Europe/Paris");

    @Transactional(readOnly = true)
    public boolean registrationOpen(InfoConcour competition) {
        if (competition.getResultsPublishedAt() != null || competition.getRegistrationOpensAt() == null
                || competition.getRegistrationClosesAt() == null) return false;
        LocalDateTime now = LocalDateTime.now(COMPETITION_ZONE);
        return !now.isBefore(competition.getRegistrationOpensAt())
                && now.isBefore(competition.getRegistrationClosesAt());
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
        if (!registrationOpen(competition)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Registration is not open for this competition");
        }
        Candidat candidate = candidates.findCandidatByUsername(candidateUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Candidate account not found"));
        if (enrollments.existsByCompetition_IdAndCandidate_Username(competitionId, candidateUsername)) {
            return; // Repeat POST from the same Candidate is safe.
        }
        // Code allocation and paper preparation lock the same competition row.
        // Once either begins, the cohort is frozen even if the configured window is still open.
        if (enrollments.existsByCompetition_IdAndAnonymousCodeIsNotNull(competitionId)
                || papers.existsByEnrollment_Competition_Id(competitionId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Registration is frozen because competition preparation has started");
        }
        enrollments.saveAndFlush(new CompetitionEnrollment(competition, candidate));
    }
}
