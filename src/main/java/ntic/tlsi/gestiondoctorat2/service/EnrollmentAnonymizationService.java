package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.CompetitionEnrollment;
import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import ntic.tlsi.gestiondoctorat2.entities.CompetitionPhase;
import ntic.tlsi.gestiondoctorat2.repo.CompetitionEnrollmentRepo;
import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
import ntic.tlsi.gestiondoctorat2.repo.EnrollmentExamPaperRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Allocates distinct, stable seven-digit anonymous codes for the selected
 * competition's registrations. Does not change legacy Candidate.code.
 */
@Service
public class EnrollmentAnonymizationService {
    private static final int CODE_SPACE = 10_000_000;
    private final CompetitionEnrollmentRepo enrollments;
    private final InfoConRepo competitions;
    private final EnrollmentExamPaperRepo papers;
    private final SecureRandom random = new SecureRandom();

    public EnrollmentAnonymizationService(CompetitionEnrollmentRepo enrollments, InfoConRepo competitions,
                                          EnrollmentExamPaperRepo papers) {
        this.enrollments = enrollments;
        this.competitions = competitions;
        this.papers = papers;
    }

    @Transactional
    public int assignCodes(Long competitionId) {
        // Lock the competition so concurrent allocations cannot issue duplicates.
        InfoConcour competition = competitions.findLockedById(competitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competition not found"));
        if (competition.effectivePhase() == CompetitionPhase.PUBLISHED
                || competition.effectivePhase() == CompetitionPhase.GRADING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Competition is closed for code allocation");
        }

        List<CompetitionEnrollment> registered = enrollments.findByCompetition_IdOrderByIdAsc(competitionId);
        // Preserve idempotent retries after preparation while prohibiting new code issuance.
        if (papers.existsByEnrollment_Competition_Id(competitionId)
                && registered.stream().anyMatch(e -> e.getAnonymousCode() == null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Codes cannot be assigned after exam preparation begins");
        }
        if (registered.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No registered candidates");
        }

        Set<String> used = new HashSet<>();
        for (CompetitionEnrollment enrollment : registered) {
            String code = enrollment.getAnonymousCode();
            if (code != null && !used.add(code)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate anonymization codes");
            }
        }
        int assigned = 0;
        for (CompetitionEnrollment enrollment : registered) {
            if (enrollment.getAnonymousCode() != null) continue;
            String code = null;
            for (int attempt = 0; attempt < 10000; attempt++) {
                String candidate = String.format(java.util.Locale.ROOT, "%07d", random.nextInt(CODE_SPACE));
                if (used.add(candidate)) {
                    code = candidate;
                    break;
                }
            }
            if (code == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "No unique anonymization code available");
            }
            enrollment.assignAnonymousCode(code);
            assigned++;
        }
        if (assigned > 0) competition.advanceTo(CompetitionPhase.PREPARATION);
        enrollments.flush();
        return assigned;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentCodeRow> listCodes(Long competitionId) {
        return enrollments.findByCompetition_IdOrderByIdAsc(competitionId).stream()
                .map(enrollment -> new EnrollmentCodeRow(
                        enrollment.getCandidate().getId(),
                        enrollment.getCandidate().getUsername(),
                        enrollment.getAnonymousCode()))
                .toList();
    }

    public record EnrollmentCodeRow(Long candidateId, String username, String code) {}
}
