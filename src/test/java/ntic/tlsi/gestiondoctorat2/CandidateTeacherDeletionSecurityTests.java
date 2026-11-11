package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Candidat;
import ntic.tlsi.gestiondoctorat2.entities.Enseignant;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.repo.AdminRepo;
import ntic.tlsi.gestiondoctorat2.repo.CandidatRepo;
import ntic.tlsi.gestiondoctorat2.repo.EnseignantRepo;
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
class CandidateTeacherDeletionSecurityTests {
    @Autowired private MockMvc mvc;
    @Autowired private CandidatRepo candidats;
    @Autowired private EnseignantRepo teachers;
    @Autowired private AdminRepo admins;

    @Test
    @WithMockUser(roles = "ADMIN")
    void candidateGetDoesNotDelete() throws Exception {
        Candidat candidate = candidate();
        mvc.perform(get("/candidat/delete").param("id", candidate.getId().toString()))
                .andExpect(status().isMethodNotAllowed());
        assertTrue(candidats.findCandidatOptionalById(candidate.getId()).isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void teacherGetDoesNotDelete() throws Exception {
        Enseignant teacher = teacher();
        mvc.perform(get("/enseignant/delete").param("id", teacher.getId().toString()))
                .andExpect(status().isMethodNotAllowed());
        assertTrue(teachers.findEnseignantOptionalById(teacher.getId()).isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void candidatePostWithoutCsrfIsForbidden() throws Exception {
        Candidat candidate = candidate();
        mvc.perform(post("/candidat/delete").param("id", candidate.getId().toString()))
                .andExpect(status().isForbidden());
        assertTrue(candidats.findCandidatOptionalById(candidate.getId()).isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void teacherPostWithoutCsrfIsForbidden() throws Exception {
        Enseignant teacher = teacher();
        mvc.perform(post("/enseignant/delete").param("id", teacher.getId().toString()))
                .andExpect(status().isForbidden());
        assertTrue(teachers.findEnseignantOptionalById(teacher.getId()).isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void candidatePostWithCsrfDeletesOnlyCandidateAndPreservesFilter() throws Exception {
        Candidat candidate = candidate();
        mvc.perform(post("/candidat/delete").with(csrf())
                        .param("id", candidate.getId().toString())
                        .param("keyword", "Smith & Jones")
                        .param("page", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/candidat/getCandidats?page=0&keyword=Smith%20%26%20Jones"));
        assertTrue(candidats.findCandidatOptionalById(candidate.getId()).isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void teacherPostWithCsrfDeletesOnlyTeacher() throws Exception {
        Enseignant teacher = teacher();
        mvc.perform(post("/enseignant/delete").with(csrf())
                        .param("id", teacher.getId().toString()).param("page", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/enseignant/getEnseignants?page=0&keyword="));
        assertTrue(teachers.findEnseignantOptionalById(teacher.getId()).isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void candidateEndpointCannotDeleteTeacherOrAdmin() throws Exception {
        Enseignant teacher = teacher();
        Admin admin = admin();
        for (Long id : new Long[]{teacher.getId(), admin.getId()}) {
            mvc.perform(post("/candidat/delete").with(csrf())
                            .param("id", id.toString()))
                    .andExpect(status().isNotFound());
        }
        assertTrue(teachers.findEnseignantOptionalById(teacher.getId()).isPresent());
        assertTrue(admins.findById(admin.getId()).isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void teacherEndpointCannotDeleteCandidateOrAdmin() throws Exception {
        Candidat candidate = candidate();
        Admin admin = admin();
        for (Long id : new Long[]{candidate.getId(), admin.getId()}) {
            mvc.perform(post("/enseignant/delete").with(csrf())
                            .param("id", id.toString()))
                    .andExpect(status().isNotFound());
        }
        assertTrue(candidats.findCandidatOptionalById(candidate.getId()).isPresent());
        assertTrue(admins.findById(admin.getId()).isPresent());
    }

    @Test
    @WithMockUser(roles = "CANDIDAT")
    void candidateCannotDeleteCandidateEvenWithCsrf() throws Exception {
        Candidat target = candidate();
        mvc.perform(post("/candidat/delete").with(csrf()).param("id", target.getId().toString()))
                .andExpect(status().isForbidden());
        assertTrue(candidats.findCandidatOptionalById(target.getId()).isPresent());
    }

    @Test
    @WithMockUser(roles = "ENSEIGNANT")
    void teacherCannotDeleteTeacherEvenWithCsrf() throws Exception {
        Enseignant target = teacher();
        mvc.perform(post("/enseignant/delete").with(csrf()).param("id", target.getId().toString()))
                .andExpect(status().isForbidden());
        assertTrue(teachers.findEnseignantOptionalById(target.getId()).isPresent());
    }

    @Test
    void anonymousUserCannotDelete() throws Exception {
        Candidat candidate = candidate();
        mvc.perform(post("/candidat/delete").with(csrf()).param("id", candidate.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        assertTrue(candidats.findCandidatOptionalById(candidate.getId()).isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listFormsSubmitPostWithCsrf() throws Exception {
        Candidat candidate = candidate();
        Enseignant teacher = teacher();

        String candidateHtml = mvc.perform(get("/candidat/getCandidats"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertProtectedDeleteForm(candidateHtml, candidate.getId());

        String teacherHtml = mvc.perform(get("/enseignant/getEnseignants"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertProtectedDeleteForm(teacherHtml, teacher.getId());
    }

    private void assertProtectedDeleteForm(String html, Long id) {
        // Spring renders HTML5, not XML; MockMvc's XPath XML parser cannot parse
        // ordinary HTML void elements such as <link>.
        int actionIndex = html.indexOf("action=\"delete\"");
        assertTrue(actionIndex >= 0, "Expected a delete form in the account list");

        int formStart = html.lastIndexOf("<form", actionIndex);
        int formEnd = html.indexOf("</form>", actionIndex);
        assertTrue(formStart >= 0 && formEnd > actionIndex, "Delete form must be complete");
        String form = html.substring(formStart, formEnd);

        assertTrue(form.contains("method=\"post\""), "Delete must submit using POST");
        assertTrue(form.contains("name=\"_csrf\""), "Delete form must include CSRF token");
        assertTrue(form.contains("name=\"id\" value=\"" + id + "\""),
                "Delete form must submit the selected account ID");
        assertTrue(form.contains("type=\"submit\""), "Delete must require explicit submission");
    }

    private Candidat candidate() {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return candidats.saveAndFlush(new Candidat("candidate-" + id, "unique-password-123", "candidate-" + id + "@example.test",
                "Security", "Candidate", Role.CANDIDAT, new Date(), null, 0));
    }

    private Enseignant teacher() {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return teachers.saveAndFlush(new Enseignant("teacher-" + id, "unique-password-123", "teacher-" + id + "@example.test",
                "Security", "Teacher", Role.ENSEIGNANT, "MCA", ntic.tlsi.gestiondoctorat2.entities.Matier.ALGO));
    }

    private Admin admin() {
        String id = UUID.randomUUID().toString().substring(0, 8);
        return admins.saveAndFlush(new Admin("admin-" + id, "unique-password-123",
                "admin-" + id + "@example.test", "Security", "Admin", Role.ADMIN, new Date()));
    }
}
