package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import ntic.tlsi.gestiondoctorat2.entities.Matier;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
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

    public CandidateResultsReadService(CandidatRepo candidates, InfoConRepo competitions) {
        this.candidates = candidates;
        this.competitions = competitions;
    }

    @Transactional(readOnly = true)
    public CandidateResultsView readFor(String username) {
        Candidat candidate = candidates.findCandidatByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Candidate account not found"));
        InfoConcour competition = competitions.findTopByOrderByIdDesc().orElse(null);
        if (competition == null) return CandidateResultsView.noCompetition();
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
