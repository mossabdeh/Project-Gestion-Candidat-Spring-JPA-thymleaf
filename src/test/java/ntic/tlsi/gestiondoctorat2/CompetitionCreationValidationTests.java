package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.InfoConRepo;
import ntic.tlsi.gestiondoctorat2.repo.VDRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class CompetitionCreationValidationTests {
    @Autowired MockMvc mvc;
    @Autowired InfoConRepo competitions;
    @Autowired VDRepo viceDeans;

    @Test
    @WithMockUser(username = "competition-vd", roles = "VD")
    void viceDeanCanCreateCompetitionWithoutMassAssigningSystemFields() throws Exception {
        VD vd = viceDeans.saveAndFlush(new VD("competition-vd", "initial-password-123",
                "vd@example.test", "Dean", "One", Role.VD, new Date()));
        long before = competitions.count();
        mvc.perform(validCreation()
                        .param("id", "999999")
                        .param("resultsPublishedAt", "2020-01-01T00:00:00Z")
                        .param("vd.id", "-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vdPage"));
        assertEquals(before + 1, competitions.count());
        InfoConcour saved = competitions.findTopByOrderByIdDesc().orElseThrow();
        assertNotEquals(999999L, saved.getId());
        assertNull(saved.getResultsPublishedAt());
        assertEquals(vd.getId(), saved.getVd().getId());
        assertEquals("Doctoral Studies", saved.getSpecialite());
        assertEquals(2, saved.getNbrPostes());
    }

    @Test
    @WithMockUser(roles = "VD")
    void invalidCapacityAndMissingFieldsNeverCreateCompetition() throws Exception {
        long before = competitions.count();
        mvc.perform(post("/vd/saveInfoConcour").with(csrf())
                        .param("nbrPostes", "0")
                        .param("specialite", " ")
                        .param("matier1", "ALGO")
                        .param("matier2", "EDL")
                        .param("dateConcour", "2030-01-20")
                        .param("registrationOpensAt", "2030-01-01T10:00")
                        .param("registrationClosesAt", "2030-01-15T10:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("InfoConcourAdd"))
                .andExpect(model().attributeHasFieldErrors("infoConcour", "nbrPostes", "specialite"));
        assertEquals(before, competitions.count());
    }

    @Test
    @WithMockUser(roles = "VD")
    void identicalSubjectsAndInvalidRegistrationWindowAreRejected() throws Exception {
        mvc.perform(validCreation()
                        .param("matier2", "ALGO")
                        .param("registrationOpensAt", "2030-01-19T15:00")
                        .param("registrationClosesAt", "2030-01-19T14:00"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotCreateOrOpenCompetitionCreationForm() throws Exception {
        mvc.perform(get("/vd/InfoConcourAdd")).andExpect(status().isForbidden());
        mvc.perform(validCreation()).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VD")
    void creationRequiresCsrf() throws Exception {
        mvc.perform(post("/vd/saveInfoConcour")
                        .param("specialite", "Doctoral Studies"))
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder validCreation() {
        return post("/vd/saveInfoConcour").with(csrf())
                .param("nbrPostes", "2")
                .param("specialite", "Doctoral Studies")
                .param("matier1", "ALGO")
                .param("matier2", "EDL")
                .param("dateConcour", "2030-01-20")
                .param("registrationOpensAt", "2030-01-01T10:00")
                .param("registrationClosesAt", "2030-01-15T10:00");
    }
}
