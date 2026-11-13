package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.CompetitionEnrollment;
import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import ntic.tlsi.gestiondoctorat2.repo.CompetitionEnrollmentRepo;
import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
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
    private final SecureRandom random = new SecureRandom();

    public EnrollmentAnonymizationService(CompetitionEnrollmentRepo enrollments, InfoConRepo competitions) {
        this.enrollments = enrollments;
        this.competitions = competitions;
    }

    @Transactional
    public int assignCodes(Long competitionId) {
        // Lock the competition so concurrent allocations cannot issue duplicates.
        InfoConcour competition = competitions.findLockedById(competitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competition not found"));
        InfoConcour latest = competitions.findTopByOrderByIdDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No competition available"));
        if (!competition.getId().equals(latest.getId()) || competition.getResultsPublishedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Competition is closed for code allocation");
        }

        List<CompetitionEnrollment> registered = enrollments.findByCompetition_IdOrderByIdAsc(competitionId);
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
