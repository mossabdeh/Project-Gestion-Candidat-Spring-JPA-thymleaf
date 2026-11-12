package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.CompetitionEnrollmentService;
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
class CompetitionEnrollmentTests {
    @Autowired CompetitionEnrollmentRepo enrollments;
    @Autowired CompetitionEnrollmentService registration;
    @Autowired CandidatRepo candidates;
    @Autowired InfoConRepo competitions;
    @Autowired MockMvc mvc;

    @Test
    @WithMockUser(username = "enroll-alice", roles = "CANDIDAT")
    void candidateCanRegisterOnceAndSeeOwnStatus() throws Exception {
        Candidat alice = candidate("enroll-alice");
        InfoConcour comp = competition(false);
        mvc.perform(post("/candidat/register").param("competitionId", comp.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidat/getConcourInfo"));
        mvc.perform(post("/candidat/register").param("competitionId", comp.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertEquals(1, enrollments.countByCompetition_IdAndCandidate_Id(comp.getId(), alice.getId()));
        mvc.perform(get("/candidat/getConcourInfo"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("enrolled", true));
    }

    @Test
    void registrationsBelongToSpecificCandidatesAndCompetitions() {
        Candidat a = candidate("enroll-a-" + UUID.randomUUID());
        Candidat b = candidate("enroll-b-" + UUID.randomUUID());
        InfoConcour old = competition(false);
        registration.register(old.getId(), a.getUsername());
        assertTrue(registration.isRegistered(old.getId(), a.getUsername()));
        assertFalse(registration.isRegistered(old.getId(), b.getUsername()));
        InfoConcour current = competition(false);
        assertFalse(registration.isRegistered(current.getId(), a.getUsername()));
        registration.register(current.getId(), a.getUsername());
        registration.register(current.getId(), b.getUsername());
        assertEquals(1, enrollments.countByCompetition_IdAndCandidate_Id(old.getId(), a.getId()));
        assertEquals(1, enrollments.countByCompetition_IdAndCandidate_Id(current.getId(), a.getId()));
        assertEquals(1, enrollments.countByCompetition_IdAndCandidate_Id(current.getId(), b.getId()));
    }

    @Test
    void publishedOrOldCompetitionCannotAcceptNewRegistration() {
        Candidat alice = candidate("enroll-closed-" + UUID.randomUUID());
        InfoConcour published = competition(true);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                    () -> registration.register(published.getId(), alice.getUsername())).getStatusCode());
        InfoConcour old = competition(false);
        competition(false);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                    () -> registration.register(old.getId(), alice.getUsername())).getStatusCode());
    }

    @Test
    @WithMockUser(username = "enroll-no-account", roles = "CANDIDAT")
    void missingAccountAndMissingCompetitionAreNotCreated() {
        InfoConcour comp = competition(false);
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class,
                    () -> registration.register(comp.getId(), "enroll-no-account")).getStatusCode());
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class,
                    () -> registration.register(Long.MAX_VALUE, "enroll-no-account")).getStatusCode());
        assertEquals(0, enrollments.count());
    }

    @Test
    @WithMockUser(username = "enroll-no-csrf", roles = "CANDIDAT")
    void registrationRequiresCsrf() throws Exception {
        InfoConcour comp = competition(false);
        mvc.perform(post("/candidat/register").param("competitionId", comp.getId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "enroll-admin", roles = "ADMIN")
    void adminsCannotRegisterAsCandidates() throws Exception {
        InfoConcour comp = competition(false);
        mvc.perform(post("/candidat/register").param("competitionId", comp.getId().toString())
                .with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "enroll-ui", roles = "CANDIDAT")
    void unregisteredCandidateSeesRegistrationForm() throws Exception {
        candidate("enroll-ui");
        InfoConcour comp = competition(false);
        String html = mvc.perform(get("/candidat/getConcourInfo"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("enrolled", false))
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("Register for competition"));
        assertTrue(html.contains("name=\"_csrf\""));
        assertTrue(html.contains(comp.getId().toString()));
    }

    private InfoConcour competition(boolean published) {
        InfoConcour comp = new InfoConcour(2, "Enrollment Test", Matier.ALGO, Matier.EDL, new Date());
        if (published) comp.setResultsPublishedAt(Instant.now());
        return competitions.saveAndFlush(comp);
    }

    private Candidat candidate(String username) {
        return candidates.saveAndFlush(new Candidat(
                username, "test-password-12345", UUID.randomUUID() + "@example.test",
                "Sample", "Candidate", Role.CANDIDAT, new Date(), null, 0));
    }
}
