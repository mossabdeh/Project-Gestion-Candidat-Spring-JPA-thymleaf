package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import ntic.tlsi.gestiondoctorat2.service.CandidateResultsReadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CandidateResultsReadModelTests {
    private static final String USERNAME = "candidate-results-view";
    @Autowired MockMvc mvc;
    @Autowired CandidatRepo candidates;
    @Autowired InfoConRepo competitions;
    @Autowired CandidateResultsReadService readService;

    @Test
    @WithMockUser(username = USERNAME, roles = "CANDIDAT")
    void pendingHidesCodeMarksAndAdmissionEvenWhenDraftDataExists() throws Exception {
        candidate(USERNAME, "7654321", 15.0, 17.0, 16.0, true);
        competition(Matier.ALGO, Matier.EDL, false);
        CandidateResultsReadService.CandidateResultsView view = readService.readFor(USERNAME);
        assertEquals(CandidateResultsReadService.CandidateResultsView.Status.PENDING, view.status());
        assertNull(view.code());
        assertNull(view.overallAverage());
        assertNull(view.admitted());
        assertTrue(view.subjects().isEmpty());
        String html = mvc.perform(get("/candidat/ResultatCandidat"))
                .andExpect(status().isOk())
                .andExpect(view().name("CandidatResultats"))
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("Results have not been published yet"));
        assertFalse(html.contains("7654321"));
        assertFalse(html.contains("16.00"));
        assertFalse(html.contains("Not admitted"));
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "CANDIDAT")
    void publishedScoresUseConfiguredSubjectOrderNotCollectionIndex() throws Exception {
        candidate(USERNAME, "3456789", 12.5, 18.0, 15.25, true);
        competition(Matier.EDL, Matier.ALGO, true);
        CandidateResultsReadService.CandidateResultsView view = readService.readFor(USERNAME);
        assertEquals(CandidateResultsReadService.CandidateResultsView.Status.PUBLISHED, view.status());
        assertEquals(Matier.EDL, view.subjects().get(0).subject());
        assertEquals(12.5, view.subjects().get(0).average());
        assertEquals(Matier.ALGO, view.subjects().get(1).subject());
        assertEquals(18.0, view.subjects().get(1).average());
        assertEquals(15.25, view.overallAverage().doubleValue());
        assertEquals(Boolean.TRUE, view.admitted());
        mvc.perform(get("/candidat/ResultatCandidat"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("results"))
                .andExpect(model().attributeDoesNotExist("MyCandidat", "ListCandidats"));
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "CANDIDAT")
    void publishedZeroGradeAndNegativeDecisionAreValid() throws Exception {
        candidate(USERNAME, "3450000", 0, 0, 0, false);
        competition(Matier.ALGO, Matier.EDL, true);
        CandidateResultsReadService.CandidateResultsView view = readService.readFor(USERNAME);
        assertEquals(0.0, view.overallAverage().doubleValue());
        assertEquals(Boolean.FALSE, view.admitted());
        String html = mvc.perform(get("/candidat/ResultatCandidat"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("Not admitted"));
        assertTrue(html.contains("3450000"));
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "CANDIDAT")
    void emptyCompetitionAndMissingCodeHaveDistinctSafeStates() throws Exception {
        candidate(USERNAME, null, 0, 0, 0, false);
        assertEquals(CandidateResultsReadService.CandidateResultsView.Status.NO_COMPETITION,
                readService.readFor(USERNAME).status());
        competition(Matier.ALGO, Matier.EDL, true);
        assertEquals(CandidateResultsReadService.CandidateResultsView.Status.NOT_AVAILABLE,
                readService.readFor(USERNAME).status());
        mvc.perform(get("/candidat/ResultatCandidat")).andExpect(status().isOk())
                .andExpect(model().attributeExists("results"));
    }

    @Test
    @WithMockUser(username = USERNAME, roles = "CANDIDAT")
    void authenticatedCandidateNeverReadsSomeoneElsesResults() {
        candidate("someone-else-" + UUID.randomUUID(), "1111111", 20, 20, 20, true);
        competition(Matier.ALGO, Matier.EDL, true);
        var thrown = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> readService.readFor(USERNAME));
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, thrown.getStatusCode());
    }

    private Candidat candidate(String username, String code, double s1, double s2, double avg, boolean admitted) {
        Candidat c = new Candidat(username, "test-password-123",
                UUID.randomUUID() + "@example.test", "Test", "Candidate", Role.CANDIDAT,
                new Date(), code, avg);
        c.setMoyMatier1(s1);
        c.setMoyMatier2(s2);
        c.setGetPoste(admitted);
        return candidates.saveAndFlush(c);
    }

    private InfoConcour competition(Matier first, Matier second, boolean published) {
        InfoConcour c = new InfoConcour(1, "TLSI", first, second, new Date());
        if (published) c.setResultsPublishedAt(Instant.now());
        return competitions.saveAndFlush(c);
    }
}
