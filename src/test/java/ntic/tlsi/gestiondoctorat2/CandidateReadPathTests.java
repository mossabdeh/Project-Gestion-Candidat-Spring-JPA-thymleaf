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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CandidateReadPathTests {
    @Autowired MockMvc mvc;
    @Autowired CandidatRepo candidates;
    @Autowired InfoConRepo competitions;

    @Test
    @WithMockUser(username = "candidate-read-path-test", roles = "CANDIDAT")
    void resultsPageOnlyLoadsAuthenticatedCandidate() throws Exception {
        Candidat own = new Candidat("candidate-read-path-test", "safe-test-password-123",
                "readpath@example.test", "Own", "Candidate", Role.CANDIDAT, new Date(), null, 0);
        candidates.saveAndFlush(own);
        Candidat unrelated = new Candidat("unrelated-read-path-test", "safe-test-password-123",
                "unrelated@example.test", "Unrelated", "Candidate", Role.CANDIDAT, new Date(), "ABC123", 15);
        candidates.saveAndFlush(unrelated);

        mvc.perform(get("/candidat/ResultatCandidat"))
                .andExpect(status().isOk())
                .andExpect(view().name("CandidatResultats"))
                .andExpect(model().attributeExists("results"))
                .andExpect(model().attributeDoesNotExist("MyCandidat"))
                .andExpect(model().attributeDoesNotExist("ListCandidats", "pages", "currentPage"));
    }

    @Test
    @WithMockUser(username = "candidate-missing-read-test", roles = "CANDIDAT")
    void candidateWithoutAccountGetsNotFoundRatherThanOptionalGetFailure() throws Exception {
        mvc.perform(get("/candidat/ResultatCandidat"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void administratorsCannotOpenCandidateResults() throws Exception {
        mvc.perform(get("/candidat/ResultatCandidat"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void newestCompetitionIsSelectedByIdNotUnspecifiedListOrder() throws Exception {
        InfoConcour older = competitions.saveAndFlush(
                new InfoConcour(1, "Older", Matier.ALGO, Matier.EDL, new Date()));
        InfoConcour newer = competitions.saveAndFlush(
                new InfoConcour(2, "Newest", Matier.ALGO, Matier.EDL, new Date()));
        assertEquals(newer.getId(), competitions.findTopByOrderByIdDesc().orElseThrow().getId());
        assertTrue(newer.getId() > older.getId());

        mvc.perform(get("/candidat/getConcourInfo"))
                .andExpect(status().isOk())
                .andExpect(view().name("CandidatConcourInfo"))
                .andExpect(model().attribute("lastConcour", org.hamcrest.Matchers.hasProperty(
                        "id", org.hamcrest.Matchers.is(newer.getId()))));
    }
}
