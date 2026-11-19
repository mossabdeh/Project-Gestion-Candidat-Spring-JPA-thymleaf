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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
    @Autowired EnrollmentExamPaperRepo papers;
    @Autowired MockMvc mvc;

    @Test
    @WithMockUser(username = "enroll-alice", roles = "CANDIDAT")
    void candidateCanRegisterOnceAndSeeOwnStatus() throws Exception {
        Candidat alice = candidate("enroll-alice");
        InfoConcour comp = competition(false);
        mvc.perform(post("/candidat/register").param("competitionId", comp.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidat/getConcourInfo?competitionId=" + comp.getId()));
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
    void publishedCompetitionRejectsRegistrationButOlderOpenCompetitionCanAccept() {
        Candidat alice = candidate("enroll-closed-" + UUID.randomUUID());
        InfoConcour published = competition(true);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                    () -> registration.register(published.getId(), alice.getUsername())).getStatusCode());
        InfoConcour old = competition(false);
        competition(false);
        registration.register(old.getId(), alice.getUsername());
        assertTrue(registration.isRegistered(old.getId(), alice.getUsername()));
    }

    @Test
    void cannotRegisterBeforeOpeningOrAfterClosing() {
        Candidat candidate = candidate("enroll-window-" + UUID.randomUUID());
        InfoConcour before = competition(false);
        before.setRegistrationOpensAt(parisNow().plusDays(1));
        before.setRegistrationClosesAt(parisNow().plusDays(2));
        competitions.saveAndFlush(before);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> registration.register(before.getId(), candidate.getUsername())).getStatusCode());

        InfoConcour after = competition(false);
        after.setRegistrationOpensAt(parisNow().minusDays(2));
        after.setRegistrationClosesAt(parisNow().minusDays(1));
        competitions.saveAndFlush(after);
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> registration.register(after.getId(), candidate.getUsername())).getStatusCode());
        assertEquals(0, enrollments.countByCompetition_IdAndCandidate_Id(after.getId(), candidate.getId()));
    }

    @Test
    void explicitPreparationPhaseRejectsRegistrationEvenWithoutCodes() {
        InfoConcour comp = competition(false);
        comp.advanceTo(CompetitionPhase.PREPARATION);
        competitions.flush();
        assertFalse(registration.registrationOpen(comp));
        Candidat late = candidate("phase-late-" + UUID.randomUUID());
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> registration.register(comp.getId(), late.getUsername())).getStatusCode());
    }

    @Test
    void registrationFreezesWhenAnyCodeIsAllocated() {
        InfoConcour comp = competition(false);
        Candidat first = candidate("freeze-first-" + UUID.randomUUID());
        Candidat late = candidate("freeze-late-" + UUID.randomUUID());
        registration.register(comp.getId(), first.getUsername());
        CompetitionEnrollment enrolled = enrollments.findByCompetition_IdAndCandidate_Username(
                comp.getId(), first.getUsername()).orElseThrow();
        enrolled.assignAnonymousCode("0000011");
        enrollments.flush();

        assertFalse(registration.registrationOpen(comp));
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> registration.register(comp.getId(), late.getUsername())).getStatusCode());
        assertEquals(0, enrollments.countByCompetition_IdAndCandidate_Id(comp.getId(), late.getId()));
        // An existing registration remains unchanged, and repeat POSTs cannot duplicate it.
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> registration.register(comp.getId(), first.getUsername())).getStatusCode());
        assertEquals(1, enrollments.countByCompetition_IdAndCandidate_Id(comp.getId(), first.getId()));
    }

    @Test
    void registrationFreezesWhenPapersExistEvenWithoutCodes() {
        InfoConcour comp = competition(false);
        Candidat first = candidate("papers-first-" + UUID.randomUUID());
        Candidat late = candidate("papers-late-" + UUID.randomUUID());
        registration.register(comp.getId(), first.getUsername());
        CompetitionEnrollment enrolled = enrollments.findByCompetition_IdAndCandidate_Username(
                comp.getId(), first.getUsername()).orElseThrow();
        papers.saveAndFlush(new EnrollmentExamPaper(enrolled, Matier.ALGO));

        assertFalse(registration.registrationOpen(comp));
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> registration.register(comp.getId(), late.getUsername())).getStatusCode());
        assertFalse(registration.isRegistered(comp.getId(), late.getUsername()));
    }

    @Test
    void missingWindowIsFailClosed() {
        Candidat candidate = candidate("enroll-missing-window-" + UUID.randomUUID());
        InfoConcour comp = competition(false);
        comp.setRegistrationOpensAt(null);
        comp.setRegistrationClosesAt(null);
        competitions.saveAndFlush(comp);
        assertFalse(registration.registrationOpen(comp));
        assertEquals(org.springframework.http.HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class,
                        () -> registration.register(comp.getId(), candidate.getUsername())).getStatusCode());
    }

    @Test
    @WithMockUser(username = "enroll-closed-ui", roles = "CANDIDAT")
    void closedWindowHidesRegistrationForm() throws Exception {
        candidate("enroll-closed-ui");
        InfoConcour comp = competition(false);
        comp.setRegistrationClosesAt(parisNow().minusMinutes(1));
        comp.setRegistrationOpensAt(parisNow().minusDays(1));
        competitions.saveAndFlush(comp);
        String html = mvc.perform(get("/candidat/getConcourInfo"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("registrationOpen", false))
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("Registration is currently closed"));
        assertFalse(html.contains("Register for competition"));
    }

    @Test
    @WithMockUser(username = "vice-dean", roles = "VD")
    void invalidCompetitionWindowReturnsValidationErrors() throws Exception {
        mvc.perform(post("/vd/saveInfoConcour").with(csrf())
                        .param("nbrPostes", "2")
                        .param("specialite", "TEST")
                        .param("matier1", "ALGO")
                        .param("matier2", "EDL")
                        .param("dateConcour", "2030-01-20")
                        .param("registrationOpensAt", "2030-01-19T14:00")
                        .param("registrationClosesAt", "2030-01-19T13:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("InfoConcourAdd"))
                .andExpect(model().hasErrors());
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
        InfoConcour comp = new InfoConcour(2, "Enrollment Test", Matier.ALGO, Matier.EDL,
                Date.from(Instant.now().plus(3, ChronoUnit.DAYS)));
        comp.setRegistrationOpensAt(parisNow().minusHours(1));
        comp.setRegistrationClosesAt(parisNow().plusHours(1));
        if (published) comp.setResultsPublishedAt(Instant.now());
        return competitions.saveAndFlush(comp);
    }

    private LocalDateTime parisNow() {
        return LocalDateTime.now(ZoneId.of("Europe/Paris"));
    }

    private Candidat candidate(String username) {
        return candidates.saveAndFlush(new Candidat(
                username, "test-password-12345", UUID.randomUUID() + "@example.test",
                "Sample", "Candidate", Role.CANDIDAT, new Date(), null, 0));
    }
}
