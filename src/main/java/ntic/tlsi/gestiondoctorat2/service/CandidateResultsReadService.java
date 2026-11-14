package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import ntic.tlsi.gestiondoctorat2.entities.Matier;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
import ntic.tlsi.gestiondoctorat2.repo.CompetitionEnrollmentRepo;
import ntic.tlsi.gestiondoctorat2.repo.EnrollmentResultSnapshotRepo;
import ntic.tlsi.gestiondoctorat2.entities.CompetitionEnrollment;
import ntic.tlsi.gestiondoctorat2.entities.EnrollmentResultSnapshot;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Read-only candidate view. A released score is mapped by the competition's
 * configured subject identities, never by JPA collection/correction ordering.
 * Unpublished scores, decision and anonymization code are never exposed.
 */
@Service
public class CandidateResultsReadService {
    private final CandidatRepo candidates;
    private final InfoConRepo competitions;
    private final CompetitionEnrollmentRepo enrollments;
    private final EnrollmentResultSnapshotRepo snapshots;

    public CandidateResultsReadService(CandidatRepo candidates, InfoConRepo competitions,
                                       CompetitionEnrollmentRepo enrollments,
                                       EnrollmentResultSnapshotRepo snapshots) {
        this.candidates = candidates;
        this.competitions = competitions;
        this.enrollments = enrollments;
        this.snapshots = snapshots;
    }

    @Transactional(readOnly = true)
    public CandidateResultsView readFor(String username) {
        Candidat candidate = candidates.findCandidatByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Candidate account not found"));
        InfoConcour competition = competitions.findTopByOrderByIdDesc().orElse(null);
        if (competition == null) return CandidateResultsView.noCompetition();
        // Enrollment-based competitions must never fall back to Candidate-wide
        // grades: those can belong to an entirely different competition.
        if (enrollments.existsByCompetition_Id(competition.getId())) {
            if (competition.getResultsPublishedAt() == null) {
                return CandidateResultsView.pending();
            }
            CompetitionEnrollment enrollment = enrollments
                    .findByCompetition_IdAndCandidate_Username(competition.getId(), username)
                    .orElse(null);
            if (enrollment == null) return CandidateResultsView.notAvailable();
            EnrollmentResultSnapshot published = snapshots.findByEnrollment_Id(enrollment.getId())
                    .orElse(null);
            if (published == null) return CandidateResultsView.notAvailable();
            return CandidateResultsView.published(
                    published.getAnonymousCode(),
                    List.of(new SubjectScore(published.getFirstSubject(), published.getFirstAverage()),
                            new SubjectScore(published.getSecondSubject(), published.getSecondAverage())),
                    published.getOverallAverage(), published.isAdmitted());
        }
        // Compatibility for competitions created before enrollment migration.

        if (competition.getResultsPublishedAt() == null) return CandidateResultsView.pending();
        if (candidate.getCode() == null || candidate.getCode().isBlank()) {
            return CandidateResultsView.notAvailable();
        }
        // Publication guarantees two valid configured subjects and complete marks.
        // moyMatier1/moyMatier2 are keyed to the configured subject fields by
        // ResultPublicationService, not to the order of candidate.getCopies().
        return CandidateResultsView.published(
                candidate.getCode(),
                List.of(new SubjectScore(competition.getMatier1(), candidate.getMoyMatier1()),
                        new SubjectScore(competition.getMatier2(), candidate.getMoyMatier2())),
                candidate.getMoyenneGeneral(),
                candidate.isGetPoste());
    }

    public record SubjectScore(Matier subject, double average) {}

    public record CandidateResultsView(Status status, String code,
                                       List<SubjectScore> subjects,
                                       Double overallAverage, Boolean admitted) {
        public enum Status { NO_COMPETITION, PENDING, NOT_AVAILABLE, PUBLISHED }

        public static CandidateResultsView noCompetition() {
            return new CandidateResultsView(Status.NO_COMPETITION, null, List.of(), null, null);
        }
        public static CandidateResultsView pending() {
            return new CandidateResultsView(Status.PENDING, null, List.of(), null, null);
        }
        public static CandidateResultsView notAvailable() {
            return new CandidateResultsView(Status.NOT_AVAILABLE, null, List.of(), null, null);
        }
        public static CandidateResultsView published(String code, List<SubjectScore> subjects,
                                                      double overallAverage, boolean admitted) {
            return new CandidateResultsView(Status.PUBLISHED, code, List.copyOf(subjects),
                    overallAverage, admitted);
        }
    }
}
