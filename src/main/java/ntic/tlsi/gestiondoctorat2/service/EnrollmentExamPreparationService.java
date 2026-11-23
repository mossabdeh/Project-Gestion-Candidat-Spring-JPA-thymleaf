package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * New enrollment-scoped paper and marker assignment; does not touch
 * legacy Copie/Correction or Candidate-wide grades.
 */
@Service
public class EnrollmentExamPreparationService {
    private final InfoConRepo competitions;
    private final CompetitionEnrollmentRepo enrollments;
    private final EnrollmentExamPaperRepo papers;
    private final EnrollmentExamCorrectionRepo corrections;
    private final EnseignantRepo teachers;

    public EnrollmentExamPreparationService(InfoConRepo competitions,
                                            CompetitionEnrollmentRepo enrollments,
                                            EnrollmentExamPaperRepo papers,
                                            EnrollmentExamCorrectionRepo corrections,
                                            EnseignantRepo teachers) {
        this.competitions = competitions;
        this.enrollments = enrollments;
        this.papers = papers;
        this.corrections = corrections;
        this.teachers = teachers;
    }

    @Transactional
    public int initializePapers(Long competitionId) {
        InfoConcour competition = openCompetition(competitionId);
        Matier first = competition.getMatier1();
        Matier second = competition.getMatier2();
        if (first == null || second == null || first == second) {
            throw conflict("Competition needs two distinct subjects");
        }
        List<CompetitionEnrollment> registered = enrollments.findByCompetition_IdOrderByIdAsc(competitionId);
        if (registered.isEmpty()) throw conflict("No candidate registrations");
        if (registered.stream().anyMatch(e -> e.getAnonymousCode() == null)) {
            throw conflict("Assign all enrollment codes before creating papers");
        }

        // Once grading begins, preparation is immutable; even a retry must not
        // resurrect missing papers or add new marker assignments.
        if (competition.effectivePhase() == CompetitionPhase.GRADING) {
            throw conflict("Exam preparation is locked after grading begins");
        }

        int created = 0;
        for (CompetitionEnrollment enrollment : registered) {
            List<EnrollmentExamPaper> existing = papers.findByEnrollment_Id(enrollment.getId());
            Set<Matier> present = new HashSet<>();
            for (EnrollmentExamPaper paper : existing) {
                if ((paper.getSubject() != first && paper.getSubject() != second)
                        || !present.add(paper.getSubject())) {
                    throw conflict("Unexpected or duplicate subject on enrollment");
                }
            }
            if (!present.contains(first)) {
                papers.save(new EnrollmentExamPaper(enrollment, first));
                created++;
            }
            if (!present.contains(second)) {
                papers.save(new EnrollmentExamPaper(enrollment, second));
                created++;
            }
        }
        if (created > 0) competition.advanceTo(CompetitionPhase.PREPARATION);
        papers.flush();
        return created;
    }

    @Transactional
    public int initializeCorrections(Long competitionId) {
        InfoConcour competition = openCompetition(competitionId);
        List<CompetitionEnrollment> registered = enrollments.findByCompetition_IdOrderByIdAsc(competitionId);
        if (registered.isEmpty() || registered.stream().anyMatch(e -> e.getAnonymousCode() == null)) {
            throw conflict("Registration codes are not ready");
        }
        List<EnrollmentExamPaper> eligible = papers.findByEnrollment_Competition_IdOrderByIdAsc(competitionId);
        if (eligible.size() != registered.size() * 2L) {
            throw conflict("Initialize both subjects for every enrollment first");
        }
        Map<Long, Set<Matier>> subjectsByEnrollment = new HashMap<>();
        for (EnrollmentExamPaper paper : eligible) {
            if (paper.getSubject() != competition.getMatier1()
                    && paper.getSubject() != competition.getMatier2()
                    || !subjectsByEnrollment.computeIfAbsent(paper.getEnrollment().getId(),
                            ignored -> new HashSet<>()).add(paper.getSubject())) {
                throw conflict("Duplicate or unexpected subject on enrollment");
            }
        }
        if (registered.stream().anyMatch(e ->
                subjectsByEnrollment.getOrDefault(e.getId(), Set.of()).size() != 2)) {
            throw conflict("Each enrollment must have both subject papers");
        }
        if (competition.effectivePhase() == CompetitionPhase.GRADING) {
            throw conflict("Exam assignments are locked after grading begins");
        }

        Map<Matier, List<Enseignant>> qualified = new EnumMap<>(Matier.class);
        for (Matier subject : List.of(competition.getMatier1(), competition.getMatier2())) {
            List<Enseignant> eligibleTeachers = new ArrayList<>(teachers.findAllBySpecialite(subject));
            eligibleTeachers.sort(Comparator.comparing(Enseignant::getId));
            if (eligibleTeachers.size() < 2) {
                throw conflict("Two qualified teachers are required for " + subject);
            }
            qualified.put(subject, eligibleTeachers);
        }

        int created = 0;
        // Validate every paper before inserting any assignments. A previously
        // assigned third marker must never be mistaken for an invalid duplicate.
        Map<Long, Set<Long>> assignedByPaper = new HashMap<>();
        for (EnrollmentExamPaper paper : eligible) {
            Set<Long> assigned = new HashSet<>();
            for (EnrollmentExamCorrection correction : corrections.findByPaper_Id(paper.getId())) {
                Enseignant teacher = correction.getTeacher();
                if (!ExaminerEligibility.canMark(teacher, paper)
                        || !assigned.add(teacher.getId()) || assigned.size() > 3) {
                    throw conflict("Invalid existing teacher assignment");
                }
            }
            long eligibleCount = qualified.get(paper.getSubject()).stream()
                    .filter(teacher -> ExaminerEligibility.canMark(teacher, paper))
                    .count();
            if (eligibleCount < 2 || assigned.size() < 2
                    && assigned.size() + qualified.get(paper.getSubject()).stream()
                    .filter(teacher -> ExaminerEligibility.canMark(teacher, paper)
                            && !assigned.contains(teacher.getId())).count() < 2) {
                throw conflict("Two qualified conflict-free examiners are required for each paper");
            }
            assignedByPaper.put(paper.getId(), assigned);
        }

        for (EnrollmentExamPaper paper : eligible) {
            Set<Long> assigned = assignedByPaper.get(paper.getId());
            for (Enseignant teacher : qualified.get(paper.getSubject())) {
                if (assigned.size() >= 2) break;
                if (ExaminerEligibility.canMark(teacher, paper) && assigned.add(teacher.getId())) {
                    corrections.save(new EnrollmentExamCorrection(paper, teacher));
                    created++;
                }
            }
        }
        if (created > 0) competition.advanceTo(CompetitionPhase.PREPARATION);
        corrections.flush();
        return created;
    }

    @Transactional(readOnly = true)
    public List<PaperSummary> listPapers(Long competitionId) {
        List<EnrollmentExamPaper> found = papers.findByEnrollment_Competition_IdOrderByIdAsc(competitionId);
        return found.stream().map(paper -> new PaperSummary(
                paper.getId(),
                paper.getEnrollment().getAnonymousCode(),
                paper.getSubject(),
                corrections.findByPaper_Id(paper.getId()).size()
        )).toList();
    }

    private InfoConcour openCompetition(Long competitionId) {
        InfoConcour competition = competitions.findLockedById(competitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competition not found"));
        if (competition.effectivePhase() == CompetitionPhase.PUBLISHED) {
            throw conflict("Published competition cannot be prepared");
        }
        return competition;
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    public record PaperSummary(Long paperId, String code, Matier subject, int assignedTeachers) {}
}
