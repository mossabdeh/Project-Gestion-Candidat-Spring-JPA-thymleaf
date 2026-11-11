package ntic.tlsi.gestiondoctorat2;

import jakarta.persistence.EntityManager;
import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.Copie;
import ntic.tlsi.gestiondoctorat2.entities.Matier;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.repo.AdminRepo;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.CopieRepo;
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
@WithMockUser(username = "acting-admin", roles = "ADMIN")
class CandidateProfileIntegrityTests {
    @Autowired MockMvc mvc;
    @Autowired CandidatRepo candidates;
    @Autowired AdminRepo admins;
    @Autowired CopieRepo copies;
    @Autowired EntityManager entityManager;

    @Test
    void forgedFieldsCannotOverwriteExamResultsOrPasswordOrRole() throws Exception {
        Candidat candidate = createCandidate();
        Copie firstPaper = copies.saveAndFlush(new Copie(Matier.ALGO, candidate));
        Copie secondPaper = copies.saveAndFlush(new Copie(Matier.EDL, candidate));
        Long id = candidate.getId();
        String originalHash = candidate.getPassword();

        mvc.perform(post("/candidat/saveEditedCandidat").with(csrf())
                        .param("id", id.toString())
                        .param("username", candidate.getUsername())
                        .param("nom", "Renamed")
                        .param("prenom", "New")
                        .param("email", "updated@example.test")
                        .param("dateNaissance", "2000-05-10")
                        .param("password", "malicious-password-123")
                        .param("typeRole", "ADMIN")
                        .param("code", "MALICIOUS")
                        .param("moyMatier1", "0")
                        .param("moyMatier2", "0")
                        .param("moyenneGeneral", "0")
                        .param("getPoste", "false")
                        .param("copies[0].id", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidat/getCandidats"));

        entityManager.flush();
        entityManager.clear();
        Candidat saved = candidates.findCandidatOptionalById(id).orElseThrow();
        assertEquals("Renamed", saved.getNom());
        assertEquals("New", saved.getPrenom());
        assertEquals("updated@example.test", saved.getEmail());
        assertEquals(Role.CANDIDAT, saved.getTypeRole());
        assertEquals(originalHash, saved.getPassword());
        assertEquals("ANON-123", saved.getCode());
        assertEquals(15.5, saved.getMoyMatier1());
        assertEquals(14.5, saved.getMoyMatier2());
        assertEquals(15.0, saved.getMoyenneGeneral());
        assertTrue(saved.isGetPoste());
        assertEquals(2, saved.getCopies().size());
        assertEquals(id, copies.findById(firstPaper.getId()).orElseThrow().getCandidat().getId());
        assertEquals(id, copies.findById(secondPaper.getId()).orElseThrow().getCandidat().getId());
        assertEquals("2000-05-10", new java.text.SimpleDateFormat("yyyy-MM-dd").format(saved.getDateNaissance()));
    }

    @Test
    void duplicateUsernameAcrossRolesIsRejected() throws Exception {
        Candidat candidate = createCandidate();
        Admin admin = admins.saveAndFlush(new Admin("already-used-" + UUID.randomUUID(),
                "initial-password-123", "admin@example.test", "Other", "Admin", Role.ADMIN, new Date()));

        mvc.perform(post("/candidat/saveEditedCandidat").with(csrf())
                        .param("id", candidate.getId().toString())
                        .param("username", admin.getUsername())
                        .param("nom", "Should Not Persist")
                        .param("prenom", candidate.getPrenom())
                        .param("email", candidate.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("AdminCandidatEdit"))
                .andExpect(model().attributeHasFieldErrors("candidat", "username"));

        entityManager.flush();
        entityManager.clear();
        assertEquals("Original", candidates.findCandidatOptionalById(candidate.getId()).orElseThrow().getNom());
    }

    @Test
    void invalidEmailIsRejectedWithoutPersistingChanges() throws Exception {
        Candidat candidate = createCandidate();
        mvc.perform(post("/candidat/saveEditedCandidat").with(csrf())
                        .param("id", candidate.getId().toString())
                        .param("username", candidate.getUsername())
                        .param("nom", "Should Not Persist")
                        .param("prenom", candidate.getPrenom())
                        .param("email", "not-an-email"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("candidat", "email"));
        entityManager.flush();
        entityManager.clear();
        assertEquals("Original", candidates.findCandidatOptionalById(candidate.getId()).orElseThrow().getNom());
    }

    @Test
    void missingCandidateReturnsNotFound() throws Exception {
        mvc.perform(post("/candidat/saveEditedCandidat").with(csrf())
                        .param("id", Long.toString(Long.MAX_VALUE))
                        .param("username", "valid-new")
                        .param("nom", "Name")
                        .param("prenom", "Given")
                        .param("email", "valid@example.test"))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingIdIsBadRequest() throws Exception {
        mvc.perform(post("/candidat/saveEditedCandidat").with(csrf())
                        .param("username", "candidate")
                        .param("nom", "Name")
                        .param("prenom", "Given")
                        .param("email", "valid@example.test"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void editPageHasValidProfileOnlyModel() throws Exception {
        Candidat candidate = createCandidate();
        mvc.perform(get("/candidat/editCandidat")
                        .param("id", candidate.getId().toString())
                        .param("page", "2").param("keyword", "Original"))
                .andExpect(status().isOk())
                .andExpect(view().name("AdminCandidatEdit"))
                .andExpect(model().attributeExists("candidat"))
                .andExpect(model().attribute("page", 2))
                .andExpect(model().attribute("keyword", "Original"));
    }

    @Test
    void successfulEditPreservesSearchAndPagination() throws Exception {
        Candidat candidate = createCandidate();
        mvc.perform(post("/candidat/saveEditedCandidat").with(csrf())
                        .param("id", candidate.getId().toString())
                        .param("username", candidate.getUsername())
                        .param("nom", "Updated")
                        .param("prenom", "Candidate")
                        .param("email", candidate.getEmail())
                        .param("page", "1")
                        .param("keyword", "Smith & Jones"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidat/getCandidats?page=1&keyword=Smith%20%26%20Jones"));
    }

    @Test
    void editingAnotherRoleIsNotFound() throws Exception {
        Admin admin = admins.saveAndFlush(new Admin("admin-" + UUID.randomUUID(),
                "initial-password-123", "admin@example.test", "Admin", "User", Role.ADMIN, new Date()));
        mvc.perform(post("/candidat/saveEditedCandidat").with(csrf())
                        .param("id", admin.getId().toString())
                        .param("username", "attacker")
                        .param("nom", "Attack")
                        .param("prenom", "Attempt")
                        .param("email", "attacker@example.test"))
                .andExpect(status().isNotFound());
        assertEquals(Role.ADMIN, admins.findById(admin.getId()).orElseThrow().getTypeRole());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotCallAdminProfileUpdate() throws Exception {
        Candidat candidate = createCandidate();
        mvc.perform(post("/candidat/saveEditedCandidat").with(csrf())
                        .param("id", candidate.getId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void csrfIsRequired() throws Exception {
        Candidat candidate = createCandidate();
        mvc.perform(post("/candidat/saveEditedCandidat")
                        .param("id", candidate.getId().toString()))
                .andExpect(status().isForbidden());
    }

    private Candidat createCandidate() {
        Candidat candidate = new Candidat(
                "candidate-" + UUID.randomUUID(), "initial-password-123",
                "original@example.test", "Original", "Candidate",
                Role.CANDIDAT, new Date(946684800000L), "ANON-123", 15.0);
        candidate.setMoyMatier1(15.5);
        candidate.setMoyMatier2(14.5);
        candidate.setGetPoste(true);
        return candidates.saveAndFlush(candidate);
    }
}
