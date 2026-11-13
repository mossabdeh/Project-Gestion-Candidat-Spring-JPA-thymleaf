package ntic.tlsi.gestiondoctorat2.service;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Independent grading path for enrollment-scoped papers.
 * Legacy candidate-wide grades and official results remain unchanged.
 */
@Service
public class EnrollmentGradingService {
    private final InfoConRepo competitions;
    private final EnrollmentExamPaperRepo papers;
    private final EnrollmentExamCorrectionRepo corrections;
    private final EnseignantRepo teachers;

    public EnrollmentGradingService(InfoConRepo competitions,
                                    EnrollmentExamPaperRepo papers,
                                    EnrollmentExamCorrectionRepo corrections,
                                    EnseignantRepo teachers) {
        this.competitions = competitions;
        this.papers = papers;
        this.corrections = corrections;
        this.teachers = teachers;
    }

    @Transactional(readOnly = true)
    public List<AssignedGrade> assignments(String username) {
        return corrections.findByTeacher_UsernameOrderByIdAsc(username).stream()
                .map(c -> new AssignedGrade(c.getId(), c.getPaper().getId(),
                        c.getPaper().getEnrollment().getCompetition().getId(),
                        c.getPaper().getEnrollment().getAnonymousCode(),
                        c.getPaper().getSubject(), c.getGrade())).toList();
    }

    @Transactional
    public void recordGrade(Long correctionId, String username, double grade) {
        if (!Double.isFinite(grade) || grade < 0 || grade > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grade must be between 0 and 20");
        }
        // Lock the competition first, consistently with paper setup and code allocation.
        EnrollmentExamCorrection correction = corrections.findById(correctionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        Long competitionId = correction.getPaper().getEnrollment().getCompetition().getId();
        requireEditable(competitionId);
        // Check ownership AFTER lock and before any write, never trust a posted teacher ID.
        if (!correction.getTeacher().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This paper is not assigned to you");
        }
        if (correction.getTeacher().getSpecialite() != correction.getPaper().getSubject()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Teacher is not qualified for this subject");
        }
        correction.recordGrade(grade);
        corrections.flush();
    }

    @Transactional
    public int assignThirdMarkers(Long competitionId) {
        InfoConcour competition = requireEditable(competitionId);
        List<EnrollmentExamPaper> target = papers.findByEnrollment_Competition_IdOrderByIdAsc(competitionId);
        if (target.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Initialize enrollment papers first");
        }
        List<ThirdAssignment> needed = new ArrayList<>();
        for (EnrollmentExamPaper paper : target) {
            if (paper.getSubject() != competition.getMatier1()
                    && paper.getSubject() != competition.getMatier2()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Unexpected subject");
            }
            List<EnrollmentExamCorrection> marks = corrections.findByPaper_Id(paper.getId());
            if (marks.size() < 2 || marks.size() > 3) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Paper needs two or three assigned markers");
            }
            Set<Long> assigned = new HashSet<>();
            for (EnrollmentExamCorrection mark : marks) {
                if (mark.getTeacher().getSpecialite() != paper.getSubject()
                        || !assigned.add(mark.getTeacher().getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid marker assignment");
                }
            }
            // Do not assign third markers before the two original grades exist.
            if (marks.get(0).getGrade() == null || marks.get(1).getGrade() == null) continue;
            double gap = Math.abs(marks.get(0).getGrade() - marks.get(1).getGrade());
            if (gap < 3 || marks.size() == 3) continue;

            List<Enseignant> qualified = new ArrayList<>(teachers.findAllBySpecialite(paper.getSubject()));
            qualified.sort(Comparator.comparing(Enseignant::getId));
            Enseignant third = qualified.stream()
                    .filter(teacher -> !assigned.contains(teacher.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                            "No qualified third teacher available"));
            needed.add(new ThirdAssignment(paper, third));
        }
        // All requirements validated before inserts; retry won't create duplicates.
        for (ThirdAssignment allocation : needed) {
            corrections.save(new EnrollmentExamCorrection(allocation.paper(), allocation.teacher()));
        }
        corrections.flush();
        return needed.size();
    }

    private InfoConcour requireEditable(Long competitionId) {
        InfoConcour competition = competitions.findLockedById(competitionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Competition not found"));
        InfoConcour current = competitions.findTopByOrderByIdDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "No competition configured"));
        if (!competition.getId().equals(current.getId()) || competition.getResultsPublishedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Competition is not open for grading");
        }
        return competition;
    }

    private record ThirdAssignment(EnrollmentExamPaper paper, Enseignant teacher) {}
    public record AssignedGrade(Long correctionId, Long paperId, Long competitionId,
                                String anonymousCode, Matier subject, Double grade) {}
}
