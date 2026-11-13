package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.EnrollmentAnonymizationService;
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
class EnrollmentAnonymizationTests {
    @Autowired private EnrollmentAnonymizationService codes;
    @Autowired private CompetitionEnrollmentRepo enrollments;
    @Autowired private InfoConRepo competitions;
    @Autowired private CandidatRepo candidates;
    @Autowired private MockMvc mvc;

    @Test
    void repeatedAllocationPreservesCodesAndDoesNotUpdateLegacyCandidateCode() {
        InfoConcour competition = competition(false);
        Candidat first = candidate();
        Candidat second = candidate();
        CompetitionEnrollment a = enrollments.saveAndFlush(new CompetitionEnrollment(competition, first));
        CompetitionEnrollment b = enrollments.saveAndFlush(new CompetitionEnrollment(competition, second));

        assertEquals(2, codes.assignCodes(competition.getId()));
        String firstCode = a.getAnonymousCode();
        String secondCode = b.getAnonymousCode();
        assertNotNull(firstCode);
        assertEquals(7, firstCode.length());
        assertTrue(firstCode.matches("[0-9]{7}"));
        assertNotEquals(firstCode, secondCode);
        assertEquals(0, codes.assignCodes(competition.getId()));
        assertEquals(firstCode, a.getAnonymousCode());
        assertEquals(secondCode, b.getAnonymousCode());
        assertNull(first.getCode());
        assertNull(second.getCode());
    }

    @Test
    void codesHaveIndependentNamespacesAcrossCompetitions() {
        Candidat candidate = candidate();
        InfoConcour first = competition(false);
        CompetitionEnrollment old = enrollments.saveAndFlush(new CompetitionEnrollment(first, candidate));
        old.assignAnonymousCode("0123456");
        enrollments.flush();

        InfoConcour second = competition(false);
        CompetitionEnrollment current = enrollments.saveAndFlush(new CompetitionEnrollment(second, candidate));
        // Same code in a different competition is valid.
        current.assignAnonymousCode("0123456");
        enrollments.flush();

        assertEquals("0123456", old.getAnonymousCode());
        assertEquals("0123456", current.getAnonymousCode());
        assertEquals(0, codes.assignCodes(second.getId()));
    }

    @Test
    void rejectsPublishedOrHistoricalCompetitions() {
        InfoConcour published = competition(true);
        enrollments.saveAndFlush(new CompetitionEnrollment(published, candidate()));
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                    () -> codes.assignCodes(published.getId())).getStatusCode());

        InfoConcour previous = competition(false);
        enrollments.saveAndFlush(new CompetitionEnrollment(previous, candidate()));
        competition(false);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                    () -> codes.assignCodes(previous.getId())).getStatusCode());
    }

    @Test
    void rejectsNoRegistrationsAndInvalidCompetition() {
        InfoConcour comp = competition(false);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                    () -> codes.assignCodes(comp.getId())).getStatusCode());
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class,
                    () -> codes.assignCodes(Long.MAX_VALUE)).getStatusCode());
    }

    @Test
    void entityDoesNotAllowChangingExistingCode() {
        InfoConcour comp = competition(false);
        CompetitionEnrollment enrollment = enrollments.saveAndFlush(new CompetitionEnrollment(comp, candidate()));
        assertThrows(IllegalArgumentException.class, () -> enrollment.assignAnonymousCode("x123"));
        enrollment.assignAnonymousCode("0000001");
        enrollment.assignAnonymousCode("0000001");
        assertThrows(IllegalStateException.class, () -> enrollment.assignAnonymousCode("0000002"));
        assertEquals("0000001", enrollment.getAnonymousCode());
    }

    @Test
    @WithMockUser(username = "vice-dean", roles = "VD")
    void vdCanInspectCodesAndPostIsCsrfProtected() throws Exception {
        InfoConcour comp = competition(false);
        enrollments.saveAndFlush(new CompetitionEnrollment(comp, candidate()));
        mvc.perform(get("/vd/enrollmentCodes"))
                .andExpect(status().isOk())
                .andExpect(view().name("VDEnrollmentCodes"))
                .andExpect(model().attributeExists("enrollmentCodes"));
        mvc.perform(get("/vd/assignEnrollmentCodes")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/vd/assignEnrollmentCodes").param("competitionId", comp.getId().toString()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/vd/assignEnrollmentCodes").with(csrf())
                .param("competitionId", comp.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd/enrollmentCodes"));
    }

    @Test
    @WithMockUser(username = "regular-candidate", roles = "CANDIDAT")
    void candidateCannotReadOrAllocateRegistrationCodes() throws Exception {
        InfoConcour comp = competition(false);
        mvc.perform(get("/vd/enrollmentCodes")).andExpect(status().isForbidden());
        mvc.perform(post("/vd/assignEnrollmentCodes").with(csrf())
                .param("competitionId", comp.getId().toString())).andExpect(status().isForbidden());
    }

    private InfoConcour competition(boolean published) {
        InfoConcour competition = new InfoConcour(2, "Anonymization", Matier.ALGO, Matier.EDL, new Date());
        if (published) competition.setResultsPublishedAt(Instant.now());
        return competitions.saveAndFlush(competition);
    }

    private Candidat candidate() {
        String token = UUID.randomUUID().toString();
        return candidates.saveAndFlush(new Candidat(
                "candidate-" + token, "test-password-12345", token + "@example.test",
                "Sample", "Candidate", Role.CANDIDAT, new Date(), null, 0));
    }
}
