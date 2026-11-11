package ntic.tlsi.gestiondoctorat2;

import ntic.tlsi.gestiondoctorat2.entities.Admin;
import ntic.tlsi.gestiondoctorat2.entities.CFD;
import ntic.tlsi.gestiondoctorat2.entities.Role;
import ntic.tlsi.gestiondoctorat2.repo.AdminRepo;
import ntic.tlsi.gestiondoctorat2.repo.UserAccountRepo;
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
class AdminEditingTests {

    @Autowired private MockMvc mvc;
    @Autowired private AdminRepo adminRepo;
    @Autowired private UserAccountRepo userAccountRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void editingProfilePreservesPasswordRoleAndDate() throws Exception {
        Admin admin = createAdmin();
        String storedHash = admin.getPassword();
        Date originalDate = admin.getLogDate();

        mvc.perform(post("/admin/saveEditedAdmin").with(csrf())
                        .param("id", admin.getId().toString())
                        .param("username", admin.getUsername())
                        .param("nom", "Updated")
                        .param("prenom", "New")
                        .param("email", "updated@example.com")
                        .param("password", "attacker-password-123")
                        .param("typeRole", "CANDIDAT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/getAdmins"));

        adminRepo.flush();
        Admin saved = adminRepo.findById(admin.getId()).orElseThrow();
        assertEquals("Updated", saved.getNom());
        assertEquals("New", saved.getPrenom());
        assertEquals("updated@example.com", saved.getEmail());
        assertEquals(Role.ADMIN, saved.getTypeRole());
        assertEquals(originalDate, saved.getLogDate());
        assertEquals(storedHash, saved.getPassword());
        assertTrue(passwordEncoder.matches("initial-password-123", saved.getPassword()));
    }

    @Test
    void unknownIdReturnsNotFound() throws Exception {
        mvc.perform(post("/admin/saveEditedAdmin").with(csrf())
                        .param("id", Long.toString(Long.MAX_VALUE))
                        .param("username", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherRoleCannotBeEditedAsAdmin() throws Exception {
        CFD cfd = new CFD("cfd-" + UUID.randomUUID(), "initial-password-123",
                "cfd@example.com", "CFD", "User", Role.CFD, new Date());
        userAccountRepo.saveAndFlush(cfd);

        mvc.perform(post("/admin/saveEditedAdmin").with(csrf())
                        .param("id", cfd.getId().toString())
                        .param("username", "modified"))
                .andExpect(status().isNotFound());
        assertEquals(Role.CFD, userAccountRepo.findById(cfd.getId()).orElseThrow().getTypeRole());
    }

    @Test
    void duplicateUsernameIsRejectedWithoutSavingChanges() throws Exception {
        Admin first = createAdmin();
        Admin second = createAdmin();

        mvc.perform(post("/admin/saveEditedAdmin").with(csrf())
                        .param("id", first.getId().toString())
                        .param("username", second.getUsername())
                        .param("nom", "Should Not Save"))
                .andExpect(status().isOk())
                .andExpect(view().name("AdminAdminEdit"))
                .andExpect(model().attributeHasFieldErrors("admin", "username"));

        adminRepo.flush();
        assertNotEquals("Should Not Save", adminRepo.findById(first.getId()).orElseThrow().getNom());
    }

    private Admin createAdmin() {
        Admin admin = new Admin("admin-" + UUID.randomUUID(),
                "initial-password-123", "before@example.com",
                "Original", "Admin", Role.ADMIN, new Date(1700000000000L));
        adminRepo.saveAndFlush(admin);
        return admin;
    }
}
