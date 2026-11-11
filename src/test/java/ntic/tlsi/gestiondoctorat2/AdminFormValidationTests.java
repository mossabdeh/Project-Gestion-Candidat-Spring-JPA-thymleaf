package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.repo.AdminRepo;
import ntic.tlsi.gestiondoctorat2.web.AdminForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "acting-admin", roles = "ADMIN")
class AdminFormValidationTests {

    @Autowired private MockMvc mvc;
    @Autowired private AdminRepo adminRepo;
    @Autowired private PasswordEncoder encoder;

    @Test
    void createRejectsBlankRequiredFields() throws Exception {
        long before = adminRepo.count();

        mvc.perform(post("/admin/saveAdmin").with(csrf())
                        .param("username", " ")
                        .param("nom", " ")
                        .param("prenom", "")
                        .param("email", " ")
                        .param("password", "long-enough-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("AdminAdminAdd"))
                .andExpect(model().attributeHasFieldErrors("admin",
                        "username", "nom", "prenom", "email"));

        assertEquals(before, adminRepo.count());
    }

    @Test
    void createRejectsInvalidEmailAndClearsPasswordFromForm() throws Exception {
        String username = "new-" + UUID.randomUUID();

        mvc.perform(post("/admin/saveAdmin").with(csrf())
                        .param("username", username)
                        .param("nom", "Last")
                        .param("prenom", "First")
                        .param("email", "not-an-email")
                        .param("password", "valid-password-123"))
                .andExpect(status().isOk())
                .andExpect(view().name("AdminAdminAdd"))
                .andExpect(model().attributeHasFieldErrors("admin", "email"))
                .andExpect(model().attribute("admin", org.hamcrest.Matchers.hasProperty(
                        "username", org.hamcrest.Matchers.is(username))))
                .andExpect(model().attribute("admin", org.hamcrest.Matchers.hasProperty(
                        "password", org.hamcrest.Matchers.nullValue())));

        assertTrue(adminRepo.findByUsername(username).isEmpty());
    }

    @Test
    void validCreatePersistsAdminWithHashedPassword() throws Exception {
        String username = "created-" + UUID.randomUUID();

        mvc.perform(post("/admin/saveAdmin").with(csrf())
                        .param("username", username)
                        .param("nom", "Family")
                        .param("prenom", "Given")
                        .param("email", "valid@example.com")
                        .param("password", "valid-password-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/getAdmins"));

        adminRepo.flush();
        Admin created = adminRepo.findByUsername(username).orElseThrow();
        assertEquals(Role.ADMIN, created.getTypeRole());
        assertEquals("valid@example.com", created.getEmail());
        assertTrue(encoder.matches("valid-password-123", created.getPassword()));
    }

    @Test
    void editRejectsInvalidFieldsWithoutOverwritingExistingAdmin() throws Exception {
        Admin admin = createAdmin();
        String initialEmail = admin.getEmail();
        String initialName = admin.getNom();

        mvc.perform(post("/admin/saveEditedAdmin").with(csrf())
                        .param("id", admin.getId().toString())
                        .param("username", admin.getUsername())
                        .param("nom", " ")
                        .param("prenom", "Given")
                        .param("email", "bad-address"))
                .andExpect(status().isOk())
                .andExpect(view().name("AdminAdminEdit"))
                .andExpect(model().attributeHasFieldErrors("admin", "nom", "email"));

        adminRepo.flush();
        Admin unchanged = adminRepo.findById(admin.getId()).orElseThrow();
        assertEquals(initialEmail, unchanged.getEmail());
        assertEquals(initialName, unchanged.getNom());
    }

    @Test
    void editAcceptsValidFieldsAndPreservesPassword() throws Exception {
        Admin admin = createAdmin();
        String hash = admin.getPassword();

        mvc.perform(post("/admin/saveEditedAdmin").with(csrf())
                        .param("id", admin.getId().toString())
                        .param("username", admin.getUsername())
                        .param("nom", "Updated")
                        .param("prenom", "Given")
                        .param("email", "new@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/getAdmins"));

        adminRepo.flush();
        Admin updated = adminRepo.findById(admin.getId()).orElseThrow();
        assertEquals("Updated", updated.getNom());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals(hash, updated.getPassword());
    }

    private Admin createAdmin() {
        Admin admin = new Admin("admin-" + UUID.randomUUID(),
                "initial-password-123", "before@example.com",
                "Original", "Given", Role.ADMIN, new Date());
        adminRepo.saveAndFlush(admin);
        return admin;
    }
}
