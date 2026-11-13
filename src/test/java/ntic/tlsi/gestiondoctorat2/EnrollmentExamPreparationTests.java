package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.EnrollmentExamPreparationService;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EnrollmentExamPreparationTests {
    @Autowired EnrollmentExamPreparationService preparation;
    @Autowired CompetitionEnrollmentRepo enrollments;
    @Autowired EnrollmentExamPaperRepo papers;
    @Autowired EnrollmentExamCorrectionRepo corrections;
    @Autowired InfoConRepo competitions;
    @Autowired CandidatRepo candidates;
    @Autowired EnseignantRepo teachers;
    @Autowired MockMvc mvc;

    @Test
    void preparationIsRepeatableAndIsolatedAcrossTwoCompetitions() {
        Candidat sameCandidate = candidate();
        InfoConcour first = competition(false);
        CompetitionEnrollment firstEnrollment = registration(first, sameCandidate, "0000001");
        assertEquals(2, preparation.initializePapers(first.getId()));
        assertEquals(0, preparation.initializePapers(first.getId()));
        List<EnrollmentExamPaper> firstPapers = papers.findByEnrollment_Id(firstEnrollment.getId());
        assertEquals(2, firstPapers.size());

        InfoConcour second = competition(false);
        CompetitionEnrollment secondEnrollment = registration(second, sameCandidate, "0000002");
        assertEquals(2, preparation.initializePapers(second.getId()));
        assertEquals(0, preparation.initializePapers(second.getId()));
        assertEquals(2, papers.findByEnrollment_Id(firstEnrollment.getId()).size());
        assertEquals(2, papers.findByEnrollment_Id(secondEnrollment.getId()).size());
        assertEquals(4, papers.count());
        assertTrue(firstPapers.stream().allMatch(p -> p.getEnrollment().getCompetition().getId().equals(first.getId())));
        assertNull(sameCandidate.getCode());
        assertTrue(sameCandidate.getCopies().isEmpty());
    }

    @Test
    void qualifiedMarkerAssignmentsAreRepeatableAndUnique() {
        InfoConcour comp = competition(false);
        CompetitionEnrollment enrollment = registration(comp, candidate(), "0000001");
        preparation.initializePapers(comp.getId());
        Enseignant algo1 = teacher(Matier.ALGO);
        Enseignant algo2 = teacher(Matier.ALGO);
        teacher(Matier.EDL);
        teacher(Matier.EDL);

        assertEquals(4, preparation.initializeCorrections(comp.getId()));
        assertEquals(0, preparation.initializeCorrections(comp.getId()));
        for (EnrollmentExamPaper paper : papers.findByEnrollment_Id(enrollment.getId())) {
            List<EnrollmentExamCorrection> allocated = corrections.findByPaper_Id(paper.getId());
            assertEquals(2, allocated.size());
            assertNotEquals(allocated.get(0).getTeacher().getId(), allocated.get(1).getTeacher().getId());
            assertTrue(allocated.stream().allMatch(c -> c.getTeacher().getSpecialite() == paper.getSubject()));
            assertTrue(allocated.stream().allMatch(c -> c.getGrade() == null));
        }
        assertEquals(4, corrections.count());
    }

    @Test
    void missingCodeAndMissingTeacherRejectIncompleteSetup() {
        InfoConcour comp = competition(false);
        enrollments.saveAndFlush(new CompetitionEnrollment(comp, candidate()));
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> preparation.initializePapers(comp.getId())).getStatusCode());
        assertTrue(papers.findByEnrollment_Competition_IdOrderByIdAsc(comp.getId()).isEmpty());
    }

    @Test
    void missingQualifiedTeachersNeverMakePartialAssignments() {
        InfoConcour comp = competition(false);
        CompetitionEnrollment e = registration(comp, candidate(), "0000010");
        preparation.initializePapers(comp.getId());
        teacher(Matier.ALGO);
        teacher(Matier.ALGO);
        teacher(Matier.EDL);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> preparation.initializeCorrections(comp.getId())).getStatusCode());
        for (EnrollmentExamPaper p : papers.findByEnrollment_Id(e.getId())) {
            assertTrue(corrections.findByPaper_Id(p.getId()).isEmpty());
        }
    }

    @Test
    void publishedAndHistoricalCompetitionsCannotBePrepared() {
        InfoConcour closed = competition(true);
        registration(closed, candidate(), "0000003");
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> preparation.initializePapers(closed.getId())).getStatusCode());
        InfoConcour previous = competition(false);
        registration(previous, candidate(), "0000004");
        competition(false);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> preparation.initializePapers(previous.getId())).getStatusCode());
    }

    @Test
    @WithMockUser(username = "cfd-test", roles = "CFD")
    void cfdCanOpenPreparationAndPostsRequireCsrf() throws Exception {
        InfoConcour comp = competition(false);
        registration(comp, candidate(), "0000005");
        mvc.perform(get("/cfd/enrollmentPapers"))
                .andExpect(status().isOk())
                .andExpect(view().name("CFDEnrollmentPapers"));
        mvc.perform(post("/cfd/initEnrollmentPapers")
                        .param("competitionId", comp.getId().toString()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/cfd/initEnrollmentPapers").with(csrf())
                        .param("competitionId", comp.getId().toString()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/cfd/initEnrollmentPapers").with(csrf())
                        .param("competitionId", comp.getId().toString()))
                .andExpect(status().is3xxRedirection());
        assertEquals(2, papers.findByEnrollment_Competition_IdOrderByIdAsc(comp.getId()).size());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotAccessOrPrepareCfdPapers() throws Exception {
        mvc.perform(get("/cfd/enrollmentPapers")).andExpect(status().isForbidden());
        mvc.perform(post("/cfd/initEnrollmentPapers").with(csrf())
                        .param("competitionId", "1"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/cfd/initEnrollmentCorrections").with(csrf())
                        .param("competitionId", "1"))
                .andExpect(status().isForbidden());
    }

    private InfoConcour competition(boolean published) {
        InfoConcour comp = new InfoConcour(2, "Scoped exam", Matier.ALGO, Matier.EDL, new Date());
        if (published) comp.setResultsPublishedAt(Instant.now());
        return competitions.saveAndFlush(comp);
    }

    private Candidat candidate() {
        String unique = UUID.randomUUID().toString();
        return candidates.saveAndFlush(new Candidat(
                "enroll-exam-" + unique, "test-password-12345",
                unique + "@example.test", "Sample", "Candidate",
                Role.CANDIDAT, new Date(), null, 0));
    }

    private CompetitionEnrollment registration(InfoConcour comp, Candidat candidate, String code) {
        CompetitionEnrollment enrollment = new CompetitionEnrollment(comp, candidate);
        enrollment.assignAnonymousCode(code);
        return enrollments.saveAndFlush(enrollment);
    }

    private Enseignant teacher(Matier subject) {
        String unique = UUID.randomUUID().toString();
        return teachers.saveAndFlush(new Enseignant("enroll-teacher-" + unique, "test-password-12345",
                unique + "@example.test", "Sample", "Teacher", Role.ENSEIGNANT, "MCA", subject));
    }
}
