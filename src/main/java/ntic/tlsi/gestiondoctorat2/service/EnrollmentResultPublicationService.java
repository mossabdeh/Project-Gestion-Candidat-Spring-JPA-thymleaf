package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

/** Publishes a complete, immutable snapshot for every registered Candidate. */
@Service
public class EnrollmentResultPublicationService {
    private final InfoConRepo competitions;
    private final CompetitionEnrollmentRepo enrollments;
    private final EnrollmentExamPaperRepo papers;
    private final EnrollmentExamCorrectionRepo corrections;
    private final EnrollmentResultSnapshotRepo snapshots;

    public EnrollmentResultPublicationService(InfoConRepo competitions,
            CompetitionEnrollmentRepo enrollments, EnrollmentExamPaperRepo papers,
            EnrollmentExamCorrectionRepo corrections, EnrollmentResultSnapshotRepo snapshots) {
        this.competitions = competitions;
        this.enrollments = enrollments;
        this.papers = papers;
        this.corrections = corrections;
        this.snapshots = snapshots;
    }

    @Transactional(readOnly = true)
    public List<PreviewRow> preview(Long competitionId) {
        InfoConcour competition = competitions.findById(competitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competition not found"));
        return scoreAll(competition).stream().map(r ->
                new PreviewRow(r.enrollment.getAnonymousCode(), r.first, r.second,
                        r.overall, r.rank, r.admitted)).toList();
    }

    @Transactional
    public int publish(Long competitionId) {
        // All preparation and grading writes lock the same competition row.
        InfoConcour competition = competitions.findLockedById(competitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competition not found"));
        InfoConcour latest = competitions.findTopByOrderByIdDesc()
                .orElseThrow(() -> conflict("No competition configured"));
        if (!competition.getId().equals(latest.getId())) {
            throw conflict("Only the latest competition may be published");
        }
        if (competition.getResultsPublishedAt() != null ||
                snapshots.existsByEnrollment_Competition_Id(competitionId)) {
            throw conflict("Results are already published");
        }
        List<RankedScore> ranked = scoreAll(competition);
        Instant publishedAt = Instant.now();
        for (RankedScore score : ranked) {
            snapshots.save(new EnrollmentResultSnapshot(score.enrollment,
                    competition.getMatier1(), competition.getMatier2(),
                    score.first, score.second, score.overall,
                    score.rank, score.admitted, publishedAt));
        }
        snapshots.flush();
        competition.setResultsPublishedAt(publishedAt);
        return ranked.size();
    }

    private List<RankedScore> scoreAll(InfoConcour competition) {
        if (competition.getMatier1() == null || competition.getMatier2() == null ||
                competition.getMatier1() == competition.getMatier2() ||
                competition.getNbrPostes() == null || competition.getNbrPostes() < 0) {
            throw conflict("Invalid competition subjects or places");
        }
        List<CompetitionEnrollment> registered =
                enrollments.findByCompetition_IdOrderByIdAsc(competition.getId());
        if (registered.isEmpty()) throw conflict("No registered candidates");
        List<EnrollmentExamPaper> allPapers =
                papers.findByEnrollment_Competition_IdOrderByIdAsc(competition.getId());
        Map<Long, List<EnrollmentExamPaper>> byEnrollment = new HashMap<>();
        for (EnrollmentExamPaper paper : allPapers) {
            byEnrollment.computeIfAbsent(paper.getEnrollment().getId(),
                    ignored -> new ArrayList<>()).add(paper);
        }
        List<RankedScore> scores = new ArrayList<>();
        for (CompetitionEnrollment enrollment : registered) {
            if (enrollment.getAnonymousCode() == null) {
                throw conflict("Registration has no anonymization code");
            }
            List<EnrollmentExamPaper> candidatePapers =
                    byEnrollment.getOrDefault(enrollment.getId(), List.of());
            if (candidatePapers.size() != 2) throw conflict("Enrollment needs exactly two papers");
            Map<Matier, EnrollmentExamPaper> bySubject = new EnumMap<>(Matier.class);
            for (EnrollmentExamPaper paper : candidatePapers) {
                if (bySubject.putIfAbsent(paper.getSubject(), paper) != null) {
                    throw conflict("Duplicate subject paper");
                }
            }
            EnrollmentExamPaper first = bySubject.get(competition.getMatier1());
            EnrollmentExamPaper second = bySubject.get(competition.getMatier2());
            if (first == null || second == null) throw conflict("Enrollment has incorrect subjects");
            double a = average(first);
            double b = average(second);
            scores.add(new RankedScore(enrollment, round(a), round(b),
                    round((a + b) / 2), 0, false));
        }
        // The admission policy is deterministic: ties use the lower enrollment ID.
        scores.sort(Comparator.comparingDouble((RankedScore r) -> r.overall).reversed()
                .thenComparing(r -> r.enrollment.getId()));
        List<RankedScore> ranked = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            RankedScore s = scores.get(i);
            ranked.add(new RankedScore(s.enrollment, s.first, s.second,
                    s.overall, i + 1, i < competition.getNbrPostes()));
        }
        return ranked;
    }

    private double average(EnrollmentExamPaper paper) {
        List<EnrollmentExamCorrection> marks =
                corrections.findByPaper_IdOrderByIdAsc(paper.getId());
        if (marks.size() < 2 || marks.size() > 3) {
            throw conflict("Paper needs two or three assigned teachers");
        }
        Set<Long> markers = new HashSet<>();
        double total = 0;
        for (EnrollmentExamCorrection mark : marks) {
            Double grade = mark.getGrade();
            if (mark.getTeacher() == null ||
                    mark.getTeacher().getSpecialite() != paper.getSubject() ||
                    !markers.add(mark.getTeacher().getId()) ||
                    grade == null || !Double.isFinite(grade) || grade < 0 || grade > 20) {
                throw conflict("Incomplete, unauthorized, or invalid grades");
            }
            total += grade;
        }
        boolean requiresThird = Math.abs(marks.get(0).getGrade() -
                marks.get(1).getGrade()) >= 3;
        if (requiresThird && marks.size() != 3) {
            throw conflict("Third correction required");
        }
        if (!requiresThird && marks.size() == 3) {
            throw conflict("Unexpected third correction");
        }
        return total / marks.size();
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private record RankedScore(CompetitionEnrollment enrollment, double first, double second,
                               double overall, int rank, boolean admitted) {}

    public record PreviewRow(String anonymousCode, double firstAverage,
                             double secondAverage, double overallAverage, int rank, boolean admitted) {}
}
