package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.InfoConcour;
import ntic.tlsi.gestiondoctorat2.entities.Matier;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CandidateDashboardUiTests {
    @Autowired private MockMvc mvc;
    @Autowired private CandidatRepo candidates;
    @Autowired private InfoConRepo competitions;

    @Test
    @WithMockUser(username = "candidate-ui", roles = "CANDIDAT")
    void dashboardRendersProfileAndRealNavigation() throws Exception {
        candidates.saveAndFlush(new Candidat("candidate-ui", "test-password-123",
                "candidate-ui@example.test", "Smith", "Alex", Role.CANDIDAT, new Date(), null, 0));
        String html = mvc.perform(get("/candidatPage"))
                .andExpect(status().isOk())
                .andExpect(view().name("CandidatPage"))
                .andExpect(model().attributeExists("candidat"))
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("Welcome,"));
        assertTrue(html.contains("Alex"));
        assertTrue(html.contains("/candidat/getConcourInfo"));
        assertTrue(html.contains("/candidat/ResultatCandidat"));
        assertTrue(html.contains("/css/candidate.css"));
        assertTrue(html.contains("method=\"post\""));
        assertTrue(html.contains("name=\"_csrf\""));
        assertFalse(html.contains("href=\"\""));
    }

    @Test
    @WithMockUser(username = "candidate-ui-details", roles = "CANDIDAT")
    void detailsPageShowsLatestAvailableCompetition() throws Exception {
        InfoConcour latest = competitions.saveAndFlush(
                new InfoConcour(2, "Candidate UI Example", Matier.ALGO, Matier.EDL, new Date()));
        mvc.perform(get("/candidat/getConcourInfo"))
                .andExpect(status().isOk())
                .andExpect(view().name("CandidatConcourInfo"))
                .andExpect(model().attribute("lastConcour",
                        org.hamcrest.Matchers.hasProperty("id", org.hamcrest.Matchers.is(latest.getId()))));
    }

    @Test
    @WithMockUser(username = "missing-candidate-ui", roles = "CANDIDAT")
    void missingCandidateReturns404() throws Exception {
        mvc.perform(get("/candidatPage")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCannotOpenCandidateDashboard() throws Exception {
        mvc.perform(get("/candidatPage")).andExpect(status().isForbidden());
    }

    @Test
    void newStylesheetIsPackaged() {
        assertNotNull(getClass().getClassLoader().getResource("static/css/candidate.css"));
        // Keep the Candidate shell bundled alongside its screens.
        assertNotNull(getClass().getClassLoader().getResource("templates/CandidateTemplate.html"));
    }
}
