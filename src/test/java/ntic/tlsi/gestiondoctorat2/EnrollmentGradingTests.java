package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.EnrollmentGradingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EnrollmentGradingTests {
    @Autowired EnrollmentGradingService grading;
    @Autowired InfoConRepo competitions;
    @Autowired CandidatRepo candidates;
    @Autowired CompetitionEnrollmentRepo enrollments;
    @Autowired EnrollmentExamPaperRepo papers;
    @Autowired EnrollmentExamCorrectionRepo corrections;
    @Autowired EnseignantRepo teachers;
    @Autowired MockMvc mvc;

    @Test
    void assignedTeacherCanSaveZeroAndTwentyButNotInvalidGrades() {
        InfoConcour competition = competition(false);
        EnrollmentExamPaper paper = paper(competition, Matier.ALGO);
        Enseignant teacher = teacher(Matier.ALGO);
        EnrollmentExamCorrection assignment = corrections.saveAndFlush(new EnrollmentExamCorrection(paper, teacher));
        assertEquals(1, grading.assignments(teacher.getUsername()).size());
        grading.recordGrade(assignment.getId(), teacher.getUsername(), 0);
        assertEquals(0.0, assignment.getGrade());
        grading.recordGrade(assignment.getId(), teacher.getUsername(), 20);
        assertEquals(20.0, assignment.getGrade());
        for (double invalid : new double[]{-1, 20.01, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST,
                    assertThrows(ResponseStatusException.class,
                            () -> grading.recordGrade(assignment.getId(), teacher.getUsername(), invalid))
                            .getStatusCode());
        }
        assertEquals(20, assignment.getGrade());
    }

    @Test
    void teacherCannotChangeAnotherTeachersGradeOrAnOldCompetition() {
        InfoConcour oldCompetition = competition(false);
        EnrollmentExamPaper paper = paper(oldCompetition, Matier.ALGO);
        Enseignant owner = teacher(Matier.ALGO);
        Enseignant stranger = teacher(Matier.ALGO);
        EnrollmentExamCorrection assigned = corrections.saveAndFlush(new EnrollmentExamCorrection(paper, owner));
        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN,
                assertThrows(ResponseStatusException.class,
                        () -> grading.recordGrade(assigned.getId(), stranger.getUsername(), 12))
                        .getStatusCode());
        competition(false);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> grading.recordGrade(assigned.getId(), owner.getUsername(), 12))
                        .getStatusCode());
        assertNull(assigned.getGrade());
    }

    @Test
    void publishedGradesAreReadOnly() {
        InfoConcour closed = competition(true);
        EnrollmentExamPaper paper = paper(closed, Matier.EDL);
        Enseignant teacher = teacher(Matier.EDL);
        EnrollmentExamCorrection assignment = corrections.saveAndFlush(new EnrollmentExamCorrection(paper, teacher));
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> grading.recordGrade(assignment.getId(), teacher.getUsername(), 9))
                        .getStatusCode());
    }

    @Test
    void thirdMarkerIsAssignedAtThreePointBoundaryAndIsRepeatable() {
        InfoConcour comp = competition(false);
        EnrollmentExamPaper paper = paper(comp, Matier.ALGO);
        Enseignant first = teacher(Matier.ALGO);
        Enseignant second = teacher(Matier.ALGO);
        Enseignant third = teacher(Matier.ALGO);
        EnrollmentExamCorrection a = corrections.saveAndFlush(new EnrollmentExamCorrection(paper, first));
        EnrollmentExamCorrection b = corrections.saveAndFlush(new EnrollmentExamCorrection(paper, second));
        grading.recordGrade(a.getId(), first.getUsername(), 12);
        grading.recordGrade(b.getId(), second.getUsername(), 15);
        assertEquals(1, grading.assignThirdMarkers(comp.getId()));
        assertEquals(0, grading.assignThirdMarkers(comp.getId()));
        var assigned = corrections.findByPaper_IdOrderByIdAsc(paper.getId());
        assertEquals(3, assigned.size());
        assertEquals(third.getId(), assigned.get(2).getTeacher().getId());
        assertNull(assigned.get(2).getGrade());
    }

    @Test
    void noThirdMarkerBelowThreePointBoundary() {
        InfoConcour comp = competition(false);
        EnrollmentExamPaper paper = paper(comp, Matier.ALGO);
        Enseignant first = teacher(Matier.ALGO);
        Enseignant second = teacher(Matier.ALGO);
        var a = corrections.saveAndFlush(new EnrollmentExamCorrection(paper, first));
        var b = corrections.saveAndFlush(new EnrollmentExamCorrection(paper, second));
        grading.recordGrade(a.getId(), first.getUsername(), 12);
        grading.recordGrade(b.getId(), second.getUsername(), 14.99);
        assertEquals(0, grading.assignThirdMarkers(comp.getId()));
        assertEquals(2, corrections.findByPaper_IdOrderByIdAsc(paper.getId()).size());
    }

    @Test
    void missingThirdMarkerFailsWithoutPartialAssignments() {
        InfoConcour comp = competition(false);
        EnrollmentExamPaper paper = paper(comp, Matier.ALGO);
        Enseignant first = teacher(Matier.ALGO);
        Enseignant second = teacher(Matier.ALGO);
        var a = corrections.saveAndFlush(new EnrollmentExamCorrection(paper, first));
        var b = corrections.saveAndFlush(new EnrollmentExamCorrection(paper, second));
        grading.recordGrade(a.getId(), first.getUsername(), 10);
        grading.recordGrade(b.getId(), second.getUsername(), 15);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> grading.assignThirdMarkers(comp.getId())).getStatusCode());
        assertEquals(2, corrections.findByPaper_IdOrderByIdAsc(paper.getId()).size());
    }

    @Test
    @WithMockUser(username = "test-teacher", roles = "ENSEIGNANT")
    void gradeEntryRequiresCsrfAndTheTeacherRole() throws Exception {
        mvc.perform(get("/enseignant/enrollmentGrades"))
                .andExpect(status().isOk()).andExpect(view().name("EnseignantEnrollmentGrades"));
        mvc.perform(post("/enseignant/saveEnrollmentGrade")
                .param("correctionId", "1").param("grade", "10")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotReadOrModifyTeacherOrCfdGrades() throws Exception {
        mvc.perform(get("/enseignant/enrollmentGrades")).andExpect(status().isForbidden());
        mvc.perform(post("/enseignant/saveEnrollmentGrade").with(csrf())
                .param("correctionId", "1").param("grade", "10")).andExpect(status().isForbidden());
        mvc.perform(post("/cfd/assignEnrollmentThirdMarkers").with(csrf())
                .param("competitionId", "1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CFD")
    void thirdMarkerActionRequiresCsrfAndPost() throws Exception {
        mvc.perform(get("/cfd/assignEnrollmentThirdMarkers")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/cfd/assignEnrollmentThirdMarkers")
                .param("competitionId", "1")).andExpect(status().isForbidden());
    }

    private InfoConcour competition(boolean published) {
        InfoConcour comp = new InfoConcour(2, "Enrollment grading", Matier.ALGO, Matier.EDL, new Date());
        if (published) comp.setResultsPublishedAt(Instant.now());
        return competitions.saveAndFlush(comp);
    }

    private EnrollmentExamPaper paper(InfoConcour comp, Matier subject) {
        String u = UUID.randomUUID().toString();
        Candidat candidate = candidates.saveAndFlush(new Candidat(
                "grade-candidate-" + u, "test-password-12345", u + "@example.test",
                "Demo", "Candidate", Role.CANDIDAT, new Date(), null, 0));
        CompetitionEnrollment enrollment = new CompetitionEnrollment(comp, candidate);
        enrollment.assignAnonymousCode("0000001");
        enrollments.saveAndFlush(enrollment);
        return papers.saveAndFlush(new EnrollmentExamPaper(enrollment, subject));
    }

    private Enseignant teacher(Matier subject) {
        String u = UUID.randomUUID().toString();
        return teachers.saveAndFlush(new Enseignant(
                "grade-teacher-" + u, "test-password-12345", u + "@example.test",
                "Demo", "Teacher", Role.ENSEIGNANT, "MCA", subject));
    }
}
