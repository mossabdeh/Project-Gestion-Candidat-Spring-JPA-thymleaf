package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.*;
import ntic.tlsi.gestiondoctorat2.repo.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
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
class CompetitionEditTests {
    @Autowired MockMvc mvc;
    @Autowired VDRepo viceDeans;
    @Autowired CandidatRepo candidates;
    @Autowired InfoConRepo competitions;
    @Autowired CompetitionEnrollmentRepo enrollments;

    @Test
    @WithMockUser(username = "edit-owner", roles = "VD")
    void ownerCanEditBeforeFirstRegistrationWithoutChangingProtectedFields() throws Exception {
        VD owner = viceDean("edit-owner");
        InfoConcour comp = competition(owner);
        mvc.perform(get("/vd/editCompetition").param("id", comp.getId().toString()))
                .andExpect(status().isOk()).andExpect(view().name("InfoConcourEdit"));
        mvc.perform(validEdit(comp).param("specialite", "Updated")
                        .param("resultsPublishedAt", "2020-01-01T00:00:00Z")
                        .param("phase", "PUBLISHED").param("vd.id", "-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd/enrollmentCodes?competitionId=" + comp.getId()));
        competitions.flush();
        InfoConcour saved = competitions.findById(comp.getId()).orElseThrow();
        assertEquals("Updated", saved.getSpecialite());
        assertEquals(owner.getId(), saved.getVd().getId());
        assertNull(saved.getResultsPublishedAt());
        assertEquals(CompetitionPhase.REGISTRATION, saved.effectivePhase());
    }

    @Test
    @WithMockUser(username = "edit-owner-registered", roles = "VD")
    void enrollmentLocksCompetitionSetupWithoutChangingAnyFields() throws Exception {
        VD owner = viceDean("edit-owner-registered");
        InfoConcour comp = competition(owner);
        Candidat candidate = candidates.saveAndFlush(new Candidat("reg-" + UUID.randomUUID(),
                "test-password-12345", "reg@example.test", "A", "B", Role.CANDIDAT,
                new Date(), null, 0));
        enrollments.saveAndFlush(new CompetitionEnrollment(comp, candidate));
        mvc.perform(get("/vd/editCompetition").param("id", comp.getId().toString()))
                .andExpect(status().isConflict());
        mvc.perform(validEdit(comp)).andExpect(status().isConflict());
        assertEquals("Original", competitions.findById(comp.getId()).orElseThrow().getSpecialite());
    }

    @Test
    @WithMockUser(username = "edit-owner-published", roles = "VD")
    void publishedCompetitionIsImmutable() throws Exception {
        VD owner = viceDean("edit-owner-published");
        InfoConcour comp = competition(owner);
        comp.setResultsPublishedAt(Instant.now());
        competitions.flush();
        mvc.perform(validEdit(comp)).andExpect(status().isConflict());
        assertNotNull(competitions.findById(comp.getId()).orElseThrow().getResultsPublishedAt());
    }

    @Test
    @WithMockUser(username = "other-vice-dean", roles = "VD")
    void anotherViceDeanCannotEditTheCompetition() throws Exception {
        VD owner = viceDean("actual-owner");
        viceDean("other-vice-dean");
        InfoConcour comp = competition(owner);
        mvc.perform(get("/vd/editCompetition").param("id", comp.getId().toString()))
                .andExpect(status().isForbidden());
        mvc.perform(validEdit(comp)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "edit-owner-validation", roles = "VD")
    void invalidFormDoesNotUpdateCompetition() throws Exception {
        VD owner = viceDean("edit-owner-validation");
        InfoConcour comp = competition(owner);
        mvc.perform(post("/vd/saveEditedCompetition").with(csrf())
                        .param("id", comp.getId().toString())
                        .param("nbrPostes", "0")
                        .param("specialite", " ")
                        .param("matier1", "ALGO").param("matier2", "ALGO")
                        .param("dateConcour", "2030-01-20")
                        .param("registrationOpensAt", "2030-01-19T14:00")
                        .param("registrationClosesAt", "2030-01-19T13:00"))
                .andExpect(status().isOk()).andExpect(view().name("InfoConcourEdit"))
                .andExpect(model().hasErrors());
        assertEquals("Original", competitions.findById(comp.getId()).orElseThrow().getSpecialite());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotEditCompetition() throws Exception {
        mvc.perform(get("/vd/editCompetition").param("id", "1")).andExpect(status().isForbidden());
        mvc.perform(post("/vd/saveEditedCompetition").with(csrf())
                .param("id", "1")).andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder validEdit(InfoConcour comp) {
        return post("/vd/saveEditedCompetition").with(csrf())
                .param("id", comp.getId().toString()).param("nbrPostes", "2")
                .param("specialite", "Updated").param("matier1", "ALGO").param("matier2", "EDL")
                .param("dateConcour", "2030-01-20")
                .param("registrationOpensAt", "2030-01-01T10:00")
                .param("registrationClosesAt", "2030-01-15T10:00");
    }

    private VD viceDean(String username) {
        return viceDeans.saveAndFlush(new VD(username, "test-password-12345",
                UUID.randomUUID() + "@example.test", "Test", "Dean", Role.VD, new Date()));
    }

    private InfoConcour competition(VD owner) {
        InfoConcour comp = new InfoConcour(2, "Original", Matier.ALGO, Matier.EDL,
                new Date(1895097600000L));
        comp.setVd(owner);
        comp.setPhase(CompetitionPhase.REGISTRATION);
        comp.setRegistrationOpensAt(LocalDateTime.of(2030, 1, 1, 10, 0));
        comp.setRegistrationClosesAt(LocalDateTime.of(2030, 1, 15, 10, 0));
        return competitions.saveAndFlush(comp);
    }
}
