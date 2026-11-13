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
        for (EnrollmentExamPaper paper : eligible) {
            if (paper.getSubject() != competition.getMatier1()
                    && paper.getSubject() != competition.getMatier2()) {
                throw conflict("Paper subject is not part of this competition");
            }
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
        for (EnrollmentExamPaper paper : eligible) {
            List<EnrollmentExamCorrection> existing = corrections.findByPaper_Id(paper.getId());
            Set<Long> assigned = new HashSet<>();
            for (EnrollmentExamCorrection correction : existing) {
                Enseignant teacher = correction.getTeacher();
                if (teacher == null || teacher.getSpecialite() != paper.getSubject()
                        || !assigned.add(teacher.getId()) || assigned.size() > 2) {
                    throw conflict("Invalid existing teacher assignment");
                }
            }
            for (Enseignant teacher : qualified.get(paper.getSubject())) {
                if (assigned.size() >= 2) break;
                if (assigned.add(teacher.getId())) {
                    corrections.save(new EnrollmentExamCorrection(paper, teacher));
                    created++;
                }
            }
        }
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
        InfoConcour latest = competitions.findTopByOrderByIdDesc()
                .orElseThrow(() -> conflict("No competition available"));
        if (!competition.getId().equals(latest.getId()) || competition.getResultsPublishedAt() != null) {
            throw conflict("Only the latest unpublished competition may be prepared");
        }
        return competition;
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    public record PaperSummary(Long paperId, String code, Matier subject, int assignedTeachers) {}
}
