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
class AdminDeletionTests {

    @Autowired private MockMvc mvc;
    @Autowired private AdminRepo adminRepo;
    @Autowired private UserAccountRepo userAccountRepo;

    @Test
    void getRequestCannotDeleteAdmin() throws Exception {
        Admin target = createAdmin("target");
        mvc.perform(get("/admin/delete").param("id", target.getId().toString()))
                .andExpect(status().isMethodNotAllowed());
        assertTrue(adminRepo.findById(target.getId()).isPresent());
    }

    @Test
    void missingCsrfTokenBlocksDeletion() throws Exception {
        Admin target = createAdmin("target");
        mvc.perform(post("/admin/delete").param("id", target.getId().toString()))
                .andExpect(status().isForbidden());
        assertTrue(adminRepo.findById(target.getId()).isPresent());
    }

    @Test
    void adminCannotDeleteOwnAccount() throws Exception {
        Admin self = createAdmin("acting-admin");
        mvc.perform(post("/admin/delete").with(csrf())
                        .param("id", self.getId().toString()))
                .andExpect(status().isForbidden());
        assertTrue(adminRepo.findById(self.getId()).isPresent());
    }

    @Test
    void adminCannotDeleteAnotherRole() throws Exception {
        CFD cfd = new CFD("cfd-" + UUID.randomUUID(), "password-of-at-least-12",
                "cfd@example.com", "CFD", "User", Role.CFD, new Date());
        userAccountRepo.saveAndFlush(cfd);

        mvc.perform(post("/admin/delete").with(csrf())
                        .param("id", cfd.getId().toString()))
                .andExpect(status().isNotFound());
        assertTrue(userAccountRepo.findById(cfd.getId()).isPresent());
    }

    @Test
    void adminCanDeleteAnotherAdmin() throws Exception {
        Admin target = createAdmin("target");
        mvc.perform(post("/admin/delete").with(csrf())
                        .param("id", target.getId().toString())
                        .param("page", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/getAdmins?page=0&keyword="));
        assertTrue(adminRepo.findById(target.getId()).isEmpty());
    }

    private Admin createAdmin(String username) {
        Admin admin = new Admin(username.equals("acting-admin") ? username : username + "-" + UUID.randomUUID(),
                "password-of-at-least-12", "admin@example.com",
                "Given", "Family", Role.ADMIN, new Date());
        adminRepo.saveAndFlush(admin);
        return admin;
    }
}
